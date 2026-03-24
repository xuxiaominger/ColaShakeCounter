package com.colashake.counter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Vibrator vibrator;

    private TextView counterText;
    private ColaScreenView colaScreenView;
    private AnimeGirlView animeGirlView;
    private int shakeCount = 0;

    // Shake detection
    private static final float SHAKE_THRESHOLD = 12f;
    private long lastShakeTime = 0;
    private static final long SHAKE_COOLDOWN = 300; // ms between shakes

    // Variable vibration
    private long lastVibrationTime = 0;
    private static final long VIBRATION_INTERVAL = 50; // ms between vibrations
    private float currentIntensity = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup edge-to-edge display
        setupFullscreen();

        setContentView(R.layout.activity_main);

        counterText = findViewById(R.id.counterText);
        colaScreenView = findViewById(R.id.colaScreenView);
        animeGirlView = findViewById(R.id.animeGirlView);

        // Setup sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // Setup vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) getSystemService(VIBRATOR_MANAGER_SERVICE);
            if (vibratorManager != null) {
                vibrator = vibratorManager.getDefaultVibrator();
            }
        } else {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        }

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setupFullscreen() {
        Window window = getWindow();

        // Use WindowCompat for edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false);

        // Get the WindowInsetsController
        View decorView = window.getDecorView();
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, decorView);

        if (controller != null) {
            // Hide system bars
            controller.hide(WindowInsetsCompat.Type.systemBars());
            // Allow showing bars with swipe
            controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Update the cola screen view with accelerometer data
        if (colaScreenView != null) {
            colaScreenView.updateAccelerometer(x, y, z);
        }

        // Calculate shake intensity
        float acceleration = (float) Math.sqrt(x * x + y * y + z * z);
        float intensity = Math.abs(acceleration - SensorManager.GRAVITY_EARTH);

        // Variable vibration based on intensity
        vibrateWithIntensity(intensity);

        // Detect shake for counter
        long currentTime = System.currentTimeMillis();
        if (intensity > SHAKE_THRESHOLD && (currentTime - lastShakeTime) > SHAKE_COOLDOWN) {
            lastShakeTime = currentTime;
            onShakeDetected();
        }
    }

    private void onShakeDetected() {
        runOnUiThread(() -> {
            shakeCount++;
            counterText.setText(String.valueOf(shakeCount));
            if (colaScreenView != null) {
                colaScreenView.onShake();
            }
            if (animeGirlView != null) {
                animeGirlView.onShake();
            }
        });
    }

    private void vibrateWithIntensity(float intensity) {
        if (vibrator == null || !vibrator.hasVibrator()) return;

        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastVibrationTime) < VIBRATION_INTERVAL) return;

        // Only vibrate if there's significant movement
        if (intensity < 2f) {
            currentIntensity = 0;
            return;
        }

        // Smooth the intensity
        float targetIntensity = Math.min(intensity / 15f, 1f);
        currentIntensity = currentIntensity + (targetIntensity - currentIntensity) * 0.3f;

        if (currentIntensity < 0.1f) return;

        lastVibrationTime = currentTime;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Calculate amplitude (1-255)
                int amplitude = (int) (currentIntensity * 200) + 30;
                amplitude = Math.min(255, Math.max(1, amplitude));

                // Short pulse with variable amplitude
                vibrator.vibrate(VibrationEffect.createOneShot(20, amplitude));
            } else {
                // For older devices, just use on/off vibration
                if (currentIntensity > 0.3f) {
                    vibrator.vibrate(15);
                }
            }
        } catch (Exception e) {
            // Ignore vibration errors
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME);
        }
        // Re-apply fullscreen on resume
        setupFullscreen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
}
