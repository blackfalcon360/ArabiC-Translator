package com.blackfalcon.arabictranslator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/** Two engines: Claude (user's own key; every language and dialect) and MyMemory (no key; en/ur/Fusha only). */
public class Net {
    public static String err = "";

    static String read(InputStream in) throws Exception {
        if (in == null) return "";
        BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[8192];
        int n;
        while ((n = r.read(buf)) > 0) sb.append(buf, 0, n);
        r.close();
        return sb.toString();
    }

    // ---------------- Claude ----------------
    static final String SYSTEM =
        "You are a professional translator between English, Urdu, Modern Standard Arabic (Fusha) and all "
        + "Arabic dialects. Translate the meaning faithfully and naturally; keep names, numbers and line breaks. "
        + "The source may be English, Urdu, or Arabic in any dialect: understand it correctly. "
        + "For a dialect target, write the way people of that country really speak (colloquial), in Arabic script. "
        + "For Fusha write correct Modern Standard Arabic. For Urdu write natural standard Urdu. "
        + "Never add explanations, notes or transliteration. "
        + "Reply with ONLY one JSON object: {\"results\":[{\"code\":\"<target code>\",\"text\":\"<translation>\"}]} "
        + "with exactly one entry per requested target code.";

    /** targets: {code, description}. Returns code -> translation, or null (see err). */
    public static HashMap<String, String> claude(String key, String model, String srcName,
                                                 String text, ArrayList<String[]> targets) {
        err = "";
        HttpURLConnection c = null;
        try {
            StringBuilder user = new StringBuilder();
            user.append("Source language: ").append(srcName).append("\nText to translate:\n<<<\n")
                .append(text).append("\n>>>\nTargets:\n");
            for (String[] t : targets) user.append("- ").append(t[0]).append(": ").append(t[1]).append("\n");

            JSONObject body = new JSONObject();
            body.put("model", model);
            body.put("max_tokens", 4096);
            body.put("system", SYSTEM);
            JSONArray msgs = new JSONArray();
            JSONObject m = new JSONObject();
            m.put("role", "user");
            m.put("content", user.toString());
            msgs.put(m);
            body.put("messages", msgs);

            c = (HttpURLConnection) new URL("https://api.anthropic.com/v1/messages").openConnection();
            c.setRequestMethod("POST");
            c.setDoOutput(true);
            c.setConnectTimeout(20000);
            c.setReadTimeout(90000);
            c.setRequestProperty("content-type", "application/json");
            c.setRequestProperty("x-api-key", key);
            c.setRequestProperty("anthropic-version", "2023-06-01");
            OutputStream os = c.getOutputStream();
            os.write(body.toString().getBytes("UTF-8"));
            os.close();

            int code = c.getResponseCode();
            String resp = read(code >= 400 ? c.getErrorStream() : c.getInputStream());
            if (code != 200) {
                try {
                    err = new JSONObject(resp).getJSONObject("error").optString("message", "HTTP " + code);
                } catch (Exception e) {
                    err = "HTTP " + code;
                }
                return null;
            }
            JSONObject root = new JSONObject(resp);
            JSONArray content = root.getJSONArray("content");
            StringBuilder txt = new StringBuilder();
            for (int i = 0; i < content.length(); i++) {
                JSONObject b = content.getJSONObject(i);
                if ("text".equals(b.optString("type"))) txt.append(b.optString("text", ""));
            }
            if ("max_tokens".equals(root.optString("stop_reason"))) {
                err = "TOO_LONG";
                return null;
            }
            JSONObject res = new JSONObject(Detect.jsonOnly(txt.toString()));
            JSONArray arr = res.getJSONArray("results");
            HashMap<String, String> out = new HashMap<String, String>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                out.put(o.getString("code"), o.optString("text", ""));
            }
            return out;
        } catch (Exception e) {
            err = "network";
            return null;
        } finally {
            if (c != null) c.disconnect();
        }
    }

    // ---------------- MyMemory (no key) ----------------
    /** from/to: "en", "ur", "ar". Returns translation, or null (see err). */
    public static String myMemory(String from, String to, String text) {
        err = "";
        StringBuilder out = new StringBuilder();
        for (String piece : Detect.chunk(text, 450)) {
            if (piece.trim().length() == 0) { out.append(piece); continue; }
            HttpURLConnection c = null;
            try {
                String u = "https://api.mymemory.translated.net/get?q=" + URLEncoder.encode(piece, "UTF-8")
                        + "&langpair=" + URLEncoder.encode(from + "|" + to, "UTF-8");
                c = (HttpURLConnection) new URL(u).openConnection();
                c.setConnectTimeout(15000);
                c.setReadTimeout(30000);
                c.setRequestProperty("User-Agent", "BlackFalconArabicTranslator/1.0");
                int code = c.getResponseCode();
                String resp = read(code >= 400 ? c.getErrorStream() : c.getInputStream());
                JSONObject root = new JSONObject(resp);
                String t = root.getJSONObject("responseData").optString("translatedText", "");
                if (t.toUpperCase().contains("MYMEMORY WARNING") || root.optInt("responseStatus", 200) != 200) {
                    err = "LIMIT";
                    return null;
                }
                String lead = piece.substring(0, piece.length() - piece.replaceAll("^\\s+", "").length());
                out.append(lead).append(Detect.unescape(t));
                if (piece.endsWith("\n")) out.append("\n");
                else if (piece.endsWith(" ")) out.append(" ");
            } catch (Exception e) {
                err = "network";
                return null;
            } finally {
                if (c != null) c.disconnect();
            }
        }
        return out.toString().trim();
    }
}
