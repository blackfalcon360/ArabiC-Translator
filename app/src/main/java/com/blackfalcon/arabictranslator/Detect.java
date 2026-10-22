package com.blackfalcon.arabictranslator;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/** Pure helpers: language guess, text chunking, JSON clean-up. */
public class Detect {
    static final String URDU_ONLY = "\u0679\u0688\u0691\u06BA\u06D2\u06C1\u06BE\u067E\u0686\u06AF\u06A9\u06CC\u06D3\u06C2";
    static final String ARABIC_ONLY = "\u0629\u064A\u0643\u0649\u0623\u0625\u0647";

    /** Returns "en", "ur" or "ar" (guess; ties between Urdu and Arabic go to "ur"). */
    public static String detect(String s) {
        int lat = 0, arab = 0, urd = 0, arSpec = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
                lat++;
            } else if ((c >= 0x0600 && c <= 0x06FF) || (c >= 0x0750 && c <= 0x077F)
                    || (c >= 0xFB50 && c <= 0xFDFF) || (c >= 0xFE70 && c <= 0xFEFF)) {
                arab++;
                if (URDU_ONLY.indexOf(c) >= 0) urd++;
                if (ARABIC_ONLY.indexOf(c) >= 0) arSpec++;
            }
        }
        if (arab == 0 || lat > arab) return "en";
        if (arSpec > urd) return "ar";
        return "ur";
    }

    static int bytes(String s) { return s.getBytes(StandardCharsets.UTF_8).length; }

    /** Splits text at sentence ends into pieces of at most maxBytes (UTF-8). */
    public static ArrayList<String> chunk(String s, int maxBytes) {
        ArrayList<String> sents = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            sb.append(ch);
            if (ch == '.' || ch == '!' || ch == '?' || ch == '\u061F' || ch == '\u06D4' || ch == '\n') {
                sents.add(sb.toString());
                sb.setLength(0);
            }
        }
        if (sb.length() > 0) sents.add(sb.toString());

        ArrayList<String> out = new ArrayList<String>();
        StringBuilder cur = new StringBuilder();
        for (String t : sents) {
            if (bytes(t) > maxBytes) {
                if (cur.length() > 0) { out.add(cur.toString()); cur.setLength(0); }
                StringBuilder piece = new StringBuilder();
                for (int i = 0; i < t.length(); ) {
                    int cp = t.codePointAt(i);
                    String ch = new String(Character.toChars(cp));
                    if (bytes(piece.toString()) + bytes(ch) > maxBytes) {
                        out.add(piece.toString());
                        piece.setLength(0);
                    }
                    piece.append(ch);
                    i += Character.charCount(cp);
                }
                if (piece.length() > 0) out.add(piece.toString());
            } else if (bytes(cur.toString()) + bytes(t) > maxBytes) {
                out.add(cur.toString());
                cur.setLength(0);
                cur.append(t);
            } else {
                cur.append(t);
            }
        }
        if (cur.length() > 0) out.add(cur.toString());
        return out;
    }

    /** Cuts a model reply down to the outermost {...} (drops ```json fences and chatter). */
    public static String jsonOnly(String s) {
        int a = s.indexOf('{');
        int b = s.lastIndexOf('}');
        if (a < 0 || b < a) return s;
        return s.substring(a, b + 1);
    }

    public static String unescape(String s) {
        return s.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"").replace("&#39;", "'");
    }
}
