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

import com.comapi.internal.push.IDService;
import com.comapi.internal.push.PushMessageListener;
import com.comapi.internal.push.PushService;
import com.comapi.internal.push.PushTokenListener;
import com.comapi.internal.push.PushTokenProvider;
import com.google.firebase.messaging.RemoteMessage;

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

    public PushBroadcastReceiver(final Handler mainThreadHandler, PushTokenProvider provider, final PushTokenListener tokenListener, final PushMessageListener messageListener) {
        super();
        this.mainThreadHandler = mainThreadHandler;
        this.provider = provider;
        this.tokenListener = tokenListener;
        this.messageListener = messageListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (IDService.ACTION_REFRESH_PUSH.equals(intent.getAction())) {
            tokenListener.onTokenRefresh(provider.getPushToken());
        } else if (PushService.ACTION_PUSH_MESSAGE.equals(intent.getAction())) {
            RemoteMessage msg = intent.getParcelableExtra(PushService.KEY_MESSAGE);
            if (msg != null) {
                dispatchMessage(messageListener, msg);
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