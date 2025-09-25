package org.obsidian.omot.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import org.obsidian.omot.R;

public class OSDLogoImporter {
    public static Bitmap importOSDLogo(Context context, String filePath) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            return Bitmap.createScaledBitmap(bitmap, 512, 512, true);
        } catch (Exception e) {
            // Fallback to vector logo
            return getVectorLogo(context);
        }
    }

    private static Bitmap getVectorLogo(Context context) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, R.drawable.ic_launcher_standard);
        Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }
}