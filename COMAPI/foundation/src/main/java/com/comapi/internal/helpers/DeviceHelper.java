/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Comapi (trading name of Dynmark International Limited)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons
 * to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.installations.FirebaseInstallations;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Helper to obtain basic device information.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
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
                deviceId = Tasks.await(FirebaseInstallations.getInstance().getId());
            } catch (IllegalStateException e) {
                //Continue.
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
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
