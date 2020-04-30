package com.assistant.toby;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.StrictMode;
import android.provider.AlarmClock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button btn;
    Button stpBtn;
    TextView textviewRes;
    TextView textViewReq;
    TextView HelpTxt;
    String talk;
//    TTS tts = new TTS();
//    Activity activity = ;

    //TODO fix TTS (repeating text)
    //TODO fix stop button for TTS

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.button);
        textviewRes = (TextView) findViewById(R.id.response);
        textViewReq = (TextView) findViewById(R.id.request);
        stpBtn = (Button) findViewById(R.id.stpBtn);
        HelpTxt = (TextView) findViewById(R.id.helpTxt);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 0);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }

            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 0);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.INTERNET}, 0);
            }
        }

//                STT stt = new STT(HelpTxt);
//                stt.moveTaskToBack(true);
//                stt.listen(getApplicationContext(), textviewRes, textViewReq, "", stpBtn, MainActivity.this);


    }

//    TTSManager ttsManager = new TTSManager();
TTS tts = new TTS();
    @SuppressLint("StaticFieldLeak")
    public void BClick(View view) {
//        AlphaAPI alphaAPI = new AlphaAPI("star wars",textviewRes);
//        alphaAPI.run();

//        tts.onPause();

        STT stt = new STT(HelpTxt, getApplicationContext(), textviewRes, textViewReq, "What do you want?", stpBtn, MainActivity.this);
        stt.listen();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tts.onPause();
    }


}

