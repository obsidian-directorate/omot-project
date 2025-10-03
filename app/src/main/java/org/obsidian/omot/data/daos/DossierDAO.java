package org.obsidian.omot.data.daos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.obsidian.omot.data.contracts.DossierContract;
import org.obsidian.omot.data.entities.Dossier;

import java.util.ArrayList;
import java.util.List;

public class DossierDAO {
    private final SQLiteDatabase database;

    public DossierDAO(SQLiteDatabase database) {
        this.database = database;
    }

    public List<Dossier> getDossiersByClearance(String clearanceCode) {
        List<Dossier> dossiers = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = database.query(
                    DossierContract.TABLE_NAME,
                    null,
                    DossierContract.COLUMN_CLEARANCE_REQUIRED_CODE + " = ?",
                    new String[]{clearanceCode},
                    null, null,
                    DossierContract.COLUMN_THREAT_LEVEL + " DESC, " + DossierContract.COLUMN_LAST_UPDATED + " DESC"
            );

            while (cursor != null && cursor.moveToNext()) {
                dossiers.add(Dossier.fromCursor(cursor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    public Dossier getDossierByID(String dossierID) {
        Cursor cursor = null;

        try {
            cursor = database.query(
                    DossierContract.TABLE_NAME,
                    null,
                    DossierContract.COLUMN_DOSSIER_ID + " = ?",
                    new String[]{dossierID},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                return Dossier.fromCursor(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    public boolean updateDossierNotes(String dossierID, String notes) {
        try {
            ContentValues values = new ContentValues();
            values.put(DossierContract.COLUMN_NOTES, notes);
            values.put(DossierContract.COLUMN_LAST_UPDATED, System.currentTimeMillis());

            int result = database.update(
                    DossierContract.TABLE_NAME,
                    values,
                    DossierContract.COLUMN_DOSSIER_ID + " = ?",
                    new String[]{dossierID}
            );

            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}