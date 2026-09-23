package com.sabbeh;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.sabbeh.data.DhikrStore;
import com.sabbeh.data.Themes;
import com.sabbeh.data.Translations;
import com.sabbeh.models.Dhikr;
import com.sabbeh.views.DashedBox;
import com.sabbeh.views.FlowLayout;
import com.sabbeh.views.GradientView;
import com.sabbeh.views.HueSliderView;
import com.sabbeh.views.IconFont;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Scanner;

/**
 * Single-activity port of the whole Expo app:
 *  - app/index.js          -> main counter screen
 *  - components/CounterDisplay.js / CounterControls.js -> device body
 *  - components/CustomDrawerContent.js                 -> left drawer panel
 *  - components/SettingsModal.js                       -> settings overlay
 *  - theme modal in index.js                           -> bottom sheet
 *  - SimpleColorPicker.js                              -> HueSliderView
 *
 * Same layout direction policy as the original
 * (I18nManager.allowRTL(false)): forced LTR.
 * Icons: original used Ionicons; here the same positions/glyphs are
 * reproduced with text glyphs so no asset install is needed.
 */
public class MainActivity extends Activity {

    private static final int REQ_EXPORT = 1001;
    private static final int REQ_IMPORT = 1002;

    private DhikrStore store;

    private GradientView bg;
    private TextView titleTv, subtitleTv, roundsTv, countTv;
    private TextView menuBtn, paletteBtn, nightBtn, settingsBtn;
    private LinearLayout deviceBody;
    private LinearLayout counterHousing;

    // drawer
    private View drawerScrim;
    private LinearLayout drawerPanel;
    private LinearLayout dhikrList;
    private LinearLayout drawerHeader;
    private TextView drawerTitleTv, drawerSubTv, drawerAddBtn, drawerAppNameTv;
    private LinearLayout drawerFooter;
    private int drawerWidth;

    // theme sheet
    private View themeScrim;
    private LinearLayout themeSheet;
    private LinearLayout themeGrid;
    private TextView themeTitleTv, customColorTitleTv, applyBtnTv, previewHexTv, themePaletteIcon;
    private View previewCircle;
    private String tempColor = "#42A5F5";
    private HueSliderView hueSlider;

    // settings overlay
    private LinearLayout settingsOverlay;
    private LinearLayout settingsRoot;

    private final DhikrStore.Listener listener = this::renderAll;

    // ------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Draw behind the status/nav bars (transparent like the original's
        // edge-to-edge) WITHOUT the fullscreen flag, so keyboard resize
        // keeps working.
        applyEdgeToEdge();

        store = DhikrStore.get(this);
        store.addListener(listener);

        FrameLayout root = new FrameLayout(this);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        // Opaque black base so translucent Night gradient colors blend over
        // black, exactly like the original dark theme.
        root.setBackgroundColor(Color.BLACK);

        buildMain(root);
        buildDrawer(root);
        buildThemeSheet(root);
        buildSettings(root);

        setContentView(root);
        renderAll();
    }

    private void applyEdgeToEdge() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) applyEdgeToEdge();
    }

    @Override
    public void onBackPressed() {
        // Overlays are views, not pages: walk back through them before exiting.
        // (AlertDialog popups already consume Back by themselves.)
        if (settingsOverlay != null && settingsOverlay.getVisibility() == View.VISIBLE) {
            closeSettings();
        } else if (themeSheet != null && themeSheet.getVisibility() == View.VISIBLE) {
            closeThemeSheet();
        } else if (drawerPanel != null && drawerPanel.getVisibility() == View.VISIBLE) {
            closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        store.removeListener(listener);
        super.onDestroy();
    }

    // ================= MAIN SCREEN (app/index.js) =================

    private void buildMain(FrameLayout root) {
        bg = new GradientView(this);
        root.addView(bg, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        bg.addView(col, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        int statusPad = getStatusBarHeight() + dp(5);
        col.setPadding(0, statusPad, 0, 0);

        // header: menu left, 3 buttons right (palette, moon, settings)
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(60));
        hp.setMargins(dp(20), 0, dp(20), 0);
        header.setLayoutParams(hp);

        menuBtn = iconButton(IconFont.MENU);
        menuBtn.setOnClickListener(v -> openDrawer());
        header.addView(menuBtn);

        Space spacer = new Space(this);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, 1, 1f);
        spacer.setLayoutParams(sp);
        header.addView(spacer);

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.HORIZONTAL);
        paletteBtn = iconButton(IconFont.COLOR_PALETTE);
        paletteBtn.setOnClickListener(v -> openThemeSheet());
        nightBtn = iconButton(IconFont.MOON_OUTLINE);
        nightBtn.setOnClickListener(v -> store.toggleNightMode());
        settingsBtn = iconButton(IconFont.SETTINGS);
        settingsBtn.setOnClickListener(v -> openSettings());
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(dp(44), dp(44));
        rp.setMargins(dp(6), 0, dp(6), 0);
        right.addView(paletteBtn, rp);
        right.addView(nightBtn, rp);
        right.addView(settingsBtn, rp);
        header.addView(right);
        col.addView(header);

        // center content
        LinearLayout center = new LinearLayout(this);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        center.setLayoutParams(cp);
        center.setGravity(Gravity.CENTER);
        // Bottom breathing room like the original (paddingBottom shifts the
        // centered content upward).
        center.setPadding(0, 0, 0, dp(50));
        col.addView(center);

        titleTv = new TextView(this);
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setTextColor(Color.WHITE);
        titleTv.setGravity(Gravity.CENTER);
        center.addView(titleTv);

        LinearLayout subRow = new LinearLayout(this);
        subRow.setOrientation(LinearLayout.HORIZONTAL);
        subRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams srp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        srp.setMargins(0, dp(5), 0, dp(30));
        subRow.setLayoutParams(srp);
        subtitleTv = new TextView(this);
        subtitleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        subtitleTv.setTextColor(Color.parseColor("#E0F2F1"));
        subRow.addView(subtitleTv);

        roundsTv = new TextView(this);
        roundsTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        roundsTv.setTypeface(Typeface.DEFAULT_BOLD);
        roundsTv.setTextColor(Color.parseColor("#E0F2F1"));
        roundsTv.setBackground(rounded(Color.argb((int) (0.2 * 255), 0, 0, 0), dp(15)));
        roundsTv.setPadding(dp(10), dp(5), dp(10), dp(5));
        LinearLayout.LayoutParams badgeP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        badgeP.setMargins(dp(10), 0, 0, 0);
        subRow.addView(roundsTv, badgeP);
        center.addView(subRow);

        // device body: 320x500, radius 90 (CounterControls + CounterDisplay live inside)
        deviceBody = new LinearLayout(this);
        deviceBody.setOrientation(LinearLayout.VERTICAL);
        deviceBody.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams dbp = new LinearLayout.LayoutParams(dp(320), dp(500));
        deviceBody.setLayoutParams(dbp);
        deviceBody.setPadding(0, dp(50), 0, 0);
        deviceBody.setElevation(dp(20));
        // Let the reset button float above its area (like the original's
        // absolute positioning) instead of being clipped.
        deviceBody.setClipChildren(false);
        center.addView(deviceBody);

        buildCounterDisplay();
        buildCounterControls();
    }

    /** Port of components/CounterDisplay.js */
    private void buildCounterDisplay() {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wp.setMargins(0, 0, 0, dp(30));
        wrap.setLayoutParams(wp);
        wrap.setElevation(dp(8));
        deviceBody.addView(wrap);

        counterHousing = new LinearLayout(this);
        counterHousing.setOrientation(LinearLayout.VERTICAL);
        counterHousing.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams chp = new LinearLayout.LayoutParams(dp(260), dp(220));
        counterHousing.setLayoutParams(chp);
        counterHousing.setPadding(0, dp(25), 0, dp(25));
        wrap.addView(counterHousing);

        TextView top = new TextView(this);
        top.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        top.setTypeface(Typeface.DEFAULT_BOLD);
        top.setTextColor(Color.parseColor("#B0BEC5"));
        top.setLetterSpacing(0.12f);
        top.setTag("counter_top");
        top.setAllCaps(true);
        top.setGravity(Gravity.CENTER);
        top.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        counterHousing.addView(top);

        LinearLayout lcd = new LinearLayout(this);
        lcd.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lcdp = new LinearLayout.LayoutParams(dp(221), dp(121));
        lcdp.setMargins(0, dp(8), 0, dp(8));
        lcd.setLayoutParams(lcdp);
        GradientDrawable lcdBg = rounded(Color.parseColor("#CFD8DC"), dp(15));
        lcdBg.setStroke(dp(4), Color.parseColor("#263238"));
        lcd.setBackground(lcdBg);
        counterHousing.addView(lcd);

        countTv = new TextView(this);
        countTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 64);
        countTv.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        countTv.setTextColor(Color.parseColor("#263238"));
        countTv.setLetterSpacing(0.1f);
        lcd.addView(countTv);

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lbp = new LinearLayout.LayoutParams(dp(208), ViewGroup.LayoutParams.WRAP_CONTENT);
        labels.setLayoutParams(lbp);
        TextView left = new TextView(this);
        left.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        left.setTypeface(Typeface.DEFAULT_BOLD);
        left.setTextColor(Color.parseColor("#90A4AE"));
        left.setTag("counter_left");
        TextView rightL = new TextView(this);
        rightL.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        rightL.setTypeface(Typeface.DEFAULT_BOLD);
        rightL.setTextColor(Color.parseColor("#90A4AE"));
        rightL.setTag("counter_right");
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        labels.addView(left, lp1);
        rightL.setGravity(Gravity.END);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        labels.addView(rightL, lp2);
        counterHousing.addView(labels);
    }

    /** Port of components/CounterControls.js.
     *  Geometry fits the 500dp device body exactly
     *  (50 pad + 220 housing + 30 gap - 30 overlap + 230 controls = 500).
     *  The controls area starts at the housing bottom so the small reset
     *  button lives INSIDE its parent's bounds (fully clickable) while
     *  visually floating high like the original's absolute positioning. */
    private void buildCounterControls() {
        FrameLayout area = new FrameLayout(this);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(230));
        ap.setMargins(0, dp(-30), 0, 0);
        area.setLayoutParams(ap);
        area.setClipChildren(false);
        deviceBody.addView(area);

        // big count button 160dp, slightly raised above the area bottom
        FrameLayout.LayoutParams bigP = new FrameLayout.LayoutParams(dp(160), dp(160));
        bigP.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        bigP.bottomMargin = dp(30);
        FrameLayout bigOuter = new FrameLayout(this);
        bigOuter.setBackground(roundedCircle(Color.parseColor("#1C262B")));
        bigOuter.setElevation(dp(10));
        bigOuter.setOnClickListener(v -> store.updateProgress(1));
        area.addView(bigOuter, bigP);

        View inner = new View(this);
        FrameLayout.LayoutParams inP = new FrameLayout.LayoutParams(dp(140), dp(140));
        inP.gravity = Gravity.CENTER;
        GradientDrawable innerBg = roundedCircle(Color.parseColor("#263238"));
        innerBg.setStroke(dp(4), Color.parseColor("#37474F"));
        inner.setBackground(innerBg);
        bigOuter.addView(inner, inP);
        // inner must not steal clicks
        inner.setClickable(false);

        // small reset button 50dp: top-right of the controls area, floating
        // next to the housing bottom like the original (fully in-bounds,
        // so every tap registers). Reset applies immediately, no warning.
        FrameLayout.LayoutParams rsP = new FrameLayout.LayoutParams(dp(50), dp(50));
        rsP.gravity = Gravity.TOP | Gravity.END;
        rsP.topMargin = dp(10);
        rsP.rightMargin = dp(50);
        FrameLayout rsOuter = new FrameLayout(this);
        rsOuter.setBackground(roundedCircle(Color.parseColor("#1C262B")));
        rsOuter.setElevation(dp(8));
        rsOuter.setOnClickListener(v -> store.resetCount());
        area.addView(rsOuter, rsP);

        View rsInner = new View(this);
        FrameLayout.LayoutParams rsIn = new FrameLayout.LayoutParams(dp(36), dp(36));
        rsIn.gravity = Gravity.CENTER;
        GradientDrawable rsBg = roundedCircle(Color.parseColor("#263238"));
        rsBg.setStroke(dp(2), Color.parseColor("#37474F"));
        rsInner.setBackground(rsBg);
        rsInner.setClickable(false);
        rsOuter.addView(rsInner, rsIn);
    }

    // ================= DRAWER (CustomDrawerContent.js) =================

    private void buildDrawer(FrameLayout root) {
        drawerScrim = new View(this);
        drawerScrim.setBackgroundColor(Color.argb((int) (0.5 * 255), 0, 0, 0));
        drawerScrim.setVisibility(View.GONE);
        drawerScrim.setOnClickListener(v -> closeDrawer());
        root.addView(drawerScrim, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        drawerPanel = new LinearLayout(this);
        drawerPanel.setOrientation(LinearLayout.VERTICAL);
        // Consume all touches so taps never leak to the screen underneath.
        drawerPanel.setClickable(true);
        int dw = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
        drawerWidth = dw;
        FrameLayout.LayoutParams dp2 = new FrameLayout.LayoutParams(dw, ViewGroup.LayoutParams.MATCH_PARENT);
        dp2.gravity = Gravity.START;
        drawerPanel.setVisibility(View.GONE);
        root.addView(drawerPanel, dp2);

        // header
        LinearLayout dh = new LinearLayout(this);
        dh.setOrientation(LinearLayout.VERTICAL);
        dh.setPadding(dp(20), getStatusBarHeight() + dp(20), dp(20), dp(20));
        drawerHeader = dh;
        drawerPanel.addView(dh);

        TextView ht = new TextView(this);
        ht.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        ht.setTypeface(Typeface.DEFAULT_BOLD);
        ht.setTextColor(Color.WHITE);
        // Absolute LEFT so RTL text (e.g. Arabic) stays on the left like the original.
        ht.setGravity(Gravity.LEFT);
        drawerTitleTv = ht;
        dh.addView(ht);
        TextView hs = new TextView(this);
        hs.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        hs.setTextColor(Color.parseColor("#E0F2F1"));
        hs.setGravity(Gravity.LEFT);
        drawerSubTv = hs;
        dh.addView(hs);

        // list
        ScrollView sv = new ScrollView(this);
        LinearLayout.LayoutParams svp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        sv.setLayoutParams(svp);
        dhikrList = new LinearLayout(this);
        dhikrList.setOrientation(LinearLayout.VERTICAL);
        dhikrList.setPadding(dp(15), dp(15), dp(15), dp(15));
        sv.addView(dhikrList);
        drawerPanel.addView(sv);

        // footer
        // Footer rides above the navigation bar, a touch higher than before.
        LinearLayout foot = new LinearLayout(this);
        foot.setOrientation(LinearLayout.VERTICAL);
        foot.setGravity(Gravity.CENTER_HORIZONTAL);
        foot.setPadding(dp(15), dp(15), dp(15), getNavBarHeight() + dp(22));
        drawerFooter = foot;
        drawerPanel.addView(foot);

        TextView add = new TextView(this);
        add.setGravity(Gravity.CENTER);
        add.setPadding(dp(10), dp(15), dp(10), dp(15));
        add.setTypeface(Typeface.DEFAULT_BOLD);
        drawerAddBtn = add;
        add.setBackground(addButtonBg());
        add.setOnClickListener(v -> openAddModal(null));
        foot.addView(add, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView fn = new TextView(this);
        fn.setGravity(Gravity.CENTER);
        fn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        drawerAppNameTv = fn;
        foot.addView(fn);
        TextView fv = new TextView(this);
        fv.setText("v1.0.2");
        fv.setGravity(Gravity.CENTER);
        fv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        fv.setTextColor(Color.parseColor("#90A4AE"));
        foot.addView(fv);
    }

    private void openDrawer() {
        if (drawerPanel.getVisibility() == View.VISIBLE && drawerPanel.getTranslationX() == 0f) return;
        drawerPanel.animate().cancel();
        drawerScrim.animate().cancel();
        drawerScrim.setVisibility(View.VISIBLE);
        drawerScrim.setAlpha(0f);
        drawerPanel.setVisibility(View.VISIBLE);
        drawerPanel.setTranslationX(-drawerWidth);
        drawerPanel.animate().translationX(0f).setDuration(160).start();
        drawerScrim.animate().alpha(1f).setDuration(160).start();
    }

    private void closeDrawer() {
        if (drawerPanel.getVisibility() != View.VISIBLE) return;
        drawerPanel.animate().cancel();
        drawerScrim.animate().cancel();
        drawerPanel.animate().translationX(-drawerWidth).setDuration(140).start();
        drawerScrim.animate().alpha(0f).setDuration(140).withEndAction(() -> {
            drawerPanel.setVisibility(View.GONE);
            drawerScrim.setVisibility(View.GONE);
        }).start();
    }

    // ================= THEME SHEET (modal in index.js) =================

    private void buildThemeSheet(FrameLayout root) {
        themeScrim = new View(this);
        themeScrim.setBackgroundColor(Color.argb((int) (0.3 * 255), 0, 0, 0));
        themeScrim.setVisibility(View.GONE);
        themeScrim.setOnClickListener(v -> closeThemeSheet());
        root.addView(themeScrim, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        themeSheet = new LinearLayout(this);
        themeSheet.setOrientation(LinearLayout.VERTICAL);
        // Consume all touches so taps never leak to the screen underneath.
        themeSheet.setClickable(true);
        // Bottom padding clears the navigation bar so Save never sits
        // under the system buttons.
        themeSheet.setPadding(dp(25), dp(8), dp(25), getNavBarHeight() + dp(20));
        themeSheet.setVisibility(View.GONE);
        FrameLayout.LayoutParams tp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.gravity = Gravity.BOTTOM;
        root.addView(themeSheet, tp);

        View handle = new View(this);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(dp(40), dp(4));
        hlp.gravity = Gravity.CENTER_HORIZONTAL;
        hlp.setMargins(0, 0, 0, dp(12));
        handle.setLayoutParams(hlp);
        handle.setBackground(rounded(Color.parseColor("#CFD8DC"), dp(2)));
        themeSheet.addView(handle);

        LinearLayout mh = new LinearLayout(this);
        mh.setOrientation(LinearLayout.HORIZONTAL);
        mh.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams mhp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mhp.setMargins(0, 0, 0, dp(20));
        mh.setLayoutParams(mhp);
        TextView ic = new TextView(this);
        ic.setTypeface(IconFont.get(this));
        ic.setText(IconFont.ch(IconFont.COLOR_PALETTE));
        ic.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        themePaletteIcon = ic;
        mh.addView(ic);
        themeTitleTv = new TextView(this);
        themeTitleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        themeTitleTv.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlp.setMargins(dp(10), 0, 0, 0);
        mh.addView(themeTitleTv, tlp);
        themeSheet.addView(mh);

        themeGrid = new LinearLayout(this);
        themeGrid.setOrientation(LinearLayout.VERTICAL);
        themeSheet.addView(themeGrid);

        customColorTitleTv = new TextView(this);
        customColorTitleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        LinearLayout.LayoutParams ccp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ccp.setMargins(0, dp(10), 0, dp(15));
        customColorTitleTv.setLayoutParams(ccp);
        themeSheet.addView(customColorTitleTv);

        previewCircle = new View(this);
        LinearLayout.LayoutParams pcp = new LinearLayout.LayoutParams(dp(80), dp(80));
        pcp.gravity = Gravity.CENTER_HORIZONTAL;
        pcp.setMargins(0, 0, 0, dp(10));
        previewCircle.setLayoutParams(pcp);
        themeSheet.addView(previewCircle);

        previewHexTv = new TextView(this);
        previewHexTv.setGravity(Gravity.CENTER);
        previewHexTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        previewHexTv.setTypeface(Typeface.DEFAULT_BOLD);
        previewHexTv.setTextColor(Color.WHITE);
        themeSheet.addView(previewHexTv);

        hueSlider = new HueSliderView(this);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(dp(300), dp(50));
        slp.gravity = Gravity.CENTER_HORIZONTAL;
        slp.setMargins(0, dp(5), 0, 0);
        GradientDrawable sliderFrame = rounded(Color.TRANSPARENT, dp(25));
        sliderFrame.setStroke(dp(2), Color.parseColor("#ECEFF1"));
        hueSlider.setBackground(sliderFrame);
        hueSlider.setClipToOutline(false);
        themeSheet.addView(hueSlider, slp);
        hueSlider.setListener(hex -> {
            tempColor = hex;
            renderPreview();
        });

        TextView apply = new TextView(this);
        apply.setGravity(Gravity.CENTER);
        apply.setTextColor(Color.WHITE);
        apply.setTypeface(Typeface.DEFAULT_BOLD);
        apply.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        apply.setPadding(0, dp(12), 0, dp(12));
        apply.setBackground(rounded(Color.parseColor("#37474F"), dp(20)));
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alp.setMargins(0, dp(20), 0, 0);
        apply.setLayoutParams(alp);
        applyBtnTv = apply;
        apply.setOnClickListener(v -> {
            store.applyCustomColor(tempColor);
            closeThemeSheet();
        });
        themeSheet.addView(apply);
    }

    private void openThemeSheet() {
        themeScrim.animate().cancel();
        themeSheet.animate().cancel();
        themeScrim.setVisibility(View.VISIBLE);
        themeScrim.setAlpha(0f);
        themeSheet.setVisibility(View.INVISIBLE);
        // Reopen exactly where the pointer was (persisted pick, like tempColor state).
        tempColor = store.customColor != null ? store.customColor : "#42A5F5";
        hueSlider.setFromHex(tempColor);
        renderPreview();
        renderThemeGrid();
        themeScrim.animate().alpha(1f).setDuration(120).start();
        themeSheet.post(() -> {
            int h = themeSheet.getHeight();
            if (h > 0) {
                themeSheet.setTranslationY(h);
                themeSheet.setVisibility(View.VISIBLE);
                themeSheet.animate().translationY(0f).setDuration(180).start();
            } else {
                themeSheet.setVisibility(View.VISIBLE);
            }
        });
    }

    private void closeThemeSheet() {
        if (themeSheet.getVisibility() == View.GONE) return;
        themeScrim.animate().cancel();
        themeSheet.animate().cancel();
        themeScrim.animate().alpha(0f).setDuration(120).start();
        themeSheet.animate().translationY(themeSheet.getHeight()).setDuration(150).withEndAction(() -> {
            themeScrim.setVisibility(View.GONE);
            themeSheet.setVisibility(View.GONE);
        }).start();
    }

    private void renderPreview() {
        previewCircle.setBackground(roundedCircle(Themes.parse(tempColor)));
        previewHexTv.setText(tempColor.toUpperCase());
    }

    private void renderThemeGrid() {
        themeGrid.removeAllViews();
        Themes.Theme[] presets = Themes.presets();
        LinearLayout row = null;
        for (int i = 0; i < presets.length; i++) {
            if (i % 2 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rlp.setMargins(0, 0, 0, dp(15));
                row.setLayoutParams(rlp);
                themeGrid.addView(row);
            }
            Themes.Theme opt = presets[i];
            boolean active = store.theme.name.equals(opt.name);
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.HORIZONTAL);
            cell.setGravity(Gravity.CENTER_VERTICAL);
            cell.setPadding(dp(10), dp(10), dp(10), dp(10));
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            if (i % 2 == 0) clp.setMargins(0, 0, dp(8), 0);
            else clp.setMargins(dp(8), 0, 0, 0);
            GradientDrawable cellBg;
            if (active) {
                cellBg = rounded(Color.parseColor("#E0F2F1"), dp(12));
                cellBg.setStroke(dp(1), Color.parseColor("#00897B"));
            } else if (dark()) {
                cellBg = rounded(Color.parseColor("#37474F"), dp(12));
                cellBg.setStroke(dp(1), Color.parseColor("#455A64"));
            } else {
                cellBg = rounded(Color.parseColor("#FAFAFA"), dp(12));
                cellBg.setStroke(dp(1), Color.parseColor("#ECEFF1"));
            }
            cell.setBackground(cellBg);
            View dot = new View(this);
            dot.setBackground(roundedCircle(Themes.parse(opt.colors[1])));
            dot.setLayoutParams(new LinearLayout.LayoutParams(dp(24), dp(24)));
            cell.addView(dot);
            TextView nm = new TextView(this);
            nm.setText(store.themeDisplayName(opt));
            nm.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            nm.setTextColor(Color.parseColor(dark() ? "#B0BEC5" : "#455A64"));
            LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            nlp.setMargins(dp(10), 0, 0, 0);
            cell.addView(nm, nlp);
            cell.setOnClickListener(v -> store.setTheme(opt));
            row.addView(cell, clp);
        }
    }

    // ================= SETTINGS (SettingsModal.js) =================

    private void buildSettings(FrameLayout root) {
        settingsOverlay = new LinearLayout(this);
        settingsOverlay.setOrientation(LinearLayout.VERTICAL);
        settingsOverlay.setVisibility(View.GONE);
        // Consume all touches so taps never leak to the screen underneath.
        settingsOverlay.setClickable(true);
        root.addView(settingsOverlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        settingsRoot = settingsOverlay;
    }

    private void openSettings() {
        renderSettings();
        settingsOverlay.animate().cancel();
        // INVISIBLE (not GONE) so it measures; first drawn frame is already
        // shifted down, then it slides up — no flash of the open state.
        settingsOverlay.setVisibility(View.INVISIBLE);
        // Slide from bottom to top, like the original modal animation.
        settingsOverlay.post(() -> {
            int h = settingsOverlay.getHeight();
            if (h > 0) {
                settingsOverlay.setTranslationY(h);
                settingsOverlay.setVisibility(View.VISIBLE);
                settingsOverlay.animate().translationY(0f).setDuration(180).start();
            } else {
                settingsOverlay.setVisibility(View.VISIBLE);
            }
        });
    }

    private void closeSettings() {
        if (settingsOverlay.getVisibility() == View.GONE) return;
        settingsOverlay.animate().cancel();
        settingsOverlay.animate().translationY(settingsOverlay.getHeight())
                .setDuration(150).withEndAction(() -> {
                    settingsOverlay.setVisibility(View.GONE);
                    settingsOverlay.setTranslationY(0f);
                }).start();
    }

    private void renderSettings() {
        settingsRoot.removeAllViews();
        boolean isDark = store.theme.isDark;
        int bgC = isDark ? Color.parseColor("#263238") : Color.parseColor("#F5F5F5");
        int cardC = isDark ? Color.parseColor("#37474F") : Color.WHITE;
        int textC = isDark ? Color.parseColor("#ECEFF1") : Color.parseColor("#37474F");
        int subC = isDark ? Color.parseColor("#B0BEC5") : Color.parseColor("#78909C");
        settingsRoot.setBackgroundColor(bgC);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setBackgroundColor(cardC);
        head.setPadding(dp(20), getStatusBarHeight() + dp(12), dp(20), dp(12));
        head.setElevation(dp(2));
        TextView back = new TextView(this);
        back.setTypeface(IconFont.get(this));
        back.setText(IconFont.ch(IconFont.ARROW_BACK));
        back.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        back.setTextColor(textC);
        back.setGravity(Gravity.CENTER);
        // Fixed 48dp width (centering symmetry) but capped height: the icon
        // font's line metrics are oversized and would balloon a wrap height.
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(dp(48), dp(32));
        back.setLayoutParams(blp);
        back.setOnClickListener(v -> closeSettings());
        head.addView(back);
        TextView ht = new TextView(this);
        ht.setText(store.t("settings"));
        ht.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        ht.setTypeface(Typeface.DEFAULT_BOLD);
        ht.setTextColor(textC);
        // Centered title: equal 48dp blocks on both sides keep it centered
        // in every language.
        ht.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams htp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        head.addView(ht, htp);
        View endSpace = new View(this);
        endSpace.setLayoutParams(new LinearLayout.LayoutParams(dp(48), dp(32)));
        head.addView(endSpace);
        settingsRoot.addView(head);

        ScrollView sv = new ScrollView(this);
        LinearLayout.LayoutParams svp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        sv.setLayoutParams(svp);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(20), dp(20), dp(40));
        sv.addView(content);
        settingsRoot.addView(sv);

        // language
        content.addView(sectionHeader(IconFont.LANGUAGE, store.t("language"), textC));
        LinearLayout langCard = card(cardC);
        langCard.setOrientation(LinearLayout.VERTICAL);
        FlowLayout langFlow = new FlowLayout(this);
        langFlow.setSpacing(dp(8), dp(8));
        for (int i = 0; i < Translations.CODES.length; i++) {
            String code = Translations.CODES[i];
            TextView chip = new TextView(this);
            chip.setText(Translations.LABELS[i]);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            boolean sel = store.language.equals(code);
            chip.setTextColor(sel ? Color.WHITE : textC);
            chip.setTypeface(null, sel ? Typeface.BOLD : Typeface.NORMAL);
            GradientDrawable chBg = rounded(sel
                    ? Themes.parse(store.theme.primaryColor())
                    : (isDark ? Color.parseColor("#455A64") : Color.parseColor("#EEEEEE")), dp(20));
            chip.setBackground(chBg);
            chip.setPadding(dp(12), dp(8), dp(12), dp(8));
            LinearLayout.LayoutParams chp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            chip.setOnClickListener(v -> {
                store.setLanguage(code);
                renderSettings();
            });
            langFlow.addView(chip, chp);
        }
        // Wrapped rows inside the card, like the original flexWrap layout
        langCard.addView(langFlow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        content.addView(langCard);

        // app icon picker: one row showing the current choice, tap opens a dialog
        content.addView(actionRow(IconFont.APPS, store.t("appIcon"), iconChoiceLabel(),
                cardC, textC, subC, v -> openIconPickerDialog()));

        // haptic + auto-advance (same row layout, one shared builder)
        content.addView(toggleRow(IconFont.PHONE_PORTRAIT,
                store.t("hapticFeedback"), store.t("hapticFeedbackDesc"),
                store.hapticEnabled, (b, c) -> store.setHapticEnabled(c),
                cardC, textC, subC));
        content.addView(toggleRow(IconFont.SWAP_HORIZONTAL,
                store.t("autoAdvance"), store.t("autoAdvanceDesc"),
                store.autoAdvanceEnabled, (b, c) -> store.setAutoAdvanceEnabled(c),
                cardC, textC, subC));

        // about
        content.addView(sectionHeader(IconFont.INFO_CIRCLE, store.t("about"), textC));
        LinearLayout ab = card(cardC);
        ab.setOrientation(LinearLayout.VERTICAL);
        TextView an = new TextView(this);
        an.setText(store.t("appName"));
        an.setTypeface(Typeface.DEFAULT_BOLD);
        an.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        an.setTextColor(textC);
        ab.addView(an);
        TextView adesc = new TextView(this);
        adesc.setText(store.t("appDesc"));
        adesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        adesc.setTextColor(subC);
        LinearLayout.LayoutParams adp2 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        adp2.setMargins(0, dp(8), 0, dp(15));
        ab.addView(adesc, adp2);
        TextView ver = new TextView(this);
        ver.setText("Version 1.0.2");
        ver.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        ver.setTextColor(subC);
        ab.addView(ver);
        content.addView(ab);

        // profile management
        TextView pmt = new TextView(this);
        pmt.setText(store.t("profileManagement"));
        pmt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        pmt.setTypeface(null, Typeface.BOLD);
        pmt.setTextColor(textC);
        LinearLayout.LayoutParams pmp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pmp.setMargins(0, dp(10), 0, dp(10));
        content.addView(pmt, pmp);

        content.addView(actionRow(IconFont.DOWNLOAD, store.t("exportProfile"), store.t("exportDesc"),
                cardC, textC, subC, v -> startExport()));
        content.addView(actionRow(IconFont.UPLOAD, store.t("importProfile"), store.t("importDesc"),
                cardC, textC, subC, v -> startImport()));

        TextView note = new TextView(this);
        note.setText(store.t("profileNote"));
        note.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        note.setTextColor(isDark ? Color.parseColor("#90CAF9") : Color.parseColor("#1565C0"));
        note.setPadding(dp(15), dp(15), dp(15), dp(15));
        note.setBackground(rounded(isDark ? Color.argb((int) (0.1 * 255), 66, 165, 245)
                : Color.parseColor("#E3F2FD"), dp(12)));
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        np.setMargins(0, dp(5), 0, 0);
        content.addView(note, np);

        content.addView(linkRow(IconFont.LOGO_GITHUB, store.t("github"), subC,
                v -> openUrl("https://github.com/SlaveOfGod1/Sabbeh")));
        content.addView(linkRow(IconFont.BUG, store.t("reportBug"), subC,
                v -> openUrl("https://github.com/SlaveOfGod1/Sabbeh/issues")));

        LinearLayout resetRow = new LinearLayout(this);
        resetRow.setOrientation(LinearLayout.HORIZONTAL);
        resetRow.setGravity(Gravity.CENTER);
        TextView resetIcon = new TextView(this);
        resetIcon.setTypeface(IconFont.get(this));
        resetIcon.setText(IconFont.ch(IconFont.TRASH));
        resetIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        resetIcon.setTextColor(Color.parseColor("#FF5252"));
        resetRow.addView(resetIcon);
        TextView reset = new TextView(this);
        reset.setText(store.t("resetData"));
        reset.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        reset.setTypeface(null, Typeface.BOLD);
        reset.setTextColor(Color.parseColor("#FF5252"));
        LinearLayout.LayoutParams resetTp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        resetTp.setMargins(dp(8), 0, 0, 0);
        resetRow.addView(reset, resetTp);
        resetRow.setPadding(0, dp(15), 0, dp(15));
        GradientDrawable rBg = rounded(isDark ? Color.parseColor("#3E2723")
                : Color.parseColor("#FFEBEE"), dp(12));
        rBg.setStroke(dp(1), Color.parseColor(isDark ? "#D32F2F" : "#FFCDD2"));
        resetRow.setBackground(rBg);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.setMargins(0, dp(30), 0, 0);
        resetRow.setOnClickListener(v -> confirmResetData());
        content.addView(resetRow, rlp);
        // Bottom breathing room so the button clears the navigation bar,
        // same fix as the theme sheet's Save button.
        Space bottomSpace = new Space(this);
        bottomSpace.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, getNavBarHeight() + dp(20)));
        content.addView(bottomSpace);
    }

    private LinearLayout sectionHeader(int iconCode, String title, int textC) {
        LinearLayout h = new LinearLayout(this);
        h.setOrientation(LinearLayout.HORIZONTAL);
        h.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(20), 0, dp(10));
        h.setLayoutParams(p);
        TextView i = new TextView(this);
        i.setTypeface(IconFont.get(this));
        i.setText(IconFont.ch(iconCode));
        i.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        i.setTextColor(textC);
        h.addView(i);
        TextView t = new TextView(this);
        t.setText(title);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        t.setTypeface(null, Typeface.BOLD);
        t.setTextColor(textC);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.setMargins(dp(8), 0, 0, 0);
        h.addView(t, tp);
        return h;
    }

    private LinearLayout card(int cardC) {
        LinearLayout c = new LinearLayout(this);
        c.setPadding(dp(20), dp(20), dp(20), dp(20));
        c.setBackground(rounded(cardC, dp(16)));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        c.setLayoutParams(p);
        return c;
    }

    private LinearLayout actionRow(int iconCode, String title, String sub,
                                   int cardC, int textC, int subC, View.OnClickListener l) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(dp(16), dp(16), dp(16), dp(16));
        r.setBackground(rounded(cardC, dp(16)));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 0, 0, dp(12));
        r.setLayoutParams(p);
        TextView i = new TextView(this);
        i.setTypeface(IconFont.get(this));
        i.setText(IconFont.ch(iconCode));
        i.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        i.setTextColor(textC);
        i.setWidth(dp(40));
        i.setGravity(Gravity.CENTER);
        r.addView(i);
        LinearLayout tx = new LinearLayout(this);
        tx.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams txp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        txp.setMargins(dp(10), 0, 0, 0);
        r.addView(tx, txp);
        TextView t = new TextView(this);
        t.setText(title);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        t.setTextColor(textC);
        tx.addView(t);
        TextView s = new TextView(this);
        s.setText(sub);
        s.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        s.setTextColor(subC);
        tx.addView(s);
        r.setOnClickListener(l);
        return r;
    }

    private LinearLayout linkRow(int iconCode, String label, int subC, View.OnClickListener l) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(10), 0, 0);
        r.setLayoutParams(p);
        r.setPadding(dp(10), dp(10), dp(10), dp(10));
        TextView i = new TextView(this);
        i.setTypeface(IconFont.get(this));
        i.setText(IconFont.ch(iconCode));
        i.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        i.setTextColor(subC);
        r.addView(i);
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        t.setTextColor(subC);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.setMargins(dp(8), 0, 0, 0);
        r.addView(t, tp);
        r.setOnClickListener(l);
        return r;
    }

    // ================= DIALOGS =================

    /** Human label for the current launcher icon choice. */
    private String iconChoiceLabel() {
        switch (store.iconChoice) {
            case "green_trans": return store.t("iconGreenTrans");
            case "white_trans": return store.t("iconWhiteTrans");
            case "white_black": return store.t("iconWhiteBlack");
            case "black_trans": return store.t("iconBlackTrans");
            default: return store.t("iconDefault");
        }
    }

    /** Icon picker prompt reusing the themed dialog style. */
    private void openIconPickerDialog() {
        boolean isDark = dark();
        LinearLayout box = dialogBox();
        TextView mt = new TextView(this);
        mt.setText(store.t("appIcon"));
        mt.setGravity(Gravity.CENTER);
        mt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        mt.setTypeface(Typeface.DEFAULT_BOLD);
        mt.setTextColor(Color.parseColor(isDark ? "#ECEFF1" : "#37474F"));
        box.addView(mt);
        AlertDialog dlg = new AlertDialog.Builder(this).setView(box).create();

        String[][] iconOpts = {
                {"default", store.t("iconDefault")},
                {"green_trans", store.t("iconGreenTrans")},
                {"white_trans", store.t("iconWhiteTrans")},
                {"white_black", store.t("iconWhiteBlack")},
                {"black_trans", store.t("iconBlackTrans")},
        };
        for (String[] opt : iconOpts) {
            final String key = opt[0];
            boolean sel = key.equals(store.iconChoice);
            TextView row = optionBtn(opt[1],
                    sel ? pc() : (isDark ? Color.parseColor("#455A64") : Color.parseColor("#F7F9F9")),
                    sel ? Color.WHITE : (isDark ? Color.parseColor("#ECEFF1") : Color.parseColor("#455A64")));
            row.setOnClickListener(v -> {
                dlg.dismiss();
                store.applyIconChoice(key);
            });
            LinearLayout.LayoutParams op = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            op.setMargins(0, dp(12), 0, 0);
            box.addView(row, op);
        }
        dlg.show();
        if (dlg.getWindow() != null) {
            dlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    /** Themed confirm dialog (matches light/dark theme, unlike system alerts). */
    private void confirmDialog(String title, String message, String okLabel,
                               int okBg, Runnable onOk) {
        boolean isDark = dark();
        LinearLayout box = dialogBox();
        TextView mt = new TextView(this);
        mt.setText(title);
        mt.setGravity(Gravity.CENTER);
        mt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        mt.setTypeface(Typeface.DEFAULT_BOLD);
        mt.setTextColor(Color.parseColor(isDark ? "#ECEFF1" : "#37474F"));
        box.addView(mt);
        TextView msg = new TextView(this);
        msg.setText(message);
        msg.setGravity(Gravity.CENTER);
        msg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        msg.setTextColor(Color.parseColor(isDark ? "#B0BEC5" : "#546E7A"));
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mp.setMargins(0, dp(12), 0, 0);
        box.addView(msg, mp);

        LinearLayout btns = new LinearLayout(this);
        btns.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0, dp(25), 0, 0);
        btns.setLayoutParams(bp);
        AlertDialog dlg = new AlertDialog.Builder(this).setView(box).create();
        TextView cancel = new TextView(this);
        cancel.setText(store.t("cancel"));
        cancel.setGravity(Gravity.CENTER);
        cancel.setTypeface(Typeface.DEFAULT_BOLD);
        cancel.setPadding(0, dp(12), 0, dp(12));
        cancel.setBackground(rounded(isDark ? Color.parseColor("#546E7A") : Color.parseColor("#ECEFF1"), dp(10)));
        cancel.setTextColor(isDark ? Color.parseColor("#ECEFF1") : Color.BLACK);
        cancel.setOnClickListener(v -> dlg.dismiss());
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        blp.setMargins(0, 0, dp(5), 0);
        btns.addView(cancel, blp);
        TextView ok = new TextView(this);
        ok.setText(okLabel);
        ok.setGravity(Gravity.CENTER);
        ok.setTypeface(Typeface.DEFAULT_BOLD);
        ok.setTextColor(Color.WHITE);
        ok.setPadding(0, dp(12), 0, dp(12));
        ok.setBackground(rounded(okBg, dp(10)));
        ok.setOnClickListener(v -> {
            dlg.dismiss();
            onOk.run();
        });
        LinearLayout.LayoutParams blp2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        blp2.setMargins(dp(5), 0, 0, 0);
        btns.addView(ok, blp2);
        box.addView(btns);
        dlg.show();
        if (dlg.getWindow() != null) {
            dlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void confirmResetData() {
        confirmDialog(store.t("resetData"), store.t("confirmResetData"),
                store.t("resetButton"), Color.parseColor("#D32F2F"), () -> {
                    store.resetAll();
                    closeSettings();
                    toast(store.t("success") + ": " + store.t("resetDataDesc"));
                });
    }

    private void openOptionsModal(Dhikr item) {
        LinearLayout box = dialogBox();
        TextView mt = new TextView(this);
        mt.setText(store.dhikrTitle(item));
        mt.setGravity(Gravity.CENTER);
        mt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        mt.setTypeface(Typeface.DEFAULT_BOLD);
        mt.setTextColor(pc());
        box.addView(mt);

        AlertDialog dlg = new AlertDialog.Builder(this).setView(box).create();

        TextView edit = optionBtn(store.t("editDhikr"),
                dark() ? Color.parseColor("#455A64") : Color.parseColor("#F7F9F9"),
                dark() ? Color.parseColor("#ECEFF1") : Color.parseColor("#455A64"));
        edit.setOnClickListener(v -> {
            dlg.dismiss();
            openAddModal(item);
        });
        LinearLayout.LayoutParams op = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        op.setMargins(0, dp(15), 0, 0);
        box.addView(edit, op);

        TextView del = optionBtn(store.t("deleteDhikr"),
                Color.parseColor("#D32F2F"), Color.WHITE);
        del.setOnClickListener(v -> {
            dlg.dismiss();
            confirmDialog(store.t("deleteDhikr"), store.t("confirmDelete"),
                    store.t("delete"), Color.parseColor("#D32F2F"),
                    () -> store.deleteDhikr(item.id));
        });
        box.addView(del, op);

        TextView cancel = new TextView(this);
        cancel.setText(store.t("cancel"));
        cancel.setGravity(Gravity.CENTER);
        cancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        cancel.setTextColor(Color.parseColor("#90A4AE"));
        cancel.setPadding(0, dp(10), 0, 0);
        cancel.setOnClickListener(v -> dlg.dismiss());
        box.addView(cancel);
        dlg.show();
        // Transparent window so no white corners peek around the rounded box.
        if (dlg.getWindow() != null) {
            dlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private TextView optionBtn(String label, int bgC, int textC) {
        TextView b = new TextView(this);
        b.setText(label);
        b.setGravity(Gravity.CENTER);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        b.setTypeface(null, Typeface.BOLD);
        b.setTextColor(textC);
        b.setPadding(0, dp(15), 0, dp(15));
        GradientDrawable d = rounded(bgC, dp(10));
        d.setStroke(dp(1), Color.parseColor("#ECEFF1"));
        b.setBackground(d);
        return b;
    }

    private void openAddModal(Dhikr editing) {
        boolean isEdit = editing != null;
        boolean isDark = dark();
        LinearLayout box = dialogBox();
        TextView mt = new TextView(this);
        mt.setText(isEdit ? store.t("editDhikr") : store.t("addDhikr"));
        mt.setGravity(Gravity.CENTER);
        mt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        mt.setTypeface(Typeface.DEFAULT_BOLD);
        mt.setTextColor(isDark ? Color.parseColor("#ECEFF1") : pc());
        LinearLayout.LayoutParams mtP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mtP.setMargins(0, 0, 0, dp(15));
        box.addView(mt, mtP);

        EditText titleIn = styledInput(store.t("phTitle"));
        EditText subIn = styledInput(store.t("phSubtitle"));
        EditText targetIn = styledInput("33");
        targetIn.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (isEdit) {
            titleIn.setText(editing.title);
            subIn.setText(editing.subtitle);
            targetIn.setText(String.valueOf(editing.target));
        }
        box.addView(label(store.t("title"), isDark));
        box.addView(titleIn);
        box.addView(label(store.t("subtitle"), isDark));
        box.addView(subIn);
        box.addView(label(store.t("target"), isDark));
        box.addView(targetIn);

        LinearLayout btns = new LinearLayout(this);
        btns.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0, dp(25), 0, 0);
        btns.setLayoutParams(bp);

        // Scrollable content + transparent window: no white corners, and the
        // panel shrinks above the keyboard so every field stays visible.
        ScrollView scroll = new ScrollView(this);
        scroll.addView(box);
        AlertDialog dlg = new AlertDialog.Builder(this).setView(scroll).create();

        TextView cancel = new TextView(this);
        cancel.setText(store.t("cancel"));
        cancel.setGravity(Gravity.CENTER);
        cancel.setTypeface(Typeface.DEFAULT_BOLD);
        cancel.setPadding(0, dp(12), 0, dp(12));
        cancel.setBackground(rounded(isDark ? Color.parseColor("#546E7A") : Color.parseColor("#ECEFF1"), dp(10)));
        cancel.setTextColor(isDark ? Color.parseColor("#ECEFF1") : Color.BLACK);
        cancel.setOnClickListener(v -> dlg.dismiss());
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        blp.setMargins(0, 0, dp(5), 0);
        btns.addView(cancel, blp);

        TextView save = new TextView(this);
        save.setText(store.t("save"));
        save.setGravity(Gravity.CENTER);
        save.setTypeface(Typeface.DEFAULT_BOLD);
        save.setTextColor(Color.WHITE);
        save.setPadding(0, dp(12), 0, dp(12));
        save.setBackground(rounded(pc(), dp(10)));
        save.setOnClickListener(v -> {
            String title = titleIn.getText().toString().trim();
            if (title.isEmpty()) {
                toast(store.t("nameRequired"));
                return;
            }
            String sub = subIn.getText().toString().trim();
            int target;
            try {
                target = Integer.parseInt(targetIn.getText().toString().trim());
            } catch (Exception e) {
                target = 33;
            }
            if (isEdit) store.updateDhikr(editing.id, title, sub, target);
            else {
                store.addDhikr(title, sub, target);
                closeDrawer();
            }
            dlg.dismiss();
        });
        LinearLayout.LayoutParams blp2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        blp2.setMargins(dp(5), 0, 0, 0);
        btns.addView(save, blp2);
        box.addView(btns);
        dlg.show();
        if (dlg.getWindow() != null) {
            dlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dlg.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    private TextView label(String s, boolean isDark) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        t.setTextColor(Color.parseColor(isDark ? "#B0BEC5" : "#546E7A"));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(10), 0, dp(5));
        t.setLayoutParams(p);
        return t;
    }

    private EditText styledInput(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        boolean isDark = dark();
        e.setTextColor(isDark ? Color.WHITE : Color.parseColor("#263238"));
        e.setHintTextColor(Color.parseColor(isDark ? "#90A4AE" : "#999999"));
        GradientDrawable d = rounded(isDark ? Color.parseColor("#455A64") : Color.parseColor("#F7F9F9"), dp(8));
        d.setStroke(dp(1), Color.parseColor(isDark ? "#546E7A" : "#CFD8DC"));
        e.setBackground(d);
        e.setPadding(dp(10), dp(10), dp(10), dp(10));
        return e;
    }

    // ================= EXPORT / IMPORT (Storage Access Framework) =================

    private void startExport() {
        try {
            Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("application/json");
            i.putExtra(Intent.EXTRA_TITLE, "sabbeh_backup.json");
            startActivityForResult(i, REQ_EXPORT);
        } catch (Exception e) {
            toast(store.t("exportFailed"));
        }
    }

    private void startImport() {
        try {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/json", "text/plain"});
            startActivityForResult(i, REQ_IMPORT);
        } catch (Exception e) {
            toast(store.t("parseFailed"));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        if (requestCode == REQ_EXPORT) {
            try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                os.write(store.buildExportJson().getBytes("UTF-8"));
                toast(store.t("exported"));
            } catch (Exception e) {
                toast(store.t("exportFailed"));
            }
        } else if (requestCode == REQ_IMPORT) {
            try (InputStream is = getContentResolver().openInputStream(uri);
                 Scanner sc = new Scanner(is, "UTF-8").useDelimiter("\\A")) {
                String text = sc.hasNext() ? sc.next() : "";
                confirmImport(text);
            } catch (Exception e) {
                toast(store.t("parseFailed"));
            }
        }
    }

    private void confirmImport(final String text) {
        confirmDialog(store.t("importProfile"), store.t("confirmImport"),
                store.t("importButton"), Color.parseColor("#D32F2F"), () -> {
                    String err = store.applyImportJson(text);
                    if (err == null) {
                        closeSettings();
                        toast(store.t("imported"));
                    } else {
                        toast(err);
                    }
                });
    }

    // ================= RENDER =================

    private void renderAll() {
        if (store == null) return;
        Themes.Theme th = store.theme;
        boolean isDark = th.isDark;
        int primary = Themes.parse(th.primaryColor());

        bg.setColors(Themes.parse(th.colors[0]), Themes.parse(th.colors[1]), th.endX, th.endY);

        // header icon tint = primary (theme.colors[1])
        menuBtn.setTextColor(primary);
        paletteBtn.setTextColor(primary);
        nightBtn.setTextColor(primary);
        nightBtn.setText(IconFont.ch("Night".equals(th.name) ? IconFont.MOON : IconFont.MOON_OUTLINE));
        settingsBtn.setTextColor(primary);

        Dhikr c = store.currentDhikr();
        titleTv.setText(c == null ? "" : store.dhikrTitle(c));
        subtitleTv.setText(c == null ? "" : store.dhikrSubtitle(c));
        roundsTv.setText(store.t("rounds") + ": " + store.rounds());
        countTv.setText(String.format(Locale.US, "%03d", store.count()));

        // counter housing gradient #37474F -> #102027 (fixed, like the original)
        GradientDrawable housing = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#37474F"), Color.parseColor("#102027")});
        housing.setCornerRadius(dp(30));
        counterHousing.setBackground(housing);
        setTagText(counterHousing, "counter_top", store.t("totalCount"));
        setTagText(counterHousing, "counter_left", store.t("countLabel"));
        setTagText(counterHousing, "counter_right", store.t("resetLabel"));

        // device body
        deviceBody.setBackground(rounded(Themes.parse(th.deviceBodyOrDefault()), dp(90)));

        renderDrawer();
        renderThemeSheetChrome();
        if (settingsOverlay.getVisibility() == View.VISIBLE) renderSettings();
    }

    private void setTagText(ViewGroup root, String tag, String text) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View v = root.getChildAt(i);
            if (tag.equals(v.getTag()) && v instanceof TextView) ((TextView) v).setText(text);
            else if (v instanceof ViewGroup) setTagText((ViewGroup) v, tag, text);
        }
    }

    private void renderDrawer() {
        boolean isDark = store.theme.isDark;
        int primary = Themes.parse(store.theme.primaryColor());
        drawerPanel.setBackgroundColor(isDark ? Color.parseColor("#263238") : Color.WHITE);

        drawerHeader.setBackgroundColor(primary);
        drawerTitleTv.setText(store.t("appName"));
        drawerSubTv.setText(store.t("appShortDesc"));

        dhikrList.removeAllViews();
        for (Dhikr d : store.dhikrs) {
            boolean active = store.currentDhikr() != null && store.currentDhikr().id.equals(d.id);
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(15), dp(15), dp(15), dp(15));
            GradientDrawable cardBg;
            if (active) {
                cardBg = rounded(primary, dp(12));
                cardBg.setStroke(dp(1), primary);
            } else if (isDark) {
                cardBg = rounded(Color.parseColor("#37474F"), dp(12));
                cardBg.setStroke(dp(1), Color.parseColor("#455A64"));
            } else {
                cardBg = rounded(Color.WHITE, dp(12));
                cardBg.setStroke(dp(1), Color.parseColor("#EEEEEE"));
            }
            card.setBackground(cardBg);
            card.setElevation(dp(2));
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            clp.setMargins(0, 0, 0, dp(10));
            card.setOnClickListener(v -> {
                store.selectDhikr(d);
                closeDrawer();
            });
            card.setOnLongClickListener(v -> {
                openOptionsModal(d);
                return true;
            });

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            card.addView(row);
            TextView t = new TextView(this);
            t.setText(store.dhikrTitle(d));
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            t.setTypeface(Typeface.DEFAULT_BOLD);
            t.setGravity(Gravity.LEFT);
            t.setTextColor(active ? Color.WHITE : (isDark ? Color.parseColor("#ECEFF1") : Color.parseColor("#37474F")));
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(t, tlp);
            TextView badge = new TextView(this);
            badge.setText(d.target + "x");
            badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            badge.setTypeface(Typeface.DEFAULT_BOLD);
            badge.setTextColor(active ? Color.WHITE : (isDark ? Color.parseColor("#B0BEC5") : primary));
            badge.setBackground(rounded(active ? Color.argb((int) (0.2 * 255), 255, 255, 255)
                    : (isDark ? Color.parseColor("#455A64") : Color.parseColor("#ECEFF1")), dp(12)));
            badge.setPadding(dp(8), dp(4), dp(8), dp(4));
            row.addView(badge);
            if (d.subtitle != null && !d.subtitle.isEmpty()) {
                TextView s = new TextView(this);
                s.setText(store.dhikrSubtitle(d));
                s.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                s.setGravity(Gravity.LEFT);
                s.setTextColor(active ? Color.parseColor("#B2DFDB") : (isDark ? Color.parseColor("#B0BEC5") : Color.parseColor("#78909C")));
                LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                slp.setMargins(0, dp(5), 0, 0);
                card.addView(s, slp);
            }
            dhikrList.addView(card, clp);
        }

        TextView add = drawerAddBtn;
        if (add != null) {
            add.setText("+ " + store.t("addDhikr"));
            add.setTextColor(Color.parseColor(isDark ? "#B0BEC5" : "#546E7A"));
            // Background must follow the theme too (it is built once in onCreate).
            add.setBackground(addButtonBg());
        }
        if (drawerAppNameTv != null) {
            drawerAppNameTv.setText(store.t("appName"));
            drawerAppNameTv.setTextColor(Color.parseColor(isDark ? "#90A4AE" : "#455A64"));
        }
        if (drawerFooter != null) {
            drawerFooter.setBackgroundColor(isDark ? Color.parseColor("#263238") : Color.WHITE);
        }
    }

    private void renderThemeSheetChrome() {
        boolean isDark = store.theme.isDark;
        int sheetBg = isDark ? Color.parseColor("#263238") : Color.WHITE;
        GradientDrawable d = new GradientDrawable();
        d.setColor(sheetBg);
        d.setCornerRadii(new float[]{dp(25), dp(25), dp(25), dp(25), 0, 0, 0, 0});
        themeSheet.setBackground(d);
        themeTitleTv.setText(store.t("theme"));
        themeTitleTv.setTextColor(Color.parseColor(isDark ? "#ECEFF1" : "#37474F"));
        themePaletteIcon.setTextColor(Color.parseColor(isDark ? "#ECEFF1" : "#37474F"));
        customColorTitleTv.setText(store.t("customColor"));
        customColorTitleTv.setTextColor(Color.parseColor(isDark ? "#ECEFF1" : "#37474F"));
        applyBtnTv.setText(store.t("save"));
        renderPreview();
        if (themeSheet.getVisibility() == View.VISIBLE) renderThemeGrid();
    }

    // ============ shared UI builders (used by dialogs + settings) ============

    /** Shortcut: current theme's primary color as an Android color int. */
    private int pc() {
        return Themes.parse(store.theme.primaryColor());
    }

    /** Shortcut: is the Night theme active? */
    private boolean dark() {
        return store.theme.isDark;
    }

    /** Dashed "+ Add Dhikr" button background, repainted on every theme change. */
    private DashedBox addButtonBg() {
        boolean isDark = dark();
        return new DashedBox(
                isDark ? Color.parseColor("#37474F") : Color.parseColor("#FAFAFA"),
                isDark ? Color.parseColor("#546E7A") : Color.parseColor("#E0E0E0"),
                dp(12), dp(1), dp(6), dp(4));
    }
    /** Rounded dialog container shared by the options + add/edit dialogs. */
    private LinearLayout dialogBox() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(25), dp(25), dp(25), dp(25));
        box.setBackground(rounded(dark() ? Color.parseColor("#37474F") : Color.WHITE, dp(20)));
        return box;
    }

    /** A settings section made of a header plus a card with a label and a toggle. */
    private LinearLayout toggleRow(int iconCode, String title, String desc, boolean value,
                                   CompoundButton.OnCheckedChangeListener onChange,
                                   int cardC, int textC, int subC) {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        section.addView(sectionHeader(iconCode, title, textC));
        LinearLayout row = card(cardC);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView d = new TextView(this);
        d.setText(desc);
        d.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        d.setTextColor(subC);
        LinearLayout.LayoutParams dp2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        dp2.setMargins(0, 0, dp(15), 0);
        row.addView(d, dp2);
        Switch sw = new Switch(this);
        sw.setChecked(value);
        sw.setOnCheckedChangeListener(onChange);
        row.addView(sw);
        section.addView(row);
        return section;
    }

    private TextView iconButton(int code) {
        TextView b = new TextView(this);
        b.setTypeface(IconFont.get(this));
        b.setText(IconFont.ch(code));
        b.setGravity(Gravity.CENTER);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        b.setBackground(roundedCircle(Color.argb((int) (0.9 * 255), 255, 255, 255)));
        b.setElevation(dp(4));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(44), dp(44));
        b.setLayoutParams(p);
        return b;
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private GradientDrawable roundedCircle(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(color);
        return d;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private int getStatusBarHeight() {
        int id = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (id > 0) return getResources().getDimensionPixelSize(id);
        return dp(24);
    }

    private int getNavBarHeight() {
        int id = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (id > 0) return getResources().getDimensionPixelSize(id);
        return dp(48);
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            toast(url);
        }
    }
}
