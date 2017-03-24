package com.comapi.internal.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * Base class for all Database Access Objects based on Android Shared Preferences.
 * <p>
 * Created by Marcin Swierczek
 * 06/04/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
@SuppressWarnings("SameParameterValue")
class BaseDAO {

    private final SharedPreferences sharedPreferences;

    /**
     * Recommended constructor.
     *
     * @param context  Application context
     * @param fileName Shared context file name.
     */
    BaseDAO(Context context, String fileName) {
        sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    /**
     * Saves Long value in internal shared preferences file.
     *
     * @param key   Key for shared preference entry.
     * @param value Value for shared preference entry.
     * @return Returns true if the new values were successfully written
     */
    boolean putInt(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    /**
     * Saves String in internal shared preferences file.
     *
     * @param key   Key for internal preference entry.
     * @param value Value for internal preference entry.
     * @return Returns true if the new values were successfully written
     */
    boolean putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    /**
     * Clear all entries in internal shared preferences file.
     *
     * @return Returns true if the new values were successfully written
     */
    boolean clearAll() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Map<String, ?> all = sharedPreferences.getAll();
        //noinspection Convert2streamapi
        for (String key : all.keySet()) {
            editor.remove(key);
        }
        return editor.commit();
    }

    /**
     * Clear entry.
     *
     * @param key Key for internal preference entry.
     * @return Returns true if the new values were successfully written
     */
    boolean clear(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        return editor.commit();
    }
}
