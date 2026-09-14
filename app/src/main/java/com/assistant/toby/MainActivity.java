package com.assistant.toby;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.AlarmClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.InputType;
import android.view.View;
import android.view.WindowInsets;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class MainActivity extends Activity {
    private static final int MICROPHONE = 100;
    private static final String HELP = "Try these commands:\n\n"
            + "Save note buy milk\nSave note (then speak or type your note)\nRead notes\n"
            + "Set a timer for ten minutes\nSet a timer for 1 hour 30 minutes\n"
            + "Set an alarm for 7 am\nStart stopwatch\nStop stopwatch\n"
            + "What time is it?\nWhat is the date?\nEcho hello\nWho created you?\n\n"
            + "Tap Speak before talking. You can begin with ‘Hey Toby’.\n"
            + "Alarms and timers open your Clock app for confirmation.\n"
            + "For general questions, add your Wolfram App ID in Settings.\n"
            + "Say or type ‘cancel’ to cancel a pending note or answer.";
    private TextView response, request, status, stopwatch;
    private EditText command;
    private Button listen, stopwatchStop;
    private SpeechRecognizer recognizer;
    private SpeechOutput speech;
    private SharedPreferences preferences;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService network = Executors.newSingleThreadExecutor();
    private Future<?> query;
    private int generation;
    private boolean listening, awaitingNote, foreground, permissionPending;
    private long stopwatchStart = -1, stoppedElapsed;
    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            updateStopwatch();
            if (foreground && stopwatchStart >= 0) handler.postDelayed(this, 200);
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        View root = findViewById(R.id.root);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            // Target 35 enforces edge-to-edge; keep controls clear of bars and the keyboard.
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        root.requestApplyInsets();
        response = findViewById(R.id.response);
        request = findViewById(R.id.request);
        status = findViewById(R.id.status);
        command = findViewById(R.id.command);
        listen = findViewById(R.id.listen);
        stopwatch = findViewById(R.id.stopwatch);
        stopwatchStop = findViewById(R.id.stopwatch_stop);
        preferences = getSharedPreferences("toby", MODE_PRIVATE);
        speech = new SpeechOutput(this);
        if (state != null) awaitingNote = state.getBoolean("awaitingNote");
        int boot = android.provider.Settings.Global.getInt(getContentResolver(), "boot_count", -1);
        if (preferences.getInt("stopwatchBoot", -2) == boot) {
            stopwatchStart = preferences.getLong("stopwatchStart", -1);
            stoppedElapsed = preferences.getLong("stopwatchElapsed", 0);
            if (stopwatchStart > SystemClock.elapsedRealtime()) stopwatchStart = -1;
        }
        findViewById(R.id.send).setOnClickListener(v -> submitTyped());
        command.setOnEditorActionListener((view, action, event) -> {
            if (action == EditorInfo.IME_ACTION_SEND) { submitTyped(); return true; }
            return false;
        });
        listen.setOnClickListener(v -> requestListening());
        findViewById(R.id.stop).setOnClickListener(v -> {
            stopInteraction();
            awaitingNote = false;
            status.setText("Stopped. Ready when you are.");
        });
        findViewById(R.id.help).setOnClickListener(v -> {
            stopInteraction(); awaitingNote = false; respond(HELP);
        });
        findViewById(R.id.settings).setOnClickListener(v -> showSettings());
        stopwatchStop.setOnClickListener(v -> stopStopwatch());
        updateStopwatch();
    }

    private void submitTyped() {
        String text = command.getText().toString().trim();
        if (text.isEmpty()) { command.setError("Enter a command or question"); return; }
        command.setText("");
        execute(text);
    }

    private void requestListening() {
        if (listening || permissionPending) return;
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionPending = true;
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE);
            return;
        }
        startListening();
    }

    @Override public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(code, permissions, results);
        if (code != MICROPHONE) return;
        permissionPending = false;
        if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            if (foreground) startListening();
        } else {
            status.setText("Microphone access was denied. You can type commands, or enable the microphone in Android’s app permissions.");
        }
    }

    private void startListening() {
        stopInteraction();
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.setText("No speech recognition service is available. Enable a speech service in Android settings, or type below.");
            return;
        }
        try {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            final SpeechRecognizer session = recognizer;
            recognizer.setRecognitionListener(new RecognitionListener() {
                private boolean active() { return recognizer == session && foreground; }
                @Override public void onReadyForSpeech(Bundle params) {
                    if (active()) status.setText(awaitingNote ? "Listening for your note…" : "Listening…");
                }
                @Override public void onBeginningOfSpeech() {}
                @Override public void onRmsChanged(float level) {}
                @Override public void onBufferReceived(byte[] buffer) {}
                @Override public void onEndOfSpeech() { if (active()) status.setText("Recognizing…"); }
                @Override public void onError(int error) {
                    if (!active()) return;
                    releaseRecognizer();
                    status.setText(speechError(error));
                }
                @Override public void onResults(Bundle results) {
                    if (!active()) return;
                    ArrayList<String> matches = results == null ? null : results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    releaseRecognizer();
                    if (matches == null || matches.isEmpty() || matches.get(0).trim().isEmpty()) {
                        status.setText("I didn’t catch that. Tap Speak to try again, or type below.");
                    } else execute(matches.get(0));
                }
                @Override public void onPartialResults(Bundle results) {}
                @Override public void onEvent(int event, Bundle params) {}
            });
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            listening = true;
            listen.setEnabled(false);
            status.setText("Starting microphone…");
            recognizer.startListening(intent);
        } catch (RuntimeException e) {
            releaseRecognizer();
            status.setText("The microphone couldn’t start. Check your speech service and microphone permission, or type below.");
        }
    }

    static String speechError(int code) {
        switch (code) {
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
            case SpeechRecognizer.ERROR_NETWORK: return "Speech recognition couldn’t connect. Check your internet or type below.";
            case SpeechRecognizer.ERROR_AUDIO: return "The microphone is unavailable. Close other recording apps and try again.";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
            case SpeechRecognizer.ERROR_NO_MATCH: return "I didn’t catch that. Tap Speak to try again, or type below.";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "Speech recognition is busy. Please try again in a moment.";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "Enable Toby’s microphone permission in Android settings, or type below.";
            case 12:
            case 13: return "English speech recognition is unavailable. Install its language data in your speech service, or type below.";
            default: return "Speech recognition stopped (" + code + "). Tap Speak to retry, or type below.";
        }
    }

    private void releaseRecognizer() {
        SpeechRecognizer old = recognizer;
        recognizer = null;
        listening = false;
        if (listen != null) listen.setEnabled(true);
        if (old != null) { old.cancel(); old.destroy(); }
    }

    private void stopInteraction() {
        releaseRecognizer();
        if (speech != null) speech.stop();
        generation++;
        if (query != null) { query.cancel(true); query = null; }
    }

    private void respond(String text) {
        response.setText(text);
        status.setText(awaitingNote ? "Your next command will be saved as a note. Say ‘cancel’ to exit." : getString(R.string.ready));
        if (foreground && preferences.getBoolean("speak", true)) speech.speak(text);
    }

    private void execute(String input) {
        stopInteraction();
        request.setText(input);
        CommandParser.Command parsed = CommandParser.parse(input);
        if (parsed.type == CommandParser.Type.CANCEL) {
            awaitingNote = false;
            respond("Cancelled.");
            return;
        }
        if (awaitingNote) { saveNote(input); return; }
        switch (parsed.type) {
            case HELP: respond(HELP); break;
            case HELLO: respond("Hi, I’m Toby. How can I help?"); break;
            case CREATOR: respond("I was created by Moshe Schwartzberg as a summer project that became a year-long project."); break;
            case SAVE_NOTE:
                if (parsed.text.isEmpty()) { awaitingNote = true; respond("What should I save? Tap Speak again or type your note."); }
                else saveNote(parsed.text);
                break;
            case READ_NOTES:
                try { respond(new Note().read(this)); }
                catch (IOException e) { respond("I couldn’t read your notes. Please try again."); }
                break;
            case TIME: respond(DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date())); break;
            case DATE: respond(DateFormat.getDateInstance(DateFormat.FULL).format(new Date())); break;
            case ECHO: respond(parsed.text); break;
            case START_STOPWATCH:
                if (stopwatchStart < 0) {
                    stoppedElapsed = 0;
                    stopwatchStart = SystemClock.elapsedRealtime();
                    persistStopwatch();
                    handler.removeCallbacks(ticker);
                    handler.post(ticker);
                }
                respond("Stopwatch running."); break;
            case STOP_STOPWATCH: stopStopwatch(); break;
            case TIMER:
                int seconds = CommandParser.timerSeconds(parsed.text);
                if (seconds < 1) { respond("Tell me a duration, for example ‘set a timer for ten minutes’ or ‘1 hour 30 minutes’."); break; }
                openClock(new Intent(AlarmClock.ACTION_SET_TIMER)
                        .putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                        .putExtra(AlarmClock.EXTRA_MESSAGE, "Toby timer")
                        .putExtra(AlarmClock.EXTRA_SKIP_UI, false), "Confirm your timer in the Clock app.");
                break;
            case ALARM:
                Intent alarm = new Intent(AlarmClock.ACTION_SET_ALARM)
                        .putExtra(AlarmClock.EXTRA_MESSAGE, "Toby alarm")
                        .putExtra(AlarmClock.EXTRA_SKIP_UI, false);
                int[] time = CommandParser.alarmTime(parsed.text);
                if (time != null) alarm.putExtra(AlarmClock.EXTRA_HOUR, time[0]).putExtra(AlarmClock.EXTRA_MINUTES, time[1]);
                openClock(alarm, time == null ? "Choose and confirm the alarm’s time and day in the Clock app." : "Confirm your alarm in the Clock app.");
                break;
            default: askWolfram(parsed.text);
        }
    }

    private void saveNote(String text) {
        try {
            new Note().save(this, text);
            awaitingNote = false;
            respond("Note saved.");
        } catch (IOException e) { respond("I couldn’t save the note. Please try again."); }
    }

    private void openClock(Intent intent, String message) {
        try {
            startActivity(intent);
            // Clock is responsible for scheduling; never claim an alarm was saved before confirmation.
            response.setText(message);
            status.setText(getString(R.string.ready));
        } catch (ActivityNotFoundException | SecurityException e) {
            respond("No compatible Clock app is available. Install or enable a Clock app that supports alarms and timers.");
        }
    }

    private void askWolfram(String question) {
        String key = preferences.getString("wolframAppId", "").trim();
        if (key.isEmpty()) {
            respond("Add your Wolfram App ID in Settings to answer general questions. Notes, alarms, timers, and the stopwatch work without it.");
            return;
        }
        final int token = generation;
        status.setText("Looking up your question…");
        response.setText("You can stop the lookup or enter another command.");
        query = network.submit(() -> {
            String answer;
            try { answer = new WolframClient().answer(question, key); }
            catch (IOException e) { answer = "I couldn’t reach Wolfram. Check your connection and try again."; }
            final String result = answer;
            handler.post(() -> {
                if (token == generation && foreground && !isDestroyed()) { query = null; respond(result); }
            });
        });
    }

    private void showSettings() {
        stopInteraction();
        status.setText(getString(R.string.ready));
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        content.setPadding(padding, padding / 2, padding, 0);
        TextView explanation = new TextView(this);
        explanation.setText("Enter your own Wolfram App ID with Short Answers API access. It is saved on this device. Questions are sent to Wolfram when you ask them.");
        content.addView(explanation);
        EditText key = new EditText(this);
        key.setHint("Wolfram App ID");
        key.setSingleLine(true);
        key.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        key.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        key.setText(preferences.getString("wolframAppId", ""));
        content.addView(key);
        Switch spoken = new Switch(this);
        spoken.setText("Speak answers aloud");
        spoken.setMinHeight((int) (48 * getResources().getDisplayMetrics().density));
        spoken.setChecked(preferences.getBoolean("speak", true));
        content.addView(spoken);
        new AlertDialog.Builder(this).setTitle("Toby settings").setView(content)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    preferences.edit().putString("wolframAppId", key.getText().toString().trim())
                            .putBoolean("speak", spoken.isChecked()).apply();
                    respond("Settings saved.");
                }).show();
    }

    private void stopStopwatch() {
        if (stopwatchStart < 0) { respond("The stopwatch isn’t running."); return; }
        stoppedElapsed = SystemClock.elapsedRealtime() - stopwatchStart;
        stopwatchStart = -1;
        handler.removeCallbacks(ticker);
        persistStopwatch();
        updateStopwatch();
        respond("Stopwatch stopped at " + formatElapsed(stoppedElapsed) + ".");
    }
    private void persistStopwatch() {
        preferences.edit().putLong("stopwatchStart", stopwatchStart).putLong("stopwatchElapsed", stoppedElapsed)
                .putInt("stopwatchBoot", android.provider.Settings.Global.getInt(getContentResolver(), "boot_count", -1)).apply();
    }
    private void updateStopwatch() {
        stopwatch.setVisibility(stopwatchStart >= 0 || stoppedElapsed > 0 ? View.VISIBLE : View.GONE);
        stopwatchStop.setVisibility(stopwatchStart >= 0 ? View.VISIBLE : View.GONE);
        stopwatch.setText(formatElapsed(stopwatchStart >= 0 ? SystemClock.elapsedRealtime() - stopwatchStart : stoppedElapsed));
    }
    static String formatElapsed(long millis) {
        long seconds = Math.max(0, millis) / 1000;
        return String.format(Locale.US, "%02d:%02d:%02d", seconds / 3600, (seconds / 60) % 60, seconds % 60);
    }
    @Override protected void onResume() {
        super.onResume(); foreground = true;
        handler.removeCallbacks(ticker); handler.post(ticker);
    }
    @Override protected void onPause() {
        foreground = false;
        boolean interrupted = listening || query != null;
        stopInteraction();
        if (interrupted) status.setText("Paused. Tap Speak or send your question again when ready.");
        handler.removeCallbacks(ticker);
        super.onPause();
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putBoolean("awaitingNote", awaitingNote);
        super.onSaveInstanceState(state);
    }
    @Override protected void onDestroy() {
        stopInteraction();
        speech.close();
        network.shutdownNow();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
