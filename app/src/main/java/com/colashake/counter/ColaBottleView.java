package com.colashake.counter;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class ColaBottleView extends View {

    private Paint bottlePaint;
    private Paint bottleHighlightPaint;
    private Paint colaPaint;
    private Paint bubblePaint;
    private Paint capPaint;
    private Paint labelPaint;
    private Paint labelTextPaint;

    private Path bottlePath;
    private Path colaPath;

    private float waveOffset = 0f;
    private float waveAmplitude = 5f;
    private float targetAmplitude = 5f;

    private ValueAnimator waveAnimator;

    private static final int BUBBLE_COUNT = 25;
    private float[] bubbleX;
    private float[] bubbleY;
    private float[] bubbleRadius;
    private float[] bubbleSpeed;

    private int viewWidth;
    private int viewHeight;

    // Bottle dimensions
    private float bottleCenterX;
    private float bottleTop;
    private float bottleBottom;
    private float neckTop;
    private float neckBottom;
    private float bodyTop;
    private float bottleWidth;
    private float neckWidth;

    public ColaBottleView(Context context) {
        super(context);
        init();
    }

    public ColaBottleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColaBottleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Bottle body paint - dark glass look
        bottlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bottlePaint.setStyle(Paint.Style.FILL);

        // Bottle highlight
        bottleHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bottleHighlightPaint.setColor(Color.argb(50, 255, 255, 255));
        bottleHighlightPaint.setStyle(Paint.Style.STROKE);
        bottleHighlightPaint.setStrokeWidth(3f);

        // Cola liquid paint
        colaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colaPaint.setStyle(Paint.Style.FILL);

        // Bubble paint
        bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubblePaint.setStyle(Paint.Style.FILL);

        // Bottle cap paint (red)
        capPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        capPaint.setColor(Color.rgb(200, 30, 30));
        capPaint.setStyle(Paint.Style.FILL);

        // Label paint (red background)
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.rgb(200, 20, 20));
        labelPaint.setStyle(Paint.Style.FILL);

        // Label text paint
        labelTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelTextPaint.setColor(Color.WHITE);
        labelTextPaint.setTextAlign(Paint.Align.CENTER);
        labelTextPaint.setFakeBoldText(true);

        bottlePath = new Path();
        colaPath = new Path();

        // Initialize bubbles
        bubbleX = new float[BUBBLE_COUNT];
        bubbleY = new float[BUBBLE_COUNT];
        bubbleRadius = new float[BUBBLE_COUNT];
        bubbleSpeed = new float[BUBBLE_COUNT];

        // Start wave animation
        waveAnimator = ValueAnimator.ofFloat(0f, (float) (2 * Math.PI));
        waveAnimator.setDuration(1500);
        waveAnimator.setRepeatCount(ValueAnimator.INFINITE);
        waveAnimator.setInterpolator(new LinearInterpolator());
        waveAnimator.addUpdateListener(animation -> {
            waveOffset = (float) animation.getAnimatedValue();
            updateBubbles();
            if (waveAmplitude > targetAmplitude + 0.3f) {
                waveAmplitude -= 0.3f;
            } else if (waveAmplitude < targetAmplitude - 0.3f) {
                waveAmplitude += 0.3f;
            }
            invalidate();
        });
        waveAnimator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;

        // Calculate bottle dimensions - classic cola bottle shape
        bottleCenterX = w / 2f;
        bottleWidth = w * 0.35f;
        neckWidth = bottleWidth * 0.25f;

        float totalHeight = h * 0.65f;
        bottleTop = (h - totalHeight) / 2f + h * 0.05f;
        bottleBottom = bottleTop + totalHeight;

        neckTop = bottleTop;
        neckBottom = bottleTop + totalHeight * 0.15f;
        bodyTop = neckBottom + totalHeight * 0.08f;

        // Bottle gradient (greenish glass)
        bottlePaint.setShader(new LinearGradient(
                bottleCenterX - bottleWidth / 2, 0,
                bottleCenterX + bottleWidth / 2, 0,
                new int[]{
                        Color.argb(200, 20, 60, 30),
                        Color.argb(220, 30, 80, 40),
                        Color.argb(200, 20, 60, 30)
                },
                new float[]{0f, 0.5f, 1f},
                Shader.TileMode.CLAMP
        ));

        // Cola gradient
        colaPaint.setShader(new LinearGradient(
                0, bodyTop,
                0, bottleBottom,
                new int[]{
                        Color.argb(230, 60, 20, 10),
                        Color.argb(250, 40, 10, 5),
                        Color.argb(255, 20, 5, 0)
                },
                new float[]{0f, 0.5f, 1f},
                Shader.TileMode.CLAMP
        ));

        labelTextPaint.setTextSize(bottleWidth * 0.18f);

        initBubbles();
    }

    private void initBubbles() {
        for (int i = 0; i < BUBBLE_COUNT; i++) {
            resetBubble(i, true);
        }
    }

    private void resetBubble(int i, boolean randomY) {
        float colaTop = bodyTop + (bottleBottom - bodyTop) * 0.15f;
        float range = bottleBottom - colaTop - 20;
        float y = randomY ? (colaTop + (float) Math.random() * range) : (bottleBottom - 10);

        float widthAtY = getBottleWidthAtY(y);
        bubbleX[i] = bottleCenterX - widthAtY / 2 + (float) Math.random() * widthAtY;
        bubbleY[i] = y;
        bubbleRadius[i] = 1.5f + (float) Math.random() * 4f;
        bubbleSpeed[i] = 0.6f + (float) Math.random() * 2f;
    }

    private float getBottleWidthAtY(float y) {
        if (y < neckBottom) {
            return neckWidth;
        } else if (y < bodyTop) {
            float t = (y - neckBottom) / (bodyTop - neckBottom);
            return neckWidth + (bottleWidth - neckWidth) * t;
        } else {
            // Body has slight curves
            float bodyProgress = (y - bodyTop) / (bottleBottom - bodyTop);
            float curve = (float) Math.sin(bodyProgress * Math.PI);
            return bottleWidth * (0.85f + 0.15f * curve);
        }
    }

    private void updateBubbles() {
        float colaTop = bodyTop + (bottleBottom - bodyTop) * 0.15f;
        for (int i = 0; i < BUBBLE_COUNT; i++) {
            bubbleY[i] -= bubbleSpeed[i];
            bubbleX[i] += (float) Math.sin(waveOffset + i) * 0.3f;
            if (bubbleY[i] < colaTop) {
                resetBubble(i, false);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (viewWidth == 0 || viewHeight == 0) return;

        canvas.drawColor(Color.BLACK);

        drawBottleBody(canvas);
        drawCola(canvas);
        drawBubbles(canvas);
        drawLabel(canvas);
        drawBottleCap(canvas);
        drawBottleHighlights(canvas);
    }

    private void drawBottleBody(Canvas canvas) {
        bottlePath.reset();

        // Start from left side of neck top
        float neckLeft = bottleCenterX - neckWidth / 2;
        float neckRight = bottleCenterX + neckWidth / 2;
        float bodyLeft = bottleCenterX - bottleWidth / 2;
        float bodyRight = bottleCenterX + bottleWidth / 2;

        // Neck
        bottlePath.moveTo(neckLeft, neckTop + 15);
        bottlePath.lineTo(neckLeft, neckBottom);

        // Left shoulder curve
        bottlePath.quadTo(neckLeft - 10, bodyTop - 20, bodyLeft, bodyTop);

        // Left body curve (classic contour)
        float midBody = bodyTop + (bottleBottom - bodyTop) * 0.5f;
        bottlePath.quadTo(bodyLeft - 15, midBody, bodyLeft + 5, bottleBottom - 20);

        // Bottom
        bottlePath.quadTo(bottleCenterX, bottleBottom + 5, bodyRight - 5, bottleBottom - 20);

        // Right body curve
        bottlePath.quadTo(bodyRight + 15, midBody, bodyRight, bodyTop);

        // Right shoulder
        bottlePath.quadTo(neckRight + 10, bodyTop - 20, neckRight, neckBottom);

        // Neck right
        bottlePath.lineTo(neckRight, neckTop + 15);

        bottlePath.close();

        canvas.drawPath(bottlePath, bottlePaint);
    }

    private void drawCola(Canvas canvas) {
        float colaTop = bodyTop + (bottleBottom - bodyTop) * 0.15f;
        float bodyLeft = bottleCenterX - bottleWidth / 2;
        float bodyRight = bottleCenterX + bottleWidth / 2;

        colaPath.reset();

        // Wave at top
        float leftAtTop = bottleCenterX - getBottleWidthAtY(colaTop) / 2 + 8;
        float rightAtTop = bottleCenterX + getBottleWidthAtY(colaTop) / 2 - 8;

        int segments = 20;
        for (int i = 0; i <= segments; i++) {
            float t = (float) i / segments;
            float x = leftAtTop + (rightAtTop - leftAtTop) * t;
            float waveY = colaTop + (float) Math.sin(waveOffset + t * 3 * Math.PI) * waveAmplitude;
            if (i == 0) {
                colaPath.moveTo(x, waveY);
            } else {
                colaPath.lineTo(x, waveY);
            }
        }

        // Right side down
        float midBody = bodyTop + (bottleBottom - bodyTop) * 0.5f;
        colaPath.quadTo(bodyRight + 12, midBody, bodyRight - 8, bottleBottom - 22);

        // Bottom
        colaPath.quadTo(bottleCenterX, bottleBottom + 2, bodyLeft + 8, bottleBottom - 22);

        // Left side up
        colaPath.quadTo(bodyLeft - 12, midBody, leftAtTop, colaTop);

        colaPath.close();
        canvas.drawPath(colaPath, colaPaint);
    }

    private void drawBubbles(Canvas canvas) {
        float colaTop = bodyTop + (bottleBottom - bodyTop) * 0.15f;
        for (int i = 0; i < BUBBLE_COUNT; i++) {
            if (bubbleY[i] > colaTop && bubbleY[i] < bottleBottom - 10) {
                float halfWidth = getBottleWidthAtY(bubbleY[i]) / 2 - 10;
                if (Math.abs(bubbleX[i] - bottleCenterX) < halfWidth) {
                    float distFromTop = (bubbleY[i] - colaTop) / (bottleBottom - colaTop);
                    int alpha = (int) (30 + 50 * distFromTop);
                    bubblePaint.setColor(Color.argb(alpha, 255, 255, 255));
                    canvas.drawCircle(bubbleX[i], bubbleY[i], bubbleRadius[i], bubblePaint);
                }
            }
        }
    }

    private void drawLabel(Canvas canvas) {
        // Red label in middle of bottle
        float labelTop = bodyTop + (bottleBottom - bodyTop) * 0.35f;
        float labelBottom = bodyTop + (bottleBottom - bodyTop) * 0.55f;
        float labelLeft = bottleCenterX - bottleWidth * 0.42f;
        float labelRight = bottleCenterX + bottleWidth * 0.42f;

        RectF labelRect = new RectF(labelLeft, labelTop, labelRight, labelBottom);
        canvas.drawRoundRect(labelRect, 5, 5, labelPaint);

        // Draw "COLA" text
        float textY = labelTop + (labelBottom - labelTop) / 2 + labelTextPaint.getTextSize() / 3;
        canvas.drawText("COLA", bottleCenterX, textY, labelTextPaint);

        // White wave decoration on label
        Paint wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint.setColor(Color.argb(100, 255, 255, 255));
        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setStrokeWidth(2f);

        Path wavePath = new Path();
        float waveY = labelTop + (labelBottom - labelTop) * 0.75f;
        wavePath.moveTo(labelLeft + 10, waveY);
        for (int i = 0; i < 5; i++) {
            float x1 = labelLeft + 10 + (labelRight - labelLeft - 20) * (i + 0.5f) / 5;
            float x2 = labelLeft + 10 + (labelRight - labelLeft - 20) * (i + 1f) / 5;
            wavePath.quadTo(x1, waveY - 8, x2, waveY);
        }
        canvas.drawPath(wavePath, wavePaint);
    }

    private void drawBottleCap(Canvas canvas) {
        float capWidth = neckWidth * 1.3f;
        float capHeight = (neckBottom - neckTop) * 0.5f;

        // Cap body
        RectF capRect = new RectF(
                bottleCenterX - capWidth / 2,
                neckTop,
                bottleCenterX + capWidth / 2,
                neckTop + capHeight
        );
        canvas.drawRoundRect(capRect, 5, 5, capPaint);

        // Cap ridges
        Paint ridgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ridgePaint.setColor(Color.rgb(150, 20, 20));
        ridgePaint.setStyle(Paint.Style.STROKE);
        ridgePaint.setStrokeWidth(2f);

        for (int i = 0; i < 4; i++) {
            float y = neckTop + capHeight * (0.2f + i * 0.2f);
            canvas.drawLine(
                    bottleCenterX - capWidth / 2 + 3, y,
                    bottleCenterX + capWidth / 2 - 3, y,
                    ridgePaint
            );
        }
    }

    private void drawBottleHighlights(Canvas canvas) {
        // Left highlight
        Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.argb(40, 255, 255, 255));
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(6f);

        float hlX = bottleCenterX - bottleWidth * 0.32f;
        canvas.drawLine(hlX, bodyTop + 30, hlX + 10, bottleBottom - 50, highlightPaint);

        // Smaller highlight
        highlightPaint.setStrokeWidth(3f);
        highlightPaint.setColor(Color.argb(25, 255, 255, 255));
        float hlX2 = bottleCenterX - bottleWidth * 0.22f;
        canvas.drawLine(hlX2, bodyTop + 50, hlX2 + 5, bottleBottom - 70, highlightPaint);
    }

    public void onShake() {
        waveAmplitude = 20f;
        targetAmplitude = 5f;

        for (int i = 0; i < BUBBLE_COUNT; i++) {
            if (Math.random() > 0.4) {
                resetBubble(i, true);
                bubbleSpeed[i] = 2f + (float) Math.random() * 3.5f;
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (waveAnimator != null) {
            waveAnimator.cancel();
        }
    }
}
