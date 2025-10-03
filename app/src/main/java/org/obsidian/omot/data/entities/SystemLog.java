package org.obsidian.omot.data.entities;

import android.database.Cursor;

import org.obsidian.omot.R;
import org.obsidian.omot.data.contracts.SystemLogContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SystemLog {
    private int logID;
    private String agentID;
    private String action;
    private long logTimestamp;
    private String details;
    private String logLevel;
    private String module;

    public SystemLog() {}

    public SystemLog(String agentID, String action, String logLevel, String module) {
        this.agentID = agentID;
        this.action = action;
        this.logLevel = logLevel;
        this.module = module;
        this.logTimestamp = System.currentTimeMillis();
    }

    public int getLogID() {
        return logID;
    }

    public void setLogID(int logID) {
        this.logID = logID;
    }

    public String getAgentID() {
        return agentID;
    }

    public void setAgentID(String agentID) {
        this.agentID = agentID;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public long getLogTimestamp() {
        return logTimestamp;
    }

    public void setLogTimestamp(long logTimestamp) {
        this.logTimestamp = logTimestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    // Utility methods
    public String getFormattedTimestamp() {
        return formatTimestamp(logTimestamp);
    }

    public int getLogLevelColor() {
        switch (logLevel) {
            case "SECURITY": return R.color.omot_red_alert;
            case "ERROR": return R.color.omot_red_alert;
            case "WARN": return R.color.omot_orange_warning;
            case "INFO": return R.color.omot_blue_light;
            default: return R.color.omot_text_secondary;
        }
    }

    private String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
    }

    public static SystemLog fromCursor(Cursor cursor) {
        SystemLog log = new SystemLog();
        log.setLogID(cursor.getInt(cursor.getColumnIndexOrThrow(SystemLogContract.COLUMN_LOG_ID)));
        log.setAgentID(cursor.getString(cursor.getColumnIndexOrThrow(SystemLogContract.COLUMN_AGENT_ID)));
        log.setAction(cursor.getString(cursor.getColumnIndexOrThrow(SystemLogContract.COLUMN_ACTION)));
        log.setLogTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(SystemLogContract.COLUMN_LOG_TIMESTAMP)));
        log.setDetails(cursor.getString(cursor.getColumnIndexOrThrow(SystemLogContract.COLUMN_DETAILS)));
        log.setLogLevel(cursor.getString(cursor.getColumnIndexOrThrow(SystemLogContract.COLUMN_LOG_LEVEL)));
        log.setModule(cursor.getString(cursor.getColumnIndexOrThrow(SystemLogContract.COLUMN_MODULE)));
        return log;
    }
}