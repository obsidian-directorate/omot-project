package org.obsidian.omot.data.entities;

import android.database.Cursor;

import org.obsidian.omot.data.contracts.MissionContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Mission {
    private String missionID;
    private String title;
    private String status;
    private String priority;
    private long startDate;
    private Long endDate;
    private String briefingFile;
    private String objective;
    private String location;
    private String clearanceRequired;

    public Mission() {}

    public Mission(String missionID, String title, String status, String priority, long startDate, String objective) {
        this.missionID = missionID;
        this.title = title;
        this.status = status;
        this.priority = priority;
        this.startDate = startDate;
        this.objective = objective;
    }

    public String getMissionID() {
        return missionID;
    }

    public void setMissionID(String missionID) {
        this.missionID = missionID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public String getBriefingFile() {
        return briefingFile;
    }

    public void setBriefingFile(String briefingFile) {
        this.briefingFile = briefingFile;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getClearanceRequired() {
        return clearanceRequired;
    }

    public void setClearanceRequired(String clearanceRequired) {
        this.clearanceRequired = clearanceRequired;
    }

    // Utility methods
    public String getFormattedStartDate() {
        return formatTimestamp(startDate);
    }

    public String getFormattedEndDate() {
        return endDate != null ? formatTimestamp(endDate) : "Ongoing";
    }

    public int getProgressPercentage() {
        if (endDate == null || "Completed".equals(status)) return 100;
        if ("Pending".equals(status)) return 0;

        long totalDuration = endDate - startDate;
        long elapsed = System.currentTimeMillis() - startDate;
        return (int) Math.min(100, Math.max(0, (elapsed * 100) / totalDuration));
    }

    public boolean isOverdue() {
        return endDate != null && System.currentTimeMillis() > endDate && !"Completed".equals(status);
    }

    private String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(new Date(timestamp));
    }

    public static Mission fromCursor(Cursor cursor) {
        Mission mission = new Mission();
        mission.setMissionID(cursor.getString(cursor.getColumnIndexOrThrow(MissionContract.COLUMN_MISSION_ID)));
        mission.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MissionContract.COLUMN_TITLE)));
        mission.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(MissionContract.COLUMN_STATUS)));
        mission.setPriority(cursor.getString(cursor.getColumnIndexOrThrow(MissionContract.COLUMN_PRIORITY)));
        mission.setStartDate(cursor.getLong(cursor.getColumnIndexOrThrow(MissionContract.COLUMN_START_DATE)));
        mission.setEndDate(cursor.isNull(cursor.getColumnIndexOrThrow(MissionContract.COLUMN_END_DATE))
                ? null : cursor.getLong(cursor.getColumnIndexOrThrow(MissionContract.COLUMN_END_DATE)));
        mission.setBriefingFile(cursor.getString(cursor.getColumnIndexOrThrow(MissionContract.COLUMN_BRIEFING_FILE)));
        mission.setObjective(cursor.getString(cursor.getColumnIndexOrThrow(MissionContract.COLUMN_OBJECTIVE)));
        mission.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(MissionContract.COLUMN_LOCATION)));
        mission.setClearanceRequired(cursor.getString(cursor.getColumnIndexOrThrow(MissionContract.COLUMN_CLEARANCE_REQUIRED)));

        return mission;
    }
}