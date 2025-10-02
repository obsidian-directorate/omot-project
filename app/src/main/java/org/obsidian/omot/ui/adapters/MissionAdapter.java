package org.obsidian.omot.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import org.obsidian.omot.R;
import org.obsidian.omot.data.entities.Mission;

import java.util.List;

public class MissionAdapter extends RecyclerView.Adapter<MissionAdapter.MissionViewHolder> {

    private List<Mission> missions;
    private boolean isActiveSection;

    public MissionAdapter(List<Mission> missions, boolean isActiveSection) {
        this.missions = missions;
        this.isActiveSection = isActiveSection;
    }

    @NonNull
    @Override
    public MissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mission_card, parent, false);
        return new MissionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MissionViewHolder holder, int position) {
        Mission mission = missions.get(position);
        holder.bind(mission, isActiveSection);
    }

    @Override
    public int getItemCount() {
        return missions.size();
    }

    static class MissionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvObjective, tvStatus, tvPriority, tvDeadline, tvLocation, tvProgress;
        private ProgressBar progressBar;
        private MaterialButton btnStatus, btnBriefing;
        private View viewPriorityIndicator;
        private TextView tvOverdueIndicator;

        public MissionViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tv_mission_title);
            tvObjective = itemView.findViewById(R.id.tv_mission_objective);
            tvStatus = itemView.findViewById(R.id.tv_mission_status);
            tvPriority = itemView.findViewById(R.id.tv_mission_priority);
            tvDeadline = itemView.findViewById(R.id.tv_mission_deadline);
            tvLocation = itemView.findViewById(R.id.tv_mission_location);
            tvProgress = itemView.findViewById(R.id.tv_mission_progress);
            progressBar = itemView.findViewById(R.id.progress_bar);
            btnStatus = itemView.findViewById(R.id.btn_status);
            btnBriefing = itemView.findViewById(R.id.btn_briefing);
            viewPriorityIndicator = itemView.findViewById(R.id.view_priority_indicator);
            tvOverdueIndicator = itemView.findViewById(R.id.tv_overdue_indicator);
        }

        public void bind(Mission mission, boolean isActiveSection) {
            tvTitle.setText(mission.getTitle());
            tvObjective.setText(mission.getObjective());
            tvStatus.setText(mission.getStatus());
            tvPriority.setText(mission.getPriority());
            tvDeadline.setText(mission.getFormattedEndDate());

            if (mission.getLocation() != null) {
                tvLocation.setText(mission.getLocation());
                tvLocation.setVisibility(View.VISIBLE);
            } else {
                tvLocation.setVisibility(View.GONE);
            }

            // Set priority indicator color
            int priorityColor = R.color.omot_text_secondary;
            switch (mission.getPriority()) {
                case "High": priorityColor = R.color.omot_red_alert; break;
                case "Medium": priorityColor = R.color.omot_orange_warning; break;
                case "Low": priorityColor = R.color.omot_green_success; break;
            }
            viewPriorityIndicator.setBackgroundColor(itemView.getContext().getColor(priorityColor));

            // Show/hide progress for active missions
            if (isActiveSection && "Active".equals(mission.getStatus())) {
                int progress = mission.getProgressPercentage();
                progressBar.setProgress(progress);
                tvProgress.setText(progress + "% complete");
                progressBar.setVisibility(View.VISIBLE);
                tvProgress.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
                tvProgress.setVisibility(View.GONE);
            }

            // Show overdue indicator
            viewPriorityIndicator.setVisibility(mission.isOverdue() ? View.VISIBLE : View.GONE);

            // Setup buttons
            setupStatusButton(mission, isActiveSection);
            setupBriefButton(mission);
        }

        private void setupStatusButton(Mission mission, boolean isActiveSection) {
            if (isActiveSection && "Active".equals(mission.getStatus())) {
                btnStatus.setText(itemView.getContext().getString(R.string.button_mark_complete));
                btnStatus.setBackgroundColor(itemView.getContext().getColor(R.color.omot_green_success));
                btnStatus.setVisibility(View.VISIBLE);
            } else if ("Pending".equals(mission.getStatus())) {
                btnStatus.setText(itemView.getContext().getString(R.string.button_activate));
                btnStatus.setBackgroundColor(itemView.getContext().getColor(R.color.omot_blue_primary));
                btnStatus.setVisibility(View.VISIBLE);
            } else {
                btnStatus.setVisibility(View.GONE);
            }
        }

        private void setupBriefButton(Mission mission) {
            btnBriefing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Launch mission briefing
                    // This will be implemented in the next step
                }
            });
        }
    }
}