package com.assistant.toby;

import android.content.Context;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** Keep the original filename so existing notes survive an in-place app update. */
public final class Note {
    public void save(Context context, String text) throws IOException {
        try (Writer writer = new OutputStreamWriter(context.openFileOutput("note.txt", Context.MODE_APPEND), StandardCharsets.UTF_8)) {
            writer.write("\n" + text.trim() + "\n");
        }
    }
    public String read(Context context) throws IOException {
        if (!context.getFileStreamPath("note.txt").exists()) return "You haven’t saved any notes yet.";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.openFileInput("note.txt"), StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) result.append(line).append('\n');
            return result.toString().trim();
        }
    }
}
