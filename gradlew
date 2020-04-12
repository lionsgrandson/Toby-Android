package com.example.tobby;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    Button button;
    EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView =(TextView) findViewById(R.id.textView);
        button =(Button) findViewById(R.id.btn);
        editText = (EditText) findViewById(R.id.editText);

   }
    public void getSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                "com.domain.app");

        SpeechRecognizer recognizer = SpeechRecognizer
                .createSpeechRecognizer(this.getApplicationContext());
        recognizer.setRecognitionListener(listener);
        recognizer.startListening(intent);


    }
    String splinting;
    ArrayList<String> voiceResults = null;
    final RecognitionListener listener = new RecognitionListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onResults(Bundle results) {
            voiceResults = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (voiceResults == null) {
                textView.setText("No voice results");
            } else {
                for (String match : voiceResults) {
                    //first command for date and time
                    if (match.contains("test")) {
                        textView.setText("test worked");
                    } else if (match.contains("date") || match.contains("time")) {
                        textView.setText(dateTest());
                    } else if (match.contains("help")) {
                        //the default text or help, will show the possible commands
                        textView.setText("The possible commands are (time, date, alarm, calc, note, define, open + anything on the desktop)");

                    } else if (match.contains("alarm")) {
                        alarm alm = new alarm();
                        String[] timeing = new String[match.length()];
                        int time = 0;

                        try {
                            for(int i =0; i < match.length(); i++) {
                                timeing[i] = match.split(" ").toString();
                                time = Integer.valueOf(timeing[i]);
                            }
                                textView.setText(alm.setAlarm(time));
                                textView.setText( match + " "+ timeing + " " + time);

                        }catch (Exception e){
                            for(String l:timeing){
                                textView.setText(
                                        "timeing" + l +
                                        " e.toString " + e.toString() +
                                        " time " +  time +
                                        " We couldn't understand that fully, please enter the time you want");
                                editText.setVisibility(View.VISIBLE);
                                alm.setAlarm(Integer.parseInt(editText.getText().toString()));
                            }

//                            textView.setText(e.toString());

                        }
                    }
                }

            }
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            textView.setText("Ready for speech");
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
            textView.setText("Error listening for speech: " + error);
        }

        @Override
        public void onBeginningOfSpeech() {
            textView.setText("Speech starting");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onEvent