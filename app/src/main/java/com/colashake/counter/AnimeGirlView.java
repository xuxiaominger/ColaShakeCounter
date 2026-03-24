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

/**
 * Anime girl view inspired by Umaru-chan style
 * - Orange hamster hoodie with ears
 * - Big chibi head
 * - Large expressive eyes
 * - Happy expression
 */
public class AnimeGirlView extends View {

    private Paint skinPaint;
    private Paint hairPaint;
    private Paint hairHighlightPaint;
    private Paint hoodiePaint;
    private Paint hoodieDetailPaint;
    private Paint hoodieShadowPaint;
    private Paint eyeWhitePaint;
    private Paint eyeIrisPaint;
    private Paint eyePupilPaint;
    private Paint eyeHighlightPaint;
    private Paint mouthPaint;
    private Paint blushPaint;
    private Paint hamsterFacePaint;
    private Paint hamsterEyePaint;

    private float bounceOffset = 0f;
    private float armWaveOffset = 0f;
    private boolean isExcited = false;
    private float excitedTimer = 0f;

    private ValueAnimator bounceAnimator;

    private int viewWidth;
    private int viewHeight;

    private float centerX;
    private float girlScale;

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
        // Skin - peachy tone
        skinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        skinPaint.setColor(Color.rgb(255, 228, 215));
        skinPaint.setStyle(Paint.Style.FILL);

        // Hair - orange/light brown like Umaru
        hairPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hairPaint.setColor(Color.rgb(235, 160, 90));
        hairPaint.setStyle(Paint.Style.FILL);

        hairHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hairHighlightPaint.setColor(Color.rgb(255, 200, 130));
        hairHighlightPaint.setStyle(Paint.Style.FILL);

        // Hoodie - orange hamster hoodie
        hoodiePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hoodiePaint.setColor(Color.rgb(230, 140, 60));
        hoodiePaint.setStyle(Paint.Style.FILL);

        hoodieDetailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hoodieDetailPaint.setColor(Color.rgb(250, 180, 100));
        hoodieDetailPaint.setStyle(Paint.Style.FILL);

        hoodieShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hoodieShadowPaint.setColor(Color.rgb(200, 110, 40));
        hoodieShadowPaint.setStyle(Paint.Style.FILL);

        // Eyes
        eyeWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyeWhitePaint.setColor(Color.WHITE);
        eyeWhitePaint.setStyle(Paint.Style.FILL);

        eyeIrisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyeIrisPaint.setColor(Color.rgb(180, 100, 60));
        eyeIrisPaint.setStyle(Paint.Style.FILL);

        eyePupilPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePupilPaint.setColor(Color.rgb(60, 30, 20));
        eyePupilPaint.setStyle(Paint.Style.FILL);

        eyeHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyeHighlightPaint.setColor(Color.WHITE);
        eyeHighlightPaint.setStyle(Paint.Style.FILL);

        // Mouth
        mouthPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mouthPaint.setColor(Color.rgb(200, 80, 80));
        mouthPaint.setStyle(Paint.Style.FILL);

        // Blush
        blushPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blushPaint.setColor(Color.argb(100, 255, 150, 150));
        blushPaint.setStyle(Paint.Style.FILL);

        // Hamster face on hoodie
        hamsterFacePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hamsterFacePaint.setColor(Color.rgb(250, 200, 150));
        hamsterFacePaint.setStyle(Paint.Style.FILL);

        hamsterEyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hamsterEyePaint.setColor(Color.rgb(40, 20, 10));
        hamsterEyePaint.setStyle(Paint.Style.FILL);

        // Animation
        bounceAnimator = ValueAnimator.ofFloat(0f, (float) (2 * Math.PI));
        bounceAnimator.setDuration(1500);
        bounceAnimator.setRepeatCount(ValueAnimator.INFINITE);
        bounceAnimator.setInterpolator(new LinearInterpolator());
        bounceAnimator.addUpdateListener(animation -> {
            bounceOffset = (float) animation.getAnimatedValue();
            armWaveOffset += 0.15f;

            if (isExcited) {
                excitedTimer += 0.1f;
                if (excitedTimer > 2f) {
                    isExcited = false;
                    excitedTimer = 0f;
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

        // Position in bottom-left area as background
        centerX = w * 0.22f;
        girlScale = Math.min(w, h) * 0.0012f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (viewWidth == 0 || viewHeight == 0) return;

        float bounce = (float) Math.sin(bounceOffset) * 6 * girlScale;
        float baseY = viewHeight * 0.45f + bounce;

        float scale = girlScale * 100;

        canvas.save();
        canvas.translate(centerX, baseY);
        canvas.scale(scale / 100f, scale / 100f);

        drawHoodieBack(canvas);
        drawBody(canvas);
        drawArms(canvas);
        drawHead(canvas);
        drawHoodie(canvas);
        drawFace(canvas);
        drawHoodieEars(canvas);
        drawHamsterOnHood(canvas);

        canvas.restore();
    }

    private void drawHoodieBack(Canvas canvas) {
        // Back of hoodie visible behind
        Path backPath = new Path();
        backPath.moveTo(-60, 80);
        backPath.quadTo(-70, 150, -50, 220);
        backPath.lineTo(50, 220);
        backPath.quadTo(70, 150, 60, 80);
        backPath.close();
        canvas.drawPath(backPath, hoodieShadowPaint);
    }

    private void drawBody(Canvas canvas) {
        // Body in hoodie
        Path bodyPath = new Path();
        bodyPath.moveTo(-45, 90);
        bodyPath.lineTo(-55, 200);
        bodyPath.quadTo(0, 220, 55, 200);
        bodyPath.lineTo(45, 90);
        bodyPath.close();
        canvas.drawPath(bodyPath, hoodiePaint);
    }

    private void drawArms(Canvas canvas) {
        // Left arm (waving when excited)
        float leftArmAngle = isExcited ? (float) Math.sin(armWaveOffset * 3) * 20 : 0;

        canvas.save();
        canvas.rotate(leftArmAngle, -50, 110);

        Path leftArmPath = new Path();
        leftArmPath.moveTo(-50, 100);
        leftArmPath.quadTo(-85, 130, -80, 170);
        leftArmPath.quadTo(-75, 180, -65, 175);
        leftArmPath.quadTo(-55, 150, -40, 120);
        leftArmPath.close();
        canvas.drawPath(leftArmPath, hoodiePaint);

        // Left hand
        canvas.drawCircle(-75, 175, 12, skinPaint);

        canvas.restore();

        // Right arm (raised up when excited)
        float rightArmRaise = isExcited ? -30 + (float) Math.sin(armWaveOffset * 4) * 15 : 0;

        canvas.save();
        canvas.rotate(rightArmRaise, 50, 110);

        Path rightArmPath = new Path();
        rightArmPath.moveTo(50, 100);
        rightArmPath.quadTo(85, 130, 80, 170);
        rightArmPath.quadTo(75, 180, 65, 175);
        rightArmPath.quadTo(55, 150, 40, 120);
        rightArmPath.close();
        canvas.drawPath(rightArmPath, hoodiePaint);

        // Right hand
        canvas.drawCircle(75, 175, 12, skinPaint);

        canvas.restore();
    }

    private void drawHead(Canvas canvas) {
        // Big chibi head
        canvas.drawOval(new RectF(-55, -55, 55, 55), skinPaint);
    }

    private void drawHoodie(Canvas canvas) {
        // Hoodie covering top of head
        Path hoodiePath = new Path();
        hoodiePath.moveTo(-58, 10);
        hoodiePath.quadTo(-65, -30, -50, -55);
        hoodiePath.quadTo(-30, -75, 0, -78);
        hoodiePath.quadTo(30, -75, 50, -55);
        hoodiePath.quadTo(65, -30, 58, 10);
        hoodiePath.quadTo(40, 25, 0, 28);
        hoodiePath.quadTo(-40, 25, -58, 10);
        hoodiePath.close();
        canvas.drawPath(hoodiePath, hoodiePaint);

        // Hoodie edge (lighter color)
        Path edgePath = new Path();
        edgePath.moveTo(-55, 8);
        edgePath.quadTo(-35, 22, 0, 25);
        edgePath.quadTo(35, 22, 55, 8);

        Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        edgePaint.setColor(Color.rgb(250, 180, 100));
        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeWidth(5);
        canvas.drawPath(edgePath, edgePaint);
    }

    private void drawFace(Canvas canvas) {
        float eyeY = 0;
        float eyeSpacing = 28;
        float eyeWidth = 18;
        float eyeHeight = 22;

        // Big anime eyes
        // Left eye
        RectF leftEyeRect = new RectF(-eyeSpacing - eyeWidth, eyeY - eyeHeight,
                -eyeSpacing + eyeWidth, eyeY + eyeHeight);
        canvas.drawOval(leftEyeRect, eyeWhitePaint);

        // Left iris
        canvas.drawCircle(-eyeSpacing, eyeY + 2, eyeWidth * 0.75f, eyeIrisPaint);

        // Left pupil
        canvas.drawCircle(-eyeSpacing, eyeY + 4, eyeWidth * 0.4f, eyePupilPaint);

        // Left highlights
        canvas.drawCircle(-eyeSpacing - 6, eyeY - 6, 6, eyeHighlightPaint);
        canvas.drawCircle(-eyeSpacing + 4, eyeY + 6, 3, eyeHighlightPaint);

        // Right eye
        RectF rightEyeRect = new RectF(eyeSpacing - eyeWidth, eyeY - eyeHeight,
                eyeSpacing + eyeWidth, eyeY + eyeHeight);
        canvas.drawOval(rightEyeRect, eyeWhitePaint);

        // Right iris
        canvas.drawCircle(eyeSpacing, eyeY + 2, eyeWidth * 0.75f, eyeIrisPaint);

        // Right pupil
        canvas.drawCircle(eyeSpacing, eyeY + 4, eyeWidth * 0.4f, eyePupilPaint);

        // Right highlights
        canvas.drawCircle(eyeSpacing - 6, eyeY - 6, 6, eyeHighlightPaint);
        canvas.drawCircle(eyeSpacing + 4, eyeY + 6, 3, eyeHighlightPaint);

        // Blush marks
        canvas.drawOval(new RectF(-50, 15, -30, 28), blushPaint);
        canvas.drawOval(new RectF(30, 15, 50, 28), blushPaint);

        // Mouth - big happy smile when excited, normal smile otherwise
        if (isExcited) {
            // Open happy mouth
            Path mouthPath = new Path();
            mouthPath.moveTo(-15, 32);
            mouthPath.quadTo(0, 50, 15, 32);
            mouthPath.quadTo(0, 42, -15, 32);
            canvas.drawPath(mouthPath, mouthPaint);

            // Tongue hint
            Paint tonguePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            tonguePaint.setColor(Color.rgb(255, 150, 150));
            canvas.drawOval(new RectF(-6, 38, 6, 46), tonguePaint);
        } else {
            // Cute small smile
            Path mouthPath = new Path();
            mouthPath.moveTo(-12, 35);
            mouthPath.quadTo(0, 45, 12, 35);

            Paint smilePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            smilePaint.setColor(Color.rgb(180, 80, 80));
            smilePaint.setStyle(Paint.Style.STROKE);
            smilePaint.setStrokeWidth(3);
            smilePaint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawPath(mouthPath, smilePaint);
        }

        // Small nose
        Paint nosePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nosePaint.setColor(Color.rgb(255, 200, 190));
        canvas.drawCircle(0, 22, 3, nosePaint);
    }

    private void drawHoodieEars(Canvas canvas) {
        // Left hamster ear on hoodie
        Path leftEarPath = new Path();
        leftEarPath.moveTo(-45, -55);
        leftEarPath.quadTo(-65, -85, -50, -95);
        leftEarPath.quadTo(-35, -90, -35, -60);
        leftEarPath.close();
        canvas.drawPath(leftEarPath, hoodiePaint);

        // Inner ear
        Path leftInnerPath = new Path();
        leftInnerPath.moveTo(-45, -60);
        leftInnerPath.quadTo(-55, -80, -48, -85);
        leftInnerPath.quadTo(-42, -82, -40, -65);
        leftInnerPath.close();
        canvas.drawPath(leftInnerPath, hoodieDetailPaint);

        // Right hamster ear
        Path rightEarPath = new Path();
        rightEarPath.moveTo(45, -55);
        rightEarPath.quadTo(65, -85, 50, -95);
        rightEarPath.quadTo(35, -90, 35, -60);
        rightEarPath.close();
        canvas.drawPath(rightEarPath, hoodiePaint);

        // Inner ear
        Path rightInnerPath = new Path();
        rightInnerPath.moveTo(45, -60);
        rightInnerPath.quadTo(55, -80, 48, -85);
        rightInnerPath.quadTo(42, -82, 40, -65);
        rightInnerPath.close();
        canvas.drawPath(rightInnerPath, hoodieDetailPaint);
    }

    private void drawHamsterOnHood(Canvas canvas) {
        // Small hamster decoration on top of hood
        float hamsterY = -70;

        // Hamster body
        canvas.drawOval(new RectF(-12, hamsterY - 8, 12, hamsterY + 12), hamsterFacePaint);

        // Hamster ears
        canvas.drawCircle(-10, hamsterY - 10, 5, hamsterFacePaint);
        canvas.drawCircle(10, hamsterY - 10, 5, hamsterFacePaint);
        canvas.drawCircle(-10, hamsterY - 10, 3, hoodieDetailPaint);
        canvas.drawCircle(10, hamsterY - 10, 3, hoodieDetailPaint);

        // Hamster eyes
        canvas.drawCircle(-5, hamsterY - 2, 3, hamsterEyePaint);
        canvas.drawCircle(5, hamsterY - 2, 3, hamsterEyePaint);

        // Eye highlights
        canvas.drawCircle(-6, hamsterY - 3, 1.2f, eyeHighlightPaint);
        canvas.drawCircle(4, hamsterY - 3, 1.2f, eyeHighlightPaint);

        // Hamster nose
        canvas.drawCircle(0, hamsterY + 2, 2, Color.rgb(255, 150, 150) == 0 ? hamsterEyePaint : new Paint() {{
            setColor(Color.rgb(200, 100, 100));
            setAntiAlias(true);
        }});

        // Hamster blush
        Paint hamsterBlush = new Paint(Paint.ANTI_ALIAS_FLAG);
        hamsterBlush.setColor(Color.argb(80, 255, 150, 150));
        canvas.drawCircle(-9, hamsterY + 2, 3, hamsterBlush);
        canvas.drawCircle(9, hamsterY + 2, 3, hamsterBlush);
    }

    public void onShake() {
        isExcited = true;
        excitedTimer = 0f;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (bounceAnimator != null) {
            bounceAnimator.cancel();
        }
    }
}
