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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.comapi.helpers.DataTestHelper;
import com.comapi.helpers.FileHelper;
import com.comapi.helpers.ResponseTestHelper;
import com.comapi.internal.CallbackAdapter;
import com.comapi.internal.ComapiException;
import com.comapi.internal.NetworkConnectivityListener;
import com.comapi.internal.helpers.HelpersTest;
import com.comapi.internal.lifecycle.LifecycleListener;
import com.comapi.internal.log.LogConfig;
import com.comapi.internal.log.LogLevel;
import com.comapi.internal.network.AuthManager;
import com.comapi.internal.push.IDService;
import com.comapi.internal.receivers.InternetConnectionReceiver;
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
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowBluetoothAdapter;
import org.robolectric.android.controller.ActivityController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * @author Marcin Swierczek
 * @since 1.0.0
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.M, constants = BuildConfig.class, packageName = "com.comapi")
public class ComapiClientTest {

    private MockWebServer server;
    private APIConfig apiConfig;

    @Before
    public void prepare() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {

        server = new MockWebServer();
        server.start();
        apiConfig = new APIConfig().service(server.url("/").toString()).socket("ws://10.0.0.0").proxy("http://10.0.0.0");
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

        RxComapiClient pc = RxComapi.initialise(RuntimeEnvironment.application, config).toBlocking().single();

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

        assertNotNull(pc.getLogger());
        pc.addLifecycleListener(new LifecycleListener() {
            @Override
            public void onForegrounded(Context context) {

            }

            @Override
            public void onBackgrounded(Context context) {

            }
        });
        assertNotNull(pc.getComapiService());

        DataTestHelper.clearSessionData();
        DataTestHelper.clearDeviceData();
        pc.clean(RuntimeEnvironment.application);
    }

    @Test(expected = RuntimeException.class)
    public void getSharedClient_comapiNotInitialised_shouldFail() throws InterruptedException, IOException {
        RxComapi.getShared().service();
        RxComapi.getShared().clean(RuntimeEnvironment.application);
    }

    @SuppressLint("CommitPrefEdits")
    @Config(shadows = {ShadowGoogleApiAvailability.class, ShadowBluetoothAdapter.class})
    @Test
    public void init_checkLogs() throws InterruptedException, IOException {

        ComapiConfig config = new ComapiConfig()
                .apiSpaceId("apiSpaceId")
                .apiConfiguration(apiConfig)
                .authenticator(new MockAuthenticator())
                .pushTokenProvider(() -> "fcm-token")
                .logConfig(new LogConfig()
                        .setConsoleLevel(LogLevel.OFF)
                        .setFileLevel(LogLevel.DEBUG)
                        .setNetworkLevel(LogLevel.OFF));

        RxComapi.reset();
        RxComapiClient pc = RxComapi.initialiseShared(RuntimeEnvironment.application, config).toBlocking().single();
        Assert.assertNotNull(pc.getLogs().toBlocking().single());

        pc.clean(RuntimeEnvironment.application);
    }

    @Test(expected = Exception.class)
    public void initTwice_shouldFail() throws InterruptedException, IOException {

        ComapiConfig config = new ComapiConfig()
                .apiSpaceId("apiSpaceId")
                .apiConfiguration(apiConfig)
                .authenticator(new MockAuthenticator())
                .pushTokenProvider(() -> "fcm-token")
                .logConfig(new LogConfig()
                        .setConsoleLevel(LogLevel.DEBUG)
                        .setFileLevel(LogLevel.OFF)
                        .setNetworkLevel(LogLevel.DEBUG));

        RxComapi.reset();
        RxComapi.initialiseShared(RuntimeEnvironment.application, config).subscribeOn(Schedulers.io()).subscribe();
        RxComapi.initialiseShared(RuntimeEnvironment.application, config).subscribe();
    }

    @Test
    public void initTwice_afterFirstFinishes_shouldSucceed() throws InterruptedException, IOException {

        ComapiConfig config1 = new ComapiConfig()
                .apiSpaceId("apiSpaceId")
                .apiConfiguration(apiConfig)
                .authenticator(new MockAuthenticator())
                .pushTokenProvider(() -> "fcm-token")
                .logConfig(new LogConfig()
                        .setConsoleLevel(LogLevel.DEBUG)
                        .setFileLevel(LogLevel.OFF)
                        .setNetworkLevel(LogLevel.DEBUG));

        RxComapiClient client1 = RxComapi.initialise(RuntimeEnvironment.application, config1).toBlocking().first();

        ComapiConfig config2 = new ComapiConfig()
                .apiSpaceId("apiSpaceId2")
                .authenticator(new MockAuthenticator())
                .pushTokenProvider(() -> "fcm-token")
                .logConfig(new LogConfig()
                        .setConsoleLevel(LogLevel.DEBUG)
                        .setFileLevel(LogLevel.OFF)
                        .setNetworkLevel(LogLevel.DEBUG));

        RxComapiClient client2 = RxComapi.initialise(RuntimeEnvironment.application, config2).toBlocking().first();

        assertTrue(client1.getState() == GlobalState.INITIALISED);
        assertTrue(client2.getState() == GlobalState.INITIALISED);
        client1.clean(RuntimeEnvironment.application);
        client2.clean(RuntimeEnvironment.application);
    }

    @Test(expected = RuntimeException.class)
    public void initTwiceSingleton_afterFirstFinishes_shouldFail() throws InterruptedException, IOException {

        ComapiConfig config1 = new ComapiConfig()
                .apiSpaceId("apiSpaceId")
                .apiConfiguration(apiConfig)
                .authenticator(new MockAuthenticator())
                .logConfig(new LogConfig()
                        .setConsoleLevel(LogLevel.DEBUG)
                        .setFileLevel(LogLevel.OFF)
                        .setNetworkLevel(LogLevel.DEBUG));

        RxComapi.initialiseShared(RuntimeEnvironment.application, config1).toBlocking().first();

        ComapiConfig config2 = new ComapiConfig()
                .apiSpaceId("apiSpaceId2")
                .authenticator(new MockAuthenticator())
                .pushTokenProvider(() -> "fcm-token")
                .logConfig(new LogConfig()
                        .setConsoleLevel(LogLevel.DEBUG)
                        .setFileLevel(LogLevel.OFF)
                        .setNetworkLevel(LogLevel.DEBUG));

        RxComapi.initialiseShared(RuntimeEnvironment.application, config2).toBlocking().first();
    }

    @SuppressLint("CommitPrefEdits")
    @Config(shadows = {ShadowGoogleApiAvailability.class, ShadowBluetoothAdapter.class})
    @Test(expected = RuntimeException.class)
    public void initialise_nullAPISPace() throws InterruptedException, IOException {

        // Force success
        ShadowGoogleApiAvailability.setIsGooglePlayServicesAvailable(ConnectionResult.SUCCESS);

        server.enqueue(createMockResponse("rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(createMockResponse("rest_session_create.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(getMockPushResponse());

        ComapiConfig config = new ComapiConfig()
                .apiSpaceId(null)
                .apiConfiguration(apiConfig)
                .authenticator(new MockAuthenticator())
                .pushTokenProvider(() -> "fcm-token")
                .logConfig(new LogConfig()
                        .setConsoleLevel(LogLevel.DEBUG)
                        .setNetworkLevel(LogLevel.DEBUG));

        RxComapi.initialise(RuntimeEnvironment.application, config).toBlocking().first();
    }

    @SuppressLint("CommitPrefEdits")
    @Config(shadows = {ShadowGoogleApiAvailability.class, ShadowBluetoothAdapter.class})
    @Test(expected = RuntimeException.class)
    public void initialise_nullConfig() throws InterruptedException, IOException {

        // Force success
        ShadowGoogleApiAvailability.setIsGooglePlayServicesAvailable(ConnectionResult.SUCCESS);

        server.enqueue(createMockResponse("rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(createMockResponse("rest_session_create.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(getMockPushResponse());

        RxComapi.initialise(RuntimeEnvironment.application, null).toBlocking().first();
    }

    @SuppressLint("CommitPrefEdits")
    @Config(shadows = {ShadowGoogleApiAvailability.class, ShadowBluetoothAdapter.class})
    @Test(expected = RuntimeException.class)
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

        RxComapi.initialise(RuntimeEnvironment.application, config).toBlocking().first();
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
                .apiSpaceId("apiSpaceId")
                .apiConfiguration(apiConfig)
                .authenticator(new MockAuthenticator())
                .pushTokenProvider(() -> "fcm-token")
                .logConfig(new LogConfig()
                        .setConsoleLevel(LogLevel.DEBUG)
                        .setNetworkLevel(LogLevel.DEBUG));

        //For push update
        MockResponse mr = new MockResponse();
        mr.setResponseCode(200);
        server.enqueue(mr);

        RxComapiClient pc = RxComapi.initialise(RuntimeEnvironment.application, config).toBlocking().first();

        mr = new MockResponse();
        mr.setResponseCode(401);
        server.enqueue(mr);
        server.enqueue(createMockResponse("rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(createMockResponse("rest_session_create.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(getMockPushResponse());
        server.enqueue(createMockResponse("rest_profile_update.json", 200).addHeader("ETag", "eTag"));

        pc.service().profile().updateProfile(new HashMap<>(), null).toBlocking().forEach(response -> {
            pc.clean(RuntimeEnvironment.application);
            assertNotNull(response.getResult().get("id"));
        });
    }

    @Test
    public void serviceInstances() {

        ComapiConfig config = new ComapiConfig()
                .apiSpaceId("apiSpaceId")
                .apiConfiguration(apiConfig)
                .authenticator(new MockAuthenticator())
                .pushTokenProvider(() -> "fcm-token")
                .logConfig(new LogConfig()
                        .setConsoleLevel(LogLevel.DEBUG)
                        .setNetworkLevel(LogLevel.DEBUG));

        RxComapiClient pc = RxComapi.initialise(RuntimeEnvironment.application, config).toBlocking().first();

        assertNotNull(pc.service().messaging());
        assertNotNull(pc.service().profile());
        assertNotNull(pc.service().session());
        assertNotNull(pc.service().channels());
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
                .apiSpaceId("apiSpaceId")
                .apiConfiguration(apiConfig)
                .authenticator(new MockAuthenticator())
                .pushTokenProvider(() -> "fcm-token")
                .logConfig(new LogConfig()
                        .setConsoleLevel(LogLevel.DEBUG)
                        .setNetworkLevel(LogLevel.DEBUG));

        server.enqueue(createMockResponse("rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(createMockResponse("rest_session_create.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(getMockPushResponse());

        RxComapiClient pc = RxComapi.initialise(RuntimeEnvironment.application, config).toBlocking().single();
        assertNotNull(pc);
        pc.clean(RuntimeEnvironment.application);
    }

    @Test
    public void initialise_reAuthenticate_error() throws InterruptedException, IOException {

        // Force success
        ShadowGoogleApiAvailability.setIsGooglePlayServicesAvailable(ConnectionResult.SUCCESS);

        DataTestHelper.saveDeviceData();
        DataTestHelper.saveExpiredSessionData();

        ComapiConfig config = new ComapiConfig()
                .apiSpaceId("apiSpaceId")
                .apiConfiguration(apiConfig)
                .authenticator(new MockAuthenticator())
                .pushTokenProvider(() -> "fcm-token")
                .logConfig(new LogConfig()
                        .setConsoleLevel(LogLevel.DEBUG)
                        .setNetworkLevel(LogLevel.DEBUG));

        server.enqueue(createMockResponse("rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(createMockResponse("rest_session_create.json", 500).addHeader("ETag", "eTag"));
        server.enqueue(getMockPushResponse());

        RxComapiClient pc = RxComapi.initialise(RuntimeEnvironment.application, config).toBlocking().single();
        assertNotNull(pc);
        assertEquals(GlobalState.SESSION_OFF, pc.getState());
        pc.clean(RuntimeEnvironment.application);
    }

    @Test
    public void connectivity() {

        final boolean[] connectionReceived = {false};

        InternetConnectionReceiver receiver = new InternetConnectionReceiver(new NetworkConnectivityListener() {
            @Override
            public void onNetworkActive() {
                connectionReceived[0] = true;
            }

            @Override
            public void onNetworkUnavailable() {

            }
        });

        RuntimeEnvironment.application.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        ShadowApplication shadowApplication = ShadowApplication.getInstance();
        Intent intent = new Intent(ConnectivityManager.CONNECTIVITY_ACTION);
        List<BroadcastReceiver> broadcastReceivers = shadowApplication.getReceiversForIntent(intent);
        broadcastReceivers.get(0).onReceive(RuntimeEnvironment.application, intent);
        assertTrue(connectionReceived[0]);
    }

    @Test
    public void listeners() {

        ComapiConfig config = new ComapiConfig()
                .apiSpaceId("apiSpaceId")
                .authenticator(new MockAuthenticator())
                .apiConfiguration(apiConfig);


        //For push update
        MockResponse mr = new MockResponse();
        mr.setResponseCode(200);
        server.enqueue(mr);

        RxComapiClient pc = RxComapi.initialise(RuntimeEnvironment.application, config).toBlocking().single();
        assertNotNull(pc);

        MessagingListener messagingListener = new MessagingListener() {};
        ProfileListener profileListener = new ProfileListener() {};
        StateListener stateListener = new StateListener() {};

        pc.addListener(messagingListener);
        pc.removeListener(messagingListener);

        pc.addListener(profileListener);
        pc.removeListener(profileListener);

        pc.addListener(stateListener);
        pc.removeListener(stateListener);

        //Check if can set null
        MessagingListener l1 = null;
        pc.addListener(l1);
        StateListener l2 = null;
        pc.addListener(l2);
        ProfileListener l3 = null;
        pc.addListener(l3);
    }

    @Test
    public void standardInit() {

        DataTestHelper.saveDeviceData();
        DataTestHelper.saveSessionData();

        ComapiConfig config = new ComapiConfig()
                .apiSpaceId("apiSpaceId")
                .authenticator(new MockAuthenticator())
                .apiConfiguration(apiConfig);

        //Despite Firebase error init will be successful
        RxComapiClient pc = RxComapi.initialise(RuntimeEnvironment.application, config).toBlocking().single();
        assertNotNull(pc);
        assertEquals(GlobalState.SESSION_ACTIVE, pc.getState());
    }

    @Test
    public void standardInit_fcmDisabled() {

        DataTestHelper.saveDeviceData();
        DataTestHelper.saveSessionData();

        ComapiConfig config = new ComapiConfig()
                .apiSpaceId("apiSpaceId")
                .authenticator(new MockAuthenticator())
                .fcmEnabled(false)
                .apiConfiguration(apiConfig);

        //Despite Firebase error init will be successful
        RxComapiClient pc = RxComapi.initialise(RuntimeEnvironment.application, config).toBlocking().single();
        assertNotNull(pc);
        assertEquals(GlobalState.SESSION_ACTIVE, pc.getState());
    }


    @Test
    public void copyLogs() throws FileNotFoundException {

        ComapiConfig config = new ComapiConfig()
                .logConfig(LogConfig.getDebugConfig())
                .apiSpaceId("apiSpaceId")
                .authenticator(new MockAuthenticator())
                .apiConfiguration(apiConfig);

        //For push update
        MockResponse mr = new MockResponse();
        mr.setResponseCode(200);
        server.enqueue(mr);

        RxComapiClient pc = RxComapi.initialise(RuntimeEnvironment.application, config).toBlocking().single();
        assertNotNull(pc);

        String fileName = UUID.randomUUID().toString();
        File file0 = new File(RuntimeEnvironment.application.getFilesDir(), fileName);
        pc.copyLogs(file0).toBlocking().first();
        String text = FileHelper.readFile(file0);
        assertFalse(TextUtils.isEmpty(text));
        file0.delete();
    }

    @Test(expected = ComapiException.class)
    public void validate() {

        apiConfig = new APIConfig().service(server.url("?////").toString()).socket("?///").proxy("?////");

        ComapiConfig config = new ComapiConfig()
                .apiSpaceId("apiSpaceId")
                .authenticator(new MockAuthenticator())
                .pushTokenProvider(() -> "fcm-token")
                .apiConfiguration(apiConfig);


        //For push update
        MockResponse mr = new MockResponse();
        mr.setResponseCode(200);
        server.enqueue(mr);

        RxComapiClient pc = RxComapi.initialise(RuntimeEnvironment.application, config).toBlocking().single();
        assertNotNull(pc);

        pc.clean(RuntimeEnvironment.application);
        apiConfig = new APIConfig().service(server.url("/").toString()).socket("ws://10.0.0.0/").proxy("http://10.0.0.0/");
    }

    @Test(expected = ComapiException.class)
    public void validate2() {

        apiConfig = new APIConfig().service(server.url("?").toString()).socket("?").proxy("?");

        ComapiConfig config = new ComapiConfig()
                .apiSpaceId("apiSpaceId")
                .authenticator(new MockAuthenticator())
                .pushTokenProvider(() -> "fcm-token")
                .apiConfiguration(apiConfig);


        //For push update
        MockResponse mr = new MockResponse();
        mr.setResponseCode(200);
        server.enqueue(mr);

        RxComapiClient pc = RxComapi.initialise(RuntimeEnvironment.application, config).toBlocking().single();
        assertNotNull(pc);

        pc.clean(RuntimeEnvironment.application);
        apiConfig = new APIConfig().service(server.url("/").toString()).socket("ws://10.0.0.0/").proxy("http://10.0.0.0/");
    }

    @Test
    public void wrongStates() throws NoSuchFieldException, IllegalAccessException {

        ComapiConfig config = new ComapiConfig()
                .logConfig(LogConfig.getDebugConfig())
                .apiSpaceId("apiSpaceId")
                .authenticator(new MockAuthenticator())
                .apiConfiguration(apiConfig);

        //For push update
        MockResponse mr = new MockResponse();
        mr.setResponseCode(200);
        server.enqueue(mr);

        RxComapiClient pc = RxComapi.initialise(RuntimeEnvironment.application, config).toBlocking().single();
        assertNotNull(pc);

        Field f = BaseClient.class.getDeclaredField("state"); //NoSuchFieldException
        f.setAccessible(true);
        AtomicInteger state = (AtomicInteger) f.get(pc); //IllegalAccessException

        state.set(GlobalState.NOT_INITIALISED);

        // With 'not initialised' state set all bellow calls should return nulls

        String fileName = UUID.randomUUID().toString();
        File file0 = new File(RuntimeEnvironment.application.getFilesDir(), fileName);
        File merged = pc.copyLogs(file0).toBlocking().first();
        assertNull(merged);

        assertFalse(pc.getSession().isSuccessfullyCreated());
        assertNull(pc.getLogs().toBlocking().first());
    }

    @Test
    public void wrongStates2() throws NoSuchFieldException, IllegalAccessException {

        ComapiConfig config = new ComapiConfig()
                .logConfig(LogConfig.getDebugConfig())
                .apiSpaceId("apiSpaceId")
                .authenticator(new MockAuthenticator())
                .apiConfiguration(apiConfig);
        BaseClient<RxServiceAccessor> client = new BaseClient<RxServiceAccessor>(config) {
            @Override
            public RxServiceAccessor service() {
                return null;
            }
        };

        Field f = BaseClient.class.getDeclaredField("state");
        f.setAccessible(true);
        AtomicInteger state = (AtomicInteger) f.get(client);

        state.set(GlobalState.INITIALISED);
        BaseClient<RxServiceAccessor> client2 = client.initialise(RuntimeEnvironment.application, client, new CallbackAdapter()).toBlocking().single();
        assertNotNull(client2);
    }

    @Test(expected = ComapiException.class)
    public void wrongStates3() throws NoSuchFieldException, IllegalAccessException {

        ComapiConfig config = new ComapiConfig()
                .logConfig(LogConfig.getDebugConfig())
                .apiSpaceId("apiSpaceId")
                .authenticator(new MockAuthenticator())
                .apiConfiguration(apiConfig);
        BaseClient<RxServiceAccessor> client = new BaseClient<RxServiceAccessor>(config) {
            @Override
            public RxServiceAccessor service() {
                return null;
            }
        };

        Field f = BaseClient.class.getDeclaredField("state");
        f.setAccessible(true);
        AtomicInteger state = (AtomicInteger) f.get(client);

        state.set(GlobalState.INITIALISING);
        client.initialise(RuntimeEnvironment.application, client, new CallbackAdapter()).toBlocking().single();
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
        RxComapi.reset();
    }
}
