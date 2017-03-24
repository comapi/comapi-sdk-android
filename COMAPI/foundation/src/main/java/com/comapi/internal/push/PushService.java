package com.comapi.internal.push;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Service implementation to handle incoming FCM messages delivered by Receiver.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class PushService extends FirebaseMessagingService {

    public static final String ACTION_PUSH_MESSAGE = "com.comapi.push.message";

    public static final String KEY_MESSAGE = "message";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Intent intent = new Intent(ACTION_PUSH_MESSAGE);
        intent.putExtra(KEY_MESSAGE, message);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}