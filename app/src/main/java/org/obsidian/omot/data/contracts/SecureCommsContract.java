package org.obsidian.omot.data.contracts;

public final class SecureCommsContract {
    public static final String TABLE_NAME = "tb_secure_comms";

    public static final String COLUMN_MESSAGE_ID = "message_id";
    public static final String COLUMN_SENDER_ID = "sender_id";
    public static final String COLUMN_RECIPIENT_ID = "recipient_id";
    public static final String COLUMN_ENCRYPTED_MESSAGE = "encrypted_message";
    public static final String COLUMN_SENT_AT = "sent_at";
    public static final String COLUMN_READ_AT = "read_at";
    public static final String COLUMN_SELF_DESTRUCT_AT = "self_destruct_at";
    public static final String COLUMN_MESSAGE_TYPE = "message_type";
    public static final String COLUMN_ATTACHMENT_PATH = "attachment_path";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " ("
            + COLUMN_MESSAGE_ID + " TEXT PRIMARY KEY NOT NULL,"
            + COLUMN_SENDER_ID + " TEXT NOT NULL,"
            + COLUMN_RECIPIENT_ID + " TEXT NOT NULL,"
            + COLUMN_ENCRYPTED_MESSAGE + " TEXT NOT NULL,"
            + COLUMN_SENT_AT + " INTEGER NOT NULL,"
            + COLUMN_READ_AT + " INTEGER,"
            + COLUMN_MESSAGE_TYPE + " TEXT CHECK(" + COLUMN_MESSAGE_TYPE + " IN ('Text', 'File', 'Alert', 'Intel')),"
            + "FOREIGN KEY (" + COLUMN_SENDER_ID + " ) REFERENCES " + AgentContract.TABLE_NAME + "(" + AgentContract.COLUMN_AGENT_ID + "),"
            + "FOREIGN KEY (" + COLUMN_RECIPIENT_ID + " ) REFERENCES " + AgentContract.TABLE_NAME + "(" + AgentContract.COLUMN_AGENT_ID + ")"
            + ");";
}