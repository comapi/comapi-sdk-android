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

package com.comapi.internal.network;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.comapi.GlobalState;
import com.comapi.StateListener;
import com.comapi.BuildConfig;
import com.comapi.helpers.DataTestHelper;
import com.comapi.helpers.ResponseTestHelper;
import com.comapi.internal.ComapiException;
import com.comapi.internal.data.DataManager;
import com.comapi.internal.data.SessionData;
import com.comapi.internal.log.LogLevel;
import com.comapi.internal.log.LogManager;
import com.comapi.internal.log.Logger;
import com.comapi.internal.network.api.RestApi;
import com.comapi.internal.push.PushManager;
import com.comapi.mock.MockAuthenticator;

import com.comapi.internal.network.sockets.SocketController;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import rx.Observable;

import static com.comapi.helpers.DataTestHelper.API_SPACE_ID;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.robolectric.RuntimeEnvironment.application;

/**
 * Robolectric for Network setup.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.M, constants = BuildConfig.class, packageName = "com.comapi")
public class SessionControllerTest {

    private MockWebServer server;

    private AtomicInteger sessionMockState;

    private RestClient restClient;

    private SessionController sessionController;

    private MockAuthenticator authenticator;

    private AtomicBoolean isSessionCreating;
    private PushManager pushMgr;

    private SessionData session;
    private DataManager dataMgr;
    private RestApi restApi;

    private boolean reAuthShouldFail = false;

    private static final int LIMIT = 1000;

    @Before
    public void setUp() throws Exception {

        server = new MockWebServer();
        server.start();

        DataTestHelper.saveSessionData();
        DataTestHelper.saveDeviceData();

        authenticator = new MockAuthenticator();
        LogManager logMgr = new LogManager();
        logMgr.init(RuntimeEnvironment.application, LogLevel.DEBUG.getValue(), LogLevel.OFF.getValue(), LIMIT);
        dataMgr = new DataManager();
        dataMgr.init(RuntimeEnvironment.application, API_SPACE_ID, new Logger(new LogManager(), ""));
        AuthManager authManager = new AuthManager() {
            @Override
            protected Observable<SessionData> restartSession() {
                return Observable.just(new SessionData().setSessionId("id").setAccessToken("token123").setProfileId("profileId").setExpiresOn(Long.MAX_VALUE));
            }
        };
        restClient = new RestClient(new OkHttpAuthenticator(authManager), LogLevel.DEBUG.getValue(), server.url("/").toString());
        restApi = restClient.getService();

        Logger log = new Logger(new LogManager(), "");

        pushMgr = new PushManager();
        pushMgr.init(application.getApplicationContext(), new Handler(Looper.getMainLooper()), log, () -> "fcm-token", token -> {
            log.d("Refreshed push token is " + token);
            if (!TextUtils.isEmpty(token)) {
                dataMgr.getDeviceDAO().setPushToken(token);
            }
        }, null);

        //InternalService service = new InternalService(dataMgr, pushMgr, "apiSpace", "packageName", log);
        sessionMockState = new AtomicInteger(GlobalState.INITIALISED);
        isSessionCreating = new AtomicBoolean();
        ServiceQueue queue = new ServiceQueue("api_space", dataMgr, log);
        sessionController = new SessionController(new SessionCreateManager(isSessionCreating), pushMgr, sessionMockState, dataMgr, authenticator, restApi, "", new Handler(Looper.getMainLooper()), log, queue.getTaskQueue(), true, new StateListener() {
        });
        sessionController.setSocketController(new SocketController(dataMgr, null, new Logger(new LogManager(), ""), new URI("ws://host"), null));
    }

    @Test
    public void createSession() throws Exception {

        DataTestHelper.clearSessionData();

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_create.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(new MockResponse().setResponseCode(200));

        sessionController.startSession().toBlocking().forEach(response -> {
            assertTrue("someProfileId".equals(response.getProfileId()));
            assertTrue(response.getExpiresOn() > System.currentTimeMillis());
            assertNotNull(response.getSessionId());
            assertNotNull(response.getProfileId());
            assertNotNull(response.getAccessToken());
        });
    }

    @Test(expected = RuntimeException.class)
    public void createSession_authChallengeThrowingException() throws Throwable {

        DataTestHelper.clearSessionData();

        authenticator.setShouldCrash(true);

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_create.json", 200).addHeader("ETag", "eTag"));

        sessionController.startSession().toBlocking().subscribe();
    }

    @Test(expected = RuntimeException.class)
    public void createSession_authChallengeReturnsNull() throws Throwable {

        DataTestHelper.clearSessionData();

        authenticator.setShouldReturnNullToken(true);

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_create.json", 200).addHeader("ETag", "eTag"));

        sessionController.startSession().toBlocking().subscribe();
    }

    @Test(expected = RuntimeException.class)
    public void createSession_sessionCreateInProgress() throws Throwable {

        DataTestHelper.clearSessionData();

        isSessionCreating.set(true);
        sessionController.startSession().toBlocking().subscribe();
    }

    @Test(expected = RuntimeException.class)
    public void createSession_notInitialised() throws Throwable {
        sessionMockState.set(GlobalState.NOT_INITIALISED);
        sessionController.startSession().toBlocking().subscribe();
    }

    @Test
    public void createSession_updatePushFail() throws Throwable {

        DataTestHelper.clearSessionData();

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_create.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(new MockResponse().setResponseCode(404));
        assertNotNull(sessionController.startSession().toBlocking().single());
    }

    @Test(expected = RuntimeException.class)
    public void reAuthenticate_sessionCreateInProgress() throws Throwable {
        isSessionCreating.set(true);
        sessionMockState.set(GlobalState.SESSION_OFF);
        assertNull(sessionController.reAuthenticate().toBlocking().first());
    }

    @Test
    public void reAuthenticate_automatically() throws Throwable {

        SessionController sessionController = new SessionController(new SessionCreateManager(new AtomicBoolean()), pushMgr, new AtomicInteger(), dataMgr, authenticator, restApi, "", new Handler(Looper.getMainLooper()), new Logger(new LogManager(), ""), null, false, new StateListener() {
        }) {
            @Override
            protected Observable<SessionData> reAuthenticate() {
                if (!reAuthShouldFail) {
                    return Observable.just(new SessionData().setAccessToken(UUID.randomUUID().toString()).setExpiresOn(Long.MAX_VALUE).setProfileId("id").setSessionId("id")).doOnNext(session1 -> SessionControllerTest.this.session = session1);
                } else {
                    return Observable.error(new ComapiException(""));
                }
            }
        };

        reAuthShouldFail = false;
        session = null;
        sessionController.scheduleNextAuthentication(0);
        assertNotNull(session);

        reAuthShouldFail = true;
        session = null;
        sessionController.scheduleNextAuthentication(0);
        assertNull(session);
    }

    @Test(expected = RuntimeException.class)
    public void reAuthenticate_sessionNotStarted() throws Throwable {
        isSessionCreating.set(false);
        sessionMockState.set(GlobalState.INITIALISED);
        assertNull(sessionController.reAuthenticate().toBlocking().first());
    }

    @Test(expected = RuntimeException.class)
    public void startSession_nullProfileId() {
        sessionController.startSession().toBlocking().first();
    }

    @Test
    public void deleteSession() throws Exception {

        MockResponse mr = new MockResponse();
        mr.setResponseCode(204);
        server.enqueue(mr);

        sessionController.endSession().toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(204, response.code());
        });
    }

    @After
    public void tearDown() throws Exception {
        DataTestHelper.clearDeviceData();
        DataTestHelper.clearSessionData();
        server.shutdown();
        pushMgr.unregisterPushReceiver(RuntimeEnvironment.application);
    }

}
