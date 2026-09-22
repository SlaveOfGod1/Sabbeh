package com.sabbeh.views;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * Rounded box with a dashed outline. Port of the drawer's "+ Add Dhikr"
 * button style (borderStyle:'dashed') from CustomDrawerContent.js.
 * GradientDrawable cannot draw dashes, hence this tiny custom drawable.
 */
public class DashedBox extends Drawable {

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float radiusPx;

    public DashedBox(int fillColor, int borderColor, float radiusPx,
                     float strokePx, float dashPx, float gapPx) {
        fill.setStyle(Paint.Style.FILL);
        fill.setColor(fillColor);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setColor(borderColor);
        stroke.setStrokeWidth(strokePx);
        stroke.setPathEffect(new DashPathEffect(new float[]{dashPx, gapPx}, 0));
        this.radiusPx = radiusPx;
    }

    @Override
    public void draw(Canvas canvas) {
        rect.set(getBounds());
        float half = stroke.getStrokeWidth() / 2f;
        rect.inset(half, half);
        canvas.drawRoundRect(rect, radiusPx, radiusPx, fill);
        canvas.drawRoundRect(rect, radiusPx, radiusPx, stroke);
    }

    @Override
    public void setAlpha(int alpha) {
        fill.setAlpha(alpha);
        stroke.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        fill.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
