//package com.assistant.toby;
//
//import android.content.Context;
//import android.os.Build;
//import android.speech.tts.TextToSpeech;
//import android.speech.tts.Voice;
//import android.util.Log;
//
//import java.util.HashSet;
//import java.util.Locale;
//import java.util.Set;
//
//public class TTSManager {
//
//
//    TextToSpeech tts;
//    String text;
//
//
//    public void speak(Context context, String textViewText) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
//            tts = new
//
//                    TextToSpeech(context, new TextToSpeech.OnInitListener() {
//
//                @Override
//                public void onInit(int status) {
//                    Voice voice = null;
//                    if (status == TextToSpeech.SUCCESS) {
//                        int result = tts.setLanguage(Locale.US);
//                        Voice v = null;
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                            Set<String> a = new HashSet<>();
//                            a.add("male");
//                            v = new Voice("en-us-x-sfg#male_2-local", new Locale("en", "US"), 400, 200, true, a);
//                            tts.setVoice(v);
//                        }
////                        tts.setSpeechRate(0.8f);
//                        if (result == TextToSpeech.LANG_MISSING_DATA ||
//                                result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                            Log.e("error", "This Language is not supported");
//                        } else {
//                            ConvertTextToSpeech(textViewText);
//                            //                        tts.speak("Text to say aloud", TextToSpeech.QUEUE_ADD, null);
//                        }
//                    } else
//                        Log.e("error", "Initilization Failed!");
//                }
//            });
//        }
//
//
//    }
//
//
//    private void ConvertTextToSpeech(String textViewText) {
//        // TODO Auto-generated method stub
//        text = textViewText;
//        if (text == null || "".equals(text)) {
//            text = "Content not available";
//            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
//        } else {
//
//            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
//
//        }
//
//    }
//
//    public void stop() {
//        if (tts.isSpeaking()) {
//            tts.stop();
//            tts.shutdown();
//        }
//    }
//
//
//}