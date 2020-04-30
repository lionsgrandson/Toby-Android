//
//}
//public void startVoiceInput() {
//        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
//        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
//        try {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        activity.startForegroundService(intent);
//        }else{
//        activity.startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
//        }
////            moveTaskToBack(true);
//        } catch (Exception a) {
//        textViewRes.setText(a.getMessage());
//
//        }
//        }
//
//@Override
//protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//        case REQ_CODE_SPEECH_INPUT: {
//        if (resultCode == RESULT_OK && null != data) {
//        ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//        voice = result.get(0);
//        textViewReq.setText(voice);
//        if (done) {
//        Note note = new Note();
//        note.writeToFile(voice, context);
//
//        } else {
//        done = false;
//        if (print != "") {
//        heard(result.get(0), context, textViewRes, textViewReq, stpBtn, activity);
//        } else {
//        runStart(result.get(0));
//        }
////                heard(result.get(0), context, textViewRes, textViewReq, stpBtn);
//        }
//
//        } else {
//        textViewRes.setText("No results");
//        ttsManager.initQueue(textViewRes.getText().toString());
//        }
//        break;
//        }
//        }
