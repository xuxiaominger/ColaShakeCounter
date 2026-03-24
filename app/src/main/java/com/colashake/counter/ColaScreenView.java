package com.colashake.counter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * Fullscreen cola liquid view that responds to accelerometer data
 * The entire screen acts as the inside of a cola bottle
 */
public class ColaScreenView extends View {

    private Paint colaPaint;
    private Paint bubblePaint;
    private Paint foamPaint;
    private Paint highlightPaint;
    private Path colaPath;

    // Accelerometer data for liquid movement
    private float accelX = 0f;
    private float accelY = 0f;
    private float smoothAccelX = 0f;
    private float smoothAccelY = 0f;

    // Wave parameters
    private float wavePhase = 0f;
    private float waveAmplitude = 20f;
    private float targetAmplitude = 20f;
    private float liquidLevel = 0.75f; // 75% filled

    // Bubbles
    private static final int BUBBLE_COUNT = 80;
    private float[] bubbleX;
    private float[] bubbleY;
    private float[] bubbleRadius;
    private float[] bubbleSpeed;
    private float[] bubblePhase;

    private int viewWidth;
    private int viewHeight;

    // Animation
    private long lastUpdateTime;

    public ColaScreenView(Context context) {
        super(context);
        init();
    }

    public ColaScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColaScreenView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Cola liquid paint - dark brown
        colaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colaPaint.setStyle(Paint.Style.FILL);

        // Bubble paint
        bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubblePaint.setStyle(Paint.Style.FILL);

        // Foam paint - lighter brown/tan
        foamPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        foamPaint.setColor(Color.argb(180, 180, 140, 100));
        foamPaint.setStyle(Paint.Style.FILL);

        // Highlight paint
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.argb(30, 255, 255, 255));
        highlightPaint.setStyle(Paint.Style.FILL);

        colaPath = new Path();

        // Initialize bubble arrays
        bubbleX = new float[BUBBLE_COUNT];
        bubbleY = new float[BUBBLE_COUNT];
        bubbleRadius = new float[BUBBLE_COUNT];
        bubbleSpeed = new float[BUBBLE_COUNT];
        bubblePhase = new float[BUBBLE_COUNT];

        lastUpdateTime = System.currentTimeMillis();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;

        // Create cola gradient - darker at bottom
        colaPaint.setShader(new LinearGradient(
                0, h * 0.2f,
                0, h,
                new int[]{
                        Color.argb(220, 100, 50, 25),
                        Color.argb(240, 60, 30, 15),
                        Color.argb(255, 35, 18, 8)
                },
                new float[]{0f, 0.4f, 1f},
                Shader.TileMode.CLAMP
        ));

        initBubbles();
    }

    private void initBubbles() {
        if (bubbleX == null || viewWidth == 0 || viewHeight == 0) return;
        for (int i = 0; i < BUBBLE_COUNT; i++) {
            resetBubble(i, true);
        }
    }

    private void resetBubble(int i, boolean randomY) {
        if (bubbleX == null || viewWidth == 0 || viewHeight == 0) return;
        bubbleX[i] = (float) Math.random() * Math.max(1, viewWidth);
        bubbleY[i] = randomY ?
                (viewHeight * (1 - liquidLevel) + (float) Math.random() * viewHeight * liquidLevel) :
                viewHeight + 10;
        bubbleRadius[i] = 2f + (float) Math.random() * 8f;
        bubbleSpeed[i] = 1f + (float) Math.random() * 4f;
        bubblePhase[i] = (float) (Math.random() * Math.PI * 2);
    }

    /**
     * Update with accelerometer data
     * @param x X-axis acceleration
     * @param y Y-axis acceleration
     * @param z Z-axis acceleration
     */
    public void updateAccelerometer(float x, float y, float z) {
        // Smooth the accelerometer data
        float smoothFactor = 0.15f;
        smoothAccelX = smoothAccelX + (x - smoothAccelX) * smoothFactor;
        smoothAccelY = smoothAccelY + (y - smoothAccelY) * smoothFactor;

        accelX = x;
        accelY = y;

        // Calculate shake intensity
        float intensity = (float) Math.sqrt(x * x + y * y + z * z) - 9.8f;
        intensity = Math.max(0, Math.abs(intensity));

        // Adjust wave amplitude based on intensity
        targetAmplitude = 20f + intensity * 15f;
        targetAmplitude = Math.min(targetAmplitude, 120f);

        invalidate();
    }

    /**
     * Called when a shake is detected - creates bigger waves
     */
    public void onShake() {
        waveAmplitude = Math.min(waveAmplitude + 40f, 150f);
        targetAmplitude = 30f;

        // Spawn more bubbles with higher speed
        for (int i = 0; i < BUBBLE_COUNT; i++) {
            if (Math.random() > 0.5) {
                resetBubble(i, true);
                bubbleSpeed[i] = 4f + (float) Math.random() * 6f;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (viewWidth == 0 || viewHeight == 0 || canvas == null) return;

        try {

        // Calculate time delta
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000f;
        lastUpdateTime = currentTime;

        // Update wave phase
        wavePhase += deltaTime * 3f;

        // Smoothly adjust wave amplitude
        if (waveAmplitude > targetAmplitude + 1f) {
            waveAmplitude -= deltaTime * 60f;
        } else if (waveAmplitude < targetAmplitude - 1f) {
            waveAmplitude += deltaTime * 30f;
        }

        // Draw background - inside of bottle (dark)
        canvas.drawColor(Color.rgb(15, 8, 4));

        // Draw cola liquid with waves
        drawColaLiquid(canvas);

        // Draw bubbles
        drawBubbles(canvas, deltaTime);

        // Draw foam layer
        drawFoam(canvas);

        // Draw glass highlights (simulate being inside bottle)
        drawGlassEffect(canvas);

        // Request next frame
        postInvalidateOnAnimation();
        } catch (Exception e) {
            // Ignore drawing errors
        }
    }

    private void drawColaLiquid(Canvas canvas) {
        float baseLevel = viewHeight * (1 - liquidLevel);

        // Add tilt effect from accelerometer
        float tiltOffset = smoothAccelX * 8f;

        colaPath.reset();

        // Create wavy surface
        int segments = 50;
        for (int i = 0; i <= segments; i++) {
            float t = (float) i / segments;
            float x = t * viewWidth;

            // Multiple wave layers for more natural look
            float wave1 = (float) Math.sin(wavePhase + t * 4 * Math.PI + smoothAccelX * 0.5f) * waveAmplitude;
            float wave2 = (float) Math.sin(wavePhase * 1.3f + t * 6 * Math.PI - smoothAccelY * 0.3f) * waveAmplitude * 0.5f;
            float wave3 = (float) Math.sin(wavePhase * 0.7f + t * 2 * Math.PI) * waveAmplitude * 0.3f;

            // Tilt effect - higher on one side based on phone tilt
            float tilt = tiltOffset * (t - 0.5f) * 2f;

            float y = baseLevel + wave1 + wave2 + wave3 + tilt;

            if (i == 0) {
                colaPath.moveTo(x, y);
            } else {
                colaPath.lineTo(x, y);
            }
        }

        // Complete the path
        colaPath.lineTo(viewWidth, viewHeight);
        colaPath.lineTo(0, viewHeight);
        colaPath.close();

        canvas.drawPath(colaPath, colaPaint);
    }

    private void drawBubbles(Canvas canvas, float deltaTime) {
        float baseLevel = viewHeight * (1 - liquidLevel);

        for (int i = 0; i < BUBBLE_COUNT; i++) {
            // Move bubbles up
            bubbleY[i] -= bubbleSpeed[i] * (1 + waveAmplitude / 50f);

            // Add horizontal wobble influenced by accelerometer
            float wobble = (float) Math.sin(wavePhase * 2 + bubblePhase[i]) * 2f;
            wobbleInfluence(i, wobble);

            // Reset bubble if it reaches surface
            if (bubbleY[i] < baseLevel - 20) {
                resetBubble(i, false);
            }

            // Only draw if within liquid
            if (bubbleY[i] > baseLevel && bubbleY[i] < viewHeight) {
                // Bubble opacity based on depth
                float depth = (bubbleY[i] - baseLevel) / (viewHeight - baseLevel);
                int alpha = (int) (30 + 50 * depth);

                bubblePaint.setColor(Color.argb(alpha, 255, 255, 255));
                canvas.drawCircle(bubbleX[i], bubbleY[i], bubbleRadius[i], bubblePaint);

                // Small highlight on bubble
                if (bubbleRadius[i] > 4) {
                    bubblePaint.setColor(Color.argb(alpha / 2, 255, 255, 255));
                    canvas.drawCircle(
                            bubbleX[i] - bubbleRadius[i] * 0.3f,
                            bubbleY[i] - bubbleRadius[i] * 0.3f,
                            bubbleRadius[i] * 0.3f,
                            bubblePaint
                    );
                }
            }
        }
    }

    private void wobbleInfluence(int i, float wobble) {
        bubbleX[i] += wobble + smoothAccelX * 0.5f;

        // Keep bubbles within screen
        if (bubbleX[i] < 0) bubbleX[i] = 0;
        if (bubbleX[i] > viewWidth) bubbleX[i] = viewWidth;
    }

    private void drawFoam(Canvas canvas) {
        float baseLevel = viewHeight * (1 - liquidLevel);
        float tiltOffset = smoothAccelX * 8f;

        // Draw foam bubbles at surface
        Paint foamBubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        int foamBubbles = 60;
        for (int i = 0; i < foamBubbles; i++) {
            float t = (float) i / foamBubbles;
            float x = t * viewWidth;

            float wave = (float) Math.sin(wavePhase + t * 4 * Math.PI) * waveAmplitude;
            float tilt = tiltOffset * (t - 0.5f) * 2f;
            float y = baseLevel + wave + tilt;

            // Random foam bubbles near surface
            float foamY = y - 5 - (float) Math.random() * 15;
            float foamSize = 3 + (float) Math.random() * 8;

            int alpha = 100 + (int) (Math.random() * 80);
            foamBubblePaint.setColor(Color.argb(alpha, 200, 160, 120));
            canvas.drawCircle(x, foamY, foamSize, foamBubblePaint);
        }
    }

    private void drawGlassEffect(Canvas canvas) {
        // Simulate being inside a glass bottle
        // Left edge highlight
        Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        edgePaint.setShader(new LinearGradient(
                0, 0, viewWidth * 0.15f, 0,
                Color.argb(40, 255, 255, 255),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
        ));
        canvas.drawRect(0, 0, viewWidth * 0.15f, viewHeight, edgePaint);

        // Right edge highlight
        edgePaint.setShader(new LinearGradient(
                viewWidth * 0.85f, 0, viewWidth, 0,
                Color.TRANSPARENT,
                Color.argb(25, 255, 255, 255),
                Shader.TileMode.CLAMP
        ));
        canvas.drawRect(viewWidth * 0.85f, 0, viewWidth, viewHeight, edgePaint);

        // Subtle curved highlight on left
        Path highlightPath = new Path();
        highlightPath.moveTo(viewWidth * 0.05f, 0);
        highlightPath.quadTo(viewWidth * 0.08f, viewHeight * 0.5f, viewWidth * 0.05f, viewHeight);
        highlightPath.lineTo(viewWidth * 0.02f, viewHeight);
        highlightPath.lineTo(viewWidth * 0.02f, 0);
        highlightPath.close();

        Paint curvePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        curvePaint.setColor(Color.argb(35, 255, 255, 255));
        canvas.drawPath(highlightPath, curvePaint);
    }

    /**
     * Get current shake intensity for vibration feedback
     */
    public float getShakeIntensity() {
        return Math.min(waveAmplitude / 100f, 1f);
    }
}
