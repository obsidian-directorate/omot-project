package org.obsidian.omot.data.repository;

import android.content.Context;

import org.obsidian.omot.data.DBHelper;
import org.obsidian.omot.data.daos.AgentDAO;
import org.obsidian.omot.data.daos.ClearanceLevelDAO;
import org.obsidian.omot.data.encryption.DBEncryptionHelper;

public class DBRepository {
    private static DBRepository instance;
    private final DBHelper dbHelper;
    private final DBEncryptionHelper encryptionHelper;
    private final Context context;

    public static synchronized DBRepository getInstance(Context context) {
        if (instance == null) {
            instance = new DBRepository(context.getApplicationContext());
        }
        return instance;
    }

    private DBRepository(Context context) {
        this.context = context;
        this.dbHelper = new DBHelper(context);
        this.encryptionHelper = new DBEncryptionHelper(context);
    }

    public AgentDAO getAgentDAO() {
        return new AgentDAO(dbHelper.getWritableDatabase(), encryptionHelper);
    }

    public ClearanceLevelDAO getClearanceLevelDAO() {
        return new ClearanceLevelDAO(dbHelper.getWritableDatabase());
    }

    public void close() {
        dbHelper.close();
    }

    public void clearAllData() {
        dbHelper.clearAllData();
    }

    public void rotateEncryptionKey() {
        encryptionHelper.rotateEncryptionKey();
    }
}