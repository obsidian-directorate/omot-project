package org.obsidian.omot.security;

import android.content.Context;

import java.io.File;

import timber.log.Timber;

public class DatabaseWipeManager {

    public static void wipeAllData(Context context) {
        // Wipe encrypted preferences
        SecurityManager.getInstance(context).clearSensitiveData();

        // Wipe database files
        deleteDatabaseFiles(context);

        // Wipe cache
        clearAppCache(context);

        SecurityLogger.logSecurityEvent("DATA_WIPE_COMPLETE", "All sensitive data erased");
    }

    private static void deleteDatabaseFiles(Context context) {
        String[] databaseList = context.databaseList();
        for (String databaseName : databaseList) {
            if (databaseName.startsWith("OMOT") || databaseName.contains("omot")) {
                context.deleteDatabase(databaseName);
            }
        }

        // Delete database files directly
        File databaseDir = new File(context.getApplicationContext().getDataDir() + "/database");
        if (databaseDir.exists()) {
            File[] files = databaseDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().toLowerCase().contains("omot")) {
                        file.delete();
                    }
                }
            }
        }
    }

    private static void clearAppCache(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                deleteDir(cacheDir);
            }
        } catch (Exception e) {
            Timber.tag("DatabaseWipeManager").e("Failed to clear cache: %s", e.getMessage());
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}