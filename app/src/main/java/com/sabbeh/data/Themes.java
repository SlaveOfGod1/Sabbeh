package com.sabbeh.data;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Port of THEMES + applyCustomColor + toggleNightMode from DhikrContext.js
 *
 * Teal:   ['#4DB6AC', '#00695C'] end {1, 0.18}
 * Purple: ['#BA68C8', '#4A148C'] end {1, 0.18}
 * Blue:   ['#42A5F5', '#0D47A1'] end {1, 0.18}
 * Rose:   ['#EC407A', '#880E4F'] end {1, 0.18}
 * Night:  ['#0000006f', '#000000ff'] end {0, 0}, isDark, deviceBody #2d363dff, text #ECEFF1
 */
public class Themes {

    public static class Theme {
        public String name;
        public String[] colors; // [start, end]
        public float endX;
        public float endY;
        public boolean isDark;
        public String deviceBody; // may be null -> default #F5F5F5
        public String text;       // may be null

        public Theme(String name, String[] colors, float endX, float endY,
                     boolean isDark, String deviceBody, String text) {
            this.name = name;
            this.colors = colors;
            this.endX = endX;
            this.endY = endY;
            this.isDark = isDark;
            this.deviceBody = deviceBody;
            this.text = text;
        }

        public String primaryColor() {
            if (colors != null && colors.length > 1 && colors[1] != null) return colors[1];
            return "#00897B";
        }

        public String deviceBodyOrDefault() {
            return deviceBody != null ? deviceBody : "#F5F5F5";
        }

        public JSONObject toJson() {
            try {
                JSONObject o = new JSONObject();
                o.put("name", name);
                JSONArray arr = new JSONArray();
                arr.put(colors[0]);
                arr.put(colors[1]);
                o.put("colors", arr);
                JSONObject end = new JSONObject();
                end.put("x", endX);
                end.put("y", endY);
                o.put("end", end);
                if (isDark) o.put("isDark", true);
                if (deviceBody != null) o.put("deviceBody", deviceBody);
                if (text != null) o.put("text", text);
                return o;
            } catch (Exception e) {
                return new JSONObject();
            }
        }

        public static Theme fromJson(JSONObject o) {
            if (o == null) return TEAL;
            try {
                String name = o.optString("name", "Teal");
                JSONArray arr = o.optJSONArray("colors");
                String[] colors = new String[]{"#4DB6AC", "#00695C"};
                if (arr != null && arr.length() >= 2) {
                    colors[0] = arr.optString(0, colors[0]);
                    colors[1] = arr.optString(1, colors[1]);
                }
                JSONObject end = o.optJSONObject("end");
                float ex = 1f, ey = 0.18f;
                if (end != null) {
                    ex = (float) end.optDouble("x", ex);
                    ey = (float) end.optDouble("y", ey);
                }
                boolean isDark = o.optBoolean("isDark", "Night".equals(name));
                String deviceBody = o.has("deviceBody") ? o.optString("deviceBody", null) : null;
                String text = o.has("text") ? o.optString("text", null) : null;
                // Match built-in presets by name so future code changes stay consistent
                Theme preset = byName(name);
                if (preset != null && !"Custom".equals(name)) {
                    // keep stored colors if present (covers custom), otherwise preset
                    return new Theme(name, colors, ex, ey, preset.isDark,
                            deviceBody != null ? deviceBody : preset.deviceBody,
                            text != null ? text : preset.text);
                }
                return new Theme(name, colors, ex, ey, isDark, deviceBody, text);
            } catch (Exception e) {
                return TEAL;
            }
        }
    }

    public static final Theme TEAL =
            new Theme("Teal", new String[]{"#4DB6AC", "#00695C"}, 1f, 0.18f, false, null, null);
    public static final Theme PURPLE =
            new Theme("Purple", new String[]{"#BA68C8", "#4A148C"}, 1f, 0.18f, false, null, null);
    public static final Theme BLUE =
            new Theme("Blue", new String[]{"#42A5F5", "#0D47A1"}, 1f, 0.18f, false, null, null);
    public static final Theme ROSE =
            new Theme("Rose", new String[]{"#EC407A", "#880E4F"}, 1f, 0.18f, false, null, null);
    public static final Theme NIGHT =
            new Theme("Night", new String[]{"#0000006f", "#000000ff"}, 0f, 0f,
                    true, "#2d363dff", "#ECEFF1");

    public static Theme[] presets() {
        return new Theme[]{TEAL, PURPLE, BLUE, ROSE};
    }

    /** All themes including Night, in the same insertion order as the JS object. */
    public static Theme[] all() {
        return new Theme[]{TEAL, PURPLE, BLUE, ROSE, NIGHT};
    }

    public static Theme byName(String name) {
        if (name == null) return null;
        for (Theme t : all()) {
            if (t.name.equalsIgnoreCase(name)) return t;
        }
        return null;
    }

    // ---- color helpers (direct port of lighten/darken in applyCustomColor) ----

    public static String lighten(String color, double percent) {
        try {
            String hex = color.replace("#", "");
            if (hex.length() == 8) hex = hex.substring(0, 6);
            long num = Long.parseLong(hex, 16);
            int amt = (int) Math.round(2.55 * percent);
            int r = (int) ((num >> 16) + amt);
            int g = (int) (((num >> 8) & 0x00FF) + amt);
            int b = (int) ((num & 0x0000FF) + amt);
            r = clamp(r); g = clamp(g); b = clamp(b);
            return "#" + Integer.toHexString(0x1000000 + r * 0x10000 + g * 0x100 + b).substring(1).toUpperCase();
        } catch (Exception e) {
            return color;
        }
    }

    public static String darken(String color, double percent) {
        try {
            String hex = color.replace("#", "");
            if (hex.length() == 8) hex = hex.substring(0, 6);
            long num = Long.parseLong(hex, 16);
            int amt = (int) Math.round(2.55 * percent);
            int r = (int) ((num >> 16) - amt);
            int g = (int) (((num >> 8) & 0x00FF) - amt);
            int b = (int) ((num & 0x0000FF) - amt);
            r = clamp(r); g = clamp(g); b = clamp(b);
            return "#" + Integer.toHexString(0x1000000 + r * 0x10000 + g * 0x100 + b).substring(1).toUpperCase();
        } catch (Exception e) {
            return color;
        }
    }

    private static int clamp(int v) {
        if (v > 255) return 255;
        if (v < 0) return 0;
        return v;
    }

    /** Port of applyCustomColor(mainColor) -> Theme(name='Custom', colors=[light, dark]). */
    public static Theme customFromBase(String mainColor) {
        String darkShade = darken(mainColor, 20);
        String lightShade = lighten(mainColor, 20);
        return new Theme("Custom", new String[]{lightShade, darkShade},
                1f, 0.18f, false, "#F5F5F5", "#E0F2F1");
    }

    /** Parse "#RRGGBB" or "#RRGGBBAA" (CSS order, as in the original JS theme)
     *  into an Android color int. */
    public static int parse(String hex) {
        String h = hex.replace("#", "");
        if (h.length() == 6) {
            return (0xFF << 24)
                    | (Integer.parseInt(h.substring(0, 2), 16) << 16)
                    | (Integer.parseInt(h.substring(2, 4), 16) << 8)
                    | Integer.parseInt(h.substring(4, 6), 16);
        } else if (h.length() == 8) {
            // Original colors like '#0000006f' are CSS #RRGGBBAA (alpha LAST),
            // e.g. Night = translucent black -> opaque black. Convert to AARRGGBB.
            int r = Integer.parseInt(h.substring(0, 2), 16);
            int g = Integer.parseInt(h.substring(2, 4), 16);
            int b = Integer.parseInt(h.substring(4, 6), 16);
            int a = Integer.parseInt(h.substring(6, 8), 16);
            return (a << 24) | (r << 16) | (g << 8) | b;
        }
        return 0xFF00897B;
    }
}
