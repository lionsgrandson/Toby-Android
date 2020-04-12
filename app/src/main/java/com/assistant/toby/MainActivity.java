package com.assistant.toby;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.AlarmClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    Button btn;
    TextView textviewRes;
    TextView textViewReq;
    Activity activity;
    STT stt = new STT();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.button);
        textviewRes = (TextView) findViewById(R.id.response);
        textViewReq = (TextView) findViewById(R.id.request);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 1);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.INTERNET}, 1);
        }
    }

    public void BClick(View view) {
//        AlphaAPI alphaAPI = new AlphaAPI("star wars",textviewRes);
//        alphaAPI.run();
        stt.listen(this.getApplicationContext(), textviewRes, textViewReq, "What do you want?");
        if (stt.alm) {
            Intent alam = new Intent(AlarmClock.ACTION_SET_ALARM);
            alam.putExtra(AlarmClock.EXTRA_MESSAGE, "StopWatch Title");
            alam.putExtra(AlarmClock.EXTRA_VIBRATE, true);
            startActivity(alam);
            stt.alm = false;
        } else if (stt.timer) {
            Intent timeIN = new Intent(AlarmClock.ACTION_SET_TIMER);
            timeIN.putExtra(AlarmClock.EXTRA_MESSAGE, "Timer Title");
            timeIN.putExtra(AlarmClock.ACTION_SET_TIMER, true);
            timeIN.putExtra(AlarmClock.EXTRA_VIBRATE, true);
            startActivity(timeIN);
            stt.timer = false;
        }
    }
}
