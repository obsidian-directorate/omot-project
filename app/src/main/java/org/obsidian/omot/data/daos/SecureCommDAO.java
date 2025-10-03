package org.obsidian.omot.data.daos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.obsidian.omot.data.contracts.SecureCommsContract;
import org.obsidian.omot.data.entities.SecureComm;

import java.util.ArrayList;
import java.util.List;

public class SecureCommDAO {
    private final SQLiteDatabase database;

    public SecureCommDAO(SQLiteDatabase database) {
        this.database = database;
    }

    public List<SecureComm> getMessagesForAgent(String agentID) {
        List<SecureComm> messages = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = database.query(
                    SecureCommsContract.TABLE_NAME,
                    null,
                    SecureCommsContract.COLUMN_RECIPIENT_ID + " = ?",
                    new String[]{agentID},
                    null, null,
                    SecureCommsContract.COLUMN_SENT_AT + " DESC"
            );

            while (cursor != null && cursor.moveToNext()) {
                messages.add(SecureComm.fromCursor(cursor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return messages;
    }

    public boolean markMessageAsRead(String messageID) {
        try {
            ContentValues values = new ContentValues();
            values.put(SecureCommsContract.COLUMN_READ_AT, System.currentTimeMillis());

            int result = database.update(
                    SecureCommsContract.TABLE_NAME,
                    values,
                    SecureCommsContract.COLUMN_MESSAGE_ID + " = ?",
                    new String[]{messageID}
            );

            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void cleanupExpiredMessages() {
        try {
            long currentTime = System.currentTimeMillis();
            database.delete(
                    SecureCommsContract.TABLE_NAME,
                    SecureCommsContract.COLUMN_SELF_DESTRUCT_AT + " IS NOT NULL AND " + SecureCommsContract.COLUMN_SELF_DESTRUCT_AT + " < ?",
                    new String[]{String.valueOf(currentTime)}
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}