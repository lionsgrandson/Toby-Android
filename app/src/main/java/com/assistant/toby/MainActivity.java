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
    Button stpBtn;
    TextView textviewRes;
    TextView textViewReq;
//    Activity activity = ;
    STT stt = new STT();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.button);
        textviewRes = (TextView) findViewById(R.id.response);
        textViewReq = (TextView) findViewById(R.id.request);
        stpBtn = (Button) findViewById(R.id.stpBtn);
        System.out.println("testing");


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
    }

    public void BClick(View view) {
//        AlphaAPI alphaAPI = new AlphaAPI("star wars",textviewRes);
//        alphaAPI.run();
        stt.listen(this.getApplicationContext(), textviewRes, textViewReq, "What do you want?",stpBtn,MainActivity.this);

        if (stt.alm) {
            Intent alam = new Intent(AlarmClock.ACTION_SET_ALARM);
            alam.putExtra(AlarmClock.EXTRA_MESSAGE, "StopWatch Title");
            alam.putExtra(AlarmClock.EXTRA_VIBRATE, true);
            startActivity(alam);
            stt.alm = false;
        }
    }
}
