package com.comapi.internal.helpers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.UUID;

/**
 * Helper to obtain basic device information.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
@SuppressLint("HardwareIds")
public class DeviceHelper {

    public static final String PLATFORM = "android";

    public static final String SDK_TYPE = "native";

    /**
     * Get Application version registered by the OS.
     *
     * @return Application version registered by the OS.
     */
    public static int getAppVersion(@NonNull Context context) {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    /**
     * Generates unique device id.
     *
     * @return Device unique identifier.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static String generateDeviceId(@NonNull Context context) {

        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        if (TextUtils.isEmpty(deviceId)) {
            deviceId = getMacAddress(context);
        }

        if (TextUtils.isEmpty(deviceId)) {
            try {
                deviceId = FirebaseInstanceId.getInstance().getId();
            } catch (IllegalStateException e) {
                //Continue.
            }
        }

        if (TextUtils.isEmpty(deviceId)) {
            deviceId = UUID.randomUUID().toString();
        }

        return deviceId;
    }

    /**
     * Gets device mac address.
     *
     * @param context App context.
     * @return Device mac address.
     */
    private static String getMacAddress(@NonNull Context context) {

        if (isWifiStatePermissionGranted(context)) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    return wifiInfo.getMacAddress();
                }
            }
        }

        return null;
    }

    /**
     * Checks if the {@link Manifest.permission#ACCESS_WIFI_STATE} permission has been granted.
     *
     * @return True if wifi access permission has been granted.
     */
    private static boolean isWifiStatePermissionGranted(@NonNull Context context) {
        int res = context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_WIFI_STATE);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
}
