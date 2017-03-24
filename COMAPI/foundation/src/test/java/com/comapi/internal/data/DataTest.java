package com.comapi.internal.data;

import android.os.Build;

import com.comapi.BuildConfig;
import com.comapi.internal.log.LogManager;

import com.comapi.internal.log.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.comapi.helpers.DataTestHelper.API_SPACE_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
public class DataTest {

    DataManager mgr;

    @Before
    public void setUpComapi() throws Exception {
        mgr = new DataManager();
        mgr.init(RuntimeEnvironment.application, API_SPACE_ID,new Logger(new LogManager(), ""));
    }

    @Test
    public void testDeviceDAO() {

        DeviceDAO deviceDAO = mgr.getDeviceDAO();

        String apiSpace = "apiSpaceValue";
        int appVer = 92734;
        String deviceId = "deviceIdValue";
        String instanceId = "instanceIdValue";
        String pushToken = "pushTokenValue";

        deviceDAO.setApiSpaceId(apiSpace);
        deviceDAO.setAppVer(appVer);
        deviceDAO.setDeviceId(deviceId);
        deviceDAO.setInstanceId(instanceId);
        deviceDAO.setPushToken(pushToken);

        Device device = deviceDAO.device();
        assertEquals(apiSpace, device.getApiSpaceId());
        assertEquals(deviceId, device.getDeviceId());
        assertEquals(instanceId, device.getInstanceId());
        assertEquals(pushToken, device.getPushToken());
        assertEquals(appVer, device.getAppVer());

        deviceDAO.clear("dId");
        device = deviceDAO.device();
        assertNull(device.getDeviceId());
        assertNotNull(device.getApiSpaceId());

        deviceDAO.clearAll();
        device = deviceDAO.device();
        assertNull(device.getDeviceId());
        assertNull(device.getApiSpaceId());
        assertEquals(-1, device.getAppVer());
        assertNull(device.getInstanceId());
        assertNull(device.getPushToken());

    }

    @Test
    public void testSessionDAO() {

        SessionDAO sessionDAO = mgr.getSessionDAO();

        String profileId = "apiSpaceValue";
        String token = "tokenValue";
        long expiresOn = 32525902231213L;
        String sessionId = "sessionIdValue";

        SessionData session = new SessionData();
        session.setProfileId(profileId);
        session.setAccessToken(token);
        session.setExpiresOn(expiresOn);
        session.setSessionId(sessionId);

        sessionDAO.startSession();
        sessionDAO.updateSessionDetails(session);

        SessionData loadedSession = sessionDAO.session();
        assertEquals(profileId, loadedSession.getProfileId());
        assertEquals(token, loadedSession.getAccessToken());
        assertEquals(expiresOn, loadedSession.getExpiresOn());
        assertEquals(sessionId, loadedSession.getSessionId());

        sessionDAO.clearSession();
        assertNull(sessionDAO.session());
    }

    @Test
    public void testController() {

        Device device = mgr.getDeviceDAO().device();
        assertNotNull(device.getDeviceId());
        assertNotNull(device.getInstanceId());
        assertEquals(true, device.getAppVer() >= 0);

    }

    @After
    public void tearDown() throws Exception {
        mgr.getDeviceDAO().clearAll();
        mgr.getSessionDAO().clearAll();
    }

}