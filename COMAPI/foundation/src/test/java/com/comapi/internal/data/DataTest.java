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

package com.comapi.internal.data;

import android.os.Build;

import com.comapi.BuildConfig;
import com.comapi.internal.log.LogManager;

import com.comapi.internal.log.Logger;
import com.comapi.internal.network.model.messaging.MessageReceived;
import com.comapi.internal.network.model.messaging.OrphanedEvent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
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
 * @since 1.0.0
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.M, constants = BuildConfig.class, packageName = "com.comapi")
public class DataTest {

    DataManager mgr;

    @Before
    public void setUpComapi() throws Exception {
        mgr = new DataManager();
        mgr.init(RuntimeEnvironment.application, API_SPACE_ID, new Logger(new LogManager(), ""));
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
    public void testOrphanedEventConstructors() throws Exception {
        OrphanedEvent event = new OrphanedEvent();
        OrphanedEvent.OrphanedEventData data = event.new OrphanedEventData();
        OrphanedEvent.OrphanedEventPayload payload = event.new OrphanedEventPayload();
        assertNotNull(data);
        assertNotNull(payload);
    }

    @Test
    public void testMsgReceivedConstructors() throws Exception {
        MessageReceived msg = new MessageReceived();
        MessageReceived.Status status = msg.new Status();
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