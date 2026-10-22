package com.blackfalcon.arabictranslator;

import android.content.Context;
import android.content.SharedPreferences;

public class Store {
    private final SharedPreferences p;

    public Store(Context c) {
        p = c.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public String lang() { return p.getString("lang", "ur"); }
    public void setLang(String l) { p.edit().putString("lang", l).apply(); }
    public boolean dark() { return p.getBoolean("dark", true); }
    public void setDark(boolean v) { p.edit().putBoolean("dark", v).apply(); }
    public boolean rtl() { return !lang().equals("en"); }

    public String tr(String ur, String en, String ar) {
        String l = lang();
        if (l.equals("en")) return en;
        if (l.equals("ar")) return ar;
        return ur;
    }

    /** Source language: 0 auto, 1 English, 2 Urdu, 3 Arabic (any dialect). */
    public int source() { return p.getInt("source", 0); }
    public void setSource(int v) { p.edit().putInt("source", v).apply(); }

    /** Basic targets: bit0 English, bit1 Urdu, bit2 Fusha (MSA). Default: Fusha. */
    public int basic() { return p.getInt("basic", 4); }
    public void setBasic(int v) { p.edit().putInt("basic", v).apply(); }

    /** Dialect targets: bit i = Langs.C[i]. */
    public int dialects() { return p.getInt("dialects", 0); }
    public void setDialects(int v) { p.edit().putInt("dialects", v).apply(); }

    public String text() { return p.getString("text", ""); }
    public void setText(String s) { p.edit().putString("text", s).apply(); }

    public String key() { return p.getString("key", ""); }
    public void setKey(String k) { p.edit().putString("key", k).apply(); }
    public String model() { return p.getString("model", "claude-haiku-5-5"); }
    public void setModel(String m) { p.edit().putString("model", m).apply(); }
}
