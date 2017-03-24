package com.comapi.internal.helpers;

import android.content.Context;
import android.os.Build;

import com.comapi.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowWifiInfo;
import org.robolectric.shadows.ShadowWifiManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Robolectric tests for application lifecycle observer.
 *
 * @author Marcin Swierczek
 *         Copyright (C) Donky Networks Ltd. All rights reserved.
 * @version 1.0.0
 * @since 1.0.0
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(manifest = "foundation/src/main/AndroidManifest.xml", sdk = Build.VERSION_CODES.M, constants = BuildConfig.class, packageName = "com.comapi")
public class HelpersTest {

    @Test
    public void testDeviceHelper() {

        assertNotNull(DeviceHelper.generateDeviceId(RuntimeEnvironment.application));
        assertEquals(true, DeviceHelper.getAppVersion(RuntimeEnvironment.application) != -1);
        assertNotNull(DeviceHelper.PLATFORM);
        assertNotNull(DeviceHelper.SDK_TYPE);
    }

    @Test
    public void testDateHelper() {

        String timeString = DateHelper.getCurrentUTC();
        long timeAgain = DateHelper.getUTCMilliseconds(timeString);

        assertNotNull(timeString);
        assertTrue(timeAgain != -1);
    }

    @Test
    public void testDateHelper_wrongDate() {
        assertEquals(true, DateHelper.getUTCMilliseconds("wrong date") == -1);
    }

    @Test
    @Config(shadows = {ShadowWifiManager.class, ShadowWifiInfo.class})
    public void deviceHelper_permissionGranted() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException, IOException {

        Method method = DeviceHelper.class.getDeclaredMethod("getMacAddress", Context.class);
        method.setAccessible(true);

        ShadowApplication shadowApp = Shadows.shadowOf(RuntimeEnvironment.application);
        shadowApp.grantPermissions(android.Manifest.permission.ACCESS_WIFI_STATE);

        assertNotNull(method.invoke(null, RuntimeEnvironment.application));
        assertNotNull(DeviceHelper.generateDeviceId(RuntimeEnvironment.application));
    }

    @Test
    @Config(shadows = {ShadowWifiManager.class, ShadowWifiInfo.class})
    public void deviceHelper_permissionDenied() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException, IOException {

        Method method = DeviceHelper.class.getDeclaredMethod("getMacAddress", Context.class);
        method.setAccessible(true);

        ShadowApplication shadowApp = Shadows.shadowOf(RuntimeEnvironment.application);
        shadowApp.denyPermissions(android.Manifest.permission.ACCESS_WIFI_STATE);

        assertNull(method.invoke(null, RuntimeEnvironment.application));
        assertNotNull(DeviceHelper.generateDeviceId(RuntimeEnvironment.application));
        //reset permission
        shadowApp.grantPermissions(android.Manifest.permission.ACCESS_WIFI_STATE);
    }

    public static void waitSomeTime(long milis) throws InterruptedException {
        Thread t = new Thread() {
            public void run() {
                try {
                    sleep(milis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        t.join();
    }
}