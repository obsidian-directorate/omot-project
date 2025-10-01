package org.obsidian.omot.ui.auth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.obsidian.omot.R;
import org.obsidian.omot.data.daos.AgentDAO;
import org.obsidian.omot.data.daos.ClearanceLevelDAO;
import org.obsidian.omot.data.entities.Agent;
import org.obsidian.omot.data.repository.DBRepository;
import org.obsidian.omot.security.SecurityManager;
import org.obsidian.omot.ui.main.MainActivity;
import org.obsidian.omot.utils.AnimationUtilities;
import org.obsidian.omot.utils.ThemeUtilities;

public class AuthenticationActivity extends AppCompatActivity implements
        LoginFragment.LoginCallback,
        RegistrationFragment.RegistrationCallback,
        ForgotPassFragment.ForgotPasswordCallback {

    private View viewScanLine, viewStatusIndicator;
    private FrameLayout frameAuthContainer;
    private LinearLayout layoutStatusGroup;
    private TextView tvTerminalHeader, tvStatus;

    private SecurityManager manager;
    private Handler mainHandler;
    private DBRepository repository;
    private AgentDAO agentDAO;
    private ClearanceLevelDAO clearanceLevelDAO;

    // Authentication state
    private int failedAttempts = 0;
    private long lockoutUntil = 0;
    private boolean isLockedOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtilities.applyFullScreenTheme(this);
        setContentView(R.layout.activity_authentication);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        manager = SecurityManager.getInstance(this);
        repository = DBRepository.getInstance(this);
        agentDAO = repository.getAgentDAO();
        clearanceLevelDAO = repository.getClearanceLevelDAO();

        mainHandler = new Handler(Looper.getMainLooper());

        initializeViews();
        initializeUI();
        checkSecurityState();
    }

    // ------------------------------------------------ \\
    // --------------- onCreate methods --------------- \\
    // ------------------------------------------------ \\

    private void initializeViews() {
        viewScanLine = findViewById(R.id.view_scan_line);
        viewStatusIndicator = findViewById(R.id.view_status_indicator);
        frameAuthContainer = findViewById(R.id.frame_auth_container);
        layoutStatusGroup = findViewById(R.id.layout_status_group);
        tvTerminalHeader = findViewById(R.id.tv_terminal_header);
        tvStatus = findViewById(R.id.tv_status);
    }

    private void initializeUI() {
        // Setup terminal-style header animation
        animateHeaderAppearance();

        // Check if we should show login or registration first
        if (shouldShowRegistration()) {
            showRegistrationFragment();
        } else {
            showLoginFragment();
        }

        // Setup background scanning animation
        startBackgroundScanAnimation();
    }

    private void checkSecurityState() {
        if (manager.isPanicModeActive()) {
            showLockoutFragment(getString(R.string.lockout_fragment_message));
            return;
        }

        if (manager.getTamperDetectionCount() > 0) {
            showLockoutFragment(getString(R.string.system_integrity_message));
        }

        // Check for existing lockout
        String lockoutTime = manager.retrieveSensitiveData("lockout_until");
        if (lockoutTime != null) {
            lockoutUntil = Long.parseLong(lockoutTime);
            if (System.currentTimeMillis() < lockoutUntil) {
                isLockedOut = true;
                showLockoutFragment(getString(R.string.account_locked_message));
            }
        }
    }

    // ------------------------------------------------- \\
    // --------------- Animation methods --------------- \\
    // ------------------------------------------------- \\

    private void animateHeaderAppearance() {
        tvTerminalHeader.setAlpha(0f);
        tvTerminalHeader.setTranslationY(-50f);

        tvTerminalHeader.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startPulseAnimation();
                    }
                })
                .start();
    }

    private void startPulseAnimation() {
        AnimationUtilities.startPulseAnimation(tvTerminalHeader, 1f, 1.05f, 1000);
    }

    private void startBackgroundScanAnimation() {
        viewScanLine.setVisibility(View.VISIBLE);
        viewScanLine.animate()
                .translationY(frameAuthContainer.getHeight())
                .setDuration(2000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        viewScanLine.setTranslationY(-100f);
                        startBackgroundScanAnimation();
                    }
                })
                .start();
    }

    // --------------------------------------------------------- \\
    // --------------- Security protocol methods --------------- \\
    // --------------------------------------------------------- \\

    private void showSuccessState() {
        showSuccessState(getString(R.string.access_granted));
    }

    private void showSuccessState(String message) {
        viewStatusIndicator.setBackgroundColor(getColor(R.color.omot_green_success));
        tvStatus.setText(message);
        layoutStatusGroup.setVisibility(View.VISIBLE);

        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                layoutStatusGroup.setVisibility(View.GONE);
            }
        }, 2000);
    }

    private void showErrorState(String message) {
        viewStatusIndicator.setBackgroundColor(getColor(R.color.omot_red_alert));
        tvStatus.setText(message);
        layoutStatusGroup.setVisibility(View.VISIBLE);

        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                layoutStatusGroup.setVisibility(View.GONE);
            }
        }, 3000);
    }

    private void triggerLockoutProtocol() {
        lockoutUntil = System.currentTimeMillis() + (10 * 60 * 1000); // 10 minutes
        isLockedOut = true;

        manager.storeSensitiveData("lockout_until", String.valueOf(lockoutUntil));
        manager.storeSensitiveData("last_lockout", String.valueOf(System.currentTimeMillis()));

        // Trigger compromised agent protocol
        if (failedAttempts >= 5) {
            manager.activatePanicMode();
        }

        showLockoutFragment(getString(R.string.unauthorized_message));
    }

    // --------------------------------------------- \\
    // --------------- Other methods --------------- \\
    // --------------------------------------------- \\

    private boolean shouldShowRegistration() {
        // Check if any agent is registered
        return manager.retrieveSensitiveData("first_run") == null;
    }

    private String getCurrentLoginAttemptCodename() {
        // This would need to track the current login attempt codename
        // For now, return from the login fragment
        return null; // Implement based on the UI state
    }

    // ----------------------------------------------------------- \\
    // --------------- Fragment navigation methods --------------- \\
    // ----------------------------------------------------------- \\

    public void showLoginFragment() {
        replaceFragment(LoginFragment.newInstance(), "login", false);
        updateHeader(getString(R.string.auth_required_header));
    }

    private void showRegistrationFragment() {
        // Ensure activity is in a valid state before showing fragment
        if (isFinishing() || isDestroyed()) {
            return;
        }
        replaceFragment(RegistrationFragment.newInstance(), "registration", false);
        updateHeader(getString(R.string.registration_header));
    }

    private void showForgotPasswordFragment() {
        replaceFragment(ForgotPassFragment.newInstance(), "forgot_password", true);
        updateHeader(getString(R.string.lost_credential_header));
    }

    private void showLockoutFragment(String message) {
        LockoutFragment fragment = LockoutFragment.newInstance(message, lockoutUntil);
        replaceFragment(fragment, "lockout", false);
        updateHeader(getString(R.string.lockout_header));

        // Disable all other UI interactions
        frameAuthContainer.setEnabled(false);
    }

    private void replaceFragment(Fragment fragment, String tag, boolean addToBackStack) {
        // Check if activity is still valid
        if (isFinishing() || isDestroyed()) {
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );

        transaction.replace(R.id.frame_auth_container, fragment, tag);

        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }

        transaction.commitAllowingStateLoss();
    }

    private void updateHeader(String title) {
        tvTerminalHeader.setText(title);
    }

    // ------------------------------------------------------- \\
    // --------------- LOGIN FRAGMENT CALLBACK --------------- \\
    // ------------------------------------------------------- \\

    @Override
    public void onLoginSuccess(String agentID) {
        // Update last login timestamp in database
        Agent agent = agentDAO.getAgentByCodename(agentID);
        if (agent != null) {
            agent.setLastLoginTimestamp(System.currentTimeMillis());
            agent.setFailedLoginAttempts(0);
            agent.setAccountLocked(false);
            agentDAO.updateAgent(agent);
        }

        // Reset failed attempts on successful login
        failedAttempts = 0;
        manager.storeSensitiveData("failed_attempts", "0");
        manager.storeSensitiveData("lockout_until", "0");
        manager.storeSensitiveData("last_authenticated_agent", agentID);

        // Show success animation
        showSuccessState();

        // Navigate to main activity
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(AuthenticationActivity.this, MainActivity.class);
                intent.putExtra("agent_id", agentID);
                startActivity(intent);
                finish();
            }
        }, 1500);
    }

    @Override
    public void onLoginFailure() {
        failedAttempts++;

        // Update failed attempts in database
        String currentCodename = getCurrentLoginAttemptCodename();
        if (currentCodename != null) {
            Agent agent = agentDAO.getAgentByCodename(currentCodename);
            if (agent != null) {
                agent.setFailedLoginAttempts(failedAttempts);
                agent.setLastFailedLoginTimestamp(System.currentTimeMillis());

                if (failedAttempts >= 5) {
                    agent.setAccountLocked(true);
                    triggerLockoutProtocol();
                }

                agentDAO.updateAgent(agent);
            }
        }

        manager.storeSensitiveData("failed_attempts", String.valueOf(failedAttempts));

        if (failedAttempts >= 5) {
            triggerLockoutProtocol();
        }
    }

    @Override
    public void onForgotPasswordRequested() {
        showForgotPasswordFragment();
    }

    @Override
    public void onRegistrationRequested() {
        showRegistrationFragment();
    }

    // -------------------------------------------------------------- \\
    // --------------- REGISTRATION FRAGMENT CALLBACK --------------- \\
    // -------------------------------------------------------------- \\

    @Override
    public void onRegistrationSuccess(String agentID) {
        showSuccessState();

        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onLoginSuccess(agentID);
            }
        }, 1500);
    }

    @Override
    public void onRegistrationFailure(String error) {
        // Handle registration failure
        showErrorState(error);
    }

    @Override
    public void onLoginInsteadRequired() {
        showLoginFragment();
    }

    // -------------------------------------------------------------- \\
    // --------------- REGISTRATION FRAGMENT CALLBACK --------------- \\
    // -------------------------------------------------------------- \\

    @Override
    public void onPasswordResetSuccess() {
        showLoginFragment();
        showSuccessState(getString(R.string.credentials_recovered));
    }

    @Override
    public void onPasswordResetFailure() {
        showErrorState(getString(R.string.recovery_protocol_failed));
    }

    @Override
    public void onCancelRecovery() {
        getSupportFragmentManager().popBackStack();
    }

    // ------------------------------------------------- \\
    // --------------- Lifecycle methods --------------- \\
    // ------------------------------------------------- \\

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}