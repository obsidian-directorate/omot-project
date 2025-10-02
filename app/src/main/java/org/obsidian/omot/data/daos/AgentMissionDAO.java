package org.obsidian.omot.data.daos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.obsidian.omot.data.contracts.AgentMissionContract;

import java.util.ArrayList;
import java.util.List;

public class AgentMissionDAO {
    private final SQLiteDatabase database;

    public AgentMissionDAO(SQLiteDatabase database) {
        this.database = database;
    }

    public boolean assignAgenToMission(String agentID, String missionID, String role) {
        try {
            ContentValues values = new ContentValues();
            values.put(AgentMissionContract.COLUMN_AGENT_ID, agentID);
            values.put(AgentMissionContract.COLUMN_MISSION_ID, missionID);
            values.put(AgentMissionContract.COLUMN_ASSIGNMENT_DATE, System.currentTimeMillis());
            values.put(AgentMissionContract.COLUMN_ROLE, role);

            long result = database.insert(AgentMissionContract.TABLE_NAME, null, values);
            return result != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getAgentsForMission(String missionID) {
        List<String> agentIDs = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = database.query(
                    AgentMissionContract.TABLE_NAME,
                    new String[]{AgentMissionContract.COLUMN_AGENT_ID},
                    AgentMissionContract.COLUMN_MISSION_ID + " = ?",
                    new String[]{missionID},
                    null, null, null
            );

            while (cursor != null && cursor.moveToFirst()) {
                agentIDs.add(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return agentIDs;
    }

    public String getLeadAgentForMission(String missionID) {
        Cursor cursor = null;

        try {
            cursor = database.query(
                    AgentMissionContract.TABLE_NAME,
                    new String[]{AgentMissionContract.COLUMN_AGENT_ID},
                    AgentMissionContract.COLUMN_MISSION_ID + " = ? AND "
                    + AgentMissionContract.COLUMN_ROLE + " = ?",
                    new String[]{missionID, "Lead"},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
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
}