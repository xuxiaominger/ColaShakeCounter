package com.colashake.counter;

import android.hardware.Sensor;
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

public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ShakeDetector shakeDetector;
    private Vibrator vibrator;

    private TextView counterText;
    private ColaBottleView colaBottleView;
    private AnimeGirlView animeGirlView;
    private int shakeCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup edge-to-edge display
        setupFullscreen();

        setContentView(R.layout.activity_main);

        counterText = findViewById(R.id.counterText);
        colaBottleView = findViewById(R.id.colaBottleView);
        animeGirlView = findViewById(R.id.animeGirlView);

        // Setup sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // Setup shake detector
        shakeDetector = new ShakeDetector();
        shakeDetector.setOnShakeListener(() -> runOnUiThread(() -> {
            shakeCount++;
            counterText.setText(String.valueOf(shakeCount));
            colaBottleView.onShake();
            animeGirlView.onShake();
            vibrateShort();
        }));

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

    private void vibrateShort() {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(50);
            }
        } catch (Exception e) {
            // Ignore vibration errors
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(shakeDetector, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME);
        }
        // Re-apply fullscreen on resume
        setupFullscreen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(shakeDetector);
        }
    }
}
