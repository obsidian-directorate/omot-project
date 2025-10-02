package org.obsidian.omot.data.contracts;

public final class MissionContract {
    public static final String TABLE_NAME = "tb_missions";

    public static final String COLUMN_MISSION_ID = "mission_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_PRIORITY = "priority";
    public static final String COLUMN_START_DATE = "start_date";
    public static final String COLUMN_END_DATE = "end_date";
    public static final String COLUMN_BRIEFING_FILE = "briefing_file";
    public static final String COLUMN_OBJECTIVE = "objective";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_CLEARANCE_REQUIRED = "clearance_required";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " ("
            + COLUMN_MISSION_ID + " TEXT PRIMARY KEY NOT NULL,"
            + COLUMN_TITLE + " TEXT NOT NULL,"
            + COLUMN_STATUS + " TEXT NOT NULL CHECK(" + COLUMN_STATUS + " IN ('Active', 'Completed', 'Deactivated', 'Pending')),"
            + COLUMN_PRIORITY + " TEXT NOT NULL CHECK(" + COLUMN_PRIORITY + " IN ('High', 'Medium', 'Low')),"
            + COLUMN_START_DATE + " INTEGER NOT NULL,"
            + COLUMN_END_DATE + " INTEGER,"
            + COLUMN_BRIEFING_FILE + " TEXT,"
            + COLUMN_OBJECTIVE + " TEXT NOT NULL,"
            + COLUMN_LOCATION + " TEXT,"
            + COLUMN_CLEARANCE_REQUIRED + " TEXT NOT NULL DEFAULT 'BETA'"
            + ");";
}