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

public class AnimeGirlView extends View {

    private Paint skinPaint;
    private Paint hairPaint;
    private Paint hairHighlightPaint;
    private Paint eyePaint;
    private Paint eyeWhitePaint;
    private Paint eyeHighlightPaint;
    private Paint mouthPaint;
    private Paint blushPaint;
    private Paint dressPaint;
    private Paint dressDetailPaint;

    private float bounceOffset = 0f;
    private float blinkTimer = 0f;
    private boolean isBlinking = false;
    private ValueAnimator bounceAnimator;

    private int viewWidth;
    private int viewHeight;

    private float centerX;
    private float girlTop;

    public AnimeGirlView(Context context) {
        super(context);
        init();
    }

    public AnimeGirlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimeGirlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Skin
        skinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        skinPaint.setColor(Color.rgb(255, 224, 210));
        skinPaint.setStyle(Paint.Style.FILL);

        // Hair (dark brown/black)
        hairPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hairPaint.setColor(Color.rgb(50, 30, 20));
        hairPaint.setStyle(Paint.Style.FILL);

        // Hair highlight
        hairHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hairHighlightPaint.setColor(Color.rgb(80, 50, 35));
        hairHighlightPaint.setStyle(Paint.Style.FILL);

        // Eyes
        eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePaint.setColor(Color.rgb(100, 60, 40));
        eyePaint.setStyle(Paint.Style.FILL);

        eyeWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyeWhitePaint.setColor(Color.WHITE);
        eyeWhitePaint.setStyle(Paint.Style.FILL);

        eyeHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyeHighlightPaint.setColor(Color.WHITE);
        eyeHighlightPaint.setStyle(Paint.Style.FILL);

        // Mouth
        mouthPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mouthPaint.setColor(Color.rgb(220, 120, 120));
        mouthPaint.setStyle(Paint.Style.FILL);

        // Blush
        blushPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blushPaint.setColor(Color.argb(80, 255, 150, 150));
        blushPaint.setStyle(Paint.Style.FILL);

        // Dress (cute pink/red)
        dressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dressPaint.setColor(Color.rgb(230, 80, 100));
        dressPaint.setStyle(Paint.Style.FILL);

        dressDetailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dressDetailPaint.setColor(Color.rgb(255, 200, 200));
        dressDetailPaint.setStyle(Paint.Style.FILL);

        // Bounce animation
        bounceAnimator = ValueAnimator.ofFloat(0f, (float) (2 * Math.PI));
        bounceAnimator.setDuration(2000);
        bounceAnimator.setRepeatCount(ValueAnimator.INFINITE);
        bounceAnimator.setInterpolator(new LinearInterpolator());
        bounceAnimator.addUpdateListener(animation -> {
            bounceOffset = (float) animation.getAnimatedValue();
            blinkTimer += 0.05f;
            if (blinkTimer > 4f) {
                isBlinking = true;
                if (blinkTimer > 4.15f) {
                    isBlinking = false;
                    blinkTimer = 0f;
                }
            }
            invalidate();
        });
        bounceAnimator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        centerX = w * 0.28f;
        girlTop = h * 0.25f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (viewWidth == 0 || viewHeight == 0) return;

        float bounce = (float) Math.sin(bounceOffset) * 8;
        float baseY = girlTop + bounce;

        drawHairBack(canvas, baseY);
        drawBody(canvas, baseY);
        drawArm(canvas, baseY);
        drawHead(canvas, baseY);
        drawHairFront(canvas, baseY);
        drawFace(canvas, baseY);
    }

    private void drawHairBack(Canvas canvas, float baseY) {
        float headCenterX = centerX;
        float headRadius = viewWidth * 0.12f;
        float headY = baseY + headRadius;

        // Long hair at back
        Path hairPath = new Path();
        hairPath.moveTo(headCenterX - headRadius * 1.1f, headY);
        hairPath.quadTo(headCenterX - headRadius * 1.3f, headY + headRadius * 2.5f,
                headCenterX - headRadius * 0.5f, headY + headRadius * 3.5f);
        hairPath.lineTo(headCenterX + headRadius * 0.5f, headY + headRadius * 3.5f);
        hairPath.quadTo(headCenterX + headRadius * 1.3f, headY + headRadius * 2.5f,
                headCenterX + headRadius * 1.1f, headY);
        hairPath.close();
        canvas.drawPath(hairPath, hairPaint);
    }

    private void drawBody(Canvas canvas, float baseY) {
        float headRadius = viewWidth * 0.12f;
        float bodyTop = baseY + headRadius * 2.2f;
        float bodyWidth = headRadius * 1.4f;

        // Dress body
        Path dressPath = new Path();
        dressPath.moveTo(centerX - bodyWidth * 0.4f, bodyTop);
        dressPath.lineTo(centerX - bodyWidth * 0.7f, bodyTop + headRadius * 2.5f);
        dressPath.quadTo(centerX, bodyTop + headRadius * 2.8f,
                centerX + bodyWidth * 0.7f, bodyTop + headRadius * 2.5f);
        dressPath.lineTo(centerX + bodyWidth * 0.4f, bodyTop);
        dressPath.close();
        canvas.drawPath(dressPath, dressPaint);

        // Dress ribbon
        float ribbonY = bodyTop + headRadius * 0.3f;
        canvas.drawCircle(centerX, ribbonY, headRadius * 0.15f, dressDetailPaint);

        // Collar
        Path collarPath = new Path();
        collarPath.moveTo(centerX - bodyWidth * 0.3f, bodyTop - 5);
        collarPath.lineTo(centerX, bodyTop + headRadius * 0.4f);
        collarPath.lineTo(centerX + bodyWidth * 0.3f, bodyTop - 5);
        canvas.drawPath(collarPath, Color.WHITE == 0 ? dressPaint : new Paint() {{
            setColor(Color.WHITE);
            setStyle(Paint.Style.FILL);
            setAntiAlias(true);
        }});
    }

    private void drawArm(Canvas canvas, float baseY) {
        float headRadius = viewWidth * 0.12f;
        float bodyTop = baseY + headRadius * 2.2f;

        // Right arm holding something (toward bottle)
        Paint armPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        armPaint.setColor(Color.rgb(255, 224, 210));
        armPaint.setStyle(Paint.Style.FILL);

        Path armPath = new Path();
        float armStartX = centerX + headRadius * 0.5f;
        float armStartY = bodyTop + headRadius * 0.3f;
        float armEndX = centerX + headRadius * 2.2f;
        float armEndY = bodyTop + headRadius * 0.8f;

        // Upper arm
        armPath.moveTo(armStartX, armStartY);
        armPath.quadTo(armStartX + headRadius * 0.8f, armStartY + headRadius * 0.2f,
                armEndX - headRadius * 0.3f, armEndY);
        armPath.quadTo(armEndX, armEndY + headRadius * 0.15f,
                armEndX - headRadius * 0.3f, armEndY + headRadius * 0.3f);
        armPath.quadTo(armStartX + headRadius * 0.5f, armStartY + headRadius * 0.5f,
                armStartX, armStartY + headRadius * 0.4f);
        armPath.close();

        // Sleeve
        dressPaint.setColor(Color.rgb(230, 80, 100));
        canvas.drawCircle(armStartX + headRadius * 0.1f, armStartY + headRadius * 0.15f,
                headRadius * 0.25f, dressPaint);

        canvas.drawPath(armPath, armPaint);

        // Hand
        canvas.drawCircle(armEndX - headRadius * 0.1f, armEndY + headRadius * 0.1f,
                headRadius * 0.18f, armPaint);
    }

    private void drawHead(Canvas canvas, float baseY) {
        float headCenterX = centerX;
        float headRadius = viewWidth * 0.12f;
        float headY = baseY + headRadius;

        // Face
        canvas.drawCircle(headCenterX, headY, headRadius, skinPaint);
    }

    private void drawHairFront(Canvas canvas, float baseY) {
        float headCenterX = centerX;
        float headRadius = viewWidth * 0.12f;
        float headY = baseY + headRadius;

        // Bangs
        Path bangsPath = new Path();
        bangsPath.moveTo(headCenterX - headRadius * 1.05f, headY - headRadius * 0.2f);

        // Left bang
        bangsPath.quadTo(headCenterX - headRadius * 0.7f, headY + headRadius * 0.3f,
                headCenterX - headRadius * 0.5f, headY - headRadius * 0.1f);

        // Middle bangs
        bangsPath.quadTo(headCenterX - headRadius * 0.25f, headY + headRadius * 0.35f,
                headCenterX, headY - headRadius * 0.05f);
        bangsPath.quadTo(headCenterX + headRadius * 0.25f, headY + headRadius * 0.35f,
                headCenterX + headRadius * 0.5f, headY - headRadius * 0.1f);

        // Right bang
        bangsPath.quadTo(headCenterX + headRadius * 0.7f, headY + headRadius * 0.3f,
                headCenterX + headRadius * 1.05f, headY - headRadius * 0.2f);

        // Top of head
        bangsPath.quadTo(headCenterX + headRadius * 0.8f, headY - headRadius * 1.1f,
                headCenterX, headY - headRadius * 1.15f);
        bangsPath.quadTo(headCenterX - headRadius * 0.8f, headY - headRadius * 1.1f,
                headCenterX - headRadius * 1.05f, headY - headRadius * 0.2f);

        canvas.drawPath(bangsPath, hairPaint);

        // Hair highlight
        Path highlightPath = new Path();
        highlightPath.moveTo(headCenterX - headRadius * 0.3f, headY - headRadius * 0.8f);
        highlightPath.quadTo(headCenterX, headY - headRadius * 0.6f,
                headCenterX + headRadius * 0.2f, headY - headRadius * 0.85f);
        highlightPath.quadTo(headCenterX, headY - headRadius * 0.75f,
                headCenterX - headRadius * 0.3f, headY - headRadius * 0.8f);
        canvas.drawPath(highlightPath, hairHighlightPaint);

        // Side hair
        Path sideHairLeft = new Path();
        sideHairLeft.moveTo(headCenterX - headRadius * 1.05f, headY - headRadius * 0.2f);
        sideHairLeft.quadTo(headCenterX - headRadius * 1.4f, headY + headRadius * 0.5f,
                headCenterX - headRadius * 1.1f, headY + headRadius * 1.5f);
        sideHairLeft.lineTo(headCenterX - headRadius * 0.85f, headY + headRadius * 1.4f);
        sideHairLeft.quadTo(headCenterX - headRadius * 1.1f, headY + headRadius * 0.5f,
                headCenterX - headRadius * 0.95f, headY);
        canvas.drawPath(sideHairLeft, hairPaint);

        Path sideHairRight = new Path();
        sideHairRight.moveTo(headCenterX + headRadius * 1.05f, headY - headRadius * 0.2f);
        sideHairRight.quadTo(headCenterX + headRadius * 1.4f, headY + headRadius * 0.5f,
                headCenterX + headRadius * 1.1f, headY + headRadius * 1.5f);
        sideHairRight.lineTo(headCenterX + headRadius * 0.85f, headY + headRadius * 1.4f);
        sideHairRight.quadTo(headCenterX + headRadius * 1.1f, headY + headRadius * 0.5f,
                headCenterX + headRadius * 0.95f, headY);
        canvas.drawPath(sideHairRight, hairPaint);
    }

    private void drawFace(Canvas canvas, float baseY) {
        float headCenterX = centerX;
        float headRadius = viewWidth * 0.12f;
        float headY = baseY + headRadius;

        float eyeY = headY + headRadius * 0.05f;
        float eyeSpacing = headRadius * 0.45f;
        float eyeWidth = headRadius * 0.28f;
        float eyeHeight = headRadius * 0.35f;

        // Eyes
        if (!isBlinking) {
            // Left eye white
            RectF leftEyeRect = new RectF(
                    headCenterX - eyeSpacing - eyeWidth,
                    eyeY - eyeHeight,
                    headCenterX - eyeSpacing + eyeWidth,
                    eyeY + eyeHeight
            );
            canvas.drawOval(leftEyeRect, eyeWhitePaint);

            // Left eye iris
            canvas.drawCircle(headCenterX - eyeSpacing, eyeY, eyeWidth * 0.7f, eyePaint);

            // Left eye highlight
            canvas.drawCircle(headCenterX - eyeSpacing - eyeWidth * 0.25f,
                    eyeY - eyeHeight * 0.3f, eyeWidth * 0.25f, eyeHighlightPaint);
            canvas.drawCircle(headCenterX - eyeSpacing + eyeWidth * 0.15f,
                    eyeY + eyeHeight * 0.2f, eyeWidth * 0.12f, eyeHighlightPaint);

            // Right eye white
            RectF rightEyeRect = new RectF(
                    headCenterX + eyeSpacing - eyeWidth,
                    eyeY - eyeHeight,
                    headCenterX + eyeSpacing + eyeWidth,
                    eyeY + eyeHeight
            );
            canvas.drawOval(rightEyeRect, eyeWhitePaint);

            // Right eye iris
            canvas.drawCircle(headCenterX + eyeSpacing, eyeY, eyeWidth * 0.7f, eyePaint);

            // Right eye highlight
            canvas.drawCircle(headCenterX + eyeSpacing - eyeWidth * 0.25f,
                    eyeY - eyeHeight * 0.3f, eyeWidth * 0.25f, eyeHighlightPaint);
            canvas.drawCircle(headCenterX + eyeSpacing + eyeWidth * 0.15f,
                    eyeY + eyeHeight * 0.2f, eyeWidth * 0.12f, eyeHighlightPaint);
        } else {
            // Closed eyes (happy expression)
            Paint closedEyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            closedEyePaint.setColor(Color.rgb(50, 30, 20));
            closedEyePaint.setStyle(Paint.Style.STROKE);
            closedEyePaint.setStrokeWidth(3f);
            closedEyePaint.setStrokeCap(Paint.Cap.ROUND);

            Path leftClosedEye = new Path();
            leftClosedEye.moveTo(headCenterX - eyeSpacing - eyeWidth, eyeY);
            leftClosedEye.quadTo(headCenterX - eyeSpacing, eyeY + eyeHeight * 0.5f,
                    headCenterX - eyeSpacing + eyeWidth, eyeY);
            canvas.drawPath(leftClosedEye, closedEyePaint);

            Path rightClosedEye = new Path();
            rightClosedEye.moveTo(headCenterX + eyeSpacing - eyeWidth, eyeY);
            rightClosedEye.quadTo(headCenterX + eyeSpacing, eyeY + eyeHeight * 0.5f,
                    headCenterX + eyeSpacing + eyeWidth, eyeY);
            canvas.drawPath(rightClosedEye, closedEyePaint);
        }

        // Blush
        float blushY = eyeY + headRadius * 0.35f;
        canvas.drawOval(new RectF(
                headCenterX - eyeSpacing - eyeWidth * 1.2f,
                blushY - eyeWidth * 0.3f,
                headCenterX - eyeSpacing + eyeWidth * 0.3f,
                blushY + eyeWidth * 0.3f
        ), blushPaint);
        canvas.drawOval(new RectF(
                headCenterX + eyeSpacing - eyeWidth * 0.3f,
                blushY - eyeWidth * 0.3f,
                headCenterX + eyeSpacing + eyeWidth * 1.2f,
                blushY + eyeWidth * 0.3f
        ), blushPaint);

        // Mouth (small happy smile)
        float mouthY = headY + headRadius * 0.45f;
        Path mouthPath = new Path();
        mouthPath.moveTo(headCenterX - headRadius * 0.15f, mouthY);
        mouthPath.quadTo(headCenterX, mouthY + headRadius * 0.12f,
                headCenterX + headRadius * 0.15f, mouthY);
        mouthPaint.setStyle(Paint.Style.STROKE);
        mouthPaint.setStrokeWidth(3f);
        mouthPaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawPath(mouthPath, mouthPaint);
    }

    public void onShake() {
        // Make girl react happily - trigger blink
        blinkTimer = 3.9f;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (bounceAnimator != null) {
            bounceAnimator.cancel();
        }
    }
}
