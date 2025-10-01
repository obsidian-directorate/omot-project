package org.obsidian.omot.ui.auth;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import org.obsidian.omot.R;
import org.obsidian.omot.data.daos.AgentDAO;
import org.obsidian.omot.data.entities.Agent;
import org.obsidian.omot.data.repository.DBRepository;
import org.obsidian.omot.security.CredentialValidator;
import org.obsidian.omot.security.SecurityManager;
import org.obsidian.omot.utils.AnimationUtilities;

import java.util.Arrays;
import java.util.List;

public class RegistrationFragment extends Fragment {

    private TextInputEditText edtCodename, edtCipherKey, edtSecurityAnswer;
    private AppCompatSpinner spnSecurityQuestion;
    private SwitchMaterial switchBiometric;
    private MaterialButton btnRegister, btnLoginBack;
    private ProgressBar progressRegister;
    private LinearLayout layoutStatusGroup;
    private TextView tvStatus, tvPasswordStrength;
    private DBRepository repository;
    private AgentDAO dao;

    private RegistrationCallback callback;

    // Security question
    private List<String> securityQuestions;

    public interface RegistrationCallback {
        void onRegistrationSuccess(String agentID);
        void onRegistrationFailure(String error);
        void onLoginInsteadRequired();
    }

    public RegistrationFragment() {}

    public static RegistrationFragment newInstance() {
        return new RegistrationFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof RegistrationCallback) {
            callback = (RegistrationCallback) getActivity();
        }

        repository = DBRepository.getInstance(requireContext());
        dao = repository.getAgentDAO();
        securityQuestions = Arrays.asList(getResources().getStringArray(R.array.security_questions));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupSecurityQuestionsSpinner();
        setupListeners();
    }

    // ------------------------------------------------ \\
    // --------------- onCreate methods --------------- \\
    // ------------------------------------------------ \\

    private void initializeViews(View view) {
        edtCodename = view.findViewById(R.id.edt_codename);
        edtCipherKey = view.findViewById(R.id.edt_cipher_key);
        edtSecurityAnswer = view.findViewById(R.id.edt_security_answer);
        spnSecurityQuestion = view.findViewById(R.id.spn_security_question);
        switchBiometric = view.findViewById(R.id.switch_biometric);
        btnRegister = view.findViewById(R.id.btn_register);
        btnLoginBack = view.findViewById(R.id.btn_login_back);
        progressRegister = view.findViewById(R.id.progress_register);
        layoutStatusGroup = view.findViewById(R.id.layout_status_group);
        tvStatus = view.findViewById(R.id.tv_status);
        tvPasswordStrength = view.findViewById(R.id.tv_password_strength);
    }

    private void setupSecurityQuestionsSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinner_item_security_question,
                securityQuestions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSecurityQuestion.setAdapter(adapter);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegistration();
            }
        });

        btnLoginBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null) {
                    callback.onLoginInsteadRequired();
                }
            }
        });

        // Real-time password strength indicator
        edtCipherKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                updatePasswordStrengthIndicator(editable.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
        });
    }

    // ------------------------------------------------- \\
    // --------------- Listeners methods --------------- \\
    // ------------------------------------------------- \\

    private void updatePasswordStrengthIndicator(String password) {
        if (password.isEmpty()) {
            tvPasswordStrength.setVisibility(View.GONE);
            return;
        }

        tvPasswordStrength.setVisibility(View.VISIBLE);

        if (CredentialValidator.isPasswordStrong(password)) {
            tvPasswordStrength.setText(getString(R.string.level_maximum));
            tvPasswordStrength.setTextColor(getColor(R.color.omot_green_success));
        } else if (password.length() >= 8) {
            tvPasswordStrength.setText(getString(R.string.level_moderate));
            tvPasswordStrength.setTextColor(getColor(R.color.omot_yellow_caution));
        } else {
            tvPasswordStrength.setText(getString(R.string.level_weak));
            tvPasswordStrength.setTextColor(getColor(R.color.omot_red_alert));
        }
    }

    private void attemptRegistration() {
        String codename = edtCodename.getText().toString().trim();
        String cipherKey = edtCipherKey.getText().toString().trim();
        String securityQuestion = spnSecurityQuestion.getSelectedItem().toString();
        String securityAnswer = edtSecurityAnswer.getText().toString().trim();
        boolean biometricEnabled = switchBiometric.isChecked();

        if (!validateRegistrationInputs(codename, cipherKey, securityAnswer)) {
            return;
        }

        showLoadingState(true);
        hideStatus();

        // Simulate registration process
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Simulate registration delay

                requireActivity().runOnUiThread(() -> {
                    if (performRegistration(codename, cipherKey, securityQuestion, securityAnswer, biometricEnabled)) {
                        onRegistrationSuccess(codename);
                    } else {
                        onRegistrationFailure(getString(R.string.codename_taken));
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                requireActivity().runOnUiThread(() -> {
                    onRegistrationFailure(getString(R.string.registration_interrupted));
                });
            }
        }).start();
    }

    // ---------------------------------------------------- \\
    // --------------- Registration methods --------------- \\
    // ---------------------------------------------------- \\

    private boolean validateRegistrationInputs(String codename, String cipherKey, String securityAnswer) {
        boolean isValid = true;

        if (codename.isEmpty() || codename.length() < 3) {
            edtCodename.setError(getString(R.string.codename_length_short));
            isValid = false;
        }

        if (!CredentialValidator.isPasswordStrong(cipherKey)) {
            edtCipherKey.setError(getString(R.string.password_secure));
            isValid = false;
        }

        if (securityAnswer.isEmpty()) {
            edtSecurityAnswer.setError(getString(R.string.security_answer_required));
            isValid = false;
        }

        return isValid;
    }

    private boolean performRegistration(String codename, String cipherKey, String securityQuestion, String securityAnswer, boolean biometricEnabled) {
        try {
            // Check if the codename is already taken
            if (dao.isCodenameTaken(codename)) {
                return false;
            }

            // Generate agent ID
            String agentID = "AGENT-" + System.currentTimeMillis();

            // Generate salt and hash password
            String salt = CredentialValidator.generateSalt();
            String passwordHash = CredentialValidator.hashPassword(cipherKey, salt);
            String securityAnswerHash = CredentialValidator.hashPassword(securityAnswer, salt);

            // Create new agent
            Agent newAgent = new Agent(agentID, codename, passwordHash, salt, getString(R.string.clearance_beta));
            newAgent.setSecurityQuestion(securityQuestion);
            newAgent.setSecurityAnswerHash(securityAnswerHash);
            newAgent.setBiometricEnabled(biometricEnabled);
            newAgent.setLastLoginTimestamp(System.currentTimeMillis());

            // Insert into database
            boolean success = dao.insertAgent(newAgent);

            if (success) {
                // Store in security manager for session
                SecurityManager manager = SecurityManager.getInstance(requireContext());
                manager.storeSensitiveData("last_authenticated_user", codename);
                manager.storeSensitiveData("clearance_level_" + codename, getString(R.string.clearance_beta));
                manager.storeSensitiveData("first_run", "false");
            }

            return success;
        } catch (Exception e) {
            return false;
        }
    }

    private void onRegistrationSuccess(String agentID) {
        showLoadingState(false);
        showStatus(requireContext().getString(R.string.registration_complete), R.color.omot_red_alert);

        if (callback != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    callback.onRegistrationSuccess(agentID);
                }
            }, 1500);
        }
    }

    private void onRegistrationFailure(String error) {
        showLoadingState(false);
        showStatus(error, R.color.omot_red_alert);
        AnimationUtilities.shakeView(btnRegister);

        if (callback != null) {
            callback.onRegistrationFailure(error);
        }
    }

    // ----------------------------------------------------- \\
    // --------------- Other private methods --------------- \\
    // ----------------------------------------------------- \\

    private int getColor(int colorRes) {
        return requireContext().getColor(colorRes);
    }

    private void showLoadingState(boolean loading) {
        btnRegister.setEnabled(!loading);
        btnLoginBack.setEnabled(!loading);

        if (loading) {
            btnRegister.setText(getString(R.string.registering_agent));
            progressRegister.setVisibility(View.VISIBLE);
        } else {
            btnRegister.setText(getString(R.string.button_register));
            progressRegister.setVisibility(View.GONE);
        }
    }

    private void showStatus(String message, int colorRes) {
        tvStatus.setText(message);
        tvStatus.setText(getColor(colorRes));
        layoutStatusGroup.setVisibility(View.VISIBLE);
    }

    private void hideStatus() {
        layoutStatusGroup.setVisibility(View.GONE);
    }
}