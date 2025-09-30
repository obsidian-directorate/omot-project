package org.obsidian.omot.data.daos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.obsidian.omot.data.contracts.AgentContract;
import org.obsidian.omot.data.encryption.DBEncryptionHelper;
import org.obsidian.omot.data.entities.Agent;

public class AgentDAO {
    private final SQLiteDatabase database;
    private final DBEncryptionHelper encryptionHelper;

    public AgentDAO(SQLiteDatabase database, DBEncryptionHelper encryptionHelper) {
        this.database = database;
        this.encryptionHelper = encryptionHelper;
    }

    public Agent getAgentByCodename(String codename) {
        Cursor cursor = null;

        try {
            cursor = database.query(
                    AgentContract.TABLE_NAME,
                    null,
                    AgentContract.COLUMN_CODENAME + " = ?",
                    new String[]{codename},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                Agent agent = Agent.fromCursor(cursor);

                // Decrypt sensitive fields
                if (agent.getSecurityQuestion() != null) {
                    agent.setSecurityQuestion(encryptionHelper.decryptField(agent.getSecurityQuestion()));
                }
                if (agent.getSecurityAnswerHash() != null) {
                    agent.setSecurityAnswerHash(encryptionHelper.decryptField(agent.getSecurityAnswerHash()));
                }

                return agent;
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

    public boolean insertAgent(Agent agent) {
        try {
            ContentValues values = new ContentValues();
            values.put(AgentContract.COLUMN_AGENT_ID, agent.getAgentID());
            values.put(AgentContract.COLUMN_CODENAME, agent.getCodename());
            values.put(AgentContract.COLUMN_PASSWORD_HASH, agent.getPasswordHash());
            values.put(AgentContract.COLUMN_SALT, agent.getSalt());
            values.put(AgentContract.COLUMN_CLEARANCE_CODE, agent.getClearanceCode());
            values.put(AgentContract.COLUMN_BIOMETRIC_ENABLED, agent.isBiometricEnabled() ? 1 : 0);
            values.put(AgentContract.COLUMN_FAILED_LOGIN_ATTEMPTS, agent.getFailedLoginAttempts());
            values.put(AgentContract.COLUMN_ACCOUNT_LOCKED, agent.isAccountLocked() ? 1 : 0);

            // Encrypt and store sensitive data
            if (agent.getSecurityQuestion() != null) {
                values.put(AgentContract.COLUMN_SECURITY_QUESTION, encryptionHelper.encryptField(agent.getSecurityQuestion()));
            }
            if (agent.getSecurityAnswerHash() != null) {
                values.put(AgentContract.COLUMN_SECURITY_ANSWER_HASH, encryptionHelper.encryptField(agent.getSecurityAnswerHash()));
            }

            long result = database.insert(AgentContract.TABLE_NAME, null, values);
            return result != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAgent(Agent agent) {
        try {
            ContentValues values = new ContentValues();
            values.put(AgentContract.COLUMN_CODENAME, agent.getCodename());
            values.put(AgentContract.COLUMN_PASSWORD_HASH, agent.getPasswordHash());
            values.put(AgentContract.COLUMN_SALT, agent.getSalt());
            values.put(AgentContract.COLUMN_CLEARANCE_CODE, agent.getClearanceCode());
            values.put(AgentContract.COLUMN_BIOMETRIC_ENABLED, agent.isBiometricEnabled() ? 1 : 0);
            values.put(AgentContract.COLUMN_FAILED_LOGIN_ATTEMPTS, agent.getFailedLoginAttempts());
            values.put(AgentContract.COLUMN_ACCOUNT_LOCKED, agent.isAccountLocked() ? 1 : 0);

            // Encrypt and update sensitive data
            if (agent.getSecurityQuestion() != null) {
                values.put(AgentContract.COLUMN_SECURITY_QUESTION, encryptionHelper.encryptField(agent.getSecurityQuestion()));
            }
            if (agent.getSecurityAnswerHash() != null) {
                values.put(AgentContract.COLUMN_SECURITY_ANSWER_HASH, encryptionHelper.encryptField(agent.getSecurityAnswerHash()));
            }

            int result = database.update(
                    AgentContract.TABLE_NAME,
                    values,
                    AgentContract.COLUMN_AGENT_ID + " = ?",
                    new String[]{agent.getAgentID()}
            );

            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateLoginAttempts(String agentID, int attempts, Long timestamp, boolean locked) {
        try {
            ContentValues values = new ContentValues();
            values.put(AgentContract.COLUMN_FAILED_LOGIN_ATTEMPTS, attempts);
            values.put(AgentContract.COLUMN_LAST_FAILED_LOGIN_TIMESTAMP, timestamp);
            values.put(AgentContract.COLUMN_ACCOUNT_LOCKED, locked ? 1 : 0);

            int result = database.update(
                    AgentContract.TABLE_NAME,
                    values,
                    AgentContract.COLUMN_AGENT_ID + " = ?",
                    new String[]{agentID}
            );

            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateLastLogin(String agentID, long timestamp) {
        try {
            ContentValues values = new ContentValues();
            values.put(AgentContract.COLUMN_LAST_LOGIN_TIMESTAMP, timestamp);
            values.put(AgentContract.COLUMN_FAILED_LOGIN_ATTEMPTS, 0);
            values.put(AgentContract.COLUMN_ACCOUNT_LOCKED, 0);

            int result = database.update(
                    AgentContract.TABLE_NAME,
                    values,
                    AgentContract.COLUMN_AGENT_ID + " = ?",
                    new String[]{agentID}
            );

            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isCodenameTaken(String codename) {
        Cursor cursor = null;

        try {
            cursor = database.query(
                    AgentContract.TABLE_NAME,
                    new String[]{AgentContract.COLUMN_AGENT_ID},
                    AgentContract.COLUMN_CODENAME + " = ?",
                    new String[]{codename},
                    null, null, null
            );

            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}