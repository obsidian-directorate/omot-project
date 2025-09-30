package org.obsidian.omot.data.daos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.obsidian.omot.data.contracts.ClearanceLevelContract;
import org.obsidian.omot.data.entities.ClearanceLevel;

import java.util.ArrayList;
import java.util.List;

public class ClearanceLevelDAO {
    private SQLiteDatabase database;

    public ClearanceLevelDAO(SQLiteDatabase database) {
        this.database = database;
    }

    public List<ClearanceLevel> getAllClearanceLevels() {
        List<ClearanceLevel> levels = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = database.query(
                    ClearanceLevelContract.TABLE_NAME,
                    null, null, null, null, null,
                    ClearanceLevelContract.COLUMN_CLEARANCE_CODE
            );

            while (cursor != null && cursor.moveToFirst()) {
                levels.add(ClearanceLevel.fromCursor(cursor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return levels;
    }

    public ClearanceLevel getClearanceLevelByCode(String clearanceCode) {
        Cursor cursor = null;

        try {
            cursor = database.query(
                    ClearanceLevelContract.TABLE_NAME,
                    null,
                    ClearanceLevelContract.COLUMN_CLEARANCE_CODE + " = ?",
                    new String[]{clearanceCode},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                return ClearanceLevel.fromCursor(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();;
            }
        }

        return null;
    }

    public boolean insertClearanceLevel(ClearanceLevel level) {
        try {
            ContentValues values = new ContentValues();
            values.put(ClearanceLevelContract.COLUMN_CLEARANCE_CODE, level.getClearanceCode());
            values.put(ClearanceLevelContract.COLUMN_CLEARANCE_NAME, level.getClearanceLevelName());
            values.put(ClearanceLevelContract.COLUMN_ROLE_DESCRIPTION, level.getRoleDescription());

            long result = database.insert(ClearanceLevelContract.TABLE_NAME, null, values);
            return result != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}