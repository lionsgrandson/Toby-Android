package com.assistant.toby;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;

/** HTTPS short answers without obsolete desktop HTTP libraries or UI-thread networking. */
final class WolframClient {
    String answer(String question, String appId) throws IOException {
        URL url = new URL("https://api.wolframalpha.com/v1/result?appid="
                + URLEncoder.encode(appId, "UTF-8") + "&i=" + URLEncoder.encode(question, "UTF-8"));
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(20000);
        connection.setInstanceFollowRedirects(false);
        try {
            int status = connection.getResponseCode();
            if (status == 401 || status == 403) return "Wolfram rejected the App ID. Check it and your API access in Settings.";
            if (status == 429) return "Wolfram’s request limit has been reached. Please try again later.";
            if (status == 501) return "Wolfram couldn’t find a short answer. Try rephrasing your question.";
            if (status != 200) return "Wolfram is unavailable right now (" + status + "). Please try again later.";
            try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                char[] buffer = new char[2048];
                StringBuilder answer = new StringBuilder();
                int count;
                while ((count = reader.read(buffer)) != -1) {
                    answer.append(buffer, 0, count);
                    if (answer.length() > 64000) throw new IOException("Answer too long");
                }
                String result = answer.toString().trim();
                return result.isEmpty() ? "Wolfram returned an empty answer. Try rephrasing your question." : result;
            }
        } finally { connection.disconnect(); }
    }
}
