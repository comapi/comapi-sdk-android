package com.comapi.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import org.robolectric.RuntimeEnvironment;

/**
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DataTestHelper {

    /*
        Session
     */

    public static final String KEY_PROFILE_ID = "pId";

    public static final String KEY_SESSION_ID = "sId";

    public static final String KEY_ACCESS_TOKEN = "sT";

    public static final String KEY_EXPIRES_ON = "exp";

    public static final String PROFILE_ID = "profileId";

    public static final String SESSION_ID = "sessionId";

    public static final String ACCESS_TOKEN = "accessToken";

    public static final long EXPIRES_ON = Long.MAX_VALUE;

    /*
        Device
     */

    public static final String KEY_INSTANCE_ID = "iId";

    public static final String KEY_APP_VER = "aV";

    public static final String KEY_DEVICE_ID = "dId";

    public static final String KEY_API_SPACE_ID = "aS";

    public static final String KEY_PUSH_TOKEN = "pushToken";

    public static final String INSTANCE_ID = "instanceId";

    public static final int APP_VER = 1;

    public static final String DEVICE_ID = "deviceId";

    public static final String API_SPACE_ID = "apiSpaceId";

    public static final String PUSH_TOKEN = "pushToken";

    /*
        Files
     */

    public static final String fileNameSession = "profile."+API_SPACE_ID;

    public static final String fileNameDevice = "device."+API_SPACE_ID;

    public static void saveSessionData() {

        SharedPreferences sharedPreferences = RuntimeEnvironment.application.getSharedPreferences(fileNameSession, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PROFILE_ID, PROFILE_ID);
        editor.putString(KEY_SESSION_ID, SESSION_ID);
        editor.putLong(KEY_EXPIRES_ON, EXPIRES_ON);
        editor.putString(KEY_ACCESS_TOKEN, ACCESS_TOKEN);
        editor.clear().apply();
    }

    public static void saveExpiredSessionData() {

        SharedPreferences sharedPreferences = RuntimeEnvironment.application.getSharedPreferences(fileNameSession, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PROFILE_ID, PROFILE_ID);
        editor.putString(KEY_SESSION_ID, SESSION_ID);
        editor.putLong(KEY_EXPIRES_ON, 0);
        editor.putString(KEY_ACCESS_TOKEN, ACCESS_TOKEN);
        editor.clear().apply();
    }

    public static void clearSessionData() {
        SharedPreferences sharedPreferences = RuntimeEnvironment.application.getSharedPreferences(fileNameSession, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().apply();
    }

    public static void saveDeviceData() {

        SharedPreferences sharedPreferences = RuntimeEnvironment.application.getSharedPreferences(fileNameDevice, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_DEVICE_ID, DEVICE_ID);
        editor.putString(KEY_INSTANCE_ID, INSTANCE_ID);
        editor.putInt(KEY_APP_VER, APP_VER);
        editor.putString(KEY_API_SPACE_ID, API_SPACE_ID);
        editor.clear().apply();
    }

    public static void clearDeviceData() {
        SharedPreferences sharedPreferences = RuntimeEnvironment.application.getSharedPreferences(fileNameDevice, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().apply();
    }
}
