package com.sabbeh.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Full-screen gradient background. Port of expo-linear-gradient usage in app/index.js:
 * colors={theme.colors} start={{x:0,y:0}} end={theme.end}
 */
public class GradientView extends FrameLayout {

    private int startColor = 0xFF4DB6AC;
    private int endColor = 0xFF00695C;
    private float endX = 1f;
    private float endY = 0.18f;
    private final Paint paint = new Paint();

    public GradientView(Context ctx) { super(ctx); }
    public GradientView(Context ctx, AttributeSet a) { super(ctx, a); }

    public void setColors(int start, int end, float ex, float ey) {
        startColor = start;
        endColor = end;
        endX = ex;
        endY = ey;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth(), h = getHeight();
        if (w > 0 && h > 0) {
            LinearGradient g = new LinearGradient(0, 0, w * endX, h * Math.max(endY, 0.02f),
                    startColor, endColor, Shader.TileMode.CLAMP);
            paint.setShader(g);
            canvas.drawRect(0, 0, w, h, paint);
        }
        super.onDraw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int w = getWidth(), h = getHeight();
        if (w > 0 && h > 0) {
            LinearGradient g = new LinearGradient(0, 0, w * endX, h * Math.max(endY, 0.02f),
                    startColor, endColor, Shader.TileMode.CLAMP);
            paint.setShader(g);
            canvas.drawRect(0, 0, w, h, paint);
        }
        super.dispatchDraw(canvas);
    }
}
