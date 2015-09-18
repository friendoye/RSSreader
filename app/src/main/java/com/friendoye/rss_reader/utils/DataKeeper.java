package com.friendoye.rss_reader.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Helper class for keeping data in SharedPreferences
 */
public class DataKeeper {

    public static void saveString(Context context, String key, String value) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        preferences.edit()
                .putString(key, value)
                .commit();
    }

    public static String restoreString(Context context, String key) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return preferences.getString(key, null);

    }
}
