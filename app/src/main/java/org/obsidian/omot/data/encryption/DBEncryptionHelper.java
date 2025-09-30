package org.obsidian.omot.data.encryption;

import android.content.Context;
import android.util.Base64;

import org.obsidian.omot.security.SecurityManager;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import timber.log.Timber;

public class DBEncryptionHelper {
    private static final String TAG = "DBEncryptionHelper";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String ALGORITHM = "AES";

    private final Context context;
    private final SecurityManager manager;

    public DBEncryptionHelper(Context context) {
        this.context = context.getApplicationContext();
        this.manager = SecurityManager.getInstance(context);
    }

    public String encryptField(String data) {
        if (data == null) return null;

        try {
            byte[] key = getEncryptionKey();
            byte[] iv = generateIV();

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

            byte[] encryptedData = cipher.doFinal(data.getBytes());

            // Combine IV + encrypted data for storage
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

            return Base64.encodeToString(combined, Base64.NO_WRAP);
        } catch (Exception e) {
            Timber.tag(TAG).e("Encryption failed: %s", e.getMessage());
            return null;
        }
    }

    public String decryptField(String encryptedData) {
        if (encryptedData == null) return null;

        try {
            byte[] combined = Base64.decode(encryptedData, Base64.NO_WRAP);
            byte[] key = getEncryptionKey();

            // Extract IV (first 16 bytes) and encrypted data
            byte[] iv = new byte[16];
            byte[] encrypted = new byte[combined.length - 16];
            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

            byte[] decryptData = cipher.doFinal(encrypted);
            return new String(decryptData);
        } catch (Exception e) {
            Timber.tag(TAG).e("Decryption failed: %s", e.getMessage());
            return null;
        }
    }

    public void rotateEncryptionKey() {
        // Generate new key = and re-encrypt all sensitive data
        byte[] newKey = generateKey();
        manager.storeSensitiveData("db_encryption_key", Base64.encodeToString(newKey, Base64.NO_WRAP));

        // TODO: Implement data re-encryption in background
        Timber.tag(TAG).i("Database encryption key rotated");
    }

    private byte[] getEncryptionKey() {
        // Retrieve or generate database encryption key
        String storedKey = manager.retrieveSensitiveData("db_encryption_key");
        if (storedKey != null) {
            return Base64.decode(storedKey, Base64.NO_WRAP);
        }

        // Generate new key
        byte[] newKey = generateKey();
        manager.storeSensitiveData("db_encryption_key", Base64.encodeToString(newKey, Base64.NO_WRAP));

        return newKey;
    }

    private byte[] generateKey() {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[32]; // 256-bit key
        random.nextBytes(key);
        return key;
    }

    private byte[] generateIV() {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16]; // 128-bit IV
        random.nextBytes(iv);
        return iv;
    }
}