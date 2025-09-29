package org.obsidian.omot.utils;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

import org.obsidian.omot.R;

public class ThemeUtilities {

    public static void applyFullScreenTheme(Activity activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        activity.getWindow().setNavigationBarColor(
                activity.getResources().getColor(R.color.omot_black)
        );
    }
}