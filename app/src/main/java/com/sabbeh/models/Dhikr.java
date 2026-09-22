package com.sabbeh.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Port of the dhikr object from context/DhikrContext.js
 * { id, title, subtitle, target, nameKey, subtitleKey }
 */
public class Dhikr {
    public String id;
    public String title;
    public String subtitle;
    public int target;
    public String nameKey;
    public String subtitleKey;

    public Dhikr(String id, String title, String subtitle, int target,
                 String nameKey, String subtitleKey) {
        this.id = id;
        this.title = title != null ? title : "";
        this.subtitle = subtitle != null ? subtitle : "";
        this.target = target <= 0 ? 33 : target;
        this.nameKey = nameKey;
        this.subtitleKey = subtitleKey;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("title", title);
        o.put("subtitle", subtitle);
        o.put("target", target);
        if (nameKey != null) o.put("nameKey", nameKey);
        if (subtitleKey != null) o.put("subtitleKey", subtitleKey);
        return o;
    }

    public static Dhikr fromJson(JSONObject o) {
        String id = o.optString("id", String.valueOf(System.currentTimeMillis()));
        String title = o.optString("title", "");
        String subtitle = o.optString("subtitle", "");
        int target = o.optInt("target", 33);
        String nameKey = o.has("nameKey") ? o.optString("nameKey", null) : null;
        String subtitleKey = o.has("subtitleKey") ? o.optString("subtitleKey", null) : null;
        if (nameKey != null && nameKey.isEmpty()) nameKey = null;
        if (subtitleKey != null && subtitleKey.isEmpty()) subtitleKey = null;
        return new Dhikr(id, title, subtitle, target, nameKey, subtitleKey);
    }
}
