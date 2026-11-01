package com.example.simplecalendar;

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
    private float centerX, centerY;
    private float radius;
    private float baseRadius;
    private float selectorAngle = 0;
    private OnColorSelectedListener listener;
    private ScaleGestureDetector scaleDetector;
    private float scaleFactor = 1.0f;

    public ColorWheelView(Context context) {
        this(context, null);
    }

    public ColorWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        wheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorPaint.setColor(Color.WHITE);
        selectorPaint.setStyle(Paint.Style.STROKE);
        selectorPaint.setStrokeWidth(4);
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override public boolean onScale(ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(0.8f, Math.min(scaleFactor, 1.8f));
                invalidate();
                return true;
            }
        });
    }

    public void setOnColorSelectedListener(OnColorSelectedListener l) {
        this.listener = l;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
        baseRadius = Math.min(w, h) / 2f - 20;
        radius = baseRadius;
        int[] colors = {Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED};
        SweepGradient gradient = new SweepGradient(centerX, centerY, colors, null);
        wheelPaint.setShader(gradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        radius = baseRadius * scaleFactor;
        canvas.drawCircle(centerX, centerY, radius, wheelPaint);
        // selector
        double angle = Math.toRadians(selectorAngle);
        float sx = (float) (centerX + (radius - 10) * Math.cos(angle));
        float sy = (float) (centerY + (radius - 10) * Math.sin(angle));
        canvas.drawCircle(sx, sy, 8, selectorPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            float dx = event.getX() - centerX;
            float dy = event.getY() - centerY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist < radius + 15) {
                selectorAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
                if (selectorAngle < 0) selectorAngle += 360;
                int color = getColorAtAngle(selectorAngle);
                if (listener != null) listener.onColorSelected(color);
                invalidate();
                return true;
            }
        }
        return true;
    }

    private int getColorAtAngle(float angle) {
        float[] hsv = {angle, 1f, 1f};
        return Color.HSVToColor(hsv);
    }

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }
}
