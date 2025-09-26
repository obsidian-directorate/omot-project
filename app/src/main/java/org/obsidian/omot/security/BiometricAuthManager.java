package org.obsidian.omot.security;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import org.obsidian.omot.R;

import java.util.concurrent.Executor;

import timber.log.Timber;

public class BiometricAuthManager {
    private static final String TAG = "BiometricAuthManager";
    private final Context context;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    public interface BiometricAuthCallback {
        void onAuthSuccess();
        void onAuthError(int errorCoden, CharSequence errString);
        void onAuthFailed();
    }

    public BiometricAuthManager(Context context) {
        this.context = context;
        initializeBiometricPrompt();
    }

    private void initializeBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(context);

        biometricPrompt = new BiometricPrompt((FragmentActivity) context, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Timber.tag(TAG).d("Authentication error: " + errorCode + " - " + errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Timber.tag(TAG).d("Authentication succeeded");
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Timber.tag(TAG).d("Authentication failed");
            }
        });

        // Build the prompt info
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(context.getString(R.string.biometric_title))
                .setSubtitle(context.getString(R.string.biometric_subtitle))
                .setDescription(context.getString(R.string.biometric_description))
                .setNegativeButtonText(context.getString(R.string.biometric_neg_btn))
                .setConfirmationRequired(false)
                .build();
    }

    public boolean isBiometricAvailable() {
        BiometricManager manager = BiometricManager.from(context);
        switch (manager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Timber.tag(TAG).d("Biometric authentication available");
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Timber.tag(TAG).d("No biometric features available");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Timber.tag(TAG).d("Biometric features currently unavailable");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Timber.tag(TAG).d("No biometric credentials enrolled");
                return false;
            default:
                return false;
        }
    }

    public void authenticateUser(BiometricAuthCallback callback) {
        if (!isBiometricAvailable()) {
            callback.onAuthError(-1, "Biometric authentication not available");
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(context);

        biometricPrompt = new BiometricPrompt((FragmentActivity) context, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                callback.onAuthError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                // Store successful authentication timestamp
                SecurityManager.getInstance(context).storeSensitiveData(
                        "last_biometric_auth",
                        String.valueOf(System.currentTimeMillis())
                );
                callback.onAuthSuccess();
            }

            @Override
            public void onAuthenticationFailed() {
                callback.onAuthFailed();
            }
        });

        biometricPrompt.authenticate(promptInfo);
    }

    public boolean isStrongBiometricSupported() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return false;
        }

        BiometricManager manager = BiometricManager.from(context);
        return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS;
    }
}