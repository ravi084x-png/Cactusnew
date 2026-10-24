package com.cactus.fix;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class FixActivity extends Activity {
    static final String PROMPT =
        "You are a text cleaner. Fix spelling mistakes, punctuation, grammar and tense in the user's text. "
      + "Remove filler words (umm, uh, matlab, like, you know) and accidental repeated words or false starts. "
      + "Keep intentional repeats (e.g. 'bahut bahut', 'dheere dheere', 'haha haha'). "
      + "Do NOT change the meaning. Keep the same language and same script (Hindi, English, Hinglish in Roman). "
      + "Keep the tone and style. Output ONLY the corrected text, nothing else.";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        CharSequence cs = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        final boolean ro = getIntent().getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false);
        final String key = getSharedPreferences("cactus", MODE_PRIVATE).getString("key", "");
        if (cs == null || cs.toString().trim().isEmpty()) { finish(); return; }
        if (key.isEmpty()) {
            toast("Pehle Cactus app kholkar API key daalo");
            finish();
            return;
        }
        toast("🌵 Fixing...");
        final String input = cs.toString();
        new Thread(() -> {
            try {
                String out = fix(input, key);
                runOnUiThread(() -> done(out, ro));
            } catch (Exception ex) {
                runOnUiThread(() -> { toast("Error: " + ex.getMessage()); finish(); });
            }
        }).start();
    }

    void done(String out, boolean ro) {
        if (ro) {
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("cactus", out));
            toast("Copy ho gaya, paste kar lo 🌵");
            setResult(RESULT_CANCELED);
        } else {
            Intent r = new Intent();
            r.putExtra(Intent.EXTRA_PROCESS_TEXT, out);
            setResult(RESULT_OK, r);
        }
        finish();
    }

    void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }

    String fix(String text, String key) throws Exception {
        JSONObject body = new JSONObject();
        body.put("model", "llama-3.3-70b-versatile");
        body.put("temperature", 0.2);
        JSONArray msgs = new JSONArray();
        msgs.put(new JSONObject().put("role", "system").put("content", PROMPT));
        msgs.put(new JSONObject().put("role", "user").put("content", text));
        body.put("messages", msgs);

        HttpURLConnection c = (HttpURLConnection) new URL("https://api.groq.com/openai/v1/chat/completions").openConnection();
        c.setRequestMethod("POST");
        c.setConnectTimeout(15000);
        c.setReadTimeout(30000);
        c.setRequestProperty("Content-Type", "application/json");
        c.setRequestProperty("Authorization", "Bearer " + key);
        c.setDoOutput(true);
        try (OutputStream os = c.getOutputStream()) {
            os.write(body.toString().getBytes("UTF-8"));
        }
        int code = c.getResponseCode();
        InputStream is = code >= 400 ? c.getErrorStream() : c.getInputStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        if (code >= 400) {
            String s = sb.toString();
            throw new Exception("HTTP " + code + " " + (s.length() > 150 ? s.substring(0, 150) : s));
        }
        return new JSONObject(sb.toString()).getJSONArray("choices")
                .getJSONObject(0).getJSONObject("message").getString("content").trim();
    }
                 }
