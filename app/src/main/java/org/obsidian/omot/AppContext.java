package org.obsidian.omot;

import android.app.Application;
import android.content.Context;

import org.obsidian.omot.security.SecurityManager;

public class AppContext extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        // Initialize security system
        initializeSecurity();
    }

    private void initializeSecurity() {
        SecurityManager.getInstance(this);
    }

    public static Context getContext() {
        return context;
    }
}