package org.obsidian.omot.data.entities;

import android.database.Cursor;

import org.obsidian.omot.data.contracts.AgentContract;

public class Agent {
    private String agentID;
    private String codename;
    private String passwordHash;
    private String salt;
    private String securityQuestion;
    private String securityAnswerHash;
    private String clearanceCode;
    private boolean biometricEnabled;
    private Long lastLoginTimestamp;
    private int failedLoginAttempts;
    private Long lastFailedLoginTimestamp;
    private boolean accountLocked;

    public Agent() {}

    public Agent(String agentID, String codename, String passwordHash, String salt, String clearanceCode) {
        this.agentID = agentID;
        this.codename = codename;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.clearanceCode = clearanceCode;
        this.biometricEnabled = false;
        this.failedLoginAttempts = 0;
        this.accountLocked = false;
    }

    public String getAgentID() {
        return agentID;
    }

    public void setAgentID(String agentID) {
        this.agentID = agentID;
    }

    public String getCodename() {
        return codename;
    }

    public void setCodename(String codename) {
        this.codename = codename;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getSecurityAnswerHash() {
        return securityAnswerHash;
    }

    public void setSecurityAnswerHash(String securityAnswerHash) {
        this.securityAnswerHash = securityAnswerHash;
    }

    public String getClearanceCode() {
        return clearanceCode;
    }

    public void setClearanceCode(String clearanceCode) {
        this.clearanceCode = clearanceCode;
    }

    public boolean isBiometricEnabled() {
        return biometricEnabled;
    }

    public void setBiometricEnabled(boolean biometricEnabled) {
        this.biometricEnabled = biometricEnabled;
    }

    public Long getLastLoginTimestamp() {
        return lastLoginTimestamp;
    }

    public void setLastLoginTimestamp(Long lastLoginTimestamp) {
        this.lastLoginTimestamp = lastLoginTimestamp;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public Long getLastFailedLoginTimestamp() {
        return lastFailedLoginTimestamp;
    }

    public void setLastFailedLoginTimestamp(Long lastFailedLoginTimestamp) {
        this.lastFailedLoginTimestamp = lastFailedLoginTimestamp;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public static Agent fromCursor(Cursor cursor) {
        Agent agent = new Agent();
        agent.setAgentID(cursor.getString(cursor.getColumnIndexOrThrow(AgentContract.COLUMN_AGENT_ID)));
        agent.setCodename(cursor.getString(cursor.getColumnIndexOrThrow(AgentContract.COLUMN_CODENAME)));
        agent.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(AgentContract.COLUMN_PASSWORD_HASH)));
        agent.setSalt(cursor.getString(cursor.getColumnIndexOrThrow(AgentContract.COLUMN_SALT)));
        agent.setSecurityQuestion(cursor.getString(cursor.getColumnIndexOrThrow(AgentContract.COLUMN_SECURITY_QUESTION)));
        agent.setSecurityAnswerHash(cursor.getString(cursor.getColumnIndexOrThrow(AgentContract.COLUMN_SECURITY_ANSWER_HASH)));
        agent.setClearanceCode(cursor.getString(cursor.getColumnIndexOrThrow(AgentContract.COLUMN_CLEARANCE_CODE)));
        agent.setBiometricEnabled(cursor.getInt(cursor.getColumnIndexOrThrow(AgentContract.COLUMN_BIOMETRIC_ENABLED)) == 1);
        agent.setLastLoginTimestamp(cursor.isNull(cursor.getColumnIndexOrThrow(AgentContract.COLUMN_LAST_LOGIN_TIMESTAMP)) ?
                null : cursor.getLong(cursor.getColumnIndexOrThrow(AgentContract.COLUMN_LAST_LOGIN_TIMESTAMP)));
        agent.setFailedLoginAttempts(cursor.getInt(cursor.getColumnIndexOrThrow(AgentContract.COLUMN_FAILED_LOGIN_ATTEMPTS)));
        agent.setLastFailedLoginTimestamp(cursor.isNull(cursor.getColumnIndexOrThrow(AgentContract.COLUMN_LAST_FAILED_LOGIN_TIMESTAMP)) ?
                null : cursor.getLong(cursor.getColumnIndexOrThrow(AgentContract.COLUMN_LAST_FAILED_LOGIN_TIMESTAMP)));
        agent.setAccountLocked(cursor.getInt(cursor.getColumnIndexOrThrow(AgentContract.COLUMN_ACCOUNT_LOCKED)) == 1);

        return agent;
    }
}