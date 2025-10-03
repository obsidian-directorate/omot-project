package org.obsidian.omot.data.entities;

import android.database.Cursor;

import org.obsidian.omot.R;
import org.obsidian.omot.data.contracts.DossierContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Dossier {
    private String dossierID;
    private String title;
    private String clearanceRequiredCode;
    private String contentFile;
    private long createdAt;
    private String threatLevel;
    private String aliases;
    private String notes;
    private long lastUpdated;

    public Dossier() {}

    public Dossier(String dossierID, String title, String clearanceRequiredCode, String contentFile, String threatLevel) {
        this.dossierID = dossierID;
        this.title = title;
        this.clearanceRequiredCode = clearanceRequiredCode;
        this.contentFile = contentFile;
        this.threatLevel = threatLevel;
        this.createdAt = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
    }

    public String getDossierID() {
        return dossierID;
    }

    public void setDossierID(String dossierID) {
        this.dossierID = dossierID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getClearanceRequiredCode() {
        return clearanceRequiredCode;
    }

    public void setClearanceRequiredCode(String clearanceRequiredCode) {
        this.clearanceRequiredCode = clearanceRequiredCode;
    }

    public String getContentFile() {
        return contentFile;
    }

    public void setContentFile(String contentFile) {
        this.contentFile = contentFile;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(String threatLevel) {
        this.threatLevel = threatLevel;
    }

    public String getAliases() {
        return aliases;
    }

    public void setAliases(String aliases) {
        this.aliases = aliases;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // Utility methods
    public String getFormattedCreatedAt() {
        return formatTimestamp(createdAt);
    }

    public String getFormattedLastUpdated() {
        return formatTimestamp(lastUpdated);
    }

    private String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(new Date(timestamp));
    }

    public int getThreatLevelColor() {
        switch (threatLevel) {
            case "Critical": return R.color.omot_red_alert;
            case "High": return R.color.omot_orange_warning;
            case "Medium": return R.color.omot_yellow_caution;
            case "Low": return R.color.omot_green_success;
            default: return R.color.omot_text_secondary;
        }
    }

    public static Dossier fromCursor(Cursor cursor) {
        Dossier dossier = new Dossier();
        dossier.setDossierID(cursor.getString(cursor.getColumnIndexOrThrow(DossierContract.COLUMN_DOSSIER_ID)));
        dossier.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DossierContract.COLUMN_TITLE)));
        dossier.setClearanceRequiredCode(cursor.getString(cursor.getColumnIndexOrThrow(DossierContract.COLUMN_CLEARANCE_REQUIRED_CODE)));
        dossier.setContentFile(cursor.getString(cursor.getColumnIndexOrThrow(DossierContract.COLUMN_CONTENT_FILE)));
        dossier.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DossierContract.COLUMN_CREATED_AT)));
        dossier.setThreatLevel(cursor.getString(cursor.getColumnIndexOrThrow(DossierContract.COLUMN_THREAT_LEVEL)));
        dossier.setAliases(cursor.getString(cursor.getColumnIndexOrThrow(DossierContract.COLUMN_ALIASES)));
        dossier.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(DossierContract.COLUMN_NOTES)));
        dossier.setLastUpdated(cursor.getLong(cursor.getColumnIndexOrThrow(DossierContract.COLUMN_LAST_UPDATED)));
        return dossier;
    }
}