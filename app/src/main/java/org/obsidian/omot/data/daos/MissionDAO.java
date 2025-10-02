package org.obsidian.omot.data.daos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.obsidian.omot.data.contracts.AgentMissionContract;
import org.obsidian.omot.data.contracts.MissionContract;
import org.obsidian.omot.data.entities.Mission;

import java.util.ArrayList;
import java.util.List;

public class MissionDAO {
    private final SQLiteDatabase database;

    public MissionDAO(SQLiteDatabase database) {
        this.database = database;
    }

    public List<Mission> getMissionByStatus(String status) {
        List<Mission> missions = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = database.query(
                    MissionContract.TABLE_NAME,
                    null,
                    MissionContract.COLUMN_STATUS + " = ?",
                    new String[]{status},
                    null, null,
                    MissionContract.COLUMN_PRIORITY + "DESC, " + MissionContract.COLUMN_START_DATE + " ASC"
            );

            while (cursor != null && cursor.moveToNext()) {
                missions.add(Mission.fromCursor(cursor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return missions;
    }

    public List<Mission> getMissionsByAgent(String agentID) {
        List<Mission> missions = new ArrayList<>();
        Cursor cursor = null;

        try {
            String query = "SELECT m.* FROM " + MissionContract.TABLE_NAME + " m"
                    + "INNER JOIN " + AgentMissionContract.TABLE_NAME + " am ON m."
                    + MissionContract.COLUMN_MISSION_ID + " = am." + AgentMissionContract.COLUMN_MISSION_ID + " "
                    + "WHERE am." + AgentMissionContract.COLUMN_AGENT_ID + " = ?"
                    + "ORDER BY m." + MissionContract.COLUMN_PRIORITY + " DESC, m."
                    + MissionContract.COLUMN_START_DATE + " ASC";

            cursor = database.rawQuery(query, new String[]{agentID});

            while (cursor != null && cursor.moveToNext()) {
                missions.add(Mission.fromCursor(cursor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return missions;
    }

    public Mission getMissionByID(String missionID) {
        Cursor cursor = null;

        try {
            cursor = database.query(
                    MissionContract.TABLE_NAME,
                    null,
                    MissionContract.COLUMN_MISSION_ID + " = ?",
                    new String[]{missionID},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                return Mission.fromCursor(cursor);
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

    public boolean updateMissionStatus(String missionID, String newStatus) {
        try {
            ContentValues values = new ContentValues();
            values.put(MissionContract.COLUMN_STATUS, newStatus);

            if ("Completed".equals(newStatus)) {
                values.put(MissionContract.COLUMN_END_DATE, System.currentTimeMillis());
            }

            int result = database.update(
                    MissionContract.TABLE_NAME,
                    values,
                    MissionContract.COLUMN_MISSION_ID + " = ?",
                    new String[]{missionID}
            );

            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Mission> getOverdueMissions() {
        List<Mission> missions = new ArrayList<>();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM " + MissionContract.TABLE_NAME
                    + " WHERE " + MissionContract.COLUMN_END_DATE + " < ? "
                    + "AND " + MissionContract.COLUMN_STATUS + " != 'Completed'";

            cursor = database.rawQuery(query, new String[]{String.valueOf(System.currentTimeMillis())});

            while (cursor != null && cursor.moveToNext()) {
                missions.add(Mission.fromCursor(cursor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return missions;
    }
}