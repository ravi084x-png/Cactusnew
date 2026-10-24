package com.cactus.fix;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        SharedPreferences sp = getSharedPreferences("cactus", MODE_PRIVATE);
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(48, 96, 48, 48);

        TextView t = new TextView(this);
        t.setText("🌵 Cactus\n\n1. Neeche Groq API key daalo aur Save dabao.\n2. Kisi bhi app mein text select karo → menu (⋮ ya More) → Cactus Fix.");
        t.setTextSize(18);

        EditText e = new EditText(this);
        e.setHint("Groq API key (gsk_...)");
        e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        e.setText(sp.getString("key", ""));

        Button s = new Button(this);
        s.setText("Save");
        s.setOnClickListener(v -> {
            sp.edit().putString("key", e.getText().toString().trim()).apply();
            Toast.makeText(this, "Save ho gaya 🌵", Toast.LENGTH_SHORT).show();
        });

        l.addView(t);
        l.addView(e);
        l.addView(s);
        setContentView(l);
    }
}
