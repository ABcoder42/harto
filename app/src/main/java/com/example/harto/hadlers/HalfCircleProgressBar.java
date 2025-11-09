package com.example.harto.hadlers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class HalfCircleProgressBar extends View {

    private static final int DEFAULT_STROKE_WIDTH = 25;
    private static final float TOTAL_SWEEP = 250f;
    private static final float START_ANGLE = 145f;
    private static final int TICK_COUNT = 50;

    private int progress = 0;
    private int strokeWidth = DEFAULT_STROKE_WIDTH;

    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint tickPaint;

    private RectF arcBounds;

    public HalfCircleProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.WHITE);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(Color.parseColor("#4178e1"));
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setColor(Color.parseColor("#4178e1"));
        tickPaint.setStrokeWidth(2.5f);

        arcBounds = new RectF();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        int size = Math.min(width, height);
        arcBounds.set(strokeWidth, strokeWidth, size - strokeWidth, size - strokeWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int size = Math.min(getWidth(), getHeight());
        float centerX = size / 2f;
        float centerY = size / 2f;
        float innerRadius = (size / 2.1f) - strokeWidth * 2;


        canvas.drawArc(arcBounds, START_ANGLE, TOTAL_SWEEP, false, backgroundPaint);


        float sweepAngle = (TOTAL_SWEEP * progress) / 100f;
        canvas.drawArc(arcBounds, START_ANGLE, sweepAngle, false, progressPaint);


        float angleStep = TOTAL_SWEEP / TICK_COUNT;
        for (int i = 0; i < TICK_COUNT; i++) {
            float angle = (float) Math.toRadians(START_ANGLE + (i * angleStep));

            float startX = (float) (centerX + innerRadius * Math.cos(angle));
            float startY = (float) (centerY + innerRadius * Math.sin(angle));
            float stopX = (float) (centerX + (innerRadius + 20) * Math.cos(angle));
            float stopY = (float) (centerY + (innerRadius + 20) * Math.sin(angle));

            canvas.drawLine(startX, startY, stopX, stopY, tickPaint);
        }
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(progress, 100)); // Clamp
        invalidate();
    }
}
