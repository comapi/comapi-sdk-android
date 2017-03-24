package com.comapi.internal.push;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Listener interface for incoming push messages. For details about condition in which FCM will deliver the message to onMessageReceived callback please refer to
 * https://firebase.google.com/docs/cloud-messaging/android/receive
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface PushMessageListener {

    /**
     * Push message received.
     *
     * @param message Push message.
     */
    void onMessageReceived(RemoteMessage message);
}
