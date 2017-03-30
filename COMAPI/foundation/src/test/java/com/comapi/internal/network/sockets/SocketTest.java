package com.comapi.internal.network.sockets;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.comapi.BuildConfig;
import com.comapi.helpers.DataTestHelper;
import com.comapi.internal.data.DataManager;
import com.comapi.internal.helpers.HelpersTest;
import com.comapi.internal.lifecycle.LifeCycleController;
import com.comapi.internal.lifecycle.LifecycleListener;
import com.comapi.internal.log.LogLevel;
import com.comapi.internal.log.LogManager;
import com.comapi.internal.log.Logger;
import com.neovisionaries.ws.client.WebSocketAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;

import static com.comapi.helpers.DataTestHelper.API_SPACE_ID;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.RuntimeEnvironment.application;

/**
 * Robolectric tests for socket controller and socket factory.
 *
 * @author Marcin Swierczek
 *         Copyright (C) Donky Networks Ltd. All rights reserved.
 * @version 1.0.0
 * @since 1.0.0
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(manifest = "com/comapi/src/main/AndroidManifest.xml", sdk = Build.VERSION_CODES.M, constants = BuildConfig.class, packageName = "com.comapi")
public class SocketTest {

    private static final String CONNECTED = "stateConnected";

    private static final String DISCONNECTED = "stateDisconnected";

    private static final String ERROR = "stateError";

    private static final String MESSAGE = "messageBody";

    private SocketConnectionController socketConnectionController;

    private String state = "unknown";

    private String receivedMessage = null;

    private boolean isNetworkActive = false;

    private TestSocketFactory testSocketFactory;

    private ActivityController<Activity> controller;

    private SocketController socketController;

    private static final int LIMIT = 1000;
    private Handler handler;
    private RetryStrategy retryStrategy;

    @Before
    public void setUpComapi() throws Exception {

        LogManager logMgr = new LogManager();
        logMgr.init(application, LogLevel.DEBUG.getValue(), LogLevel.OFF.getValue(), LIMIT);
        Logger log = new Logger(new LogManager(), "");
        DataManager dataMgr = new DataManager();
        dataMgr.init(application, API_SPACE_ID,new Logger(new LogManager(), ""));

        DataTestHelper.saveSessionData();

        testSocketFactory = new TestSocketFactory(
                new URI("ws://10.0.0.0"),
                text -> {
                    receivedMessage = MESSAGE;
                },
                log);

        handler = new Handler();
        retryStrategy = new RetryStrategy(1, 0);
        socketConnectionController = new SocketConnectionController(handler, dataMgr, testSocketFactory, retryStrategy, log) {

            @Override
            public void onConnected() {
                super.onConnected();
                SocketTest.this.state = CONNECTED;
            }

            @Override
            public void onDisconnected() {
                super.onDisconnected();
                SocketTest.this.state = DISCONNECTED;
            }

            @Override
            public void onError(String uriStr, URI proxyAddress, Exception exception) {
                super.onError(uriStr, proxyAddress, exception);
                SocketTest.this.state = ERROR;
            }

            @Override
            public void onNetworkActive() {
                super.onNetworkActive();
                isNetworkActive = true;
            }

            @Override
            public void onNetworkUnavailable() {
                super.onNetworkUnavailable();
                isNetworkActive = false;
            }

        };

        socketController = new SocketController(dataMgr, null, log, new URI("ws://10.0.0.0"), new URI("http://10.0.0.0"));

        initialiseLifecycleObserver(application, socketController.createLifecycleListener());
    }

    @Test
    public void testForegrounded() {
        controller = Robolectric.buildActivity(Activity.class);
        controller.create().start().resume().get();
        assertTrue(socketController.isAllowedToConnect());
    }

    @Test
    public void testBackgrounded() throws InterruptedException {
        controller = Robolectric.buildActivity(Activity.class);
        controller.create().start().resume().get();
        HelpersTest.waitSomeTime(1500);
        assertTrue(socketController.isAllowedToConnect());

        controller.pause().stop().saveInstanceState(new Bundle()).destroy().get();
        HelpersTest.waitSomeTime(3000);
        assertFalse(socketController.isAllowedToConnect());

        controller = Robolectric.buildActivity(Activity.class);
        controller.create().start().resume().get();
        HelpersTest.waitSomeTime(1500);
        assertTrue(socketController.isAllowedToConnect());
    }

    @Test
    public void messageReceived() throws Exception {
        socketConnectionController.connect();
        testSocketFactory.webSocketAdapter.onTextMessage(null, MESSAGE);
        testSocketFactory.webSocketAdapter.onBinaryMessage(null, new byte[10]);
        assertEquals(MESSAGE, receivedMessage);
    }

    @Test
    public void networkAvailability() throws Exception {
        socketConnectionController.onNetworkActive();
        assertEquals(true, isNetworkActive);
        socketConnectionController.onNetworkUnavailable();
        assertEquals(false, isNetworkActive);
        socketConnectionController.onNetworkActive();
        assertEquals(true, isNetworkActive);
    }

    @Test
    public void testConnectionCallbacks() throws Exception {
        socketConnectionController.connect();
        testSocketFactory.webSocketAdapter.onConnected(null, null);
        assertEquals(CONNECTED, state);
        testSocketFactory.webSocketAdapter.onDisconnected(null, null, null, false);
        assertEquals(DISCONNECTED, state);
        testSocketFactory.webSocketAdapter.onConnectError(null, null);
        assertEquals(ERROR, state);
    }

    @Test
    public void testSocketReconnect() {
        retryStrategy = new RetryStrategy(0, 0);
        socketConnectionController.setManageReconnection(true);
        socketConnectionController.connect();
        socketConnectionController.onError(null, null, null);
        handler.removeCallbacksAndMessages(null);
        socketConnectionController.onConnected();
        retryStrategy = new RetryStrategy(1, 0);
    }

    @Test
    public void testCreateSocket() {
        try {
            SocketFactory factory = new TestSocketFactory(new URI("ws://10.0.0.0"), text -> {

            }, new Logger(new LogManager(), ""));
            assertNotNull(factory);
            SocketInterface socket = factory.createSocket(DataTestHelper.ACCESS_TOKEN, new WeakReference<>(null));
            assertNotNull(socket);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            assertNull(e);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSocket_fail() throws URISyntaxException {

        SocketFactory factory = new TestSocketFactory(new URI("something_wrong"), text -> {

        }, new Logger(new LogManager(), ""));
        assertNotNull(factory);
        SocketInterface socket = factory.createSocket(DataTestHelper.ACCESS_TOKEN, new WeakReference<>(null));
        assertNotNull(socket);

    }

    @After
    public void tearDown() throws Exception {
        socketConnectionController.disconnect();
        DataTestHelper.clearSessionData();
        state = "unknown";
        receivedMessage = null;
    }

    class TestSocketFactory extends SocketFactory {

        private WebSocketAdapter webSocketAdapter;

        TestSocketFactory(@NonNull URI uri, @NonNull SocketMessageListener messageListener, @NonNull Logger log) {
            super(uri, messageListener, log);
        }

        @Override
        protected WebSocketAdapter createWebSocketAdapter(@NonNull final WeakReference<SocketStateListener> stateListenerWeakReference) {
            webSocketAdapter = super.createWebSocketAdapter(stateListenerWeakReference);
            return webSocketAdapter;
        }
    }

    private void initialiseLifecycleObserver(Application application, LifecycleListener listener) {
        LifeCycleController.registerLifeCycleObserver(application, listener);
    }
}