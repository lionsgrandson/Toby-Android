package com.assistant.toby;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.EditText;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TobySmokeTest {
    @Before public void reset() {
        Context context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("toby", Context.MODE_PRIVATE).edit().clear().putBoolean("speak", false).commit();
        context.deleteFile("note.txt");
    }
    private void send(MainActivity activity, String command) {
        ((EditText) activity.findViewById(R.id.command)).setText(command);
        activity.findViewById(R.id.send).performClick();
    }
    private String answer(MainActivity activity) {
        return ((TextView) activity.findViewById(R.id.response)).getText().toString();
    }
    @Test public void startsWithoutPermissionAndTypedCommandsWork() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(a -> {
                assertEquals(PackageManager.PERMISSION_DENIED, a.checkSelfPermission(Manifest.permission.RECORD_AUDIO));
                send(a, "Hey Toby, hello"); assertTrue(answer(a).contains("Toby"));
                send(a, "what time is it?"); assertFalse(answer(a).isEmpty());
                send(a, "what is the capital of France?"); assertTrue(answer(a).contains("App ID"));
                send(a, "timer for 0 minutes"); assertTrue(answer(a).contains("duration"));
                a.findViewById(R.id.stop).performClick();
            });
        }
    }
    @Test public void oldNotesAndTwoTurnNotesSurviveRecreation() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        try (OutputStream file = context.openFileOutput("note.txt", Context.MODE_PRIVATE)) {
            file.write("(original note)".getBytes(StandardCharsets.UTF_8));
        }
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(a -> send(a, "save note"));
            scenario.recreate();
            scenario.onActivity(a -> { send(a, "Buy milk"); assertEquals("Note saved.", answer(a)); });
            scenario.recreate();
            scenario.onActivity(a -> {
                send(a, "read notes");
                assertTrue(answer(a).contains("original note")); assertTrue(answer(a).contains("Buy milk"));
                send(a, "save note"); send(a, "cancel"); send(a, "hello");
                assertTrue(answer(a).contains("Toby"));
                send(a, "read notes"); assertFalse(answer(a).contains("hello"));
            });
        }
    }
    @Test public void stopwatchSurvivesActivityRecreationAndStops() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(a -> send(a, "start stopwatch"));
            scenario.recreate();
            scenario.onActivity(a -> {
                assertEquals(android.view.View.VISIBLE, a.findViewById(R.id.stopwatch_stop).getVisibility());
                send(a, "stop stopwatch");
                assertTrue(answer(a).startsWith("Stopwatch stopped"));
                assertEquals(android.view.View.GONE, a.findViewById(R.id.stopwatch_stop).getVisibility());
            });
        }
    }
    @Test public void speechErrorsNeverIndexOutsideLegacyArray() {
        for (int error = 1; error <= 30; error++) assertNotNull(MainActivity.speechError(error));
        assertEquals("01:01:01", MainActivity.formatElapsed(3661000));
    }
}
