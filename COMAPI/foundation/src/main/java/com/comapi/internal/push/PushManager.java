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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.comapi.internal.log.Logger;
import com.comapi.internal.network.InternalService;
import com.comapi.internal.receivers.PushBroadcastReceiver;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Manager for FCM functionality, obtaining push tokens and listens for messages.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public class PushManager {

    /**
     * Logger instance.
     */
    private Logger log;

    private BroadcastReceiver receiver;

    private PushTokenProvider provider;

    private LocalNotificationsManager lNM;

    /**
     * Initialise PushManager.
     *
     * @param context           Application context.
     * @param mainThreadHandler Main thread handler to post push messages.
     * @param logger            Internal logger.
     * @param provider          Provides fcm push token.
     * @param tokenListener     Listener for refreshed push tokens.
     * @param messageListener   Listener for push messages.
     */
    public void init(final Context context, final Handler mainThreadHandler, final Logger logger, final PushTokenProvider provider, final PushTokenListener tokenListener, final PushMessageListener messageListener) {
        log = logger;
        this.provider = provider != null ? provider : () -> FirebaseInstanceId.getInstance().getToken();
        this.lNM = new LocalNotificationsManager(context, log);
        registerPushReceiver(mainThreadHandler, context, this.provider, tokenListener, messageListener);
    }

    /**
     * Register local broadcast listener for refreshed push tokens and push messages.
     *
     * @param context         Application context.
     * @param provider        Provides fcm push token.
     * @param tokenListener   Listener for refreshed push tokens.
     * @param messageListener Listener for push messages.
     */
    private void registerPushReceiver(final Handler mainThreadHandler, final Context context, PushTokenProvider provider, final PushTokenListener tokenListener, final PushMessageListener messageListener) {

        IntentFilter filter = new IntentFilter();
        filter.addAction(IDService.ACTION_REFRESH_PUSH);
        filter.addAction(PushService.ACTION_PUSH_MESSAGE);
        filter.addAction(PushDataKeys.PUSH_CLICK_ACTION);

        receiver = new PushBroadcastReceiver(mainThreadHandler, provider, tokenListener, messageListener, lNM, log);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).registerReceiver(receiver, filter);
    }

    /**
     * Unregister broadcast receiver.
     *
     * @param context Application context.
     */
    public void unregisterPushReceiver(Context context) {
        LocalBroadcastManager.getInstance(context.getApplicationContext()).unregisterReceiver(receiver);
    }

    /**
     * Generates new FCM token for a given sender id.
     *
     * @return New FCM registration token.
     */
    public String getPushToken() {
        return provider.getPushToken();
    }

    /**
     * Checks if Google Play Services is available on the device.
     *
     * @return True if Google Play Services is available.
     */
    boolean checkAvailablePush(Context context) {

        int connectionStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (connectionStatus == ConnectionResult.SUCCESS) {
            log.i("Google Play Services are available on this device.");
            return true;
        } else {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(connectionStatus)) {
                log.e("Google Play Services is probably not up to date. User recoverable Error Code is " + connectionStatus);
            } else {
                log.e("This device is not supported by Google Play Services.");
            }
            return false;
        }
    }

    public void setService(InternalService service) {
        lNM.setService(service);
    }
}