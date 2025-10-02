package org.obsidian.omot.data.contracts;

public final class AgentMissionContract {
    public static final String TABLE_NAME = "tb_agent_missions";

    public static final String COLUMN_AGENT_ID = "agent_id";
    public static final String COLUMN_MISSION_ID = "mission_id";
    public static final String COLUMN_ASSIGNMENT_DATE = "assignment_date";
    public static final String COLUMN_ROLE = "role"; // Lead, Support, Intelligence

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " ("
            + COLUMN_AGENT_ID + " TEXT NOT NULL,"
            + COLUMN_MISSION_ID + " TEXT NOT NULL,"
            + COLUMN_ASSIGNMENT_DATE + " INTEGER NOT NULL,"
            + COLUMN_ROLE + " TEXT DEFAULT 'Support',"
            + "PRIMARY KEY (" + COLUMN_AGENT_ID + ", " + COLUMN_MISSION_ID + "),"
            + "FOREIGN KEY (" + COLUMN_AGENT_ID + ") REFERENCES " + AgentContract.TABLE_NAME + "(" + AgentContract.COLUMN_AGENT_ID + "),"
            + "FOREIGN KEY (" + COLUMN_MISSION_ID + ") REFERENCES " + MissionContract.TABLE_NAME + "(" + MissionContract.COLUMN_MISSION_ID + ")"
            + ");";
}