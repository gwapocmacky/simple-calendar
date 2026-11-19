package com.elegantcal.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class ColorWheelView extends View {
    private Paint wheelPaint;
    private Paint selectorPaint;
    private Paint centerPaint;
    private Paint centerStrokePaint;

    private float centerX, centerY;
    private float radius;
    private float baseRadius;
    private float selectorAngle = 0;
    private int currentColor = 0xff7c6af7;

    private OnColorSelectedListener listener;
    private ScaleGestureDetector scaleDetector;
    private float scaleFactor = 1.0f;

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    public ColorWheelView(Context context) {
        this(context, null);
    }

    public ColorWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        wheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorPaint.setColor(Color.WHITE);
        selectorPaint.setStyle(Paint.Style.STROKE);
        selectorPaint.setStrokeWidth(6);

        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setStyle(Paint.Style.FILL);
        centerPaint.setColor(currentColor);

        centerStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerStrokePaint.setStyle(Paint.Style.STROKE);
        centerStrokePaint.setColor(0x88ffffff);
        centerStrokePaint.setStrokeWidth(4);

        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(0.85f, Math.min(scaleFactor, 1.5f));
                invalidate();
                return true;
            }
        });
    }

    public void setOnColorSelectedListener(OnColorSelectedListener l) {
        this.listener = l;
    }

    public void setColor(int color) {
        this.currentColor = color;
        centerPaint.setColor(color);
        // Find approximate angle
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        selectorAngle = hsv[0];
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
        baseRadius = Math.min(w, h) / 2f - 24;
        radius = baseRadius;

        int[] colors = {
                Color.RED,
                Color.YELLOW,
                Color.GREEN,
                Color.CYAN,
                Color.BLUE,
                Color.MAGENTA,
                Color.RED
        };
        SweepGradient gradient = new SweepGradient(centerX, centerY, colors, null);
        wheelPaint.setShader(gradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        radius = baseRadius * scaleFactor;

        // Draw color wheel
        canvas.drawCircle(centerX, centerY, radius, wheelPaint);

        // Center preview dot
        float centerR = radius * 0.30f;
        centerPaint.setColor(currentColor);
        canvas.drawCircle(centerX, centerY, centerR, centerPaint);
        canvas.drawCircle(centerX, centerY, centerR, centerStrokePaint);

        // Selector ring on the outer wheel
        double angle = Math.toRadians(selectorAngle);
        float sx = (float) (centerX + (radius - 14) * Math.cos(angle));
        float sy = (float) (centerY + (radius - 14) * Math.sin(angle));
        canvas.drawCircle(sx, sy, 12, selectorPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            float dx = event.getX() - centerX;
            float dy = event.getY() - centerY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            // If touched outside center
            if (dist > radius * 0.25f && dist < radius + 30) {
                selectorAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
                if (selectorAngle < 0) selectorAngle += 360;
                currentColor = getColorAtAngle(selectorAngle);
                centerPaint.setColor(currentColor);
                if (listener != null) {
                    listener.onColorSelected(currentColor);
                }
                invalidate();
                return true;
            }
        }
        return true;
    }

    private int getColorAtAngle(float angle) {
        float[] hsv = {angle, 0.90f, 0.95f};
        return Color.HSVToColor(hsv);
    }
}
