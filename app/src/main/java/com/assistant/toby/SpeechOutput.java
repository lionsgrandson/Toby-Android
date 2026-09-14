package com.assistant.toby;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

/** One engine per Activity, with pending speech cleared on stop and destruction. */
final class SpeechOutput {
    private TextToSpeech engine;
    private boolean ready, closed;
    private String pending;
    SpeechOutput(Context context) {
        engine = new TextToSpeech(context.getApplicationContext(), status -> {
            if (closed || engine == null || status != TextToSpeech.SUCCESS) return;
            int language = engine.setLanguage(Locale.US);
            ready = language != TextToSpeech.LANG_MISSING_DATA && language != TextToSpeech.LANG_NOT_SUPPORTED;
            if (ready && pending != null) speak(pending);
        });
    }
    void speak(String text) {
        if (closed) return;
        pending = text;
        if (!ready) return;
        pending = null;
        engine.stop();
        int limit = TextToSpeech.getMaxSpeechInputLength() - 1;
        for (int start = 0; start < text.length(); start += limit) {
            engine.speak(text.substring(start, Math.min(text.length(), start + limit)),
                    start == 0 ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD, null, "toby-" + start);
        }
    }
    void stop() { pending = null; if (engine != null) engine.stop(); }
    void close() { closed = true; stop(); if (engine != null) engine.shutdown(); }
}
