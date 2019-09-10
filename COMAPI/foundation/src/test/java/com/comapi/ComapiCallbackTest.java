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

package com.comapi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.comapi.helpers.DataTestHelper;
import com.comapi.helpers.MockCallback;
import com.comapi.helpers.ResponseTestHelper;
import com.comapi.internal.CallbackAdapter;
import com.comapi.internal.helpers.HelpersTest;
import com.comapi.internal.log.LogConfig;
import com.comapi.internal.log.LogLevel;
import com.comapi.internal.network.AuthManager;
import com.comapi.internal.network.ComapiResult;
import com.comapi.internal.push.IDService;
import com.comapi.mock.MockAuthenticator;
import com.comapi.mock.ShadowGoogleApiAvailability;
import com.google.android.gms.common.ConnectionResult;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowBluetoothAdapter;
import org.robolectric.android.controller.ActivityController;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for APIs version with callbacks in client class.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.M, constants = BuildConfig.class, packageName = "com.comapi")
public class ComapiCallbackTest {

    private static final long TIME_OUT = 10000;

    private MockWebServer server;
    private CallbackAdapter callbackAdapter;
    private APIConfig apiConfig;

    @Before
    public void prepare() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {

        server = new MockWebServer();
        server.start();

        apiConfig = new APIConfig().service(server.url("/").toString()).socket("ws://10.0.0.0");

        callbackAdapter = new CallbackAdapter() {
            public <T> void adapt(@NonNull final Observable<T> subscriber, @Nullable final Callback<T> callback) {
                subscriber.subscribeOn(Schedulers.io())
                        .subscribe(new Subscriber<T>() {

                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                if (callback != null) {
                                    callback.error(e);
                                }
                            }

                            @Override
                            public void onNext(T result) {
                                if (callback != null) {
                                    callback.success(result);
                                }
                            }
                        });
            }
        };
    }

    @SuppressLint("CommitPrefEdits")
    @Config(shadows = {ShadowGoogleApiAvailability.class, ShadowBluetoothAdapter.class})
    @Test
    public void initWithExistingSession_checkIfDataLoaded() throws InterruptedException, IOException {

        DataTestHelper.saveSessionData();
        DataTestHelper.saveDeviceData();

        // Force success
        ShadowGoogleApiAvailability.setIsGooglePlayServicesAvailable(ConnectionResult.SUCCESS);

        ComapiConfig config = new ComapiConfig()
                .overrideCallbackAdapter(callbackAdapter)
                .apiSpaceId("apiSpaceId")
                .authenticator(new MockAuthenticator())
                .apiConfiguration(apiConfig)
                .pushTokenProvider(() -> "fcm-token")
                .logConfig(new LogConfig()
                        .setConsoleLevel(LogLevel.DEBUG)
                        .setFileLevel(LogLevel.OFF)
                        .setNetworkLevel(LogLevel.DEBUG));

        //For push update
        MockResponse mr = new MockResponse();
        mr.setResponseCode(200);
        server.enqueue(mr);

        final MockCallback<ComapiClient> listener = new MockCallback<>();

        Comapi.initialise(RuntimeEnvironment.application, config, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        ComapiClient pc = listener.getResult();

        ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class);
        controller.create().start().resume().get();
        HelpersTest.waitSomeTime(1500);
        controller.pause().stop().saveInstanceState(new Bundle()).destroy().get();
        HelpersTest.waitSomeTime(1500);
        controller = Robolectric.buildActivity(Activity.class);
        controller.create().start().resume().get();
        HelpersTest.waitSomeTime(1500);

        Intent intent = new Intent(IDService.ACTION_REFRESH_PUSH);
        LocalBroadcastManager.getInstance(RuntimeEnvironment.application).sendBroadcast(intent);

        assertTrue(pc.getState() == GlobalState.SESSION_ACTIVE);

        Session session = pc.getSession();
        assertNotNull(session);
        assertNotNull(session.getProfileId());
        assertEquals(DataTestHelper.PROFILE_ID, session.getProfileId());
        assertTrue(session.isSuccessfullyCreated());

        DataTestHelper.clearSessionData();
        DataTestHelper.clearDeviceData();

        pc.clean(RuntimeEnvironment.application);
    }

    @Test(expected = RuntimeException.class)
    public void getSharedClient_comapiNotInitialised_shouldFail() throws InterruptedException, IOException {
        Comapi.getShared().service();
        Comapi.getShared().clean(RuntimeEnvironment.application);
    }

    @Test
    public void init_shouldFail() throws InterruptedException, IOException {

        ComapiConfig config = new ComapiConfig();

        final MockCallback<ComapiClient> listener = new MockCallback<>();

        Comapi.reset();
        Comapi.initialiseShared(RuntimeEnvironment.application, config, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertNotNull(listener.getError());
    }

    @SuppressLint("CommitPrefEdits")
    @Config(shadows = {ShadowGoogleApiAvailability.class, ShadowBluetoothAdapter.class})
    @Test
    public void init_checkLogs() throws InterruptedException, IOException {

        ComapiConfig config = new ComapiConfig()
                .overrideCallbackAdapter(callbackAdapter)
                .apiSpaceId("apiSpaceId")
                .apiConfiguration(apiConfig)
                .authenticator(new MockAuthenticator())
                .pushTokenProvider(() -> "fcm-token")
                .logConfig(new LogConfig()
                        .setConsoleLevel(LogLevel.OFF)
                        .setFileLevel(LogLevel.DEBUG)
                        .setNetworkLevel(LogLevel.OFF));

        Comapi.reset();

        final MockCallback<ComapiClient> listener = new MockCallback<>();
        Comapi.initialiseShared(RuntimeEnvironment.application, config, listener);
        synchronized (listener) {
            listener.wait(TIME_OUT);
        }
        ComapiClient pc = listener.getResult();

        final MockCallback<String> listenerLogs = new MockCallback<>();
        pc.getLogs(listenerLogs);

        synchronized (listenerLogs) {
            listenerLogs.wait(TIME_OUT);
        }

        Assert.assertNotNull(listenerLogs.getResult());
        pc.clean(RuntimeEnvironment.application);
    }

    @SuppressLint("CommitPrefEdits")
    @Config(shadows = {ShadowGoogleApiAvailability.class, ShadowBluetoothAdapter.class})
    @Test
    public void initialise_nullAPISPace() throws InterruptedException, IOException {

        // Force success
        ShadowGoogleApiAvailability.setIsGooglePlayServicesAvailable(ConnectionResult.SUCCESS);

        server.enqueue(createMockResponse("rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(createMockResponse("rest_session_create.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(getMockPushResponse());

        ComapiConfig config = new ComapiConfig()
                .overrideCallbackAdapter(callbackAdapter)
                .apiSpaceId(null)
                .apiConfiguration(apiConfig)
                .authenticator(new MockAuthenticator())
                .pushTokenProvider(() -> "fcm-token")
                .logConfig(new LogConfig()
                        .setConsoleLevel(LogLevel.DEBUG)
                        .setNetworkLevel(LogLevel.DEBUG));

        final MockCallback<ComapiClient> listener = new MockCallback<>();
        Comapi.initialise(RuntimeEnvironment.application, config, listener);
        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertNotNull(listener.getError());
    }

    @SuppressLint("CommitPrefEdits")
    @Config(shadows = {ShadowGoogleApiAvailability.class, ShadowBluetoothAdapter.class})
    @Test
    public void initialise_nullConfig() throws InterruptedException, IOException {

        // Force success
        ShadowGoogleApiAvailability.setIsGooglePlayServicesAvailable(ConnectionResult.SUCCESS);

        server.enqueue(createMockResponse("rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(createMockResponse("rest_session_create.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(getMockPushResponse());

        final MockCallback<ComapiClient> listener = new MockCallback<>();
        Comapi.initialise(RuntimeEnvironment.application, new ComapiConfig().apiConfiguration(apiConfig).pushTokenProvider(() -> "fcm-token")
                .overrideCallbackAdapter(callbackAdapter), listener);
        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertNotNull(listener.getError());
    }

    @SuppressLint("CommitPrefEdits")
    @Config(shadows = {ShadowGoogleApiAvailability.class, ShadowBluetoothAdapter.class})
    @Test
    public void initialise_nullAuthenticator() throws InterruptedException, IOException {

        // Force success
        ShadowGoogleApiAvailability.setIsGooglePlayServicesAvailable(ConnectionResult.SUCCESS);

        server.enqueue(createMockResponse("rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(createMockResponse("rest_session_create.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(getMockPushResponse());

        ComapiConfig config = new ComapiConfig()
                .apiSpaceId("apiSpaceId")
                .apiConfiguration(apiConfig)
                .authenticator(null)
                .pushTokenProvider(() -> "fcm-token")
                .logConfig(new LogConfig()
                        .setConsoleLevel(LogLevel.DEBUG)
                        .setNetworkLevel(LogLevel.DEBUG));

        final MockCallback<ComapiClient> listener = new MockCallback<>();
        Comapi.initialise(RuntimeEnvironment.application, config, listener);
        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertNotNull(listener.getError());
    }

    @SuppressLint("CommitPrefEdits")
    @Config(shadows = {ShadowGoogleApiAvailability.class, ShadowBluetoothAdapter.class})
    @Test
    public void reAuthenticate() throws InterruptedException, IOException {

        // Force success
        ShadowGoogleApiAvailability.setIsGooglePlayServicesAvailable(ConnectionResult.SUCCESS);

        DataTestHelper.saveDeviceData();
        DataTestHelper.saveSessionData();

        ComapiConfig config = new ComapiConfig()
                .overrideCallbackAdapter(callbackAdapter)
                .apiSpaceId("apiSpaceId")
                .apiConfiguration(apiConfig)
                .pushTokenProvider(() -> "fcm-token")
                .authenticator(new MockAuthenticator())
                .logConfig(new LogConfig()
                        .setConsoleLevel(LogLevel.DEBUG)
                        .setNetworkLevel(LogLevel.DEBUG));

        //For push update
        MockResponse mr = new MockResponse();
        mr.setResponseCode(200);
        server.enqueue(mr);

        final MockCallback<ComapiClient> listenerInit = new MockCallback<>();
        Comapi.initialise(RuntimeEnvironment.application, config, listenerInit);
        synchronized (listenerInit) {
            listenerInit.wait(TIME_OUT);
        }
        ComapiClient pc = listenerInit.getResult();

        mr = new MockResponse();
        mr.setResponseCode(401);
        server.enqueue(mr);
        server.enqueue(createMockResponse("rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(createMockResponse("rest_session_create.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(getMockPushResponse());
        server.enqueue(createMockResponse("rest_profile_update.json", 200).addHeader("ETag", "eTag"));

        final MockCallback<ComapiResult<Map<String, Object>>> listener = new MockCallback<>();
        pc.service().profile().updateProfile(new HashMap<>(), null, listener);
        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        pc.clean(RuntimeEnvironment.application);
        assertNotNull(listener.getResult().getResult().get("id"));
    }

    @Test
    public void serviceInstances() throws InterruptedException {

        ComapiConfig config = new ComapiConfig()
                .overrideCallbackAdapter(callbackAdapter)
                .apiSpaceId("apiSpaceId")
                .apiConfiguration(apiConfig)
                .pushTokenProvider(() -> "fcm-token")
                .authenticator(new MockAuthenticator())

                .logConfig(new LogConfig()
                        .setConsoleLevel(LogLevel.DEBUG)
                        .setNetworkLevel(LogLevel.DEBUG));

        final MockCallback<ComapiClient> listenerInit = new MockCallback<>();
        Comapi.initialise(RuntimeEnvironment.application, config, listenerInit);
        synchronized (listenerInit) {
            listenerInit.wait(TIME_OUT);
        }
        ComapiClient pc = listenerInit.getResult();

        assertNotNull(pc.service().messaging());
        assertNotNull(pc.service().profile());
        assertNotNull(pc.service().session());
    }

    @SuppressLint("CommitPrefEdits")
    @Config(shadows = {ShadowGoogleApiAvailability.class, ShadowBluetoothAdapter.class})
    @Test
    public void initialise_reAuthenticate() throws InterruptedException, IOException {

        // Force success
        ShadowGoogleApiAvailability.setIsGooglePlayServicesAvailable(ConnectionResult.SUCCESS);

        DataTestHelper.saveDeviceData();
        DataTestHelper.saveExpiredSessionData();

        ComapiConfig config = new ComapiConfig()
                .overrideCallbackAdapter(callbackAdapter)
                .apiSpaceId("apiSpaceId")
                .apiConfiguration(apiConfig)
                .pushTokenProvider(() -> "fcm-token")
                .authenticator(new MockAuthenticator())
                .logConfig(new LogConfig()
                        .setConsoleLevel(LogLevel.DEBUG)
                        .setNetworkLevel(LogLevel.DEBUG));

        server.enqueue(createMockResponse("rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(createMockResponse("rest_session_create.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(getMockPushResponse());

        final MockCallback<ComapiClient> listenerInit = new MockCallback<>();
        Comapi.initialise(RuntimeEnvironment.application, config, listenerInit);
        synchronized (listenerInit) {
            listenerInit.wait(TIME_OUT);
        }
        ComapiClient pc = listenerInit.getResult();

        assertNotNull(pc);
        pc.clean(RuntimeEnvironment.application);
    }

    private MockResponse createMockResponse(String fileName, int responseCode) throws IOException {
        String json = ResponseTestHelper.readFromFile(this, fileName);
        MockResponse response = new MockResponse();
        response.addHeader("Authorization", AuthManager.addAuthPrefix("token123"));
        response.setResponseCode(responseCode);
        response.setBody(json);
        return response;
    }

    private MockResponse getMockPushResponse() {
        MockResponse mr = new MockResponse();
        mr.setResponseCode(200);
        return mr;
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
        DataTestHelper.clearDeviceData();
        DataTestHelper.clearSessionData();
        Comapi.reset();
    }
}
