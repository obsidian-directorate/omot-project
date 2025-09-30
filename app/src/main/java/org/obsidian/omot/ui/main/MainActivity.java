package org.obsidian.omot.ui.main;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

import org.obsidian.omot.R;
import org.obsidian.omot.data.entities.Agent;
import org.obsidian.omot.data.repository.DBRepository;
import org.obsidian.omot.security.SecurityManager;
import org.obsidian.omot.ui.auth.AuthenticationActivity;
import org.obsidian.omot.ui.fragments.DashboardFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView tvClearanceBadge, tvSecurityFooter;
    private MaterialToolbar toolbar;

    private SecurityManager manager;
    private DBRepository repository;
    private Agent currentAgent;
    private String currentClearance;

    // Fragment tags
    private static final String TAG_DASHBOARD = "dashboard";
    private static final String TAG_MISSIONS = "missions";
    private static final String TAG_DOSSIERS = "dossiers";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeDependencies();
        initializeUI();
        loadAgentData();
        setupNavigation();
        showInitialFragment();
    }

    // ------------------------------------------------ \\
    // --------------- onCreate methods --------------- \\
    // ------------------------------------------------ \\

    private void initializeDependencies() {
        manager = SecurityManager.getInstance(this);
        repository = DBRepository.getInstance(this);

        // Get agent data from intent or security manager
        String agentID = getIntent().getStringExtra("agent_id");
        if (agentID != null) {
            loadAgentFromDatabase(agentID);
        }
    }

    private void initializeUI() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        tvClearanceBadge = findViewById(R.id.tv_clearance_badge);
        tvSecurityFooter = findViewById(R.id.tv_security_footer);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        }
    }

    private void loadAgentData() {
        // Try to get agent from intent first
        String agentCodename = getIntent().getStringExtra("agent_codename");
        if (agentCodename != null) {
            loadAgentFromDatabase(agentCodename);
        } else {
            // Fallback to security manager
            String lastAgent = manager.retrieveSensitiveData("last_authenticated_user");
            if (lastAgent != null) {
                loadAgentFromDatabase(lastAgent);
            }
        }

        updateUIWithAgentData();
    }

    private void setupNavigation() {
        navigationView.setNavigationItemSelectedListener(this);
        filterNavigationMenuByClearance();

        // Set item icon colors
        navigationView.setItemIconTintList(null);
    }

    private void showInitialFragment() {
        // Show dashboard by default
        showFragment(new DashboardFragment(), TAG_DASHBOARD, false);

        // Update navigation selection
        navigationView.setCheckedItem(R.id.nav_dashboard);
    }

    // ---------------------------------------------------- \\
    // --------------- Data loading methods --------------- \\
    // ---------------------------------------------------- \\

    private void loadAgentFromDatabase(String identifier) {
        // This is a simplified version - proper agent loading implementation needed
        currentClearance = manager.retrieveSensitiveData("clearance_level_" + identifier);
        if (currentClearance == null) {
            currentClearance = "BETA"; // Default clearance
        }

        // Create a mock agent for now - replace with actual database call
        currentAgent = new Agent();
        currentAgent.setCodename(identifier);
        currentAgent.setClearanceCode(currentClearance);
        currentAgent.setLastLoginTimestamp(System.currentTimeMillis());
    }

    // --------------------------------------------------- \\
    // --------------- UI updating methods --------------- \\
    // --------------------------------------------------- \\

    private void updateUIWithAgentData() {
        if (currentAgent != null) {
            // Update navigation header
            View headerView = navigationView.getHeaderView(0);
            TextView tvCodenameView = headerView.findViewById(R.id.tv_agent_codename);
            TextView tvAgentClearance = headerView.findViewById(R.id.tv_agent_clearance);
            TextView tvLastLogin = headerView.findViewById(R.id.tv_last_login);

            tvCodenameView.setText(currentAgent.getCodename());
            tvAgentClearance.setText(currentClearance);

            if (currentAgent.getLastLoginTimestamp() != null) {
                tvLastLogin.setText(getString(R.string.last_login_timestamp) + formatLastLogin(currentAgent.getLastLoginTimestamp()));
            }

            // Update clearance badge
            updateClearanceBadge();

            // Update security footer base on clearance
            updateSecurityFooter();
        }
    }

    private void updateClearanceBadge() {
        if (tvClearanceBadge == null) return;;

        tvClearanceBadge.setVisibility(View.VISIBLE);
        tvClearanceBadge.setText(currentClearance);

        // Set background color based on clearance
        int bgRes = R.drawable.badge_background_beta;
        switch (currentClearance) {
            case "ALPHA": bgRes = R.drawable.badge_background_alpha; break;
            case "OMEGA": bgRes = R.drawable.badge_background_omega; break;
            case "SHADOW": bgRes = R.drawable.badge_background_shadow; break;
        }

        tvClearanceBadge.setBackgroundResource(bgRes);
    }

    private void updateSecurityFooter() {
        if (tvSecurityFooter == null) return;

        String footerText = getString(R.string.security_footer_2);

        switch (currentClearance) {
            case "SHADOW":
                footerText = getString(R.string.security_footer_shadow);
                tvSecurityFooter.setTextColor(getColor(R.color.omot_red_alert));
                break;
            case "OMEGA":
                footerText = getString(R.string.security_footer_omega);
                tvSecurityFooter.setTextColor(getColor(R.color.omot_green_success));
                break;
            default:
                tvSecurityFooter.setTextColor(getColor(R.color.omot_text_secondary));
                break;
        }

        tvSecurityFooter.setText(footerText);
    }

    private void updateMenuAppearance(Menu menu) {
        // Set different colors for different clearance groups
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);

            if (item.getGroupId() == R.id.group_command) {
                // OMEGA items - red tint
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    item.setIconTintList(ColorStateList.valueOf(getColor(R.color.clearance_omega)));
                }
            } else if (item.getGroupId() == R.id.group_shadow) {
                // SHADOW items - purple tint
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    item.setIconTintList(ColorStateList.valueOf(getColor(R.color.clearance_shadow)));
                }
            }
        }
    }

    private void updateToolbarTitle(String fragmentTag) {
        String title = getString(R.string.title_omot_terminal);

        switch (fragmentTag) {
            case TAG_DASHBOARD: title = getString(R.string.title_dashboard); break;
            case TAG_MISSIONS: title = getString(R.string.title_missions); break;
            case TAG_DOSSIERS: title = getString(R.string.title_dossiers); break;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    // ------------------------------------------------------- \\
    // --------------- Navigation view methods --------------- \\
    // ------------------------------------------------------- \\

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemID = menuItem.getItemId();
        Fragment fragment = null;
        String tag = null;
        boolean addToBackStack = true;

        if (itemID == R.id.nav_dashboard) {
            fragment = new DashboardFragment();
            tag = TAG_DASHBOARD;
        } else if (itemID == R.id.nav_logout) {
            performLogout();
            return true;
        }

        if (fragment != null) {
            showFragment(fragment, tag, addToBackStack);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        // Hide panic mode for SHADOW agents (they're monitored)
        if ("SHADOW".equals(currentClearance)) {
            menu.findItem(R.id.action_panic).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        } else if (itemID == R.id.action_panic) {
            triggerPanicMode();
            return true;
        } else if (itemID == R.id.action_voice) {
            startVoiceCommand();
            return true;
        } else if (itemID == R.id.action_camouflage) {
            toggleCamouflageMode();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // -------------------------------------------------------- \\
    // --------------- Toolbar listener methods --------------- \\
    // -------------------------------------------------------- \\

    private void triggerPanicMode() {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.panic_mode_title))
                .setMessage(getString(R.string.panic_mode_message))
                .setPositiveButton(getString(R.string.button_dialog_positive), (dialog, which) -> {
                    manager.activatePanicMode();
                    performLogout();
                })
                .setNegativeButton(getString(R.string.button_dialog_negative), null)
                .show();
    }

    private void startVoiceCommand() {
        // Launch voice command interface
        // This will be implemented in the voice features step
    }

    private void toggleCamouflageMode() {
        // Switch to camouflage UI (calculator, notes app, etc.)
        // This will be implemented in the security features step
    }

    // --------------------------------------------------------------- \\
    // --------------- Fragment and navigation methods --------------- \\
    // --------------------------------------------------------------- \\

    private void filterNavigationMenuByClearance() {
        Menu menu = navigationView.getMenu();

        // Hide all clearance-based groups initially
        menu.setGroupVisible(R.id.group_comms, false);
        menu.setGroupVisible(R.id.group_tactical, false);
        menu.setGroupVisible(R.id.group_command, false);
        menu.setGroupVisible(R.id.group_shadow, false);

        // Show items based on clearance level
        switch (currentClearance) {
            case "SHADOW":
                menu.setGroupVisible(R.id.group_shadow, true);
                // SHADOW has limited access - break early
                break;
            case "OMEGA":
                menu.setGroupVisible(R.id.group_command, true);
                // Fall through - OMEGA gets all lower access
            case "ALPHA":
                menu.setGroupVisible(R.id.group_tactical, true);
                menu.setGroupVisible(R.id.group_comms, true);
                // Fall through - ALPHA and BETA access plus more
            case "BETA":
                // BETA gets basic access (already visible)
                break;
        }

        // Update menu item appearances based on clearance
        updateMenuAppearance(menu);
    }

    private void showFragment(Fragment fragment, String tag, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );

        transaction.replace(R.id.fragment_container, fragment, tag);

        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }

        transaction.commit();

        // Update toolbar title based on fragment
        updateToolbarTitle(tag);
    }

    private void performLogout() {
        // Clear session data
        manager.storeSensitiveData("last_authenticated_user", null);

        // Navigate to authentication
        Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ----------------------------------------------- \\
    // --------------- Utility methods --------------- \\
    // ----------------------------------------------- \\

    private String formatLastLogin(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long minutes = diff / (60 * 1000);
        long hours = minutes / 60;

        if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            return "Just now";
        }
    }

    // ------------------------------------------------- \\
    // --------------- Lifecycle methods --------------- \\
    // ------------------------------------------------- \\

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (repository != null) {
            repository.close();
        }
    }
}