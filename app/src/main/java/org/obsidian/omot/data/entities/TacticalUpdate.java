package org.obsidian.omot.data.entities;

import android.database.Cursor;

import org.obsidian.omot.data.contracts.TacticalUpdateContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TacticalUpdate {
    private String updateID;
    private String missionID;
    private String agentID;
    private long updateTimestamp;
    private String updateData;
    private String updateType;
    private Double locationLat;
    private Double locationLng;
    private String updateStatus;

    public TacticalUpdate() {
    }

    public TacticalUpdate(String updateID, String missionID, String agentID, String updateData, String updateType) {
        this.updateID = updateID;
        this.missionID = missionID;
        this.agentID = agentID;
        this.updateData = updateData;
        this.updateType = updateType;
        this.updateTimestamp = System.currentTimeMillis();
        this.updateStatus = "Active";
    }

    public String getUpdateID() {
        return updateID;
    }

    public void setUpdateID(String updateID) {
        this.updateID = updateID;
    }

    public String getMissionID() {
        return missionID;
    }

    public void setMissionID(String missionID) {
        this.missionID = missionID;
    }

    public String getAgentID() {
        return agentID;
    }

    public void setAgentID(String agentID) {
        this.agentID = agentID;
    }

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public String getUpdateData() {
        return updateData;
    }

    public void setUpdateData(String updateData) {
        this.updateData = updateData;
    }

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public Double getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(Double locationLat) {
        this.locationLat = locationLat;
    }

    public Double getLocationLng() {
        return locationLng;
    }

    public void setLocationLng(Double locationLng) {
        this.locationLng = locationLng;
    }

    public String getUpdateStatus() {
        return updateStatus;
    }

    public void setUpdateStatus(String updateStatus) {
        this.updateStatus = updateStatus;
    }

    // Utility methods
    public boolean hasLocation() {
        return locationLat != null && locationLng != null;
    }

    public boolean isCritical() {
        return "Critical".equals(updateStatus);
    }

    public String getFormattedTimestamp() {
        return formatTimestamp(updateTimestamp);
    }

    private String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
    }

    public static TacticalUpdate fromCursor(Cursor cursor) {
        TacticalUpdate update = new TacticalUpdate();
        update.setUpdateID(cursor.getString(cursor.getColumnIndexOrThrow(TacticalUpdateContract.COLUMN_UPDATE_ID)));
        update.setMissionID(cursor.getString(cursor.getColumnIndexOrThrow(TacticalUpdateContract.COLUMN_MISSION_ID)));
        update.setAgentID(cursor.getString(cursor.getColumnIndexOrThrow(TacticalUpdateContract.COLUMN_AGENT_ID)));
        update.setUpdateTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(TacticalUpdateContract.COLUMN_UPDATE_TIMESTAMP)));
        update.setUpdateData(cursor.getString(cursor.getColumnIndexOrThrow(TacticalUpdateContract.COLUMN_UPDATE_DATA)));
        update.setLocationLat(cursor.isNull(cursor.getColumnIndexOrThrow(TacticalUpdateContract.COLUMN_LOCATION_LAT))
                ? null : cursor.getDouble(cursor.getColumnIndexOrThrow(TacticalUpdateContract.COLUMN_LOCATION_LAT)));
        update.setLocationLng(cursor.isNull(cursor.getColumnIndexOrThrow(TacticalUpdateContract.COLUMN_LOCATION_LNG))
                ? null : cursor.getDouble(cursor.getColumnIndexOrThrow(TacticalUpdateContract.COLUMN_LOCATION_LNG)));
        update.setUpdateStatus(cursor.getString(cursor.getColumnIndexOrThrow(TacticalUpdateContract.COLUMN_UPDATE_STATUS)));
        return update;
    }
}