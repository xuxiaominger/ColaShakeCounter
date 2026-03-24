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

public class ColaGlassView extends View {

    private Paint glassPaint;
    private Paint glassHighlightPaint;
    private Paint colaPaint;
    private Paint bubblePaint;
    private Paint foamPaint;
    private Paint icePaint;

    private Path glassPath;
    private Path colaPath;

    private float waveOffset = 0f;
    private float waveAmplitude = 8f;
    private float targetAmplitude = 8f;

    private ValueAnimator waveAnimator;
    private ValueAnimator shakeAnimator;

    private static final int BUBBLE_COUNT = 20;
    private float[] bubbleX;
    private float[] bubbleY;
    private float[] bubbleRadius;
    private float[] bubbleSpeed;

    private int viewWidth;
    private int viewHeight;

    // Glass dimensions (will be calculated in onSizeChanged)
    private float glassLeft;
    private float glassRight;
    private float glassTop;
    private float glassBottom;
    private float glassMouthLeft;
    private float glassMouthRight;

    public ColaGlassView(Context context) {
        super(context);
        init();
    }

    public ColaGlassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColaGlassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Glass body paint - transparent with white edge
        glassPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glassPaint.setColor(Color.argb(60, 200, 220, 255));
        glassPaint.setStyle(Paint.Style.STROKE);
        glassPaint.setStrokeWidth(4f);

        // Glass highlight
        glassHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glassHighlightPaint.setColor(Color.argb(40, 255, 255, 255));
        glassHighlightPaint.setStyle(Paint.Style.FILL);

        // Cola liquid paint
        colaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colaPaint.setStyle(Paint.Style.FILL);

        // Bubble paint
        bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubblePaint.setColor(Color.argb(100, 255, 255, 255));
        bubblePaint.setStyle(Paint.Style.FILL);

        // Foam paint
        foamPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        foamPaint.setColor(Color.argb(200, 255, 230, 180));
        foamPaint.setStyle(Paint.Style.FILL);

        // Ice paint
        icePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        icePaint.setColor(Color.argb(80, 200, 230, 255));
        icePaint.setStyle(Paint.Style.FILL);

        glassPath = new Path();
        colaPath = new Path();

        // Initialize bubbles
        bubbleX = new float[BUBBLE_COUNT];
        bubbleY = new float[BUBBLE_COUNT];
        bubbleRadius = new float[BUBBLE_COUNT];
        bubbleSpeed = new float[BUBBLE_COUNT];

        // Start wave animation
        waveAnimator = ValueAnimator.ofFloat(0f, (float) (2 * Math.PI));
        waveAnimator.setDuration(2000);
        waveAnimator.setRepeatCount(ValueAnimator.INFINITE);
        waveAnimator.setInterpolator(new LinearInterpolator());
        waveAnimator.addUpdateListener(animation -> {
            waveOffset = (float) animation.getAnimatedValue();
            updateBubbles();
            // Smoothly reduce amplitude back to idle
            if (waveAmplitude > targetAmplitude + 0.5f) {
                waveAmplitude -= 0.5f;
            } else if (waveAmplitude < targetAmplitude - 0.5f) {
                waveAmplitude += 0.5f;
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

        // Calculate glass dimensions - centered, taking about 45% width
        float glassWidth = w * 0.42f;
        float glassHeight = h * 0.52f;
        float centerX = w / 2f;
        float centerY = h / 2f + h * 0.02f;

        // Glass is a trapezoid - wider at top (mouth) than bottom
        glassMouthLeft = centerX - glassWidth / 2f;
        glassMouthRight = centerX + glassWidth / 2f;
        glassTop = centerY - glassHeight / 2f;
        glassBottom = centerY + glassHeight / 2f;
        // Bottom is narrower
        glassLeft = centerX - glassWidth * 0.35f;
        glassRight = centerX + glassWidth * 0.35f;

        // Cola gradient
        colaPaint.setShader(new LinearGradient(
                0, glassTop + glassHeight * 0.2f,
                0, glassBottom,
                new int[]{
                        Color.argb(220, 80, 20, 5),
                        Color.argb(240, 50, 10, 2),
                        Color.argb(250, 30, 5, 0)
                },
                new float[]{0f, 0.5f, 1f},
                Shader.TileMode.CLAMP
        ));

        // Glass fill gradient (slight transparency for glass look)
        glassHighlightPaint.setShader(new LinearGradient(
                glassMouthLeft, glassTop,
                glassMouthRight, glassTop,
                new int[]{
                        Color.argb(5, 255, 255, 255),
                        Color.argb(25, 200, 220, 255),
                        Color.argb(5, 255, 255, 255)
                },
                new float[]{0f, 0.3f, 1f},
                Shader.TileMode.CLAMP
        ));

        // Initialize bubble positions
        initBubbles();
    }

    private void initBubbles() {
        for (int i = 0; i < BUBBLE_COUNT; i++) {
            resetBubble(i, true);
        }
    }

    private void resetBubble(int i, boolean randomY) {
        float colaTop = glassTop + (glassBottom - glassTop) * 0.22f;
        float range = glassBottom - colaTop;
        // Interpolate glass width at random Y
        float t = (float) Math.random();
        float y = randomY ? (colaTop + range * t) : glassBottom;
        float widthAtY = getGlassWidthAtY(y);
        float centerX = (glassMouthLeft + glassMouthRight) / 2f;

        bubbleX[i] = centerX - widthAtY / 2f + (float) Math.random() * widthAtY;
        bubbleY[i] = y;
        bubbleRadius[i] = 2f + (float) Math.random() * 5f;
        bubbleSpeed[i] = 0.8f + (float) Math.random() * 2.5f;
    }

    private float getGlassWidthAtY(float y) {
        float fraction = (y - glassTop) / (glassBottom - glassTop);
        float leftAtY = glassMouthLeft + (glassLeft - glassMouthLeft) * fraction;
        float rightAtY = glassMouthRight + (glassRight - glassMouthRight) * fraction;
        return rightAtY - leftAtY;
    }

    private float getGlassLeftAtY(float y) {
        float fraction = (y - glassTop) / (glassBottom - glassTop);
        return glassMouthLeft + (glassLeft - glassMouthLeft) * fraction;
    }

    private float getGlassRightAtY(float y) {
        float fraction = (y - glassTop) / (glassBottom - glassTop);
        return glassMouthRight + (glassRight - glassMouthRight) * fraction;
    }

    private void updateBubbles() {
        float colaTop = glassTop + (glassBottom - glassTop) * 0.22f;
        for (int i = 0; i < BUBBLE_COUNT; i++) {
            bubbleY[i] -= bubbleSpeed[i];
            // Slight horizontal wobble
            bubbleX[i] += (float) Math.sin(waveOffset + i) * 0.5f;
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

        drawColaLiquid(canvas);
        drawBubbles(canvas);
        drawFoam(canvas);
        drawIceCubes(canvas);
        drawGlass(canvas);
        drawGlassHighlight(canvas);
    }

    private void drawGlass(Canvas canvas) {
        glassPath.reset();
        // Left side
        glassPath.moveTo(glassMouthLeft, glassTop);
        glassPath.lineTo(glassLeft, glassBottom);
        // Bottom - slight curve
        float bottomCenterX = (glassLeft + glassRight) / 2f;
        glassPath.quadTo(bottomCenterX, glassBottom + 12, glassRight, glassBottom);
        // Right side
        glassPath.lineTo(glassMouthRight, glassTop);

        canvas.drawPath(glassPath, glassPaint);

        // Draw glass rim at top
        Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rimPaint.setColor(Color.argb(90, 200, 220, 255));
        rimPaint.setStyle(Paint.Style.STROKE);
        rimPaint.setStrokeWidth(6f);
        canvas.drawLine(glassMouthLeft - 2, glassTop, glassMouthRight + 2, glassTop, rimPaint);
    }

    private void drawGlassHighlight(Canvas canvas) {
        // Vertical highlight strip on the left side of glass
        Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.argb(30, 255, 255, 255));
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(8f);

        float hx1 = glassMouthLeft + (glassMouthRight - glassMouthLeft) * 0.15f;
        float hx2 = glassLeft + (glassRight - glassLeft) * 0.15f;
        canvas.drawLine(hx1, glassTop + 20, hx2, glassBottom - 20, highlightPaint);

        // Smaller highlight
        highlightPaint.setColor(Color.argb(18, 255, 255, 255));
        highlightPaint.setStrokeWidth(4f);
        float hx3 = glassMouthLeft + (glassMouthRight - glassMouthLeft) * 0.22f;
        float hx4 = glassLeft + (glassRight - glassLeft) * 0.22f;
        canvas.drawLine(hx3, glassTop + 30, hx4, glassBottom - 30, highlightPaint);
    }

    private void drawColaLiquid(Canvas canvas) {
        float colaTop = glassTop + (glassBottom - glassTop) * 0.22f;
        float leftAtColaTop = getGlassLeftAtY(colaTop);
        float rightAtColaTop = getGlassRightAtY(colaTop);

        colaPath.reset();

        // Wave at top of cola
        colaPath.moveTo(leftAtColaTop + 4, colaTop);
        int segments = 30;
        for (int i = 0; i <= segments; i++) {
            float fraction = (float) i / segments;
            float x = leftAtColaTop + 4 + (rightAtColaTop - leftAtColaTop - 8) * fraction;
            float waveY = colaTop + (float) Math.sin(waveOffset + fraction * 4 * Math.PI) * waveAmplitude;
            if (i == 0) {
                colaPath.moveTo(x, waveY);
            } else {
                colaPath.lineTo(x, waveY);
            }
        }

        // Right side down
        colaPath.lineTo(glassRight - 3, glassBottom - 2);
        // Bottom
        float bottomCenterX = (glassLeft + glassRight) / 2f;
        colaPath.quadTo(bottomCenterX, glassBottom + 10, glassLeft + 3, glassBottom - 2);
        // Left side up
        colaPath.close();

        canvas.drawPath(colaPath, colaPaint);
    }

    private void drawBubbles(Canvas canvas) {
        float colaTop = glassTop + (glassBottom - glassTop) * 0.22f;
        for (int i = 0; i < BUBBLE_COUNT; i++) {
            if (bubbleY[i] > colaTop && bubbleY[i] < glassBottom) {
                float leftEdge = getGlassLeftAtY(bubbleY[i]) + 5;
                float rightEdge = getGlassRightAtY(bubbleY[i]) - 5;
                if (bubbleX[i] > leftEdge && bubbleX[i] < rightEdge) {
                    // Bubbles become more transparent near top
                    float distFromTop = (bubbleY[i] - colaTop) / (glassBottom - colaTop);
                    int alpha = (int) (40 + 60 * distFromTop);
                    bubblePaint.setColor(Color.argb(alpha, 255, 255, 255));
                    canvas.drawCircle(bubbleX[i], bubbleY[i], bubbleRadius[i], bubblePaint);
                }
            }
        }
    }

    private void drawFoam(Canvas canvas) {
        float colaTop = glassTop + (glassBottom - glassTop) * 0.22f;
        float leftAtColaTop = getGlassLeftAtY(colaTop);
        float rightAtColaTop = getGlassRightAtY(colaTop);

        // Draw foam layer - series of overlapping circles
        float foamY = colaTop;
        float foamWidth = rightAtColaTop - leftAtColaTop - 8;
        int foamBubbles = 12;
        for (int i = 0; i < foamBubbles; i++) {
            float fraction = (float) i / (foamBubbles - 1);
            float x = leftAtColaTop + 4 + foamWidth * fraction;
            float y = foamY + (float) Math.sin(waveOffset + fraction * 4 * Math.PI) * waveAmplitude;
            float r = 8 + (float) Math.sin(i * 1.5) * 3;

            foamPaint.setColor(Color.argb(120, 255, 230, 180));
            canvas.drawCircle(x, y - 2, r, foamPaint);

            foamPaint.setColor(Color.argb(80, 255, 240, 200));
            canvas.drawCircle(x + 3, y - 5, r * 0.6f, foamPaint);
        }
    }

    private void drawIceCubes(Canvas canvas) {
        float colaTop = glassTop + (glassBottom - glassTop) * 0.22f;
        float centerX = (glassMouthLeft + glassMouthRight) / 2f;

        // Draw 3 ice cubes at different positions
        drawIceCube(canvas, centerX - 30, colaTop + 40, 28, 22,
                (float) Math.sin(waveOffset * 0.5) * 5);
        drawIceCube(canvas, centerX + 25, colaTop + 30, 24, 20,
                (float) Math.sin(waveOffset * 0.5 + 1) * 5);
        drawIceCube(canvas, centerX - 5, colaTop + 65, 22, 18,
                (float) Math.sin(waveOffset * 0.5 + 2) * 3);
    }

    private void drawIceCube(Canvas canvas, float cx, float cy, float w, float h, float waveY) {
        float y = cy + waveY;
        RectF rect = new RectF(cx - w / 2, y - h / 2, cx + w / 2, y + h / 2);

        // Ice cube body
        Paint iceBodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        iceBodyPaint.setColor(Color.argb(50, 180, 220, 255));
        iceBodyPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rect, 4, 4, iceBodyPaint);

        // Ice cube border
        Paint iceBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        iceBorderPaint.setColor(Color.argb(70, 200, 230, 255));
        iceBorderPaint.setStyle(Paint.Style.STROKE);
        iceBorderPaint.setStrokeWidth(1.5f);
        canvas.drawRoundRect(rect, 4, 4, iceBorderPaint);

        // Small highlight
        Paint iceHighlight = new Paint(Paint.ANTI_ALIAS_FLAG);
        iceHighlight.setColor(Color.argb(60, 255, 255, 255));
        canvas.drawRoundRect(
                new RectF(cx - w / 4, y - h / 3, cx, y - h / 6),
                2, 2, iceHighlight);
    }

    public void onShake() {
        // Increase wave amplitude temporarily
        waveAmplitude = 30f;
        targetAmplitude = 8f;

        // Randomize some bubbles for more activity
        for (int i = 0; i < BUBBLE_COUNT; i++) {
            if (Math.random() > 0.5) {
                resetBubble(i, true);
                bubbleSpeed[i] = 2f + (float) Math.random() * 4f;
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
