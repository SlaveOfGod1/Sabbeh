package com.sabbeh.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.os.Build;

import com.sabbeh.models.Dhikr;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Port of DhikrContext.js provider logic.
 * Persistence: SharedPreferences key '@sabbeh_dhikrs_v1' with the same JSON shape
 * as AsyncStorage in the original (dhikrs, progress, currentDhikrId, theme,
 * language, hapticEnabled, autoAdvanceEnabled).
 */
public class DhikrStore {

    public interface Listener { void onChange(); }

    private static final String PREFS = "sabbeh_prefs";
    private static final String KEY = "@sabbeh_dhikrs_v1";

    private static DhikrStore instance;

    public static synchronized DhikrStore get(Context ctx) {
        if (instance == null) instance = new DhikrStore(ctx.getApplicationContext());
        return instance;
    }

    private final Context app;
    private final List<Listener> listeners = new ArrayList<>();

    public List<Dhikr> dhikrs = new ArrayList<>();
    // progress: dhikrId -> [count, rounds]
    public Map<String, int[]> progress = new HashMap<>();
    public String currentDhikrId = "1";
    public Themes.Theme theme = Themes.TEAL;
    public Themes.Theme lastTheme = Themes.TEAL;
    public String language = systemLanguage();
    public boolean hapticEnabled = true;
    public boolean autoAdvanceEnabled = false;
    /** Last custom base color picked in the theme sheet (marker position memory). */
    public String customColor = "#42A5F5";

    private static final List<String> DEFAULT_ORDER = Arrays.asList("4", "1", "2", "3", "5", "6");

    private DhikrStore(Context app) {
        this.app = app;
        resetToDefaults();
        load();
    }

    /** Phone language when the app supports it, otherwise English. */
    private static String systemLanguage() {
        try {
            String code = Locale.getDefault().getLanguage();
            if (code == null) return "en";
            code = code.toLowerCase(Locale.US);
            if (code.equals("in")) code = "ms"; // legacy Indonesian code -> Malay
            for (String c : Translations.CODES) {
                if (c.equals(code)) return code;
            }
        } catch (Exception ignored) {}
        return "en";
    }

    public void addListener(Listener l) { listeners.add(l); }
    public void removeListener(Listener l) { listeners.remove(l); }
    private void notifyChanged() {
        for (Listener l : new ArrayList<>(listeners)) l.onChange();
    }

    private void resetToDefaults() {
        dhikrs = new ArrayList<>(Arrays.asList(
                new Dhikr("4", "Istighfar", "Astaghfirullah", 100, "d_istighfar", "s_istighfar"),
                new Dhikr("1", "Tasbih", "SubhanAllah", 33, "d_tasbih", "s_tasbih"),
                new Dhikr("2", "Tahmid", "Alhamdulillah", 33, "d_tahmid", "s_tahmid"),
                new Dhikr("3", "Takbir", "Allahu Akbar", 34, "d_takbir", "s_takbir"),
                new Dhikr("5", "Salawat", "Salawat on Prophet", 100, "d_salawat", "s_salawat"),
                new Dhikr("6", "La ilaha illallah", "Tahlil", 100, "d_tahlil", "s_tahlil")
        ));
        currentDhikrId = "1";
    }

    public String t(String key) {
        return Translations.t(language, key);
    }

    public Dhikr currentDhikr() {
        for (Dhikr d : dhikrs) if (d.id.equals(currentDhikrId)) return d;
        return dhikrs.isEmpty() ? null : dhikrs.get(0);
    }

    public int count() {
        Dhikr c = currentDhikr();
        if (c == null) return 0;
        int[] p = progress.get(c.id);
        return p == null ? 0 : p[0];
    }

    public int rounds() {
        Dhikr c = currentDhikr();
        if (c == null) return 0;
        int[] p = progress.get(c.id);
        return p == null ? 0 : p[1];
    }

    public String dhikrTitle(Dhikr d) {
        if (d.nameKey != null) return t(d.nameKey);
        switch (d.id) {
            case "1": return t("d_tasbih");
            case "2": return t("d_tahmid");
            case "3": return t("d_takbir");
            case "4": return t("d_istighfar");
            case "5": return t("d_salawat");
            case "6": return t("d_tahlil");
            default: return d.title;
        }
    }

    public String dhikrSubtitle(Dhikr d) {
        if (d.subtitleKey != null) return t(d.subtitleKey);
        switch (d.id) {
            case "1": return t("s_tasbih");
            case "2": return t("s_tahmid");
            case "3": return t("s_takbir");
            case "4": return t("s_istighfar");
            case "5": return t("s_salawat");
            case "6": return t("s_tahlil");
            default: return d.subtitle;
        }
    }

    public String themeDisplayName(Themes.Theme th) {
        String key = "t_" + th.name.toLowerCase();
        String v = t(key);
        return v.equals(key) ? th.name : v;
    }

    // ---------------- actions (same semantics as JS) ----------------

    public void selectDhikr(Dhikr d) {
        currentDhikrId = d.id;
        save();
        notifyChanged();
    }

    public void updateProgress(int delta) {
        Dhikr c = currentDhikr();
        if (c == null) return;
        int[] old = progress.get(c.id);
        int oc = old == null ? 0 : old[0];
        int or = old == null ? 0 : old[1];
        int target = c.target <= 0 ? 33 : c.target;
        int nc = oc + delta;
        int nr = or;
        if (nc >= target) {
            nc = 0;
            nr += 1;
            if (hapticEnabled) vibrateRound();
            if (autoAdvanceEnabled && dhikrs.size() > 1) {
                int idx = -1;
                for (int i = 0; i < dhikrs.size(); i++) {
                    if (dhikrs.get(i).id.equals(c.id)) { idx = i; break; }
                }
                if (idx >= 0) {
                    Dhikr next = dhikrs.get((idx + 1) % dhikrs.size());
                    currentDhikrId = next.id;
                }
            }
        }
        progress.put(c.id, new int[]{nc, nr});
        save();
        notifyChanged();
    }

    public void resetCount() {
        Dhikr c = currentDhikr();
        if (c == null) return;
        int[] old = progress.get(c.id);
        int r = old == null ? 0 : old[1];
        progress.put(c.id, new int[]{0, r});
        save();
        notifyChanged();
    }

    public void addDhikr(String title, String subtitle, int target) {
        Dhikr d = new Dhikr(String.valueOf(System.currentTimeMillis()), title, subtitle,
                target <= 0 ? 33 : target, null, null);
        dhikrs.add(d);
        progress.put(d.id, new int[]{0, 0});
        currentDhikrId = d.id;
        save();
        notifyChanged();
    }

    public void deleteDhikr(String id) {
        List<Dhikr> updated = new ArrayList<>();
        for (Dhikr d : dhikrs) if (!d.id.equals(id)) updated.add(d);
        dhikrs = updated;
        progress.remove(id);
        if (currentDhikrId.equals(id) && !updated.isEmpty()) currentDhikrId = updated.get(0).id;
        save();
        notifyChanged();
    }

    public void updateDhikr(String id, String title, String subtitle, int target) {
        for (Dhikr d : dhikrs) {
            if (d.id.equals(id)) {
                d.title = title;
                d.subtitle = subtitle;
                d.target = target <= 0 ? 33 : target;
            }
        }
        save();
        notifyChanged();
    }

    public void setTheme(Themes.Theme th) {
        theme = th;
        save();
        notifyChanged();
    }

    public void applyCustomColor(String mainColor) {
        customColor = mainColor;
        theme = Themes.customFromBase(mainColor);
        save();
        notifyChanged();
    }

    public void toggleNightMode() {
        if ("Night".equals(theme.name)) {
            theme = lastTheme != null ? lastTheme : Themes.TEAL;
        } else {
            lastTheme = theme;
            theme = Themes.NIGHT;
        }
        save();
        notifyChanged();
    }

    public void setLanguage(String code) {
        language = code;
        save();
        notifyChanged();
    }

    public void setHapticEnabled(boolean v) {
        hapticEnabled = v;
        save();
        notifyChanged();
    }

    public void setAutoAdvanceEnabled(boolean v) {
        autoAdvanceEnabled = v;
        save();
        notifyChanged();
    }

    private void vibrateRound() {
        try {
            Vibrator vib = (Vibrator) app.getSystemService(Context.VIBRATOR_SERVICE);
            if (vib == null || !vib.hasVibrator()) return;
            // Port of the JS loop: 9 ticks 60ms apart (~half a second of vibration)
            if (Build.VERSION.SDK_INT >= 26) {
                long[] pattern = {0, 80, 60, 80, 60, 80, 60, 80, 60, 80};
                vib.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                vib.vibrate(450);
            }
        } catch (Exception ignored) {}
    }

    // ---------------- persistence ----------------

    private JSONArray dhikrsJson() throws JSONException {
        JSONArray arr = new JSONArray();
        for (Dhikr d : dhikrs) arr.put(d.toJson());
        return arr;
    }

    private JSONObject progressJson() throws JSONException {
        JSONObject pr = new JSONObject();
        for (Map.Entry<String, int[]> e : progress.entrySet()) {
            JSONObject v = new JSONObject();
            v.put("count", e.getValue()[0]);
            v.put("rounds", e.getValue()[1]);
            pr.put(e.getKey(), v);
        }
        return pr;
    }

    public void save() {
        try {
            JSONObject data = new JSONObject();
            data.put("dhikrs", dhikrsJson());
            data.put("progress", progressJson());
            data.put("currentDhikrId", currentDhikrId);
            data.put("theme", theme.toJson());
            data.put("language", language);
            data.put("customColor", customColor);
            data.put("hapticEnabled", hapticEnabled);
            data.put("autoAdvanceEnabled", autoAdvanceEnabled);
            SharedPreferences sp = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            sp.edit().putString(KEY, data.toString()).apply();
        } catch (Exception ignored) {}
    }

    private void load() {
        try {
            SharedPreferences sp = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String stored = sp.getString(KEY, null);
            if (stored == null) return;
            JSONObject parsed = new JSONObject(stored);
            if (parsed.has("dhikrs")) {
                JSONArray arr = parsed.getJSONArray("dhikrs");
                List<Dhikr> defaults = new ArrayList<>();
                List<Dhikr> customs = new ArrayList<>();
                Set<String> orderSet = new HashSet<>(DEFAULT_ORDER);
                for (int i = 0; i < arr.length(); i++) {
                    Dhikr d = Dhikr.fromJson(arr.getJSONObject(i));
                    if (orderSet.contains(d.id)) defaults.add(d);
                    else customs.add(d);
                }
                defaults.sort((a, b) ->
                        Integer.compare(DEFAULT_ORDER.indexOf(a.id), DEFAULT_ORDER.indexOf(b.id)));
                defaults.addAll(customs);
                if (!defaults.isEmpty()) dhikrs = defaults;
            }
            if (parsed.has("progress")) {
                JSONObject pr = parsed.getJSONObject("progress");
                for (Dhikr d : dhikrs) {
                    if (pr.has(d.id)) {
                        JSONObject v = pr.getJSONObject(d.id);
                        progress.put(d.id, new int[]{v.optInt("count", 0), v.optInt("rounds", 0)});
                    }
                }
            }
            if (parsed.has("currentDhikrId")) currentDhikrId = parsed.optString("currentDhikrId", currentDhikrId);
            if (parsed.has("theme")) theme = Themes.Theme.fromJson(parsed.optJSONObject("theme"));
            if (parsed.has("language")) language = parsed.optString("language", systemLanguage());
            if (parsed.has("customColor")) customColor = parsed.optString("customColor", "#42A5F5");
            if (parsed.has("hapticEnabled")) hapticEnabled = parsed.optBoolean("hapticEnabled", true);
            if (parsed.has("autoAdvanceEnabled")) autoAdvanceEnabled = parsed.optBoolean("autoAdvanceEnabled", false);
        } catch (Exception ignored) {}
    }

    // ---------------- export / import payloads (same shape as JS) ----------------

    /** JSON string used for Export Profile, identical keys to the Expo version. */
    public String buildExportJson() {
        try {
            JSONObject data = new JSONObject();
            data.put("version", "1.0.2");
            try {
                java.text.SimpleDateFormat sdf =
                        new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US);
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                data.put("timestamp", sdf.format(new java.util.Date()));
            } catch (Exception ignored) {}
            data.put("dhikrs", dhikrsJson());
            data.put("progress", progressJson());
            data.put("currentDhikrId", currentDhikrId);
            Themes.Theme base = "Night".equals(theme.name) && lastTheme != null ? lastTheme : theme;
            data.put("theme", base.toJson());
            data.put("isNightMode", "Night".equals(theme.name));
            return data.toString(2);
        } catch (Exception e) {
            return "{}";
        }
    }

    /**
     * Apply an imported profile JSON string. Returns null on success,
     * otherwise a user-facing error string (already translated).
     */
    public String applyImportJson(String text) {
        try {
            JSONObject data = new JSONObject(text);
            if (!data.has("dhikrs") || !data.has("progress")) return t("invalidProfile");
            JSONArray arr = data.getJSONArray("dhikrs");
            List<Dhikr> list = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) list.add(Dhikr.fromJson(arr.getJSONObject(i)));
            JSONObject pr = data.getJSONObject("progress");
            Map<String, int[]> np = new HashMap<>();
            for (int i = 0; i < arr.length(); i++) {
                String id = list.get(i).id;
                if (pr.has(id)) {
                    JSONObject v = pr.getJSONObject(id);
                    np.put(id, new int[]{v.optInt("count", 0), v.optInt("rounds", 0)});
                }
            }
            dhikrs = list;
            progress = np;
            if (data.has("currentDhikrId")) currentDhikrId = data.optString("currentDhikrId", currentDhikrId);
            if (data.has("theme")) theme = Themes.Theme.fromJson(data.optJSONObject("theme"));
            save();
            notifyChanged();
            return null;
        } catch (Exception e) {
            return t("parseFailed");
        }
    }

    public void resetAll() {
        SharedPreferences sp = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().remove(KEY).apply();
        resetToDefaults();
        progress = new HashMap<>();
        currentDhikrId = "1";
        theme = Themes.TEAL;
        lastTheme = Themes.TEAL;
        hapticEnabled = true;
        autoAdvanceEnabled = false;
        save();
        notifyChanged();
    }
}
