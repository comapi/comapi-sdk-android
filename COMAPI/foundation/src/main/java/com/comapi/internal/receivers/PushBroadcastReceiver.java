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

package com.comapi.internal.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.comapi.internal.Parser;
import com.comapi.internal.log.Logger;
import com.comapi.internal.push.IDService;
import com.comapi.internal.push.PushBuilder;
import com.comapi.internal.push.LocalNotificationsManager;
import com.comapi.internal.push.PushDataKeys;
import com.comapi.internal.push.PushMessageListener;
import com.comapi.internal.push.PushService;
import com.comapi.internal.push.PushTokenListener;
import com.comapi.internal.push.PushTokenProvider;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.internal.LinkedTreeMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Local broadcast receiver to listen for push messages and token refresh requests.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public class PushBroadcastReceiver extends BroadcastReceiver {

    private final PushTokenListener tokenListener;
    private final PushMessageListener messageListener;
    private final Handler mainThreadHandler;
    private final PushTokenProvider provider;
    private final LocalNotificationsManager lNM;
    private Logger log;

    public PushBroadcastReceiver(final Handler mainThreadHandler, PushTokenProvider provider, final PushTokenListener tokenListener, final PushMessageListener messageListener, LocalNotificationsManager lNM, Logger log) {
        super();
        this.mainThreadHandler = mainThreadHandler;
        this.provider = provider;
        this.tokenListener = tokenListener;
        this.messageListener = messageListener;
        this.lNM = lNM;
        this.log = log;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (IDService.ACTION_REFRESH_PUSH.equals(intent.getAction())) {
            tokenListener.onTokenRefresh(provider.getPushToken());
        } else if (PushService.ACTION_PUSH_MESSAGE.equals(intent.getAction())) {
            RemoteMessage msg = intent.getParcelableExtra(PushService.KEY_MESSAGE);
            if (msg != null) {
                handleData(new HashMap<>(msg.getData()));
                dispatchMessage(messageListener, msg);
            }
        } else if (PushDataKeys.PUSH_CLICK_ACTION.equals(intent.getAction())) {
            Serializable data = intent.getSerializableExtra(PushDataKeys.KEY_PUSH_DATA);
            lNM.handleNotificationClick(intent.getStringExtra(PushDataKeys.KEY_PUSH_CORRELATION_ID), data, intent.getStringExtra(PushDataKeys.KEY_PUSH_DEEP_LINK));
        }
    }

    @SuppressWarnings("unchecked")
    private void handleData(HashMap<String, String> data) {
        if (data != null) {
            String dd = data.get(PushDataKeys.KEY_PUSH_DEEP_LINK);
            if (dd != null) {
                Parser parser = new Parser();
                try {
                    LinkedTreeMap<String, ?> params = parser.parse(dd, LinkedTreeMap.class);
                    String title = String.valueOf(params.get(PushDataKeys.KEY_PUSH_TITLE));
                    String body = String.valueOf(params.get(PushDataKeys.KEY_PUSH_BODY));
                    String url = String.valueOf(params.get(PushDataKeys.KEY_PUSH_URL));
                    String correlationId = String.valueOf(params.get(PushDataKeys.KEY_PUSH_CORRELATION_ID));
                    lNM.handleNotification(new PushBuilder(correlationId, title, body, url, data));
                } catch (Exception e) {
                    log.e("Error when parsing push message. "+e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Dispatch received push message to external listener.
     *
     * @param listener Push message listener.
     * @param message  Received push message to be dispatched.
     */
    private void dispatchMessage(PushMessageListener listener, RemoteMessage message) {
        if (listener != null) {
            mainThreadHandler.post(() -> listener.onMessageReceived(message));
        }
    }
}