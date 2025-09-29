package org.obsidian.omot.ui.auth;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.obsidian.omot.R;
import org.obsidian.omot.security.SecurityManager;
import org.obsidian.omot.utils.AnimationUtilities;

public class LockoutFragment extends Fragment {

    private static final long LOCKOUT_DURATION = 10 * 60 * 1000; // 10 minutes

    private TextView tvLockoutHeader, tvLockoutMessage, tvCountdownTimer, tvPanicWarning;
    private CountDownTimer countDownTimer;
    private long lockoutEndTime;
    private boolean isPanicMode;
    private Handler mainHandler;

    public LockoutFragment() {}

    public static LockoutFragment newInstance(String message, long lockoutUntil) {
        LockoutFragment fragment = new LockoutFragment();
        Bundle args = new Bundle();
        args.putString("lockout_message", message);
        args.putLong("lockout_until", lockoutUntil);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lockout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        parseArguments();
        setupLockoutState();
        startCountdownTimer();
        startPulseAnimation();
    }

    // ------------------------------------------------ \\
    // --------------- onCreate methods --------------- \\
    // ------------------------------------------------ \\

    private void initializeViews(View view) {
        tvLockoutHeader = view.findViewById(R.id.tv_lockout_header);
        tvLockoutMessage = view.findViewById(R.id.tv_lockout_message);
        tvCountdownTimer = view.findViewById(R.id.tv_countdown_timer);
        tvPanicWarning = view.findViewById(R.id.tv_panic_warning);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    private void parseArguments() {
        Bundle args = getArguments();
        if (args != null) {
            String message = args.getString("lockout_message", getString(R.string.lockout_header_1));
            lockoutEndTime = args.getLong("lockout_until", System.currentTimeMillis() + LOCKOUT_DURATION);

            tvLockoutMessage.setText(message);
        } else {
            lockoutEndTime = System.currentTimeMillis() + LOCKOUT_DURATION;
        }
    }

    private void setupLockoutState() {
        SecurityManager manager = SecurityManager.getInstance(requireContext());
        isPanicMode = manager.isPanicModeActive();

        if (isPanicMode) {
            tvLockoutHeader.setText(getString(R.string.lockout_header_1));
            tvLockoutMessage.setText(getString(R.string.data_purge_message));
            tvPanicWarning.setVisibility(View.VISIBLE);

            // Start blinking animation for panic warning
            startBlinkingAnimation(tvPanicWarning);
        } else {
            // Check if this is a tamper detection lockout
            int tamperCount = manager.getTamperDetectionCount();
            if (tamperCount > 0) {
                tvLockoutHeader.setText(getString(R.string.lockout_header_2));
                tvLockoutMessage.setText(getString(R.string.tamper_attempts_message));
            }
        }
    }

    private void startCountdownTimer() {
        long remainingTime = lockoutEndTime - System.currentTimeMillis();

        if (remainingTime <= 0) {
            // Lockout already expired
            onLockoutExpired();
            return;
        }

        countDownTimer = new CountDownTimer(remainingTime, 1000) {
            @Override
            public void onFinish() {
                onLockoutExpired();
            }

            @Override
            public void onTick(long millisUntilFinished) {
                updateCountdownDisplay(millisUntilFinished);
            }
        }.start();
    }

    private void startPulseAnimation() {
        AnimationUtilities.startPulseAnimation(tvLockoutHeader, 1f, 1.05f, 800);

        // Add heartbeat-like pulse countdown timer
        ValueAnimator pulseAnimator = ValueAnimator.ofFloat(1f, 1,1f, 1f);
        pulseAnimator.setDuration(1200);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            tvCountdownTimer.setScaleX(scale);
            tvCountdownTimer.setScaleY(scale);
        });
        pulseAnimator.start();
    }

    // ------------------------------------------------- \\
    // --------------- Animation methods --------------- \\
    // ------------------------------------------------- \\

    private void startBlinkingAnimation(View view) {
        ValueAnimator blinkAnimator = ValueAnimator.ofFloat(0.3f, 1f);
        blinkAnimator.setDuration(500);
        blinkAnimator.setRepeatCount(ValueAnimator.INFINITE);
        blinkAnimator.setRepeatMode(ValueAnimator.REVERSE);
        blinkAnimator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            view.setAlpha(alpha);
        });
        blinkAnimator.start();
    }

    // ------------------------------------------------- \\
    // --------------- Countdown methods --------------- \\
    // ------------------------------------------------- \\

    private void onLockoutExpired() {
        tvCountdownTimer.setText(getString(R.string.countdown_timer_starter));

        // Show unlock message
        tvLockoutHeader.setText(getString(R.string.lockout_expired));
        tvLockoutMessage.setText(getString(R.string.lockout_expired_message));

        // Clear lockout state
        SecurityManager manager = SecurityManager.getInstance(requireContext());
        manager.storeSensitiveData("lockout_until", "0");
        manager.storeSensitiveData("failed_attempts", "0");

        // Navigate back to login after delay
        mainHandler.postDelayed(() -> {
            if (getActivity() instanceof AuthenticationActivity) {
                ((AuthenticationActivity) getActivity()).showLoginFragment();
            }
        }, 3000);
    }

    private void updateCountdownDisplay(long millisUntilFinished) {
        long minutes = (millisUntilFinished / 1000) / 60;
        long seconds = (millisUntilFinished / 1000) % 60;

        String timeText = String.format("%02d:%02d", minutes, seconds);
        tvCountdownTimer.setText(timeText);

        // Add visual effect when time is running low
        if (millisUntilFinished < 30000) { // Last 30 seconds
            if (millisUntilFinished % 2000 < 1000) { // Blink every second
                tvCountdownTimer.setAlpha(0.3f);
            } else {
                tvCountdownTimer.setAlpha(1.0f);
            }
        }
    }

    // ------------------------------------------------- \\
    // --------------- Lifecycle methods --------------- \\
    // ------------------------------------------------- \\

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Prevent back button from working during lockout
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {}
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }
}