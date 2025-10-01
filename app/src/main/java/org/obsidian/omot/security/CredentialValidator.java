package org.obsidian.omot.security;

import android.content.Context;
import android.util.Base64;

import org.obsidian.omot.AppContext;
import org.obsidian.omot.data.daos.AgentDAO;
import org.obsidian.omot.data.entities.Agent;
import org.obsidian.omot.data.repository.DBRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import timber.log.Timber;

public class CredentialValidator {
    private static final String TAG = "CredentialValidator";
    private static final String HASH_ALGORITHM = "SHA-256";

    public static boolean validateCredentials(Context context, String codename, String password) {
        DBRepository repository = DBRepository.getInstance(context);
        AgentDAO dao = repository.getAgentDAO();

        Agent agent = dao.getAgentByCodename(codename);
        if (agent == null || agent.isAccountLocked()) {
            return false;
        }

        String computedHash = hashPassword(password, agent.getSalt());
        return computedHash != null && computedHash.equals(agent.getPasswordHash());
    }

    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(Base64.decode(salt, Base64.NO_WRAP));
            byte[] hash = digest.digest(password.getBytes());
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            Timber.tag(TAG).e("Hashing algorithm not available: %s", e.getMessage());
            return null;
        }
    }

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32];
        random.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    public static boolean isPasswordStrong(String password) {
        if (password.length() < 8) return false;

        boolean hasUpper = !password.equals(password.toLowerCase());
        boolean hasLower = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}