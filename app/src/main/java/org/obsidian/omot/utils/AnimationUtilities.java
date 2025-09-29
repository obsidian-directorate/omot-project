package org.obsidian.omot.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import org.obsidian.omot.R;

public class AnimationUtilities {

    public static void startPulseAnimation(View view, float fromScale, float toScale, long duration) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                fromScale, toScale, fromScale, toScale,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(duration);
        scaleAnimation.setRepeatCount(Animation.INFINITE);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        view.startAnimation(scaleAnimation);
    }

    public static void shakeView(View view) {
        Animation shake = AnimationUtils.loadAnimation(view.getContext(), R.anim.shake);
        view.startAnimation(shake);
    }

    public static void slideInFromRight(View view) {
        Animation slideIn = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_in_right);
        view.startAnimation(slideIn);
    }

    public static void slideOutToLeft(View view) {
        Animation slideOut = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_out_left);
        view.startAnimation(slideOut);
    }

    public static void slideInFromLeft(View view) {
        Animation slideIn = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_in_left);
        view.startAnimation(slideIn);
    }

    public static void slideOutToRight(View view) {
        Animation slideOut = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_out_right);
        view.startAnimation(slideOut);
    }

    public static void startScanLineAnimation(View scanLine, View container) {
        TranslateAnimation animation = new TranslateAnimation(
                Animation.ABSOLUTE, 0f,
                Animation.ABSOLUTE, 0f,
                Animation.ABSOLUTE, -100f,
                Animation.ABSOLUTE, container.getHeight()
        );
        animation.setDuration(2000);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.RESTART);
        scanLine.startAnimation(animation);
    }
}