package org.obsidian.omot.security;

import org.obsidian.omot.AppContext;

import timber.log.Timber;

public class SecurityLogger {
    private static final String TAG = "SecurityLogger";

    public static void logSecurityEvent(String eventType, String details) {
        Timber.tag(TAG).i("SECURITY_EVENT: " + eventType + " - " + details);

        // Store in encrypted preferences for audit trail
        String timestamp = String.valueOf(System.currentTimeMillis());
        String logEntry = timestamp + "|" + eventType + "|" + details;

        // TODO: Integrate with database when available
        // For now, store in encrypted prefs
        SecurityManager.getInstance(AppContext.getContext())
                .storeSensitiveData("security_log_" + timestamp, logEntry);
    }

    public static void logAuthenticationAttempt(String agentID, boolean success, String method) {
        String eventType = success ? "AUTH_SUCCESS" : "AUTH_FAILED";
        logSecurityEvent(eventType, "Agent: " + agentID + ", Method " + method);
    }

    public static void logPanicModeActivation(String trigger) {
        logSecurityEvent("PANIC_MODE_ACTIVATED", "Trigger: " + trigger);
    }
}