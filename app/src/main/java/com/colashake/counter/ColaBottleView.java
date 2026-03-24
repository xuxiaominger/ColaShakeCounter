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
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

public class ColaBottleView extends View {

    private Paint bottlePaint;
    private Paint bottleOutlinePaint;
    private Paint bottleHighlightPaint;
    private Paint colaPaint;
    private Paint bubblePaint;
    private Paint capPaint;
    private Paint capTopPaint;
    private Paint labelPaint;
    private Paint labelTextPaint;
    private Paint labelTextCnPaint;

    private Path bottlePath;
    private Path colaPath;

    private float waveOffset = 0f;
    private float waveAmplitude = 4f;
    private float targetAmplitude = 4f;

    // Shake animation
    private float shakeAngle = 0f;
    private float shakeOffsetX = 0f;
    private ValueAnimator shakeAnimator;

    private ValueAnimator waveAnimator;

    private static final int BUBBLE_COUNT = 30;
    private float[] bubbleX;
    private float[] bubbleY;
    private float[] bubbleRadius;
    private float[] bubbleSpeed;

    private int viewWidth;
    private int viewHeight;

    // Bottle dimensions - Coca-Cola style
    private float bottleCenterX;
    private float bottleTop;
    private float bottleBottom;
    private float neckTop;
    private float neckBottom;
    private float shoulderTop;
    private float bodyTop;
    private float waistY;
    private float bottleWidth;
    private float neckWidth;
    private float waistWidth;

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
        // Bottle body - transparent plastic
        bottlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bottlePaint.setStyle(Paint.Style.FILL);

        // Bottle outline
        bottleOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bottleOutlinePaint.setColor(Color.argb(60, 100, 100, 100));
        bottleOutlinePaint.setStyle(Paint.Style.STROKE);
        bottleOutlinePaint.setStrokeWidth(2f);

        // Bottle highlight
        bottleHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bottleHighlightPaint.setColor(Color.argb(80, 255, 255, 255));
        bottleHighlightPaint.setStyle(Paint.Style.STROKE);
        bottleHighlightPaint.setStrokeWidth(4f);

        // Cola liquid paint
        colaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colaPaint.setStyle(Paint.Style.FILL);

        // Bubble paint
        bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubblePaint.setStyle(Paint.Style.FILL);

        // Bottle cap (black)
        capPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        capPaint.setColor(Color.rgb(30, 30, 30));
        capPaint.setStyle(Paint.Style.FILL);

        capTopPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        capTopPaint.setColor(Color.rgb(50, 50, 50));
        capTopPaint.setStyle(Paint.Style.FILL);

        // Label paint (red)
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.rgb(218, 41, 28));
        labelPaint.setStyle(Paint.Style.FILL);

        // Label text paint (Chinese style)
        labelTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelTextPaint.setColor(Color.WHITE);
        labelTextPaint.setTextAlign(Paint.Align.CENTER);
        labelTextPaint.setFakeBoldText(true);

        labelTextCnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelTextCnPaint.setColor(Color.BLACK);
        labelTextCnPaint.setTextAlign(Paint.Align.CENTER);
        labelTextCnPaint.setFakeBoldText(true);

        bottlePath = new Path();
        colaPath = new Path();

        // Initialize bubbles
        bubbleX = new float[BUBBLE_COUNT];
        bubbleY = new float[BUBBLE_COUNT];
        bubbleRadius = new float[BUBBLE_COUNT];
        bubbleSpeed = new float[BUBBLE_COUNT];

        // Start wave animation
        waveAnimator = ValueAnimator.ofFloat(0f, (float) (2 * Math.PI));
        waveAnimator.setDuration(1200);
        waveAnimator.setRepeatCount(ValueAnimator.INFINITE);
        waveAnimator.setInterpolator(new LinearInterpolator());
        waveAnimator.addUpdateListener(animation -> {
            waveOffset = (float) animation.getAnimatedValue();
            updateBubbles();
            if (waveAmplitude > targetAmplitude + 0.2f) {
                waveAmplitude -= 0.2f;
            } else if (waveAmplitude < targetAmplitude - 0.2f) {
                waveAmplitude += 0.2f;
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

        // Calculate bottle dimensions - Coca-Cola classic contour bottle
        bottleCenterX = w * 0.5f;
        bottleWidth = w * 0.38f;
        neckWidth = bottleWidth * 0.22f;
        waistWidth = bottleWidth * 0.75f;

        float totalHeight = h * 0.7f;
        bottleTop = (h - totalHeight) / 2f;
        bottleBottom = bottleTop + totalHeight;

        neckTop = bottleTop;
        neckBottom = bottleTop + totalHeight * 0.12f;
        shoulderTop = neckBottom + totalHeight * 0.03f;
        bodyTop = shoulderTop + totalHeight * 0.08f;
        waistY = bodyTop + (bottleBottom - bodyTop) * 0.45f;

        // Transparent bottle gradient
        bottlePaint.setShader(new LinearGradient(
                bottleCenterX - bottleWidth / 2, 0,
                bottleCenterX + bottleWidth / 2, 0,
                new int[]{
                        Color.argb(30, 200, 200, 200),
                        Color.argb(50, 255, 255, 255),
                        Color.argb(30, 200, 200, 200)
                },
                new float[]{0f, 0.3f, 1f},
                Shader.TileMode.CLAMP
        ));

        // Cola gradient (dark brown)
        colaPaint.setShader(new LinearGradient(
                0, waistY - 50,
                0, bottleBottom,
                new int[]{
                        Color.argb(200, 80, 40, 20),
                        Color.argb(230, 50, 25, 10),
                        Color.argb(250, 30, 15, 5)
                },
                new float[]{0f, 0.4f, 1f},
                Shader.TileMode.CLAMP
        ));

        labelTextPaint.setTextSize(bottleWidth * 0.22f);
        labelTextCnPaint.setTextSize(bottleWidth * 0.13f);

        initBubbles();
    }

    private void initBubbles() {
        for (int i = 0; i < BUBBLE_COUNT; i++) {
            resetBubble(i, true);
        }
    }

    private void resetBubble(int i, boolean randomY) {
        float colaTop = waistY - 30;
        float range = bottleBottom - colaTop - 30;
        float y = randomY ? (colaTop + (float) Math.random() * range) : (bottleBottom - 15);

        float widthAtY = getBottleWidthAtY(y) - 20;
        bubbleX[i] = bottleCenterX - widthAtY / 2 + (float) Math.random() * widthAtY;
        bubbleY[i] = y;
        bubbleRadius[i] = 1f + (float) Math.random() * 3.5f;
        bubbleSpeed[i] = 0.5f + (float) Math.random() * 1.8f;
    }

    private float getBottleWidthAtY(float y) {
        if (y < neckBottom) {
            return neckWidth;
        } else if (y < shoulderTop) {
            float t = (y - neckBottom) / (shoulderTop - neckBottom);
            return neckWidth + (neckWidth * 1.5f - neckWidth) * t;
        } else if (y < bodyTop) {
            float t = (y - shoulderTop) / (bodyTop - shoulderTop);
            return neckWidth * 1.5f + (bottleWidth - neckWidth * 1.5f) * t;
        } else if (y < waistY) {
            float t = (y - bodyTop) / (waistY - bodyTop);
            // Curve in toward waist
            return bottleWidth - (bottleWidth - waistWidth) * (float)Math.sin(t * Math.PI / 2);
        } else {
            float t = (y - waistY) / (bottleBottom - waistY);
            // Curve out from waist then in at bottom
            float bulge = (float)Math.sin(t * Math.PI) * 0.15f;
            return waistWidth + (bottleWidth * 0.9f - waistWidth) * t * (1 - t * 0.3f) + bottleWidth * bulge;
        }
    }

    private void updateBubbles() {
        float colaTop = waistY - 30;
        for (int i = 0; i < BUBBLE_COUNT; i++) {
            bubbleY[i] -= bubbleSpeed[i];
            bubbleX[i] += (float) Math.sin(waveOffset * 2 + i) * 0.4f + shakeOffsetX * 0.1f;
            if (bubbleY[i] < colaTop) {
                resetBubble(i, false);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (viewWidth == 0 || viewHeight == 0) return;

        // Apply shake transformation
        canvas.save();
        canvas.translate(shakeOffsetX, 0);
        canvas.rotate(shakeAngle, bottleCenterX, bottleBottom);

        drawBottleBody(canvas);
        drawCola(canvas);
        drawBubbles(canvas);
        drawBottleHighlights(canvas);
        drawLabel(canvas);
        drawBottleCap(canvas);

        canvas.restore();
    }

    private void drawBottleBody(Canvas canvas) {
        bottlePath.reset();

        float neckLeft = bottleCenterX - neckWidth / 2;
        float neckRight = bottleCenterX + neckWidth / 2;

        // Start from neck
        bottlePath.moveTo(neckLeft, neckBottom);

        // Left shoulder curve
        float shoulderLeftX = bottleCenterX - bottleWidth / 2;
        bottlePath.quadTo(neckLeft - 5, shoulderTop, shoulderLeftX, bodyTop);

        // Left body - waist curve (Coca-Cola contour)
        float waistLeftX = bottleCenterX - waistWidth / 2;
        bottlePath.quadTo(shoulderLeftX - 8, (bodyTop + waistY) / 2, waistLeftX, waistY);

        // Left lower body - bulge out then in
        float lowerBulgeY = waistY + (bottleBottom - waistY) * 0.5f;
        float bottomLeftX = bottleCenterX - bottleWidth * 0.42f;
        bottlePath.quadTo(waistLeftX - 15, lowerBulgeY, bottomLeftX, bottleBottom - 10);

        // Bottom curve
        bottlePath.quadTo(bottleCenterX, bottleBottom + 5,
                         bottleCenterX + bottleWidth * 0.42f, bottleBottom - 10);

        // Right lower body
        float waistRightX = bottleCenterX + waistWidth / 2;
        bottlePath.quadTo(waistRightX + 15, lowerBulgeY, waistRightX, waistY);

        // Right body - waist curve
        float shoulderRightX = bottleCenterX + bottleWidth / 2;
        bottlePath.quadTo(shoulderRightX + 8, (bodyTop + waistY) / 2, shoulderRightX, bodyTop);

        // Right shoulder
        bottlePath.quadTo(neckRight + 5, shoulderTop, neckRight, neckBottom);

        // Neck
        bottlePath.lineTo(neckRight, neckTop + 20);
        bottlePath.lineTo(neckLeft, neckTop + 20);
        bottlePath.lineTo(neckLeft, neckBottom);

        bottlePath.close();

        canvas.drawPath(bottlePath, bottlePaint);
        canvas.drawPath(bottlePath, bottleOutlinePaint);
    }

    private void drawCola(Canvas canvas) {
        float colaTop = waistY - 30;

        colaPath.reset();

        // Wave at top of cola
        float leftAtTop = bottleCenterX - getBottleWidthAtY(colaTop) / 2 + 10;
        float rightAtTop = bottleCenterX + getBottleWidthAtY(colaTop) / 2 - 10;

        int segments = 25;
        for (int i = 0; i <= segments; i++) {
            float t = (float) i / segments;
            float x = leftAtTop + (rightAtTop - leftAtTop) * t;
            float waveY = colaTop + (float) Math.sin(waveOffset + t * 4 * Math.PI) * waveAmplitude;
            // Add extra wave from shake
            waveY += (float) Math.sin(waveOffset * 3 + t * 2 * Math.PI) * waveAmplitude * 0.5f;
            if (i == 0) {
                colaPath.moveTo(x, waveY);
            } else {
                colaPath.lineTo(x, waveY);
            }
        }

        // Right side down following bottle contour
        float waistRightX = bottleCenterX + waistWidth / 2 - 8;
        float lowerBulgeY = waistY + (bottleBottom - waistY) * 0.5f;
        colaPath.quadTo(waistRightX + 12, lowerBulgeY,
                       bottleCenterX + bottleWidth * 0.40f, bottleBottom - 12);

        // Bottom
        colaPath.quadTo(bottleCenterX, bottleBottom + 3,
                       bottleCenterX - bottleWidth * 0.40f, bottleBottom - 12);

        // Left side up
        float waistLeftX = bottleCenterX - waistWidth / 2 + 8;
        colaPath.quadTo(waistLeftX - 12, lowerBulgeY, leftAtTop, colaTop);

        colaPath.close();
        canvas.drawPath(colaPath, colaPaint);
    }

    private void drawBubbles(Canvas canvas) {
        float colaTop = waistY - 30;
        for (int i = 0; i < BUBBLE_COUNT; i++) {
            if (bubbleY[i] > colaTop && bubbleY[i] < bottleBottom - 15) {
                float halfWidth = getBottleWidthAtY(bubbleY[i]) / 2 - 12;
                if (Math.abs(bubbleX[i] - bottleCenterX) < halfWidth) {
                    float distFromTop = (bubbleY[i] - colaTop) / (bottleBottom - colaTop);
                    int alpha = (int) (25 + 45 * distFromTop);
                    bubblePaint.setColor(Color.argb(alpha, 255, 255, 255));
                    canvas.drawCircle(bubbleX[i], bubbleY[i], bubbleRadius[i], bubblePaint);
                }
            }
        }
    }

    private void drawLabel(Canvas canvas) {
        // Red label in upper-middle area (like Coca-Cola)
        float labelTop = bodyTop + (waistY - bodyTop) * 0.15f;
        float labelBottom = bodyTop + (waistY - bodyTop) * 0.75f;
        float labelWidth = bottleWidth * 0.85f;
        float labelLeft = bottleCenterX - labelWidth / 2;
        float labelRight = bottleCenterX + labelWidth / 2;

        // Draw curved label (follows bottle shape)
        Path labelPath = new Path();
        float labelLeftEdge = bottleCenterX - getBottleWidthAtY(labelTop) / 2 + 8;
        float labelRightEdge = bottleCenterX + getBottleWidthAtY(labelTop) / 2 - 8;

        labelPath.moveTo(labelLeftEdge, labelTop);
        labelPath.lineTo(labelRightEdge, labelTop);

        float labelBottomLeftEdge = bottleCenterX - getBottleWidthAtY(labelBottom) / 2 + 8;
        float labelBottomRightEdge = bottleCenterX + getBottleWidthAtY(labelBottom) / 2 - 8;

        labelPath.lineTo(labelBottomRightEdge, labelBottom);
        labelPath.lineTo(labelBottomLeftEdge, labelBottom);
        labelPath.close();

        canvas.drawPath(labelPath, labelPaint);

        // Draw "可口可乐" text
        float textY = labelTop + (labelBottom - labelTop) * 0.45f;
        canvas.drawText("可口可乐", bottleCenterX, textY, labelTextCnPaint);

        // Draw smaller text below
        float subTextY = labelTop + (labelBottom - labelTop) * 0.72f;
        Paint smallTextPaint = new Paint(labelTextCnPaint);
        smallTextPaint.setTextSize(labelTextCnPaint.getTextSize() * 0.6f);
        smallTextPaint.setColor(Color.WHITE);
        canvas.drawText("无糖", bottleCenterX, subTextY, smallTextPaint);

        // Wave decoration at bottom of label
        Paint wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint.setColor(Color.argb(60, 255, 255, 255));
        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setStrokeWidth(2f);

        Path wavePath = new Path();
        float waveY = labelBottom - (labelBottom - labelTop) * 0.15f;
        wavePath.moveTo(labelBottomLeftEdge + 15, waveY);
        for (int i = 0; i < 4; i++) {
            float x1 = labelBottomLeftEdge + 15 + (labelBottomRightEdge - labelBottomLeftEdge - 30) * (i + 0.5f) / 4;
            float x2 = labelBottomLeftEdge + 15 + (labelBottomRightEdge - labelBottomLeftEdge - 30) * (i + 1f) / 4;
            wavePath.quadTo(x1, waveY - 6, x2, waveY);
        }
        canvas.drawPath(wavePath, wavePaint);
    }

    private void drawBottleCap(Canvas canvas) {
        float capWidth = neckWidth * 1.4f;
        float capHeight = (neckBottom - neckTop) * 0.7f;

        // Cap body (black)
        RectF capRect = new RectF(
                bottleCenterX - capWidth / 2,
                neckTop,
                bottleCenterX + capWidth / 2,
                neckTop + capHeight
        );
        canvas.drawRoundRect(capRect, 4, 4, capPaint);

        // Cap top (slightly lighter)
        RectF capTopRect = new RectF(
                bottleCenterX - capWidth / 2 + 3,
                neckTop + 2,
                bottleCenterX + capWidth / 2 - 3,
                neckTop + capHeight * 0.3f
        );
        canvas.drawRoundRect(capTopRect, 3, 3, capTopPaint);

        // Cap ring at bottom
        Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setColor(Color.rgb(60, 60, 60));
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(3f);
        canvas.drawLine(bottleCenterX - capWidth / 2 + 2, neckTop + capHeight - 2,
                       bottleCenterX + capWidth / 2 - 2, neckTop + capHeight - 2, ringPaint);
    }

    private void drawBottleHighlights(Canvas canvas) {
        // Left vertical highlight
        float hlX = bottleCenterX - bottleWidth * 0.35f;
        Path hlPath = new Path();
        hlPath.moveTo(hlX, bodyTop + 20);
        hlPath.quadTo(hlX - 10, waistY, hlX - 5, bottleBottom - 40);

        bottleHighlightPaint.setColor(Color.argb(50, 255, 255, 255));
        bottleHighlightPaint.setStrokeWidth(5f);
        canvas.drawPath(hlPath, bottleHighlightPaint);

        // Second smaller highlight
        float hlX2 = bottleCenterX - bottleWidth * 0.25f;
        Path hlPath2 = new Path();
        hlPath2.moveTo(hlX2, bodyTop + 40);
        hlPath2.quadTo(hlX2 - 5, waistY, hlX2, bottleBottom - 60);

        bottleHighlightPaint.setColor(Color.argb(30, 255, 255, 255));
        bottleHighlightPaint.setStrokeWidth(3f);
        canvas.drawPath(hlPath2, bottleHighlightPaint);

        // Neck highlight
        canvas.drawLine(bottleCenterX - neckWidth / 3, neckTop + 25,
                       bottleCenterX - neckWidth / 3, neckBottom - 5, bottleHighlightPaint);
    }

    public void onShake() {
        // Increase wave amplitude
        waveAmplitude = 18f;
        targetAmplitude = 4f;

        // Start shake animation
        if (shakeAnimator != null) {
            shakeAnimator.cancel();
        }

        shakeAnimator = ValueAnimator.ofFloat(0f, 1f);
        shakeAnimator.setDuration(600);
        shakeAnimator.setInterpolator(new DecelerateInterpolator());
        shakeAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            float intensity = 1f - progress;
            shakeAngle = (float) Math.sin(progress * 8 * Math.PI) * 5f * intensity;
            shakeOffsetX = (float) Math.sin(progress * 10 * Math.PI) * 15f * intensity;
        });
        shakeAnimator.start();

        // Reset more bubbles with higher speed
        for (int i = 0; i < BUBBLE_COUNT; i++) {
            if (Math.random() > 0.3) {
                resetBubble(i, true);
                bubbleSpeed[i] = 2f + (float) Math.random() * 3f;
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (waveAnimator != null) {
            waveAnimator.cancel();
        }
        if (shakeAnimator != null) {
            shakeAnimator.cancel();
        }
    }
}
