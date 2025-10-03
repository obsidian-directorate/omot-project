package org.obsidian.omot.data.contracts;

public final class DossierContract {
    public static final String TABLE_NAME = "tb_dossiers";

    public static final String COLUMN_DOSSIER_ID = "dossier_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CLEARANCE_REQUIRED_CODE = "clearance_required_code";
    public static final String COLUMN_CONTENT_FILE = "content_file";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_THREAT_LEVEL = "threat_level";
    public static final String COLUMN_ALIASES = "aliases";
    public static final String COLUMN_NOTES = "notes";
    public static final String COLUMN_LAST_UPDATED = "last_updated";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " ("
            + COLUMN_DOSSIER_ID + " TEXT PRIMARY KEY NOT NULL,"
            + COLUMN_TITLE + " TEXT NOT NULL,"
            + COLUMN_CLEARANCE_REQUIRED_CODE + " TEXT NOT NULL,"
            + COLUMN_CONTENT_FILE + " TEXT NOT NULL,"
            + COLUMN_CREATED_AT + " INTEGER NOT NULL,"
            + COLUMN_THREAT_LEVEL + " TEXT CHECK(" + COLUMN_THREAT_LEVEL + " IN ('Low', 'Medium', 'High', 'Critical')),"
            + COLUMN_ALIASES + " TEXT,"
            + COLUMN_NOTES + " TEXT,"
            + COLUMN_LAST_UPDATED + " INTEGER NOT NULL,"
            + "FOREIGN KEY (" + COLUMN_CLEARANCE_REQUIRED_CODE + ") REFERENCES " + ClearanceLevelContract.TABLE_NAME + "(" + ClearanceLevelContract.COLUMN_CLEARANCE_CODE + ")"
            + ");";
}