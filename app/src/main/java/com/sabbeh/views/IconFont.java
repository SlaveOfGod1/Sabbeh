package com.sabbeh.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * The exact Ionicons font from the original Expo app, copied from
 * node_modules/expo/node_modules/@expo/vector-icons (.../Fonts/Ionicons.ttf).
 * Codepoints come from its glyphmaps/Ionicons.json — same glyphs, same look,
 * zero downloads. Works fully offline.
 */
public final class IconFont {

    public static final int MENU = 62545;
    public static final int COLOR_PALETTE = 62078; // color-palette-outline
    public static final int MOON = 62560;
    public static final int MOON_OUTLINE = 62561;
    public static final int SETTINGS = 62828;      // settings-outline
    public static final int ARROW_BACK = 61735;
    public static final int LANGUAGE = 62378;      // language-outline
    public static final int PHONE_PORTRAIT = 62645;// phone-portrait-outline
    public static final int SWAP_HORIZONTAL = 62897; // swap-horizontal-outline
    public static final int INFO_CIRCLE = 62361;   // information-circle-outline
    public static final int DOWNLOAD = 62138;      // download-outline
    public static final int UPLOAD = 62048;        // cloud-upload-outline
    public static final int LOGO_GITHUB = 62451;
    public static final int BUG = 61883;           // bug-outline
    public static final int TRASH = 62966;         // trash-outline
    public static final int APPS = 61730;          // apps-outline

    private static Typeface tf;

    private IconFont() {}

    public static Typeface get(Context ctx) {
        if (tf == null) {
            tf = Typeface.createFromAsset(ctx.getApplicationContext().getAssets(),
                    "fonts/Ionicons.ttf");
        }
        return tf;
    }

    public static String ch(int code) {
        return new String(Character.toChars(code));
    }

    /** Apply an Ionicons glyph to a TextView (same usage as <Ionicons> in RN). */
    public static void set(TextView tv, int code, float sizeSp, int color) {
        tv.setTypeface(get(tv.getContext()));
        tv.setText(ch(code));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        tv.setTextColor(color);
    }
}
