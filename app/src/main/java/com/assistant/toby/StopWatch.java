package com.assistant.toby;

import android.os.CountDownTimer;
import android.widget.TextView;


public class StopWatch {
    public void setStopWatch(TextView textView, int time) {
        new CountDownTimer(time * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                textView.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                textView.setText("done!");
            }
        }.start();
    }
}
