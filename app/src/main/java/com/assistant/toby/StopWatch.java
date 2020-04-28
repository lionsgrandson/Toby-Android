package com.assistant.toby;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.fragment.app.FragmentManager;


public class StopWatch  {

    boolean clicked = false;

    public void setStopWatch(TextView textView, Button stpBtn, Context context, Activity Activity) {

        stpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicked = true;
            }
        });

        int time = 0;
        while (!clicked) {

            try {

//                changeTXT(time,textView);
                textView.setText(time);
                TTS tts = new TTS();
                tts.speak(context,textView.getText().toString());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            textView.setText(e.getMessage());
                        }
                    }
                }).start();
                time++;
            } catch (Exception e) {
                textView.setText(e.getMessage());
            }


        }
        stpBtn.setVisibility(View.INVISIBLE);
    }
    public void changeTXT(int time, TextView textView){

    }
}

