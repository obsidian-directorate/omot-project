package org.obsidian.omot.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.obsidian.omot.ui.main.MainActivity;
import org.obsidian.omot.R;
import org.obsidian.omot.security.EncryptionManager;
import org.obsidian.omot.ui.auth.AuthenticationActivity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WelcomeActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 4000; // 4 seconds total
    private static final int BOOT_SEQUENCE_DELAY = 500;

    private Vibrator vibrator;
    private SoundPool soundPool;
    private int clickSoundID, scanSoundID, successSoundID;

    private TextView tvBootLine1, tvBootLine2, tvBootLine3, tvProgressIndicator, tvSecurityFooter;
    private ImageView ivSplashLogo;

    private ScheduledExecutorService scheduler;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupAudioVibration();
        startBootSequence();
    }

    // -------------------------------------------------------------------- \\
    // --------------- Overridden AppCompatActivity methods --------------- \\
    // -------------------------------------------------------------------- \\

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }

    // ------------------------------------------------ \\
    // --------------- onCreate methods --------------- \\
    // ------------------------------------------------ \\

    private void initializeViews() {
        tvBootLine1 = findViewById(R.id.tv_boot_line_1);
        tvBootLine2 = findViewById(R.id.tv_boot_line_2);
        tvBootLine3 = findViewById(R.id.tv_boot_line_3);
        tvProgressIndicator = findViewById(R.id.tv_progress_indicator);
        tvSecurityFooter = findViewById(R.id.tv_security_footer);
        ivSplashLogo = findViewById(R.id.iv_splash_logo);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mainHandler = new Handler(Looper.getMainLooper());
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    private void setupAudioVibration() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build();

        clickSoundID = soundPool.load(this, R.raw.boot_click, 1);
        scanSoundID = soundPool.load(this, R.raw.scan_sound, 1);
        successSoundID = soundPool.load(this, R.raw.boot_success, 1);
    }

    private void startBootSequence() {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animateLogoAppearance();
                playClickSound();
                gentleVibrate(50);
            }
        }, BOOT_SEQUENCE_DELAY);

        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start boot text sequence
                animateBootText();
            }
        }, BOOT_SEQUENCE_DELAY + 800);
    }

    // ------------------------------------------------------------ \\
    // --------------- Animation and effect methods --------------- \\
    // ------------------------------------------------------------ \\

    private void animateLogoAppearance() {
        ivSplashLogo.setVisibility(View.VISIBLE);
        ivSplashLogo.setAlpha(0f);
        ivSplashLogo.setScaleX(0.5f);
        ivSplashLogo.setScaleY(0.5f);

        ivSplashLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startLogoPulseAnimation();
                    }
                })
                .start();
    }

    private void startLogoPulseAnimation() {
        ValueAnimator pulseAnimator = ValueAnimator.ofFloat(1f, 1.1f, 1f);
        pulseAnimator.setDuration(1200);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            ivSplashLogo.setScaleX(scale);
            ivSplashLogo.setScaleY(scale);
        });
        pulseAnimator.start();
    }

    private void animateBootText() {
        playScanSound();
        gentleVibrate(200);

        // Line 1: System initialization
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvBootLine1.setVisibility(View.VISIBLE);
                typewriterEffect(tvBootLine1, getString(R.string.boot_line_1), 80, () -> {
                    playClickSound();
                });
            }
        }, 0);

        // Line 2: Security check
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                typewriterEffect(tvBootLine2, getString(R.string.boot_line_2), 60, () -> {
                    playClickSound();
                    gentleVibrate(30);
                });
            }
        }, 800);

        // Line 3: OMOT ready
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                typewriterEffect(tvBootLine3, getString(R.string.boot_line_3), 70, () -> {
                    playSuccessSound();
                    gentleVibrate(100);

                    // Show progress indicator
                    showProgressIndicator();
                    showSecurityFooter();

                    // Start loading tasks
                    startBackgroundLoading();
                });
            }
        }, 1600);
    }

    private void typewriterEffect(TextView textView, String text, int delayPerChar, Runnable onComplete) {
        textView.setText("*");

        new Thread(() -> {
            for (int i = 0; i < text.length(); i++) {
                final int index = i;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(text.substring(0, index + 1));
                    }
                });

                try {
                    Thread.sleep(delayPerChar);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        }).start();
    }

    private void animateProgressDots() {
        final String[] dotFrames = {".", "..", "...", "...."};
        final int[] frameIndex = {0};

        Handler progressHandler = new Handler();
        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                tvProgressIndicator.setText(getString(R.string.boot_progress) + dotFrames[frameIndex[0]]);
                frameIndex[0] = (frameIndex[0] + 1) % dotFrames.length;
                progressHandler.postDelayed(this, 500);
            }
        };
        progressHandler.post(progressRunnable);
    }

    // --------------------------------------------------------------- \\
    // --------------- Methods for media and vibrating --------------- \\
    // --------------------------------------------------------------- \\

    private void playClickSound() {
        soundPool.play(clickSoundID, 0.3f, 0.3f, 1, 0, 1f);
    }

    private void playScanSound() {
        soundPool.play(scanSoundID, 0.5f, 0.5f, 1, 0, 1f);
    }

    private void playSuccessSound() {
        soundPool.play(successSoundID, 0.7f, 0.7f, 1, 0, 1f);
    }

    private void gentleVibrate(long milliseconds) {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(milliseconds);
            }
        }
    }

    // -------------------------------------------------------------- \\
    // --------------- Methods for showing components --------------- \\
    // -------------------------------------------------------------- \\

    private void showProgressIndicator() {
        tvProgressIndicator.setVisibility(View.VISIBLE);

        // Animate progress dots
        animateProgressDots();
    }

    private void showSecurityFooter() {
        tvSecurityFooter.setVisibility(View.VISIBLE);
        tvSecurityFooter.setAlpha(0f);
        tvSecurityFooter.animate().alpha(1f).setDuration(1000).start();
    }

    // ----------------------------------------------------- \\
    // --------------- Other private methods --------------- \\
    // ----------------------------------------------------- \\

    private void startBackgroundLoading() {
        // Simulate background initialization tasks
        scheduler.schedule(() -> {
            // Initialize encryption manager
            EncryptionManager.getInstance().initialize(getApplicationContext());
        }, 500, TimeUnit.MILLISECONDS);

        scheduler.schedule(() -> {
            // Load user preferences and check authentication state

        }, 1000, TimeUnit.MILLISECONDS);

        // Navigate to next activity after total duration
        mainHandler.postDelayed(this::navigateToNextScreen, SPLASH_DURATION);
    }

    private void navigateToNextScreen() {
        Intent intent;

        // Check if user is already authenticated
        if (isUserAuthenticated()) {
            intent = new Intent(WelcomeActivity.this, MainActivity.class);
        } else {
            intent = new Intent(WelcomeActivity.this, AuthenticationActivity.class);
        }

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private boolean isUserAuthenticated() {
        // TODO: Implement actual authentication date
        return false;
    }
}