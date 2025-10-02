package org.obsidian.omot.data.contracts;

public final class ClearanceLevelContract {
    public static final String TABLE_NAME = "tb_clearance_levels";

    public static final String COLUMN_CLEARANCE_CODE = "clearance_code";
    public static final String COLUMN_CLEARANCE_NAME = "clearance_level_name";
    public static final String COLUMN_ROLE_DESCRIPTION = "role_description";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " ("
            + COLUMN_CLEARANCE_CODE + " TEXT PRIMARY KEY NOT NULL,"
            + COLUMN_CLEARANCE_NAME + " TEXT NOT NULL UNIQUE,"
            + COLUMN_ROLE_DESCRIPTION + " TEXT NOT NULL"
            + ");";

    public static final String SQL_INSERT_DEFAULT_DATA =
            "INSERT INTO " + TABLE_NAME + " VALUES "
            + "('BETA', 'Field Agent', 'Regular ops, basic missions'),"
            + "('ALPHA', 'Senior Operative', 'Advanced dossiers, encrypted channels'),"
            + "('OMEGA', 'Command Authority', 'Full app access, manage agents, override'),"
            + "('SHADOW', 'Rogue Operative', 'Special conditions for monitored access');";
}