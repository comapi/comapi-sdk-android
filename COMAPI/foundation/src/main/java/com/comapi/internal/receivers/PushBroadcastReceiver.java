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
 * Copyright (C) Donky Networks Ltd. All rights reserved.
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
            dispatchMessage(messageListener, intent.getParcelableExtra(PushService.KEY_MESSAGE));
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