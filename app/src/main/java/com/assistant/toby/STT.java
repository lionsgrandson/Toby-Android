package com.assistant.toby;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.AlarmClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class STT extends Activity {
    boolean done = false;
    boolean alm = false;
    boolean timer = false;
    boolean stopwatchB = false;
    TTS tts = new TTS();
    int time;
    int STPtime;
    String voice;
    CountDown countDown = new CountDown();
    Settings settings = new Settings();
    boolean lOrS;
    TextView helpTxt;
    String speakTxt = "";
    Context context;
    TextView textViewRes;
    TextView textViewReq;
    String print;
    Button stpBtn;
    Activity actvity;

    public STT(TextView helpTxt,  Context context, TextView textViewRes, TextView textViewReq, String print, Button stpBtn, Activity actvity) {
        this.helpTxt = helpTxt;
        this.context = context;
        this.textViewRes = textViewRes;
        this.textViewReq = textViewReq;
        this.print = print;
        this.stpBtn = stpBtn;
        this.actvity = actvity;
    }

    public void listen() {


        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                "com.domain.app");
        SpeechRecognizer recognizer = SpeechRecognizer
                .createSpeechRecognizer(context);
        RecognitionListener listener = new RecognitionListener() {

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> voiceResults = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (voiceResults == null) {
                    textViewRes.setText("No results");
                    tts.speak(context, textViewRes.getText().toString());
                } else {
                    for (String match : voiceResults) {
                        System.out.println(match);
                    }
                    voice = voiceResults.get(0);
                    textViewReq.setText(voice);
                    if (done) {
                        Note note = new Note();
                        note.writeToFile(voice, context);
//                        textViewRes.setText("Your notes are:" + note.readFromFile(context));

                    } else {
                        done = false;
                        heard(voiceResults.get(0), context, textViewRes, textViewReq, stpBtn, actvity);
                    }

                }
            }


            @Override
            public void onReadyForSpeech(Bundle params) {
                textViewRes.setText("Ready for speech" + "\n" + print);
//                tts.speak(context,"Ready for speech");
            }

            /**
             *  ERROR_NETWORK_TIMEOUT = 1;
             *  ERROR_NETWORK = 2;
             *  ERROR_AUDIO = 3;
             *  ERROR_SERVER = 4;
             *  ERROR_CLIENT = 5;
             *  ERROR_SPEECH_TIMEOUT = 6;
             *  ERROR_NO_MATCH = 7;
             *  ERROR_RECOGNIZER_BUSY = 8;
             *  ERROR_INSUFFICIENT_PERMISSIONS = 9;
             *
             * @param error code is defined in SpeechRecognizer
             */
            @Override
            public void onError(int error) {
                String er[] = new String[10];
                er[0] = "Sorry, there is a problem with the network";
                er[1] = "No internet connection";
                er[2] = "Couldn't understand what you said";
                er[3] = "Something is wrong with our server";
                er[4] = "Something is wrong with your side, try again.";
                er[5] = "Didn't hear anything";
                er[6] = "Can't understand what you said";
//                er[7] = "you've already started the Microphone, wait a minute and try again";
                er[8] = "Don't have the Permissions for that";
                textViewRes.setText(er[error - 1]);
                tts.speak(context, textViewRes.getText().toString());
            }

            @Override
            public void onBeginningOfSpeech() {
                textViewRes.setText("Speech starting" + "\n" + print);
//                tts.speak(context,textViewRes.getText().toString());
            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {
//                textView.setText(heard());
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // TODO Auto-generated method stub

            }

        };
        recognizer.setRecognitionListener(listener);
        recognizer.startListening(intent);

    }

    @SuppressLint("SetTextI18n")
    public void heard(String voiceResults, Context context, TextView textViewRes, TextView textViewReq, Button stpBtn, Activity activity) {
        String[] voiceResultsSpl;

        if (voiceResults.contains("note") && voiceResults.contains("save")) {
            listen();
            done = true;
        } else if (voiceResults.contains("read") && voiceResults.contains("note")) {
            Note note = new Note();
            textViewRes.setText(note.readFromFile(context));
            tts.speak(context, "Your notes are" + note.readFromFile(context));
        } else if (voiceResults.contains("alarm")) {
            try {
                Intent alam = new Intent(AlarmClock.ACTION_SET_ALARM);
                alam.putExtra(AlarmClock.EXTRA_MESSAGE, "My Alarm");
                alam.putExtra(AlarmClock.EXTRA_VIBRATE, true);
                alam.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(alam);
            } catch (Exception e) {
                textViewRes.setText("something didn't work");
                tts.speak(context, textViewRes.getText().toString());
            }
        } else if (voiceResults.toLowerCase().contains("stopwatch")) {
            stpBtn.setVisibility(View.VISIBLE);
            try {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        STPtime = 0;
                        while (STPtime >= 0) {
                            try {
                                textViewRes.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (STPtime >= 60 && STPtime <= 3600) {
                                            int min = STPtime / 60;
//                                        tts.speak(context,(min)+" "+(STPtime- (60 *min )));
                                            textViewRes.setText("Minute " + (min) + ":" + (STPtime - (60 * min)));
                                        } else if (STPtime >= 3600) {
                                            textViewRes.setText("stopped at one hour");
                                            tts.speak(context, textViewRes.getText().toString());
                                        } else {
                                            textViewRes.setText("Seconds " + STPtime);
//                                        tts.speak(context,String.valueOf(STPtime.intValue()));
                                        }

                                    }
                                });
                                if (STPtime >= 0) {
                                    Thread.sleep(500);
                                    STPtime = (STPtime + 1);
                                    Thread.sleep(500);
                                }
                            } catch (Exception e) {
                                textViewRes.setText("something didn't work, please try again");
                                tts.speak(context, textViewRes.getText().toString());
                            }

                        }
                        tts.speak(context, textViewRes.getText().toString());
                    }
                };
                Thread myThread = new Thread(runnable);
                myThread.start();

                stpBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        STPtime = -2;
                        stpBtn.setVisibility(View.INVISIBLE);
                    }
                });
            } catch (Exception e) {
                textViewRes.setText(e.getMessage());
            }
        } else if (voiceResults.toLowerCase().contains("countdown") || voiceResults.contains("timer")) {
            try {
                voiceResultsSpl = voiceResults.split(" ");
                for (int i = 0; i <= voiceResultsSpl.length; i++) {
                    try {
                        if (voiceResultsSpl[i].equalsIgnoreCase("one")) {
                            time = 1; // just in case it reads it as a string not an int
                        } else {
                            time = Integer.parseInt(voiceResultsSpl[i]);
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
                if (voiceResults.toLowerCase().contains("minute")) {
                    time *= 60; //if it's at minutes, then convert it to seconds (1 min = 60 sec)
                } else if (voiceResults.toLowerCase().contains("hour")) {
                    time *= 3600; //converting from hours to seconds
                }
                countDown.setStopWatch(textViewRes, time, context);
            } catch (Exception e) {
                textViewRes.setText("didn't work, please try again");
                tts.speak(context, textViewRes.getText().toString());
            }

//        } else if (voiceResults.contains("how")
//                || voiceResults.contains("what")
//                || voiceResults.contains("why")
//                || voiceResults.contains("who")
//                || voiceResults.contains("where")
//                || voiceResults.contains("when")
//                || voiceResults.contains("define")) {
//            alphaAPISample.AlphaAPISample(textView, voiceResults);
        } else if (voiceResults.equalsIgnoreCase("Hello") || voiceResults.equalsIgnoreCase("Hi")) {
            textViewRes.setText("Hi, I am Toby, created on June of 2019. How can I help?");
            tts.speak(context, textViewRes.getText().toString());
        } else if (voiceResults.contains("Who created you") || voiceResults.contains("who created you")) {
            textViewRes.setText("I was ceated by Moshe Schwartzberg, as a summer project that then became a year long project");
        } else if (voiceResults.toLowerCase().contains("help")) {
            textViewRes.setText(
                    "notes -read/save note\n" +
                            "alarm - set alarm\n" +
                            "countdown  - set timer/start a countdown  \n" +
                            "timer - start stopWatch\n" +
                            "Local time- what time is it\n" +
                            "for any search just ask");
            tts.speak(context, textViewRes.getText().toString());
//        }
//        else if (voiceResults.contains("setting") ){//||  voiceResults.contains("long") || voiceResults.contains("short")) {
//            if (voiceResults.toLowerCase().contains("long")) {
//                settings.writeToFile("long", context);
//                textViewRes.setText("answer changed to long");
//            } else if (voiceResults.toLowerCase().contains("short")) {
//                settings.writeToFile("short", context);
//                textViewRes.setText("answer changed to short");
//            } else {
//                textViewRes.setText("That's not an option in the settings");
//            }
//            lOrS = Boolean.parseBoolean(settings.readFromFile(context));
//            textViewRes.setText(String.valueOf(lOrS));
//            listen(context, textViewRes, textViewReq, "What do you want to change? \n long or short answer");

        } else if (voiceResults.contains("time")) {
//            SystemClock currentTime = new SystemClock();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                textViewRes.setText(SystemClock.currentGnssTimeClock().toString());//+ "\nNote: this only works for you're local time.");
            } else {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                textViewRes.setText(sdf.format(cal.getTime()));
            }
            tts.speak(context, textViewRes.getText().toString());

        } else {

            tts.speak(context, textViewRes.getText().toString());
            AlphaAPI alphaAPI = new AlphaAPI(voiceResults, textViewRes, context);
            alphaAPI.run();

            String setTextStr = alphaAPI.runStr();
            String[] setTextSpl = setTextStr.split("\n");
            ;

            textViewRes.setText(setTextSpl[0] + "\n" + setTextSpl[1] + ("\nRead More..."));
            speakTxt = (setTextSpl[0] + "\n" + setTextSpl[1]);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                textViewRes.setContextClickable(true);
                String finalSetTextStr = setTextStr;
                textViewRes.setOnClickListener(v -> {
                    textViewRes.setText(finalSetTextStr);
                    speakTxt = (finalSetTextStr);
                });
            }
            tts.speak(context, speakTxt);
        }
    }

}
