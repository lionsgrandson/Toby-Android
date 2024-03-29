package com.assistant.toby;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.TextView;


public class CountDown extends Activity {
    public void setStopWatch(TextView textView, int time, Context context) {
//        TTSManager tts = new TTSManager();
//        TTS tts = new TTS();
        new CountDownTimer(time * 1000, 1000) {
            public void onTick(long millisUntilFinished) {

                textView.setText("seconds remaining: " + millisUntilFinished / 1000);
//                tts.initQueue(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                textView.setText("done!");
//                tts.initQueue(textView.getText().toString());
                try {
                    Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                        Notification notification = new Notification(textView);
                        notification.addNotification(context,textView,"end of timer"," timer is at 0 seconds");
                    } else {
                        //deprecated in API 26
                        v.vibrate(1000);
                        Notification notification = new Notification(textView);
                        notification.addNotification(context,textView,"end of timer"," timer is at 0 seconds");

                    }
                }catch (Exception e){
                    textView.setText("something went wrong, please try again");
//                    tts.initQueue(textView.getText().toString());
                }
            }
        }.start();
    }
}
