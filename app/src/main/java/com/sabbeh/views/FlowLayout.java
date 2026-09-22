package com.sabbeh.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Minimal flow layout (child margins are ignored; use setSpacing instead).
 * Port of flexWrap:'wrap' used for the language chips in SettingsModal.js.
 * The app is forced LTR, so layout is always left-to-right.
 */
public class FlowLayout extends ViewGroup {

    private int hGapPx = 0;
    private int vGapPx = 0;

    public FlowLayout(Context ctx) { super(ctx); }
    public FlowLayout(Context ctx, AttributeSet a) { super(ctx, a); }

    public void setSpacing(int hGap, int vGap) {
        hGapPx = hGap;
        vGapPx = vGap;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int mode = MeasureSpec.getMode(widthSpec);
        int maxW = MeasureSpec.getSize(widthSpec) - getPaddingLeft() - getPaddingRight();
        if (mode == MeasureSpec.UNSPECIFIED) maxW = Integer.MAX_VALUE;

        int x = 0, y = 0, lineH = 0, contentW = 0;
        int childHSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        for (int i = 0; i < getChildCount(); i++) {
            View c = getChildAt(i);
            if (c.getVisibility() == GONE) continue;
            measureChild(c, MeasureSpec.makeMeasureSpec(maxW, MeasureSpec.AT_MOST), childHSpec);
            int cw = c.getMeasuredWidth();
            int ch = c.getMeasuredHeight();
            if (x > 0 && x + cw > maxW) {
                if (x - hGapPx > contentW) contentW = x - hGapPx;
                x = 0;
                y += lineH + vGapPx;
                lineH = 0;
            }
            x += cw + hGapPx;
            if (ch > lineH) lineH = ch;
        }
        if (x > 0 && x - hGapPx > contentW) contentW = x - hGapPx;

        setMeasuredDimension(
                resolveSize(contentW + getPaddingLeft() + getPaddingRight(), widthSpec),
                resolveSize(y + lineH + getPaddingTop() + getPaddingBottom(), heightSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int limit = getWidth() - getPaddingRight();
        int x = getPaddingLeft();
        int y = getPaddingTop();
        int lineH = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View c = getChildAt(i);
            if (c.getVisibility() == GONE) continue;
            int cw = c.getMeasuredWidth();
            int ch = c.getMeasuredHeight();
            if (x > getPaddingLeft() && x + cw > limit) {
                x = getPaddingLeft();
                y += lineH + vGapPx;
                lineH = 0;
            }
            c.layout(x, y, x + cw, y + ch);
            x += cw + hGapPx;
            if (ch > lineH) lineH = ch;
        }
    }
}
