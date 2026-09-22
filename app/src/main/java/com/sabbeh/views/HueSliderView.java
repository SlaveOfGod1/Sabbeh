package com.sabbeh.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Port of components/SimpleColorPicker.js hue slider.
 * 300x50dp rainbow bar, white marker knob, touch/drag to pick.
 * Emits dimmed colors: hslToHex(hue, 60, 45) exactly like the original.
 */
public class HueSliderView extends View {

    public interface OnColorSelected { void onColor(String hex); }

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint knobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint knobBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();

    private float hue = 180f;
    private float markerX = 0.5f; // 0..1
    private String selectedColor = "#2EADB3";
    private OnColorSelected listener;

    public HueSliderView(Context ctx) { super(ctx); init(); }
    public HueSliderView(Context ctx, AttributeSet a) { super(ctx, a); init(); }

    private void init() {
        knobPaint.setColor(Color.WHITE);
        knobBorder.setColor(Color.argb((int) (0.2 * 255), 0, 0, 0));
        knobBorder.setStyle(Paint.Style.STROKE);
        knobBorder.setStrokeWidth(2f);
        setFromHex(selectedColor);
    }

    public void setListener(OnColorSelected l) { listener = l; }
    public String getSelectedColor() { return selectedColor; }

    public void setFromHex(String hex) {
        if (hex == null) return;
        try {
            String h = hex.replace("#", "");
            if (h.length() == 8) h = h.substring(0, 6);
            if (h.length() != 6) return;
            int r = Integer.parseInt(h.substring(0, 2), 16);
            int g = Integer.parseInt(h.substring(2, 4), 16);
            int b = Integer.parseInt(h.substring(4, 6), 16);
            float[] hsv = new float[3];
            Color.RGBToHSV(r, g, b, hsv);
            hue = hsv[0];
            markerX = hue / 360f;
            selectedColor = hex.length() >= 7 ? hex.substring(0, 7).toUpperCase() : hex.toUpperCase();
            invalidate();
        } catch (Exception ignored) {}
    }

    /** Same conversion as hslToHex(h, 60, 45) in the JS picker. */
    public static String hslToHex(float h, float s, float l) {
        l /= 100f;
        float a = s * Math.min(l, 1 - l) / 100f;
        return "#" + f(0, h, a, l) + f(8, h, a, l) + f(4, h, a, l);
    }

    private static String f(int n, float h, float a, float l) {
        float k = (n + h / 30f) % 12f;
        float c = l - a * Math.max(Math.min(Math.min(k - 3, 9 - k), 1), -1);
        return String.format("%02X", Math.round(255 * c));
    }

    private void pickAt(float x) {
        float w = getWidth() - getPaddingLeft() - getPaddingRight();
        if (w <= 0) return;
        float rel = (x - getPaddingLeft()) / w;
        if (rel < 0) rel = 0;
        if (rel > 1) rel = 1;
        markerX = rel;
        hue = (float) Math.floor(rel * 360);
        selectedColor = hslToHex(hue, 60, 45);
        invalidate();
        if (listener != null) listener.onColor(selectedColor);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN
                || e.getAction() == MotionEvent.ACTION_MOVE) {
            pickAt(e.getX());
            return true;
        }
        return super.onTouchEvent(e);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth(), h = getHeight();
        float rad = h / 2f;
        barRect.set(0, 0, w, h);
        int[] rainbow = {Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED};
        LinearGradient g = new LinearGradient(0, 0, w, 0,
                rainbow, null, Shader.TileMode.CLAMP);
        barPaint.setShader(g);
        canvas.drawRoundRect(barRect, rad, rad, barPaint);
        // marker knob: 6dp wide white vertical bar
        float mx = markerX * w;
        float kw = 6 * getResources().getDisplayMetrics().density;
        canvas.drawRect(mx - kw / 2, 0, mx + kw / 2, h, knobPaint);
        canvas.drawRect(mx - kw / 2, 0, mx + kw / 2, h, knobBorder);
    }
}
