package com.assistant.toby;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.TextView;


public class StopWatch  extends Activity {
    public void setStopWatch(TextView textView, int time, Context context) {

        new CountDownTimer(time * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                textView.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                textView.setText("done!");
                try {
                    Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        //deprecated in API 26
                        v.vibrate(1000);

                    }
                }catch (Exception e){
                    textView.setText(e.getMessage());
                }
            }
        }.start();
    }
}
