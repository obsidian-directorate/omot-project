package org.obsidian.omot.data.entities;

import android.database.Cursor;

import org.obsidian.omot.data.contracts.AgentMissionContract;

public class AgentMission {
    private String agentID;
    private String missionID;
    private long assignmentDate;
    private String role;

    public AgentMission() {}

    public AgentMission(String agentID, String missionID, long assignmentDate, String role) {
        this.agentID = agentID;
        this.missionID = missionID;
        this.assignmentDate = assignmentDate;
        this.role = role;
    }

    public String getAgentID() {
        return agentID;
    }

    public void setAgentID(String agentID) {
        this.agentID = agentID;
    }

    public String getMissionID() {
        return missionID;
    }

    public void setMissionID(String missionID) {
        this.missionID = missionID;
    }

    public long getAssignmentDate() {
        return assignmentDate;
    }

    public void setAssignmentDate(long assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isLeadAgent() {
        return "Lead".equals(role);
    }

    public static AgentMission fromCursor(Cursor cursor) {
        return new AgentMission(
                cursor.getString(cursor.getColumnIndexOrThrow(AgentMissionContract.COLUMN_AGENT_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(AgentMissionContract.COLUMN_MISSION_ID)),
                cursor.getLong(cursor.getColumnIndexOrThrow(AgentMissionContract.COLUMN_ASSIGNMENT_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(AgentMissionContract.COLUMN_ROLE))
        );
    }
}