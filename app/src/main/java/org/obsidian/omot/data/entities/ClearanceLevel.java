package org.obsidian.omot.data.entities;

import android.database.Cursor;

import org.obsidian.omot.data.contracts.ClearanceLevelContract;

public class ClearanceLevel {
    private String clearanceCode;
    private String clearanceLevelName;
    private String roleDescription;

    public ClearanceLevel() {}

    public ClearanceLevel(String clearanceCode, String clearanceLevelName, String roleDescription) {
        this.clearanceCode = clearanceCode;
        this.clearanceLevelName = clearanceLevelName;
        this.roleDescription = roleDescription;
    }

    public String getClearanceCode() {
        return clearanceCode;
    }

    public void setClearanceCode(String clearanceCode) {
        this.clearanceCode = clearanceCode;
    }

    public String getClearanceLevelName() {
        return clearanceLevelName;
    }

    public void setClearanceLevelName(String clearanceLevelName) {
        this.clearanceLevelName = clearanceLevelName;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
    }

    public static ClearanceLevel fromCursor(Cursor cursor) {
        return new ClearanceLevel(
                cursor.getString(cursor.getColumnIndexOrThrow(ClearanceLevelContract.COLUMN_CLEARANCE_CODE)),
                cursor.getString(cursor.getColumnIndexOrThrow(ClearanceLevelContract.COLUMN_CLEARANCE_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(ClearanceLevelContract.COLUMN_ROLE_DESCRIPTION))
        );
    }
}