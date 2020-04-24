package com.assistant.toby;

import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;

public class STT extends AppCompatActivity {
    boolean done = false;
    boolean alm = false;
    boolean timer = false;
    boolean stopwatchB = false;
    int time;
    String voice;
    StopWatch stopWatch = new StopWatch();

    public STT() {

    }

    public void listen(Context context, TextView textViewRes, TextView textViewReq, String print) {


        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                "com.domain.app");
        SpeechRecognizer recognizer = SpeechRecognizer
                .createSpeechRecognizer(context);
        RecognitionListener listener = new RecognitionListener() {

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> voiceResults = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (voiceResults == null) {
                    textViewRes.setText("No results");
                } else {
                    for (String match : voiceResults) {
                        System.out.println(match);
                    }
                    heard(voiceResults.get(0), context, textViewRes, textViewReq);
                    voice = voiceResults.get(0);
                    textViewReq.setText(voice);

                }
            }


            @Override
            public void onReadyForSpeech(Bundle params) {
                textViewRes.setText("Ready for speech" + "\n" + print);
            }

            /**
             *  ERROR_NETWORK_TIMEOUT = 1;
             *  ERROR_NETWORK = 2;
             *  ERROR_AUDIO = 3;
             *  ERROR_SERVER = 4;
             *  ERROR_CLIENT = 5;
             *  ERROR_SPEECH_TIMEOUT = 6;
             *  ERROR_NO_MATCH = 7;
             *  ERROR_RECOGNIZER_BUSY = 8;
             *  ERROR_INSUFFICIENT_PERMISSIONS = 9;
             *
             * @param error code is defined in SpeechRecognizer
             */
            @Override
            public void onError(int error) {
                String er[] = new String[10];
                er[0] = "Sorry, there is a problem with the network";
                er[1] = "No internet connection";
                er[2] = "Couldn't understand what you said";
                er[3] = "Something is wrong with our server";
                er[4] = "Something is wrong with your side of the server, try again.";
                er[5] = "Didn't hear anything";
                er[6] = "Can't understand what you said";
                er[7] = "you've already started the Microphone, wait a minute and try again";
                er[8] = "Don't have the Permissions for that";
                textViewRes.setText(er[error - 1]);
            }

            @Override
            public void onBeginningOfSpeech() {
                textViewRes.setText("Speech starting" + "\n" + print);
            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {
//                textView.setText(heard());
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // TODO Auto-generated method stub

            }

        };
        recognizer.setRecognitionListener(listener);
        recognizer.startListening(intent);
    }

    public void heard(String voiceResults, Context context, TextView textViewRes, TextView textViewReq) {
        boolean lOrS = false;
        Settings settings = new Settings();
        lOrS = settings.readFromFile(context);

        if (voiceResults.contains("note") && voiceResults.contains("save")) {
            listen(context, textViewRes, textViewReq, "What is the note?");
            done = true;
        } else if (voiceResults.contains("read") && voiceResults.contains("note")) {
            Note note = new Note();
            textViewRes.setText(note.readFromFile(context));
        } else if (voiceResults.contains("alarm") && voiceResults.contains("set")) {
            listen(context, textViewRes, textViewReq, "What hour will the alarm be? only numbers.");
            alm = true;
        } else if (voiceResults.contains("set") && voiceResults.contains("timer")) {
            timer = true;
        } else if (voiceResults.contains("countdown")) {
            listen(context, textViewRes, textViewReq, "How long should the stopwatch be? the seconds only, numbers only.");
            stopwatchB = true;
//        } else if (voiceResults.contains("how")
//                || voiceResults.contains("what")
//                || voiceResults.contains("why")
//                || voiceResults.contains("who")
//                || voiceResults.contains("where")
//                || voiceResults.contains("when")
//                || voiceResults.contains("define")) {
//            alphaAPISample.AlphaAPISample(textView, voiceResults);
        } else if (voiceResults.equalsIgnoreCase("Hello") || voiceResults.equalsIgnoreCase("Hi")) {
            textViewRes.setText("Hi, I am Toby, created on June of 2019. How can I help?");
        } else if (voiceResults.contains("Who created you") || voiceResults.contains("who created you")) {
            textViewRes.setText("I was ceated by Moshe Schwartzberg, as a summer project that then became a year long project");
        } else if (done) {
            Note note = new Note();
            note.writeToFile(voiceResults, context);
            textViewRes.setText("Your notes are:" + note.readFromFile(context));
            done = false;
        } else if (stopwatchB) {
            try {
                time = Integer.parseInt(voiceResults);
                stopWatch.setStopWatch(textViewRes, time);
                stopwatchB = false;
            } catch (Exception e) {
                textViewRes.setText("didn't work, please try again");
            }
        } else if (voiceResults.contains("help") || voiceResults.contains("Help")) {
            textViewRes.setText(
                    "notes -read/save note\n" +
                            "alarm - set alarm\n" +
                            "stopWatch - set timer\n" +
                            "countdown - set countdown\n" +
                            "Local time- what time is it\n" +
                            "for any search just ask");
        } else if (voiceResults.contains("settings") ){//||  voiceResults.contains("long") || voiceResults.contains("short")) {
            if (voiceResults.toLowerCase().contains("long")) {
                settings.writeToFile("answer: long", context);
                textViewRes.setText("answer changed to long");
            } else if (voiceResults.toLowerCase().contains("short")) {
                settings.writeToFile("answer: short", context);
                textViewRes.setText("answer changed to short");
            } else {
                textViewRes.setText("That's not an option in the settings");
            }
            lOrS = settings.readFromFile(context);
//            textViewRes.setText(String.valueOf(lOrS));
//            listen(context, textViewRes, textViewReq, "What do you want to change? \n long or short answer");
        } else {
            if (voiceResults.contains("time")) {
                Date currentTime = new Date();
                textViewRes.setText(currentTime.toString() + "\nNote: this only works for you're local time.");
            } else {
//                    textViewRes.setText(String.valueOf(lOrS));
                    AlphaAPI alphaAPI = new AlphaAPI(voiceResults, textViewRes, lOrS);
                    alphaAPI.run();
            }
        }
    }
}