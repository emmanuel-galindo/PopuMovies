package com.popumovies.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefUtil {


    static public final class Prefs {
        public static SharedPreferences get(Context context) {
            return context.getSharedPreferences("_popumovies_pref", 0);
        }
    } 

    static public String getString(Context context, String key) {
        SharedPreferences settings = Prefs.get(context);
        return settings.getString(key, "");
    }
    
    static public String getString(Context context, String key, String defaultString) {
        SharedPreferences settings = Prefs.get(context);
        return settings.getString(key, defaultString);
    }

    static public synchronized void setString(Context context, String key,
            String value) {
        SharedPreferences settings = Prefs.get(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    static public synchronized void setInt(Context context, String key,
                                              int value) {
        SharedPreferences settings = Prefs.get(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    static public int getInt(Context context, String key) {
        SharedPreferences settings = Prefs.get(context);
        return settings.getInt(key, 0);
    }

    static public int getInt(Context context, String key, int defValue) {
        SharedPreferences settings = Prefs.get(context);
        return settings.getInt(key, defValue);
    }

    static public synchronized void setBoolean(Context context, String key,
                                           boolean value) {
        SharedPreferences settings = Prefs.get(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    static public boolean getBoolean(Context context, String key) {
        SharedPreferences settings = Prefs.get(context);
        return settings.getBoolean(key, false);
    }

    static public boolean getBoolean(Context context, String key, boolean defValue) {
        SharedPreferences settings = Prefs.get(context);
        return settings.getBoolean(key, defValue);
    }

}

