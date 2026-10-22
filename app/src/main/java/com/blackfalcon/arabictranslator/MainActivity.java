package com.blackfalcon.arabictranslator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity {

    static final int TEAL = 0xFF00897B;

    static class Tgt {
        String code, desc, flag, title, sub = "";
        boolean dialect;
    }

    Store st;
    int cBg, cCard, cText, cSub;
    TextView[] tabViews;
    ScrollView[] scrolls;
    LinearLayout mainBox, dialBox, setBox, resultBox;
    EditText et, etKey, etModel;
    TextView status, dialSummary;
    TextView[] srcChips, basicChips;
    RadioGroup rgLang, rgTheme;
    int[] langIds = new int[3];
    int[] themeIds = new int[2];
    boolean busy = false;
    final StringBuilder allText = new StringBuilder();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        st = new Store(this);
        buildUI();
        showTab(0);
    }

    // ---------- helpers ----------
    String tr(String ur, String en, String ar) { return st.tr(ur, en, ar); }

    int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }

    GradientDrawable round(int color, int r) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(r));
        return g;
    }

    TextView tv(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    LinearLayout cardBox() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(round(cCard, 14));
        c.setPadding(dp(14), dp(10), dp(14), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(10), dp(5), dp(10), dp(5));
        c.setLayoutParams(lp);
        return c;
    }

    Button btn(String s, int color, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(Color.WHITE);
        b.setBackground(round(color, 10));
        b.setOnClickListener(l);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(8), 0, 0);
        b.setLayoutParams(lp);
        return b;
    }

    TextView label(String s) {
        TextView t = tv(s, 15, cText, true);
        t.setPadding(0, dp(8), 0, dp(4));
        return t;
    }

    TextView chip(String s, View.OnClickListener l) {
        TextView t = tv(s, 14, cText, false);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(6), dp(9), dp(6), dp(9));
        t.setOnClickListener(l);
        return t;
    }

    void paintChip(TextView t, boolean on) {
        t.setTextColor(on ? Color.WHITE : cText);
        t.setBackground(round(on ? TEAL : (st.dark() ? 0xFF2A2A2A : 0xFFE0E0E0), 18));
    }

    RadioGroup radios(String[] labels, int checked, int[] ids) {
        RadioGroup g = new RadioGroup(this);
        for (int i = 0; i < labels.length; i++) {
            RadioButton r = new RadioButton(this);
            r.setText(labels[i]);
            r.setTextColor(cText);
            r.setTextSize(15);
            r.setId(View.generateViewId());
            ids[i] = r.getId();
            g.addView(r);
        }
        g.check(ids[checked]);
        return g;
    }

    EditText edit(String val, String hint, boolean multi) {
        EditText e = new EditText(this);
        e.setText(val);
        e.setHint(hint);
        e.setHintTextColor(0xFF777777);
        e.setTextColor(Color.BLACK);
        e.setTextSize(multi ? 18 : 14);
        e.setBackground(round(0xFFFFFFFF, 10));
        e.setPadding(dp(12), dp(10), dp(12), dp(10));
        if (multi) {
            e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            e.setMinLines(4);
            e.setGravity(Gravity.TOP);
        } else {
            e.setSingleLine(true);
            e.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        return e;
    }

    // ---------- UI ----------
    void buildUI() {
        boolean dark = st.dark();
        cBg = dark ? 0xFF000000 : 0xFFF2F2F2;
        cCard = dark ? 0xFF1A1A1A : 0xFFFFFFFF;
        cText = dark ? 0xFFFFFFFF : 0xFF111111;
        cSub = dark ? 0xFFAAAAAA : 0xFF555555;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(cBg);
        root.setLayoutDirection(st.rtl() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

        TextView title = tv("Arabic Translator", 22, cText, true);
        title.setPadding(dp(14), dp(14), dp(14), dp(6));
        root.addView(title);

        String[] names = {tr("ترجمہ", "Translate", "ترجمة"), tr("لہجے", "Dialects", "اللهجات"),
            tr("سیٹنگز", "Settings", "الإعدادات")};
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(dp(6), dp(4), dp(6), dp(8));
        tabViews = new TextView[3];
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            TextView t = tv(names[i], 14, cText, false);
            t.setGravity(Gravity.CENTER);
            t.setPadding(dp(4), dp(9), dp(4), dp(9));
            t.setOnClickListener(v -> showTab(idx));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(dp(3), 0, dp(3), 0);
            tabs.addView(t, lp);
            tabViews[i] = t;
        }
        root.addView(tabs);

        FrameLayout content = new FrameLayout(this);
        scrolls = new ScrollView[3];
        mainBox = new LinearLayout(this);
        dialBox = new LinearLayout(this);
        setBox = new LinearLayout(this);
        LinearLayout[] boxes = {mainBox, dialBox, setBox};
        for (int i = 0; i < 3; i++) {
            boxes[i].setOrientation(LinearLayout.VERTICAL);
            boxes[i].setPadding(0, dp(4), 0, dp(12));
            scrolls[i] = new ScrollView(this);
            scrolls[i].addView(boxes[i]);
            content.addView(scrolls[i], new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        root.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView credit = tv("By: Black Falcon \uD83E\uDD85", 13, cSub, false);
        credit.setGravity(Gravity.CENTER);
        credit.setPadding(0, dp(8), 0, dp(10));
        root.addView(credit);

        setContentView(root);
        buildMain();
        buildDialects();
        buildSettings();
    }

    void showTab(int i) {
        for (int k = 0; k < 3; k++) {
            boolean sel = k == i;
            tabViews[k].setTextColor(sel ? Color.WHITE : cText);
            tabViews[k].setBackground(round(sel ? TEAL : cCard, 20));
            scrolls[k].setVisibility(sel ? View.VISIBLE : View.GONE);
        }
    }

    // ---------- translate tab ----------
    void refreshChips() {
        for (int i = 0; i < srcChips.length; i++) paintChip(srcChips[i], st.source() == i);
        for (int i = 0; i < basicChips.length; i++) paintChip(basicChips[i], (st.basic() & (1 << i)) != 0);
        int m = st.dialects();
        StringBuilder sb = new StringBuilder();
        int n = 0;
        for (int i = 0; i < Langs.count(); i++) {
            if ((m & (1 << i)) != 0) { sb.append(Langs.flag(Langs.C[i][0])); n++; }
        }
        dialSummary.setText(tr("لہجے: ", "Dialects: ", "اللهجات: ")
            + (n == 0 ? "\u2014" : n + "  " + sb));
    }

    void buildMain() {
        mainBox.removeAllViews();

        LinearLayout c1 = cardBox();
        c1.addView(label(tr("کس زبان سے", "From", "من")));
        String[] sn = {tr("خودکار", "Auto", "تلقائي"), "English", "اردو", "العربية"};
        LinearLayout sr = new LinearLayout(this);
        srcChips = new TextView[4];
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            srcChips[i] = chip(sn[i], v -> { st.setSource(idx); refreshChips(); });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(dp(3), 0, dp(3), 0);
            sr.addView(srcChips[i], lp);
        }
        c1.addView(sr);
        et = edit(st.text(), tr("یہاں متن لکھیں یا پیسٹ کریں…", "Type or paste text here…",
                "اكتب أو الصق النص هنا…"), true);
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ep.setMargins(0, dp(8), 0, 0);
        c1.addView(et, ep);
        LinearLayout row = new LinearLayout(this);
        TextView paste = chip(tr("پیسٹ", "Paste", "لصق"), v -> pasteText());
        TextView clear = chip(tr("صاف", "Clear", "مسح"), v -> et.setText(""));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        rp.setMargins(dp(3), dp(8), dp(3), 0);
        paintChip(paste, false);
        paintChip(clear, false);
        row.addView(paste, rp);
        row.addView(clear, rp);
        c1.addView(row);
        mainBox.addView(c1);

        LinearLayout c2 = cardBox();
        c2.addView(label(tr("کس میں", "To", "إلى")));
        String[] bn = {tr("انگریزی", "English", "الإنجليزية"), tr("اردو", "Urdu", "الأردية"),
            tr("فصیح عربی", "Fusha Arabic", "الفصحى")};
        LinearLayout br = new LinearLayout(this);
        basicChips = new TextView[3];
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            basicChips[i] = chip(bn[i], v -> { st.setBasic(st.basic() ^ (1 << idx)); refreshChips(); });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(dp(3), 0, dp(3), 0);
            br.addView(basicChips[i], lp);
        }
        c2.addView(br);
        dialSummary = tv("", 14, cText, false);
        dialSummary.setPadding(0, dp(10), 0, 0);
        c2.addView(dialSummary);
        LinearLayout dr = new LinearLayout(this);
        TextView pick = chip(tr("لہجے چنیں", "Choose dialects", "اختر اللهجات"), v -> showTab(1));
        TextView all = chip(tr("تمام لہجے", "All dialects", "كل اللهجات"), v -> {
            st.setDialects((1 << Langs.count()) - 1);
            buildDialects();
            refreshChips();
        });
        paintChip(pick, false);
        paintChip(all, false);
        LinearLayout.LayoutParams dp2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        dp2.setMargins(dp(3), dp(8), dp(3), 0);
        dr.addView(pick, dp2);
        dr.addView(all, dp2);
        c2.addView(dr);
        c2.addView(btn(tr("ترجمہ کریں", "Translate", "ترجم"), TEAL, v -> translate()));
        status = tv("", 12, cSub, false);
        status.setPadding(0, dp(6), 0, 0);
        c2.addView(status);
        mainBox.addView(c2);

        resultBox = new LinearLayout(this);
        resultBox.setOrientation(LinearLayout.VERTICAL);
        mainBox.addView(resultBox);
        refreshChips();
    }

    void pasteText() {
        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null && cm.hasPrimaryClip() && cm.getPrimaryClip().getItemCount() > 0) {
                CharSequence t = cm.getPrimaryClip().getItemAt(0).coerceToText(this);
                if (t != null) et.setText(t);
            }
        } catch (Exception e) { }
    }

    // ---------- dialects tab ----------
    void buildDialects() {
        dialBox.removeAllViews();
        LinearLayout top = cardBox();
        LinearLayout row = new LinearLayout(this);
        TextView all = chip(tr("سب چنیں", "Select all", "تحديد الكل"), v -> {
            st.setDialects((1 << Langs.count()) - 1);
            buildDialects();
            refreshChips();
        });
        TextView none = chip(tr("سب ہٹائیں", "Clear all", "إلغاء الكل"), v -> {
            st.setDialects(0);
            buildDialects();
            refreshChips();
        });
        paintChip(all, false);
        paintChip(none, false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(dp(3), 0, dp(3), 0);
        row.addView(all, lp);
        row.addView(none, lp);
        top.addView(row);
        top.addView(tv(tr("جن لہجوں میں ترجمہ چاہیے انہیں چنیں۔ لہجوں کے لیے سیٹنگز میں Claude کی لازمی ہے۔",
                "Tick the dialects you want. Dialects need a Claude API key (Settings).",
                "اختر اللهجات المطلوبة. اللهجات تتطلب مفتاح Claude API (الإعدادات)."), 12, cSub, false));
        dialBox.addView(top);

        String[] regions = {
            tr("وادیٔ نیل", "Nile Valley", "وادي النيل"),
            tr("بلاد الشام", "Levant", "بلاد الشام"),
            tr("جزیرہ نما عرب، عراق اور یمن", "Arabian Peninsula, Iraq & Yemen", "الجزيرة العربية والعراق واليمن"),
            tr("مغرب", "Maghreb", "المغرب العربي"),
            tr("افریقی ساحل اور جزائر", "Horn of Africa & islands", "القرن الأفريقي والجزر")};
        for (int r = 0; r < regions.length; r++) {
            LinearLayout card = cardBox();
            card.addView(tv(regions[r], 15, 0xFF4DB6AC, true));
            for (int i = 0; i < Langs.count(); i++) {
                if (Langs.region(i) != r) continue;
                final int idx = i;
                CheckBox cb = new CheckBox(this);
                cb.setText(Langs.flag(Langs.C[i][0]) + "  " + Langs.country(i, st.lang())
                        + " \u2014 " + Langs.dialect(i, st.lang()));
                cb.setTextColor(cText);
                cb.setTextSize(15);
                cb.setChecked((st.dialects() & (1 << i)) != 0);
                cb.setOnCheckedChangeListener((x, on) -> {
                    st.setDialects(on ? (st.dialects() | (1 << idx)) : (st.dialects() & ~(1 << idx)));
                    refreshChips();
                });
                card.addView(cb);
            }
            dialBox.addView(card);
        }
        TextView note = tv(tr(
            "صومالیہ، جبوتی اور کومورو میں عربی کے الگ نمایاں لہجے کم ہیں؛ وہاں نتیجہ فصیح کے قریب سادہ عربی ہو گا۔",
            "Somalia, Djibouti and the Comoros have few distinct Arabic dialects; expect simple Arabic close to Fusha there.",
            "في الصومال وجيبوتي وجزر القمر لهجات عربية مميزة قليلة؛ توقع عربية بسيطة قريبة من الفصحى."),
            11, cSub, false);
        note.setPadding(dp(14), dp(4), dp(14), dp(8));
        dialBox.addView(note);
    }

    // ---------- translating ----------
    ArrayList<Tgt> buildTargets(String src) {
        ArrayList<Tgt> out = new ArrayList<Tgt>();
        String lang = st.lang();
        int b = st.basic();
        if ((b & 1) != 0 && !src.equals("en")) {
            Tgt t = new Tgt();
            t.code = "en"; t.desc = "English"; t.flag = "\uD83C\uDDEC\uD83C\uDDE7";
            t.title = t.flag + "  " + tr("انگریزی", "English", "الإنجليزية");
            out.add(t);
        }
        if ((b & 2) != 0 && !src.equals("ur")) {
            Tgt t = new Tgt();
            t.code = "ur"; t.desc = "Urdu"; t.flag = Langs.flag("PK");
            t.title = t.flag + "  " + tr("اردو", "Urdu", "الأردية");
            out.add(t);
        }
        if ((b & 4) != 0) {
            Tgt t = new Tgt();
            t.code = "msa"; t.desc = "Modern Standard Arabic (Fusha)"; t.flag = "\uD83C\uDF0D";
            t.title = t.flag + "  " + tr("فصیح عربی (MSA)", "Fusha Arabic (MSA)", "العربية الفصحى");
            out.add(t);
        }
        for (int i = 0; i < Langs.count(); i++) {
            if ((st.dialects() & (1 << i)) == 0) continue;
            Tgt t = new Tgt();
            t.code = Langs.code(i);
            t.desc = Langs.C[i][4] + " (" + Langs.C[i][1] + ")";
            t.flag = Langs.flag(Langs.C[i][0]);
            t.title = t.flag + "  " + Langs.country(i, lang);
            t.sub = Langs.dialect(i, lang);
            t.dialect = true;
            out.add(t);
        }
        return out;
    }

    String errText(String e) {
        if (e.equals("TOO_LONG")) return tr("جواب بہت لمبا ہو گیا؛ متن چھوٹا کریں یا کم لہجے چنیں۔",
                "The answer was too long; shorten the text or pick fewer dialects.",
                "الإجابة طويلة جدًا؛ اختصر النص أو اختر لهجات أقل.");
        if (e.equals("LIMIT")) return tr("مفت سروس کی روزانہ حد پوری ہو گئی؛ کل کوشش کریں یا Claude کی لگائیں۔",
                "The free service's daily limit is reached; try tomorrow or add a Claude key.",
                "بلغت الخدمة المجانية حدها اليومي؛ جرّب غدًا أو أضف مفتاح Claude.");
        if (e.equals("network")) return tr("انٹرنیٹ یا سرور کا مسئلہ؛ دوبارہ کوشش کریں۔",
                "Internet or server problem; try again.", "مشكلة في الإنترنت أو الخادم؛ حاول مرة أخرى.");
        return e;
    }

    void translate() {
        if (busy) return;
        final String text = et.getText().toString().trim();
        if (text.length() == 0) {
            Toast.makeText(this, tr("پہلے متن لکھیں", "Write some text first", "اكتب نصًا أولًا"),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        st.setText(text);
        final String src = st.source() == 0 ? Detect.detect(text)
                : new String[] {"", "en", "ur", "ar"}[st.source()];
        final ArrayList<Tgt> tg = buildTargets(src);
        if (tg.isEmpty()) {
            Toast.makeText(this, tr("ترجمے کی زبان چنیں", "Pick a target language", "اختر لغة الهدف"),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        final String key = st.key().trim();
        final String model = st.model().trim().length() > 0 ? st.model().trim() : "claude-haiku-5-5";
        boolean onlyDialects = true;
        for (Tgt t : tg) if (!t.dialect) onlyDialects = false;
        if (key.length() == 0 && onlyDialects) {
            new AlertDialog.Builder(this)
                .setMessage(tr("لہجوں کے ترجمے کے لیے سیٹنگز میں اپنی Claude API کی ڈالیں۔",
                        "Dialect translation needs your Claude API key in Settings.",
                        "ترجمة اللهجات تتطلب مفتاح Claude API في الإعدادات."))
                .setPositiveButton(tr("سیٹنگز", "Settings", "الإعدادات"), (d, w) -> showTab(2))
                .setNegativeButton(tr("بند", "Close", "إغلاق"), null)
                .show();
            return;
        }
        final String srcName = src.equals("en") ? "English" : (src.equals("ur") ? "Urdu"
                : "Arabic (could be any dialect or Fusha)");
        busy = true;
        status.setText(tr("ترجمہ ہو رہا ہے…", "Translating…", "جارٍ الترجمة…"));
        resultBox.removeAllViews();

        new Thread(() -> {
            HashMap<String, String> res = new HashMap<String, String>();
            String error = "";
            if (key.length() > 0) {
                int bs = text.length() > 400 ? 4 : 8;
                for (int i = 0; i < tg.size(); i += bs) {
                    ArrayList<String[]> batch = new ArrayList<String[]>();
                    for (int k = i; k < Math.min(tg.size(), i + bs); k++) {
                        batch.add(new String[] {tg.get(k).code, tg.get(k).desc});
                    }
                    HashMap<String, String> r = Net.claude(key, model, srcName, text, batch);
                    if (r == null) { error = Net.err; break; }
                    res.putAll(r);
                }
            } else {
                for (Tgt t : tg) {
                    if (t.dialect) continue;
                    String to = t.code.equals("msa") ? "ar" : t.code;
                    if (src.equals("ar") && to.equals("ar")) continue;
                    String r = Net.myMemory(src, to, text);
                    if (r == null) { error = Net.err; break; }
                    res.put(t.code, r);
                }
            }
            final HashMap<String, String> fres = res;
            final String ferr = error;
            runOnUiThread(() -> showResults(tg, fres, ferr, key.length() > 0));
        }).start();
    }

    void showResults(ArrayList<Tgt> tg, HashMap<String, String> res, String err, boolean hasKey) {
        busy = false;
        resultBox.removeAllViews();
        allText.setLength(0);
        int ok = 0;
        for (Tgt t : tg) {
            String r = res.get(t.code);
            if (r != null && r.length() > 0) {
                ok++;
                allText.append(t.title.replace("  ", " ")).append(t.sub.length() > 0 ? " (" + t.sub + ")" : "")
                       .append(":\n").append(r).append("\n\n");
            }
            resultBox.addView(resultCard(t, r, hasKey));
        }
        if (ok > 1) {
            LinearLayout c = cardBox();
            c.addView(btn(tr("سب نتائج کاپی کریں", "Copy all results", "نسخ كل النتائج"), 0xFF455A64,
                    v -> copy(allText.toString().trim())));
            resultBox.addView(c);
        }
        status.setText(err.length() > 0 ? errText(err)
                : tr("مکمل", "Done", "تم") + " \u2714");
    }

    View resultCard(final Tgt t, final String text, boolean hasKey) {
        LinearLayout c = cardBox();
        c.addView(tv(t.title, 16, cText, true));
        if (t.sub.length() > 0) c.addView(tv(t.sub, 12, cSub, false));
        TextView body;
        if (text == null || text.length() == 0) {
            String why = (t.dialect && !hasKey)
                ? tr("اس کے لیے Claude کی چاہیے (سیٹنگز)", "Needs a Claude key (Settings)", "يتطلب مفتاح Claude (الإعدادات)")
                : "\u2014";
            body = tv(why, 13, cSub, false);
        } else {
            body = tv(text, 20, cText, false);
            body.setTextIsSelectable(true);
            if (!t.code.equals("en")) {
                body.setTextDirection(View.TEXT_DIRECTION_RTL);
                body.setGravity(Gravity.START);
            }
        }
        body.setPadding(0, dp(6), 0, dp(6));
        c.addView(body);
        if (text != null && text.length() > 0) {
            LinearLayout row = new LinearLayout(this);
            TextView cp = chip(tr("کاپی", "Copy", "نسخ"), v -> copy(text));
            TextView sh = chip(tr("شیئر", "Share", "مشاركة"), v -> share(text));
            paintChip(cp, false);
            paintChip(sh, false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(dp(3), 0, dp(3), 0);
            row.addView(cp, lp);
            row.addView(sh, lp);
            c.addView(row);
        }
        return c;
    }

    void copy(String s) {
        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("translation", s));
            Toast.makeText(this, tr("کاپی ہو گیا", "Copied", "تم النسخ"), Toast.LENGTH_SHORT).show();
        } catch (Exception e) { }
    }

    void share(String s) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, s);
        startActivity(Intent.createChooser(i, tr("شیئر", "Share", "مشاركة")));
    }

    // ---------- settings ----------
    void buildSettings() {
        setBox.removeAllViews();
        LinearLayout c = cardBox();
        c.addView(label(tr("زبان", "Language", "اللغة")));
        String l = st.lang();
        rgLang = radios(new String[] {"اردو", "English", "العربية"},
                l.equals("en") ? 1 : (l.equals("ar") ? 2 : 0), langIds);
        c.addView(rgLang);
        c.addView(label(tr("تھیم", "Theme", "المظهر")));
        rgTheme = radios(new String[] {tr("ڈارک موڈ", "Dark mode", "الوضع الداكن"),
            tr("لائٹ موڈ", "Light mode", "الوضع الفاتح")}, st.dark() ? 0 : 1, themeIds);
        c.addView(rgTheme);
        setBox.addView(c);

        LinearLayout k = cardBox();
        k.addView(label(tr("Claude API کی (لہجوں کے لیے لازمی)", "Claude API key (needed for dialects)",
                "مفتاح Claude API (مطلوب للهجات)")));
        etKey = edit(st.key(), "sk-ant-\u2026", false);
        etKey.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        k.addView(etKey);
        k.addView(label(tr("ماڈل", "Model", "النموذج")));
        etModel = edit(st.model(), "claude-haiku-5-5", false);
        k.addView(etModel);
        k.addView(btn(tr("محفوظ کریں", "Save", "حفظ"), TEAL, v -> saveSettings()));
        setBox.addView(k);

        TextView info = tv(tr(
            "بغیر کی کے: صرف انگریزی، اردو اور فصیح عربی کے درمیان مفت سروس (MyMemory) سے ترجمہ ہوتا ہے، جس کی روزانہ حد ہے اور معیار اوسط ہو سکتا ہے۔\n\nکی کے ساتھ: ہر زبان اور تمام عرب لہجوں میں ترجمہ، اور عربی لہجے سے انگریزی/اردو/فصیح میں بھی۔ ترجمہ شدہ متن Anthropic کے سرور کو جاتا ہے اور فیس آپ کی کی پر لگتی ہے۔ کی صرف آپ کے فون میں محفوظ رہتی ہے۔\n\nمشین ترجمہ ہے؛ اہم یا مذہبی/قانونی متن کی تصدیق کسی ماہر سے کروائیں۔ لہجوں کا نتیجہ بھی اندازاً ہوتا ہے۔",
            "Without a key: only English, Urdu and Fusha Arabic through a free service (MyMemory), which has a daily limit and average quality.\n\nWith a key: every language and all Arab dialects, and also from an Arabic dialect to English/Urdu/Fusha. The text is sent to Anthropic's servers and the cost is billed to your key. The key is stored only on your phone.\n\nMachine translation; have important, religious or legal text checked by an expert. Dialect output is approximate too.",
            "بدون مفتاح: الترجمة فقط بين الإنجليزية والأردية والفصحى عبر خدمة مجانية (MyMemory) لها حد يومي وجودة متوسطة.\n\nمع المفتاح: كل اللغات وجميع اللهجات العربية، ومن اللهجة إلى الإنجليزية/الأردية/الفصحى. يُرسل النص إلى خوادم Anthropic وتُحسب التكلفة على مفتاحك. يُحفظ المفتاح على هاتفك فقط.\n\nترجمة آلية؛ اطلب من مختص مراجعة النصوص المهمة أو الدينية أو القانونية. ونتائج اللهجات تقريبية أيضًا."),
            12, cSub, false);
        info.setPadding(dp(14), dp(10), dp(14), dp(8));
        setBox.addView(info);
    }

    void saveSettings() {
        String[] langs = {"ur", "en", "ar"};
        int sel = rgLang.getCheckedRadioButtonId();
        for (int i = 0; i < 3; i++) if (langIds[i] == sel) st.setLang(langs[i]);
        st.setDark(rgTheme.getCheckedRadioButtonId() == themeIds[0]);
        st.setKey(etKey.getText().toString().trim());
        st.setModel(etModel.getText().toString().trim());
        st.setText(et.getText().toString());
        buildUI();
        showTab(2);
        Toast.makeText(this, tr("محفوظ ہو گیا", "Saved", "تم الحفظ"), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (et != null) st.setText(et.getText().toString());
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setTitle(tr("ایپ بند کریں؟", "Exit app?", "إغلاق التطبيق؟"))
            .setMessage(tr("کیا آپ واقعی باہر نکلنا چاہتے ہیں؟", "Do you really want to exit?",
                    "هل تريد الخروج فعلًا؟"))
            .setPositiveButton(tr("ہاں", "Yes", "نعم"), (dlg, w) -> finish())
            .setNegativeButton(tr("نہیں", "No", "لا"), null)
            .show();
    }
}
