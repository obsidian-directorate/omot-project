package org.obsidian.omot.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.obsidian.omot.data.contracts.AgentContract;
import org.obsidian.omot.data.contracts.AgentMissionContract;
import org.obsidian.omot.data.contracts.ClearanceLevelContract;
import org.obsidian.omot.data.contracts.DBContract;
import org.obsidian.omot.data.contracts.MissionContract;

import timber.log.Timber;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    private final Context context;

    public DBHelper(Context context) {
        super(context, DBContract.DB_NAME, null, DBContract.DB_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Create tables
            db.execSQL(ClearanceLevelContract.SQL_CREATE_TABLE);
            db.execSQL(AgentContract.SQL_CREATE_TABLE);
            db.execSQL(MissionContract.SQL_CREATE_TABLE);
            db.execSQL(AgentMissionContract.SQL_CREATE_TABLE);

            // Insert default clearance levels
            db.execSQL(ClearanceLevelContract.SQL_INSERT_DEFAULT_DATA);
            insertSampleMissions(db);

            Timber.tag(TAG).i("Database created successfully");
        } catch (Exception e) {
            Timber.tag(TAG).e("Database creation failed: %s", e.getMessage());
            throw new RuntimeException("Database creation failed", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            // Handle database migrations
            if (oldVersion < newVersion) {
                Timber.tag(TAG).i("Upgrading database from version + " + oldVersion + " to " + newVersion);
                performMigration(db, oldVersion, newVersion);
            }
        } catch (Exception e) {
            Timber.tag(TAG).e("Database upgrade failed: %s", e.getMessage());
            // In case of error, recreate database (lose data)
            recreateDatabase(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.tag(TAG).w("Database downgrade requested from " + oldVersion + " to " + newVersion);
        recreateDatabase(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enable foreign keys
        db.setForeignKeyConstraintsEnabled(true);
    }

    private void performMigration(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Implement migration logic for each version
        for (int version = oldVersion + 1; version <= newVersion; version++) {
            switch (version) {
                case 2:
                    // Future migration example
                    // db.execSQL("ALTER TABLE " + AgentContract.TABLE_NAME + " ADD COLUMN new_column TEXT;");
                    break;
                default:
                    Timber.tag(TAG).w("Unknown migration version: %s", version);
                    break;
            }
        }
    }

    private void recreateDatabase(SQLiteDatabase db) {
        Timber.tag(TAG).w("Recreating database due to migration failure");

        db.execSQL("DROP TABLE IF EXISTS " + AgentContract.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ClearanceLevelContract.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MissionContract.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AgentMissionContract.TABLE_NAME);

        onCreate(db);
    }

    public void clearAllData() {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL("DELETE FROM " + AgentContract.TABLE_NAME);
            // Don't clear clearance levels as they're system data
            Timber.tag(TAG).i("All agent data cleared");
        } catch (Exception e) {
            Timber.tag(TAG).e("Failed to clear database + %s", e.getMessage());
        } finally {
            db.close();
        }
    }

    private void insertSampleMissions(SQLiteDatabase db) {
        // Insert sample missions for testing
        long now = System.currentTimeMillis();
        long dayInMillis = 24 * 60 * 60 * 1000;

        ContentValues mission1 = new ContentValues();
        mission1.put(MissionContract.COLUMN_MISSION_ID, "MISSION-001");
        mission1.put(MissionContract.COLUMN_TITLE, "Operation Silent Shadow");
        mission1.put(MissionContract.COLUMN_STATUS, "Active");
        mission1.put(MissionContract.COLUMN_PRIORITY, "High");
        mission1.put(MissionContract.COLUMN_START_DATE, now - (2 * dayInMillis));
        mission1.put(MissionContract.COLUMN_END_DATE, now + (3 * dayInMillis));
        mission1.put(MissionContract.COLUMN_OBJECTIVE, "Infiltrate and extract intelligence from target facility");
        mission1.put(MissionContract.COLUMN_LOCATION, "District 7 - Industrial Zone");
        mission1.put(MissionContract.COLUMN_CLEARANCE_REQUIRED, "ALPHA");
        db.insert(MissionContract.TABLE_NAME, null, mission1);

        ContentValues mission2 = new ContentValues();
        mission2.put(MissionContract.COLUMN_MISSION_ID, "MISSION-002");
        mission2.put(MissionContract.COLUMN_TITLE, "Asset Recovery");
        mission2.put(MissionContract.COLUMN_STATUS, "Pending");
        mission2.put(MissionContract.COLUMN_PRIORITY, "Medium");
        mission2.put(MissionContract.COLUMN_START_DATE, now + dayInMillis);
        mission2.put(MissionContract.COLUMN_END_DATE, now + (5 * dayInMillis));
        mission2.put(MissionContract.COLUMN_OBJECTIVE, "Recover compromised intelligence assets");
        mission2.put(MissionContract.COLUMN_LOCATION, "Downtown Sector");
        mission2.put(MissionContract.COLUMN_CLEARANCE_REQUIRED, "BETA");
        db.insert(MissionContract.TABLE_NAME, null, mission2);
    }
}