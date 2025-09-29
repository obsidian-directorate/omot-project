package org.obsidian.omot.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Debug;
import android.util.Base64;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.File;

import timber.log.Timber;

public class SecurityManager {
    private static final String TAG = "SecurityManager";
    private static final String ENCRYPTED_PREFS_NAME = "omot_secure_prefs";
    private static final String KEY_MASTER_ALIAS = "omot_master_key";
    private static final String KEY_PANIC_MODE = "panic_mode_activated";
    private static final String KEY_TAMPER_COUNT = "tamper_detection_count";
    private static final String KEY_LAST_SECURITY_CHECK = "last_security_check";

    private static SecurityManager instance;
    private final Context context;
    private SharedPreferences encryptedPreferences;
    private MasterKey masterKey;

    // Security state
    private boolean isPanicMode = false;
    private int tamperDetectionCount = 0;

    public static synchronized SecurityManager getInstance(Context context) {
        if (instance == null) {
            instance = new SecurityManager(context.getApplicationContext());
        }
        return instance;
    }

    private SecurityManager(Context context) {
        this.context = context;
        initializeSecuritySystems();
    }

    private void initializeSecuritySystems() {
        try {
            // Initialize EncryptedSharedPreferences
            initializeEncryptedPreferences();

            // Check for existing panic mode state
            isPanicMode = encryptedPreferences.getBoolean(KEY_PANIC_MODE, false);
            tamperDetectionCount = encryptedPreferences.getInt(KEY_TAMPER_COUNT, 0);

            // Perform initial security checks
            performSecurityChecks();
        } catch (Exception e) {
            Timber.tag(TAG).e("Security initialization failed: %s", e.getMessage());
            handleSecurityFailure();
        }
    }

    private void initializeEncryptedPreferences() throws Exception {
        masterKey = new MasterKey.Builder(context, KEY_MASTER_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        encryptedPreferences = EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    // === ENCRYPTED PREFERENCES METHODS ===

    public void storeSensitiveData(String key, String value) {
        if (isPanicMode) return;

        try {
            encryptedPreferences.edit()
                    .putString(encryptKey(key), encryptValue(value))
                    .apply();
        } catch (Exception e) {
            Timber.tag(TAG).e("Failed to store sensitive data: %s", e.getMessage());
        }
    }

    public String retrieveSensitiveData(String key) {
        if (isPanicMode) return null;

        try {
            String encryptedValue = encryptedPreferences.getString(encryptKey(key), null);
            return encryptedValue != null ? decryptValue(encryptedValue) : null;
        } catch (Exception e) {
            Timber.tag(TAG).e("Failed to retrieve sensitive data: %s", e.getMessage());
            return null;
        }
    }

    public void clearSensitiveData() {
        encryptedPreferences.edit().clear().apply();
    }

    private String encryptKey(String key) {
        return Base64.encodeToString(key.getBytes(), Base64.NO_WRAP);
    }

    private String encryptValue(String value) {
        return Base64.encodeToString(value.getBytes(), Base64.NO_WRAP);
    }

    private String decryptValue(String encryptedValue) {
        return new String(Base64.decode(encryptedValue, Base64.NO_WRAP));
    }

    // === ROOT DETECTION & TAMPER DETECTION ===

    private void performSecurityChecks() {
        new Thread(() -> {
            if (detectRootAccess()) {
                Timber.tag(TAG).w("Root access detected!");
                onTamperDetected();
            }

            if (detectDebugger()) {
                Timber.tag(TAG).w("Debugger detected!");
                onTamperDetected();
            }

            if (detectAppTampering()) {
                Timber.tag(TAG).w("App tampering detected!");
                onTamperDetected();
            }

            // Update last check timestamp
            encryptedPreferences.edit()
                    .putLong(KEY_LAST_SECURITY_CHECK, System.currentTimeMillis())
                    .apply();
        }).start();
    }

    private boolean detectRootAccess() {
        // Check for root binaries
        String[] paths = {
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su"
        };

        for (String path : paths) {
            if (new File(path).exists()) {
                return true;
            }
        }

        // Check for test keys (custom ROM indicator)
        try {
            String buildTags = Build.TAGS;
            if (buildTags != null && buildTags.contains("test-keys")) {
                return true;
            }
        } catch (Exception e) {
            // Ignore
        }

        return false;
    }

    private boolean detectDebugger() {
        return Debug.isDebuggerConnected();
    }

    private boolean detectAppTampering() {
        try {
            // Check if app signature matches expected value
            String appSignature = AppIntegrityValidator.getAppSignature(context);
            String expectedSignature = retrieveSensitiveData("app_signature");

            if (expectedSignature == null) {
                // First run, store current signature
                storeSensitiveData("app_signature", appSignature);
                return false;
            }

            return !appSignature.equals(expectedSignature);
        } catch (Exception e) {
            return true; // Assume tampering if check fails
        }
    }

    private void onTamperDetected() {
        tamperDetectionCount++;
        encryptedPreferences.edit()
                .putInt(KEY_TAMPER_COUNT, tamperDetectionCount)
                .apply();

        // Trigger panic mode after 3 tamper detections
        if (tamperDetectionCount >= 3) {
            activatePanicMode();
        }
    }

    // === PANIC MODE SYSTEM ===

    public void activatePanicMode() {
        isPanicMode = true;
        encryptedPreferences.edit()
                .putBoolean(KEY_PANIC_MODE, true)
                .apply();

        // Trigger data wipe
        triggerDataWipe();

        Timber.tag(TAG).w("PANIC MODE ACTIVATED - Data wipe initiated");
    }

    public void deactivatePanicMode() {
        isPanicMode = false;
        tamperDetectionCount = 0;
        encryptedPreferences.edit()
                .putBoolean(KEY_PANIC_MODE, false)
                .putInt(KEY_TAMPER_COUNT, 0)
                .apply();
    }

    public boolean isPanicModeActive() {
        return isPanicMode;
    }

    private void triggerDataWipe() {
        // Clear all sensitive data
        clearSensitiveData();

        // Wipe database (will be implemented in database step)
        DatabaseWipeManager.wipeAllData(context);

        // Log the event
        SecurityLogger.logSecurityEvent("PANIC_MODE_ACTIVATED", "Automatic data wipe");
    }

    private void handleSecurityFailure() {
        // Emergency fallback - basic shared prefs with warning
        Timber.tag(TAG).e("CRITICAL: Security systems compromised");
        SecurityLogger.logSecurityEvent("SECURITY_SYSTEM_FAILURE", "Fallback mode activated");
    }

    // === SECURITY STATE METHODS ===

    public boolean isSecurityCompromised() {
        return isPanicMode || tamperDetectionCount > 0;
    }

    public int getTamperDetectionCount() {
        return tamperDetectionCount;
    }

    // === LOCKOUT SYSTEM ===

    public boolean isAccountLocked() {
        String lockoutTime = retrieveSensitiveData("lockout_until");
        if (lockoutTime != null) {
            long lockoutUntil = Long.parseLong(lockoutTime);
            return System.currentTimeMillis() < lockoutUntil;
        }
        return false;
    }

    public long getRemainingLockoutTime() {
        String lockoutTime = retrieveSensitiveData("lockout_until");
        if (lockoutTime != null) {
            long lockoutUntil = Long.parseLong(lockoutTime);
            long remaining = lockoutUntil - System.currentTimeMillis();
            return Math.max(0, remaining);
        }
        return 0;
    }
}