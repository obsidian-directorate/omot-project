package org.obsidian.omot.security;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import timber.log.Timber;

public class AppIntegrityValidator {
    private static final String TAG = "AppIntegrityValidator";

    public static String getAppSignature(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);

            Signature[] signatures = packageInfo.signatures;
            if (signatures.length > 0) {
                Signature signature = signatures[0];
                return calculateSHA256(signature.toByteArray());
            }
        } catch (PackageManager.NameNotFoundException e) {
            Timber.tag(TAG).e("Package mot found: %s", e.getMessage());
        }
        return null;
    }

    private static String calculateSHA256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            Timber.tag(TAG).e("SHA-256 algorithm not available");
            return null;
        }
    }

    public static boolean verifyAppIntegrity(Context context, String expectedSignature) {
        String currentSignature = getAppSignature(context);
        return currentSignature != null && currentSignature.equals(expectedSignature);
    }
}