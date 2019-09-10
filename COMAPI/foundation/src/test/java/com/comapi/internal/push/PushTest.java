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

package com.comapi.internal.push;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;

import com.comapi.BuildConfig;
import com.comapi.internal.log.LogManager;
import com.comapi.internal.log.Logger;
import com.comapi.mock.ShadowGoogleApiAvailability;
import com.google.android.gms.common.ConnectionResult;
import com.google.firebase.messaging.RemoteMessage;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;

/**
 * Robolectric tests for application lifecycle observer.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.M, constants = BuildConfig.class, packageName = "com.comapi")
public class PushTest {

    PushManager mgr;

    private String token = "initial";

    private RemoteMessage message = null;

    private String TOKEN = "token";

    private PushMessageListener messageListener;

    public void setUpComapi(String token) {

        mgr = new PushManager();
        PushTokenListener tokenListener = tokenReceived -> PushTest.this.token = tokenReceived;
        messageListener = messageReceived -> PushTest.this.message = messageReceived;

        mgr.init(RuntimeEnvironment.application, new Handler(Looper.getMainLooper()), new Logger(new LogManager(), ""), () -> token, tokenListener, messageListener);
    }

    @Test
    @Config(shadows = {ShadowGoogleApiAvailability.class})
    public void testCheckAvailability() {

        setUpComapi(TOKEN);

        // Force success
        ShadowGoogleApiAvailability.setIsGooglePlayServicesAvailable(ConnectionResult.SUCCESS);
        assertEquals(true, mgr.checkAvailablePush(RuntimeEnvironment.application));

        // Force failure
        ShadowGoogleApiAvailability.setIsGooglePlayServicesAvailable(ConnectionResult.API_UNAVAILABLE);
        assertEquals(false, mgr.checkAvailablePush(RuntimeEnvironment.application));

        ShadowGoogleApiAvailability.setIsGooglePlayServicesAvailable(ConnectionResult.INTERNAL_ERROR);
        assertEquals(false, mgr.checkAvailablePush(RuntimeEnvironment.application));

        ShadowGoogleApiAvailability.setIsGooglePlayServicesAvailable(ConnectionResult.SERVICE_DISABLED);
        assertEquals(false, mgr.checkAvailablePush(RuntimeEnvironment.application));
    }

    @Test
    @Config(shadows = {ShadowGoogleApiAvailability.class})
    public void testGetToken() {

        setUpComapi(TOKEN);

        Intent intent = new Intent(IDService.ACTION_REFRESH_PUSH);
        LocalBroadcastManager.getInstance(RuntimeEnvironment.application).sendBroadcast(intent);
        assertEquals(TOKEN, token);
    }

    @After
    public void tearDown() throws Exception {
        mgr.unregisterPushReceiver(RuntimeEnvironment.application);
    }

}