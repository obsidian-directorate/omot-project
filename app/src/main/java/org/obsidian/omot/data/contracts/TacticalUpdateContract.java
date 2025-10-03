package org.obsidian.omot.data.contracts;

public final class TacticalUpdateContract {
    public static final String TABLE_NAME = "tb_tactical_updates";

    public static final String COLUMN_UPDATE_ID = "update_id";
    public static final String COLUMN_MISSION_ID = "mission_id";
    public static final String COLUMN_AGENT_ID = "agent_id";
    public static final String COLUMN_UPDATE_TIMESTAMP = "update_timestamp";
    public static final String COLUMN_UPDATE_DATA = "update_data";
    public static final String COLUMN_UPDATE_TYPE = "update_type";
    public static final String COLUMN_LOCATION_LAT = "location_lat";
    public static final String COLUMN_LOCATION_LNG = "location_lng";
    public static final String COLUMN_UPDATE_STATUS = "update_status";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " ("
            + COLUMN_UPDATE_ID + " TEXT PRIMARY KEY NOT NULL,"
            + COLUMN_MISSION_ID + " TEXT NOT NULL,"
            + COLUMN_AGENT_ID + " TEXT,"
            + COLUMN_UPDATE_TIMESTAMP + " INTEGER NOT NULL,"
            + COLUMN_UPDATE_DATA + " TEXT NOT NULL,"
            + COLUMN_UPDATE_TYPE + " TEXT NOT NULL CHECK (" + COLUMN_UPDATE_TYPE + " IN ('Location', 'Intel', 'Status', 'Alert')),"
            + COLUMN_LOCATION_LAT + " REAL,"
            + COLUMN_LOCATION_LNG + " REAL,"
            + COLUMN_UPDATE_STATUS + " TEXT CHECK(" + COLUMN_UPDATE_STATUS + " IN ('Active', 'Resolved', 'Critical')),"
            + "FOREIGN KEY (" + COLUMN_MISSION_ID + ") REFERENCES " + MissionContract.TABLE_NAME + "(" + MissionContract.COLUMN_MISSION_ID + "),"
            + "FOREIGN KEY (" + COLUMN_AGENT_ID + ") REFERENCES " + AgentContract.TABLE_NAME + "(" + AgentContract.COLUMN_AGENT_ID + ")"
            + ");";
}