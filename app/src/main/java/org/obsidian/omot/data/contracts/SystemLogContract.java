package org.obsidian.omot.data.contracts;

public final class SystemLogContract {
    public static final String TABLE_NAME = "tb_system_logs";

    public static final String COLUMN_LOG_ID = "log_id";
    public static final String COLUMN_AGENT_ID = "agent_id";
    public static final String COLUMN_ACTION = "action";
    public static final String COLUMN_LOG_TIMESTAMP = "log_timestamp";
    public static final String COLUMN_DETAILS = "details";
    public static final String COLUMN_LOG_LEVEL = "log_level";
    public static final String COLUMN_MODULE = "module";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
            + COLUMN_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_AGENT_ID + " TEXT,"
            + COLUMN_ACTION + " TEXT NOT NULL,"
            + COLUMN_LOG_TIMESTAMP + " INTEGER NOT NULL,"
            + COLUMN_DETAILS + " TEXT,"
            + COLUMN_LOG_LEVEL + " TEXT CHECK(" + COLUMN_LOG_LEVEL + " IN ('INFO', 'WARN', 'ERROR', 'SECURITY')),"
            + COLUMN_MODULE + " TEXT,"
            + "FOREIGN KEY (" + COLUMN_AGENT_ID + ") REFERENCES " + AgentContract.TABLE_NAME + "(" + AgentContract.COLUMN_AGENT_ID + ")"
            + ");";
}