package org.obsidian.omot.data.contracts;

public final class AgentContract {
    public static final String TABLE_NAME = "tb_agents";

    public static final String COLUMN_AGENT_ID = "agent_id";
    public static final String COLUMN_CODENAME = "codename";
    public static final String COLUMN_PASSWORD_HASH = "password_hash";
    public static final String COLUMN_SALT = "salt";
    public static final String COLUMN_SECURITY_QUESTION = "security_question";
    public static final String COLUMN_SECURITY_ANSWER_HASH = "security_answer_hash";
    public static final String COLUMN_CLEARANCE_CODE = "clearance_code";
    public static final String COLUMN_BIOMETRIC_ENABLED = "biometric_enabled";
    public static final String COLUMN_LAST_LOGIN_TIMESTAMP = "last_login_timestamp";
    public static final String COLUMN_FAILED_LOGIN_ATTEMPTS = "failed_login_attempts";
    public static final String COLUMN_LAST_FAILED_LOGIN_TIMESTAMP = "last_failed_login_timestamp";
    public static final String COLUMN_ACCOUNT_LOCKED = "account_locked";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " ("
            + COLUMN_AGENT_ID + " TEXT PRIMARY KEY NOT NULL,"
            + COLUMN_CODENAME + " TEXT UNIQUE NOT NULL,"
            + COLUMN_PASSWORD_HASH + " TEXT NOT NULL,"
            + COLUMN_SALT + " TEXT NOT NULL UNIQUE,"
            + COLUMN_SECURITY_QUESTION + " TEXT,"
            + COLUMN_SECURITY_ANSWER_HASH + " TEXT,"
            + COLUMN_CLEARANCE_CODE + " TEXT NOT NULL,"
            + COLUMN_BIOMETRIC_ENABLED + " INTEGER DEFAULT 0,"
            + COLUMN_LAST_LOGIN_TIMESTAMP + " INTEGER,"
            + COLUMN_FAILED_LOGIN_ATTEMPTS + " INTEGER DEFAULT 0,"
            + COLUMN_LAST_FAILED_LOGIN_TIMESTAMP + " INTEGER,"
            + COLUMN_ACCOUNT_LOCKED + " INTEGER DEFAULT 0,"
            + "FOREIGN KEY (" + COLUMN_CLEARANCE_CODE + ") REFERENCES " + ClearanceLevelContract.TABLE_NAME + "(" + ClearanceLevelContract.COLUMN_CLEARANCE_CODE + ")"
            + ");";
}