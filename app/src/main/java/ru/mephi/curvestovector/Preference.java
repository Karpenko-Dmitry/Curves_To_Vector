package ru.mephi.curvestovector;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;


public class Preference {

    private static final String PREF_X_VALUE = "xValue";
    private static final String PREF_Y_VALUE = "yValue";
    private static final String PREF_COLOR_PIXEL = "colorPixel";
    private static final String PREF_MIN_WIDTH = "minWidth";
    private static final String PREF_MAX_WIDTH = "maxWidth";
    private static final String PREF_MIN_LENGHT = "minLenght";
    private static final String PREF_MAX_LENGHT = "maxLenght";
    private static final String PREF_IS_LINE = "line";
    private static final String PREF_TIME = "time";

    public static void setXValue(Context context, float value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putFloat(PREF_X_VALUE, value)
                .apply();
    }

    public static float getXValue(Context context) {
         return PreferenceManager.getDefaultSharedPreferences(context)
                 .getFloat(PREF_X_VALUE, 50);
    }

    public static void setYValue(Context context, float value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putFloat(PREF_Y_VALUE, value)
                .apply();
    }

    public static float getYValue(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getFloat(PREF_Y_VALUE, 4);
    }

    public static int getColorPixel(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_COLOR_PIXEL, Color.BLACK);
    }

    public static void setColorPixel(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_COLOR_PIXEL, value)
                .apply();
    }

    public static int getMinWidth(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_MIN_WIDTH, 50);
    }

    public static void setMinWidth(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_MIN_WIDTH, value)
                .apply();
    }

    public static int getMaxWidth(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_MAX_WIDTH, 200);
    }

    public static void setMaxWidth(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_MAX_WIDTH, value)
                .apply();
    }

    public static int getMinLenght(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_MIN_LENGHT, 100);
    }

    public static void setMinLenght(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_MIN_LENGHT, value)
                .apply();
    }

    public static int getMaxLenght(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_MAX_LENGHT, 2000);
    }

    public static void setMaxLenght(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_MAX_LENGHT, value)
                .apply();
    }

    public static boolean isLine(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_IS_LINE, true);
    }

    public static void setIsLine(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_IS_LINE, value)
                .apply();
    }

    public static int getTime(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_TIME, 1000);
    }

    public static void setTime(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_TIME, value)
                .apply();
    }

}
