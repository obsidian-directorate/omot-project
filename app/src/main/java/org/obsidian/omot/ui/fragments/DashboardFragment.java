package org.obsidian.omot.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.obsidian.omot.R;
import org.obsidian.omot.data.daos.MissionDAO;
import org.obsidian.omot.data.entities.Mission;
import org.obsidian.omot.data.repository.DBRepository;
import org.obsidian.omot.security.SecurityManager;
import org.obsidian.omot.ui.adapters.MissionAdapter;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends BaseFragment {

    private RecyclerView rvActiveMissions, rvPendingMissions;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvActiveCount, tvPendingCount, tvCompletedCount, tvTotalCount;
    private TextView tvOverdueAlert;

    private DBRepository repository;
    private MissionDAO dao;
    private String currentAgentID;

    private List<Mission> activeMissions = new ArrayList<>();
    private List<Mission> pendingMissions = new ArrayList<>();

    private Handler refreshHandler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;
    private static final long REFRESH_INTERVAL = 30000; // 30 seconds

    public DashboardFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        initializeViews(view);
        setupRecyclerView();
        initializeData();
        startAutoRefresh();

        return view;
    }

    // ------------------------------------------------ \\
    // --------------- onCreate methods --------------- \\
    // ------------------------------------------------ \\

    private void initializeViews(View view) {
        rvActiveMissions = view.findViewById(R.id.rc_active_missions);
        rvPendingMissions = view.findViewById(R.id.rc_pending_missions);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        tvActiveCount = view.findViewById(R.id.tv_active_count);
        tvPendingCount = view.findViewById(R.id.tv_pending_count);
        tvCompletedCount = view.findViewById(R.id.tv_completed_count);
        tvTotalCount = view.findViewById(R.id.tv_total_count);
        tvOverdueAlert = view.findViewById(R.id.tv_overdue_alert);

        swipeRefresh.setOnRefreshListener(this::refreshData);
        swipeRefresh.setColorSchemeColors(
                getResources().getColor(R.color.omot_blue_light),
                getResources().getColor(R.color.omot_green_success),
                getResources().getColor(R.color.omot_red_alert)
        );
    }

    private void setupRecyclerView() {
        rvActiveMissions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPendingMissions.setLayoutManager(new LinearLayoutManager(getContext()));

        rvActiveMissions.setAdapter(new MissionAdapter(activeMissions, true));
        rvPendingMissions.setAdapter(new MissionAdapter(pendingMissions, false));
    }

    private void initializeData() {
        repository = DBRepository.getInstance(requireContext());
        dao = repository.getMissionDAO();

        // Get current agent
        SecurityManager securityManager = SecurityManager.getInstance(requireContext());
        String lastAgent = securityManager.retrieveSensitiveData("last_authentication_user");
        if (lastAgent != null) {
            currentAgentID = lastAgent;
        }

        refreshData();
    }

    private void startAutoRefresh() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                refreshData();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL);
            }
        };
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
    }

    // ----------------------------------------------------- \\
    // --------------- Swipe refresh methods --------------- \\
    // ----------------------------------------------------- \\

    private void refreshData() {
        if (dao == null) return;;

        new Thread(() -> {
            // Get missions for current agent
            List<Mission> allAgentMissions = dao.getMissionsByAgent(currentAgentID);

            // Filter by status
            activeMissions.clear();
            pendingMissions.clear();
            final int[] completed = {0};

            for (Mission mission : allAgentMissions) {
                switch (mission.getStatus()) {
                    case "Active": activeMissions.add(mission); break;
                    case "Pending": pendingMissions.add(mission); break;
                    case "Completed": completed[0]++; break;
                }
            }

            // Check for overdue missions
            List<Mission> overdueMissions = dao.getOverdueMissions();
            boolean hasOverdue = !overdueMissions.isEmpty();

            requireActivity().runOnUiThread(() -> {
                updateUI(allAgentMissions.size(), completed[0], hasOverdue);
                swipeRefresh.setRefreshing(false);
            });
        }).start();
    }

    private void stopAutoRefresh() {
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    // ------------------------------------------------- \\
    // --------------- UI update methods --------------- \\
    // ------------------------------------------------- \\

    private void updateUI(int total, int completed, boolean hasOverdue) {
        tvActiveCount.setText(String.valueOf(activeMissions.size()));
        tvPendingCount.setText(String.valueOf(pendingMissions.size()));
        tvCompletedCount.setText(String.valueOf(completed));
        tvTotalCount.setText(String.valueOf(total));

        // Update recycler views
        if (rvActiveMissions.getAdapter() != null) {
            rvActiveMissions.getAdapter().notifyDataSetChanged();
        }
        if (rvPendingMissions.getAdapter() != null) {
            rvPendingMissions.getAdapter().notifyDataSetChanged();
        }

        // Show/hide overdue alert
        if (hasOverdue) {
            tvOverdueAlert.setVisibility(View.VISIBLE);
            tvOverdueAlert.setText("⚠️ " + dao.getOverdueMissions().size() + " MISSION(S) OVERDUE");
        } else {
            tvOverdueAlert.setVisibility(View.GONE);
        }
    }

    // ---------------------------------------------------- \\
    // --------------- BaseFragment methods --------------- \\
    // ---------------------------------------------------- \\

    @Override
    protected void applyClearanceRestrictions() {
        // Apply clearance-based restrictions to mission access
        // For example, hide high-clearance missions from lower-level agents
    }

    // ------------------------------------------------- \\
    // --------------- Lifecycle methods --------------- \\
    // ------------------------------------------------- \\

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoRefresh();
    }
}