package com.assistant.toby;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
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
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button btn;
    Button stpBtn;
    TextView textviewRes;
    TextView textViewReq;
    TextView HelpTxt;
    Button endSpeech;
//    TTS tts = new TTS();
//    Activity activity = ;

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
        endSpeech = (Button) findViewById(R.id.endSpeech);

        STT stt = new STT(HelpTxt, getApplicationContext(), textviewRes, textViewReq, "What do you want?", stpBtn, MainActivity.this, endSpeech);
        stt.listen();

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
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_NOTIFICATION_POLICY}, 0);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE}, 0);
            }
        }

    }

    //    TTSManager ttsManager = new TTSManager();
    TTS ttss = new TTS();

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void BClick(View view) {

        STT stt = new STT(HelpTxt, getApplicationContext(), textviewRes, textViewReq, "What do you want?", stpBtn, MainActivity.this, endSpeech);
        stt.listen();
    }

    public void EndSP(View view) {
        addNotification();
    }

    public void addNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.toby_icon)
                        .setContentTitle("Unread Message")   //this is the title of notification
                        .setColor(101)
                        .setContentText("You have an unread message.");
        System.out.println();//this is the message showed in notification
        Intent intent = new Intent(this, MainActivity.class);
        System.out.println();
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }
//    public void addNotiff() {
//        try {
//            addNotification();
//        }catch (Exception e){
//            textviewRes.setText("main " +e.getMessage());
//        }
//
//    }

}

