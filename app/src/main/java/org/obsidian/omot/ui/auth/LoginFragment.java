package org.obsidian.omot.ui.auth;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.obsidian.omot.R;
import org.obsidian.omot.security.BiometricAuthManager;
import org.obsidian.omot.security.CredentialValidator;
import org.obsidian.omot.security.SecurityManager;
import org.obsidian.omot.utils.AnimationUtilities;

public class LoginFragment extends Fragment {

    private TextInputEditText edtCodename, edtCipherKey;
    private MaterialButton btnLogin, btnBiometric;
    private Button btnForgotPassword, btnRegister;
    private ProgressBar progressLogin;
    private LinearLayout layoutStatusGroup;
    private TextView tvStatus;

    private BiometricAuthManager biometricAuthManager;
    private LoginCallback callback;

    public interface LoginCallback {
        void onLoginSuccess(String agentID);
        void onLoginFailure();
        void onForgotPasswordRequested();
        void onRegistrationRequested();
    }

    public LoginFragment() {}

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        biometricAuthManager = new BiometricAuthManager(requireContext());

        if (getActivity() instanceof LoginCallback) {
            callback = (LoginCallback) getActivity();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupUI();
        setupListeners();
        checkBiometricAvailability();
    }

    // ------------------------------------------------ \\
    // --------------- onCreate methods --------------- \\
    // ------------------------------------------------ \\

    private void initializeViews(View view) {
        edtCodename = view.findViewById(R.id.edt_codename);
        edtCipherKey = view.findViewById(R.id.edt_cipher_key);
        btnLogin = view.findViewById(R.id.btn_login);
        btnBiometric = view.findViewById(R.id.btn_biometric);
        btnForgotPassword = view.findViewById(R.id.btn_forgot_password);
        btnRegister = view.findViewById(R.id.btn_register);
        progressLogin = view.findViewById(R.id.progress_login);
        layoutStatusGroup = view.findViewById(R.id.layout_status_group);
        tvStatus = view.findViewById(R.id.tv_status);
    }

    private void setupUI() {
        // Set up text input styles
        edtCodename.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.roboto_mono));
        edtCipherKey.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.roboto_mono));

        // Add text watchers for real-time validation
        edtCodename.addTextChangedListener(new AuthenticationTextWatcher());
        edtCipherKey.addTextChangedListener(new AuthenticationTextWatcher());
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        btnBiometric.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptBiometricLogin();
            }
        });

        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null) {
                    callback.onForgotPasswordRequested();
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null) {
                    callback.onRegistrationRequested();
                }
            }
        });
    }

    private void checkBiometricAvailability() {
        if (biometricAuthManager.isBiometricAvailable()) {
            btnBiometric.setVisibility(View.VISIBLE);
            AnimationUtilities.startPulseAnimation(btnBiometric, 1f, 1.1f, 800);
        } else {
            btnBiometric.setVisibility(View.GONE);
        }
    }

    // ------------------------------------------------- \\
    // --------------- Listeners methods --------------- \\
    // ------------------------------------------------- \\

    private void attemptLogin() {
        String codename = edtCodename.getText().toString().trim();
        String cipherKey = edtCipherKey.getText().toString().trim();

        if (!validateInputs(codename, cipherKey)) {
            return;
        }

        showLoadingState(true);

        // Simulate authentication process
        new Thread(() -> {
            try {
                Thread.sleep(1500); // Simulate network/DB delay

                requireActivity().runOnUiThread(() -> {
                    if (CredentialValidator.validateCredentials(codename, cipherKey)) {
                        onLoginSuccess(codename);
                    } else {
                        onLoginFailure();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void attemptBiometricLogin() {
        biometricAuthManager.authenticateUser(new BiometricAuthManager.BiometricAuthCallback() {
            @Override
            public void onAuthSuccess() {
                requireActivity().runOnUiThread(() -> {
                    // Retrieve stored credentials and auto-login
                    String lastUser = SecurityManager.getInstance(requireContext()).retrieveSensitiveData("last_authenticated_user");
                    if (lastUser != null) {
                        onLoginSuccess(lastUser);
                    }
                });
            }

            @Override
            public void onAuthError(int errorCoden, CharSequence errString) {
                requireActivity().runOnUiThread(() -> {
                    tvStatus.setText(getString(R.string.biometric_failed) + errString);
                    layoutStatusGroup.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onAuthFailed() {
                requireActivity().runOnUiThread(() -> {
                    tvStatus.setText(R.string.auth_failed);
                    layoutStatusGroup.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    // --------------------------------------------- \\
    // --------------- Other methods --------------- \\
    // --------------------------------------------- \\

    private boolean validateInputs(String codename, String cipherKey) {
        if (codename.isEmpty()) {
            edtCodename.setError(requireContext().getString(R.string.codename_required));
            return false;
        }

        if (cipherKey.isEmpty()) {
            edtCipherKey.setError(requireContext().getString(R.string.password_required));
            return false;
        }

        if (cipherKey.length() < 8) {
            edtCipherKey.setError(requireContext().getString(R.string.password_length_short));
            return false;
        }

        return true;
    }

    private void showLoadingState(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnBiometric.setEnabled(!loading);

        if (loading) {
            btnLogin.setText(R.string.authenticating);
            progressLogin.setVisibility(View.VISIBLE);
        } else {
            btnLogin.setText(R.string.button_login);
            progressLogin.setVisibility(View.GONE);
        }
    }

    private void onLoginSuccess(String agentID) {
        showLoadingState(false);

        // Store successful login
        SecurityManager.getInstance(requireContext())
                .storeSensitiveData("last_authenticated_user", agentID);

        if (callback != null) {
            callback.onLoginSuccess(agentID);
        }
    }

    private void onLoginFailure() {
        showLoadingState(false);
        AnimationUtilities.shakeView(btnLogin);

        tvStatus.setText(R.string.login_failed);
        layoutStatusGroup.setVisibility(View.VISIBLE);

        if (callback != null) {
            callback.onLoginFailure();
        }
    }

    // ----------------------------------------------------------------- \\
    // --------------- Authentication text watcher class --------------- \\
    // ----------------------------------------------------------------- \\

    private class AuthenticationTextWatcher implements TextWatcher {
        @Override
        public void afterTextChanged(Editable editable) {}

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            layoutStatusGroup.setVisibility(View.GONE);
        }
    }
}