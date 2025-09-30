package org.obsidian.omot.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.obsidian.omot.R;
import org.obsidian.omot.security.SecurityManager;

public abstract class BaseFragment extends Fragment {

    protected SecurityManager manager;
    protected String currentClearance;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = SecurityManager.getInstance(context);

        // Get current clearance level
        String lastAgent = manager.retrieveSensitiveData("last_authenticated_user");
        if (lastAgent != null) {
            currentClearance = manager.retrieveSensitiveData("clearance_level_" + lastAgent);
        }

        if (currentClearance == null) {
            currentClearance = getString(R.string.clearance_beta);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        applyClearanceRestrictions();
    }

    protected abstract void applyClearanceRestrictions();

    protected boolean hasClearance(String requiredClearance) {
        String[] levels = {"BETA", "ALPHA", "OMEGA", "SHADOW"};
        int requiredIndex = -1;
        int currentIndex = -1;

        for (int i = 0; i < levels.length; i++) {
            if (levels[i].equals(requiredClearance)) requiredIndex = i;
            if (levels[i].equals(currentClearance)) currentIndex = i;
        }

        return currentIndex >= requiredIndex;
    }
}