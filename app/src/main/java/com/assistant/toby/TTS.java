package com.assistant.toby;

import android.content.Context;
import android.content.pm.FeatureInfo;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.PrecomputedText;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Set;

public class TTS extends AppCompatActivity {

    TextToSpeech tts;
    String text;

    public void speak(Context context, String textViewText)  {
        tts = new

                TextToSpeech(context, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                Voice voice = null;
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
//                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//                         voice = new Voice("toby",Locale.US,Voice.QUALITY_HIGH,Voice.LATENCY_NORMAL,true, (Set<String>) FeatureInfo.CREATOR);
//                    }
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        tts.setVoice(voice);
//                    }
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("error", "This Language is not supported");
                    } else {
                        ConvertTextToSpeech(textViewText);
//                        tts.speak("Text to say aloud", TextToSpeech.QUEUE_ADD, null);
                    }
                } else
                    Log.e("error", "Initilization Failed!");
            }
        });


    }



    private void ConvertTextToSpeech(String textViewText) {
        // TODO Auto-generated method stub
        text = textViewText;
        if (text == null || "".equals(text)) {
            text = "Content not available";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        } else {

            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);

        }

    }
public void stop(){
this.finishActivity(this.getTaskId());
}

//    @Override
//    public void onPause() {
//        if (tts != null) {
//            tts.stop();
//            tts.shutdown();
////            getApplicationContext().stopService(this.getIntent());
//        }
//        super.onPause();
//
//    }


}
