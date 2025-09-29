package org.obsidian.omot.ui.auth;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.obsidian.omot.R;
import org.obsidian.omot.security.CredentialValidator;
import org.obsidian.omot.security.SecurityManager;
import org.obsidian.omot.utils.AnimationUtilities;

public class ForgotPassFragment extends Fragment {

    private TextInputEditText edtCodename, edtSecurityAnswer, edtNewCipherKey, edtConfirmCipherKey;
    private TextView tvSecurityQuestion, tvStatus;
    private MaterialButton btnVerify, btnReset, btnCancel;
    private ProgressBar progressVerify, progressReset;
    private LinearLayout layoutStatusGroup, layoutNewPassword;

    private ForgotPasswordCallback callback;
    private String currentCodename;
    private String storedSecurityAnswer;

    public interface ForgotPasswordCallback {
        void onPasswordResetSuccess();
        void onPasswordResetFailure();
        void onCancelRecovery();
    }

    public ForgotPassFragment() {}

    public static ForgotPassFragment newInstance() {
        return new ForgotPassFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof ForgotPasswordCallback) {
            callback = (ForgotPasswordCallback) getActivity();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_pass, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializationViews(view);
        setupListeners();
    }

    // ------------------------------------------------ \\
    // --------------- onCreate methods --------------- \\
    // ------------------------------------------------ \\

    private void initializationViews(View view) {
        edtCodename = view.findViewById(R.id.edt_codename);
        edtSecurityAnswer = view.findViewById(R.id.edt_security_answer);
        edtNewCipherKey = view.findViewById(R.id.edt_new_cipher_key);
        edtConfirmCipherKey = view.findViewById(R.id.edt_confirm_cipher_key);
        tvSecurityQuestion = view.findViewById(R.id.tv_security_question);
        tvStatus = view.findViewById(R.id.tv_status);
        btnVerify = view.findViewById(R.id.btn_verify);
        btnReset = view.findViewById(R.id.btn_reset);
        btnCancel = view.findViewById(R.id.btn_cancel);
        progressVerify = view.findViewById(R.id.progress_verify);
        progressReset = view.findViewById(R.id.progress_reset);
        layoutStatusGroup = view.findViewById(R.id.layout_status_group);
        layoutNewPassword = view.findViewById(R.id.layout_new_password);
    }

    private void setupListeners() {
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyIdentity();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null) {
                    callback.onCancelRecovery();
                }
            }
        });

        // Auto-lookup security question when codename is entered
        edtCodename.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                lookupSecurityQuestion();
            }
        });
    }

    // ------------------------------------------------- \\
    // --------------- Listeners methods --------------- \\
    // ------------------------------------------------- \\

    private void lookupSecurityQuestion() {
        String codename = edtCodename.getText().toString().trim();
        if (codename.isEmpty()) {
            return;
        }

        if (!isAdded() || getContext() == null) {
            return;
        }

        SecurityManager manager = SecurityManager.getInstance(requireContext());
        String securityQuestion = manager.retrieveSensitiveData("security_question_" + codename);
        storedSecurityAnswer = manager.retrieveSensitiveData("security_answer_" + codename);

        if (securityQuestion != null && storedSecurityAnswer != null) {
            currentCodename = codename;
            tvSecurityQuestion.setText(securityQuestion);
            tvSecurityQuestion.setVisibility(View.VISIBLE);
            showStatus(getString(R.string.security_question_found), R.color.omot_red_alert);
        } else {
            tvSecurityQuestion.setVisibility(View.GONE);
            showStatus(getString(R.string.no_agent_found), R.color.omot_red_alert);
        }
    }

    private void verifyIdentity() {
        String codename = edtCodename.getText().toString().trim();
        String answer = edtSecurityAnswer.getText().toString().trim();

        if (codename.isEmpty()) {
            showStatus(getString(R.string.codename_empty), R.color.omot_red_alert);
            return;
        }

        if (answer.isEmpty()) {
            showStatus(getString(R.string.security_answer_empty), R.color.omot_red_alert);
            return;
        }

        if (storedSecurityAnswer == null) {
            showStatus(getString(R.string.codename_first), R.color.omot_red_alert);
            return;
        }

        showVerifyLoading(true);

        // Simulate verification process
        new Thread(() -> {
            try {
                Thread.sleep(1500); // Simulate verification delay

                requireActivity().runOnUiThread(() -> {
                    showVerifyLoading(false);

                    if (answer.equalsIgnoreCase(storedSecurityAnswer)) {
                        onVerificationSuccess();
                    } else {
                        onVerificationFailure();
                    }
                });
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                requireActivity().runOnUiThread(() -> {
                    showVerifyLoading(false);
                    onVerificationFailure();
                });
            }
        }).start();
    }

    private void resetPassword() {
        String newPassword = edtNewCipherKey.getText().toString().trim();
        String confirmPassword = edtConfirmCipherKey.getText().toString().trim();

        if (!validateNewPassword(newPassword, confirmPassword)) {
            return;
        }

        showResetLoading(true);

        // Simulate password reset process
        new Thread(() -> {
            try {
                Thread.sleep(1500); // Simulate reset delay

                requireActivity().runOnUiThread(() -> {
                    showResetLoading(false);

                    if (performPasswordReset(newPassword)) {
                        onPasswordResetSuccess();
                    } else {
                        onPasswordResetFailure();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();;
                requireActivity().runOnUiThread(() -> {
                    showResetLoading(false);
                    onPasswordResetFailure();
                });
            }
        }).start();
    }

    // ---------------------------------------------------- \\
    // --------------- Verification methods --------------- \\
    // ---------------------------------------------------- \\

    private void onVerificationSuccess() {
        layoutNewPassword.setVisibility(View.VISIBLE);
        showStatus(getString(R.string.verification_successful), R.color.omot_green_success);
        AnimationUtilities.slideInFromRight(layoutNewPassword);
    }

    private void onVerificationFailure() {
        showStatus(getString(R.string.wrong_security_answer), R.color.omot_red_alert);
        AnimationUtilities.shakeView(btnVerify);
    }

    // ------------------------------------------------------ \\
    // --------------- Password reset methods --------------- \\
    // ------------------------------------------------------ \\

    private boolean validateNewPassword(String newPass, String confirmPass) {
        if (!CredentialValidator.isPasswordStrong(newPass)) {
            showStatus(getString(R.string.new_password_secure), R.color.omot_red_alert);
            return false;
        }

        if (!TextUtils.equals(newPass, confirmPass)) {
            showStatus(getString(R.string.password_mismatch), R.color.omot_red_alert);
            return false;
        }

        return true;
    }

    private boolean performPasswordReset(String newPass) {
        try {
            SecurityManager manager = SecurityManager.getInstance(requireContext());

            // Generate new salt and hash
            String newSalt = CredentialValidator.generateSalt();
            String newHash = CredentialValidator.hashPassword(newPass, newSalt);

            // Update stored credentials
            manager.storeSensitiveData("hash_" + currentCodename, newHash);
            manager.storeSensitiveData("salt_" + currentCodename, newSalt);

            // Log the recovery event
            manager.storeSensitiveData("recovery_timestamp_" + currentCodename, String.valueOf(System.currentTimeMillis()));

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void onPasswordResetSuccess() {
        showStatus(getString(R.string.reset_success), R.color.omot_green_success);

        if (callback != null) {
            new Handler().postDelayed(() -> {
                callback.onPasswordResetSuccess();
            }, 2000);
        }
    }

    private void onPasswordResetFailure() {
        showStatus(getString(R.string.reset_failed), R.color.omot_red_alert);
        AnimationUtilities.shakeView(btnReset);

        if (callback != null) {
            callback.onPasswordResetFailure();
        }
    }

    // ----------------------------------------------------- \\
    // --------------- Other private methods --------------- \\
    // ----------------------------------------------------- \\

    private void showVerifyLoading(boolean loading) {
        btnVerify.setEnabled(!loading);
        progressVerify.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnVerify.setText(loading ? getString(R.string.verifying) : getString(R.string.button_verify_identity));
    }

    private void showResetLoading(boolean loading) {
        btnReset.setEnabled(!loading);
        progressReset.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnReset.setText(loading ? getString(R.string.resetting) : getString(R.string.button_reset_password));
    }

    private void showStatus(String message, int coloRes) {
        tvStatus.setText(message);
        tvStatus.setTextColor(getColor(coloRes));
        layoutStatusGroup.setVisibility(View.VISIBLE);
    }

    private int getColor(int colorRes) {
        return requireContext().getColor(colorRes);
    }
}