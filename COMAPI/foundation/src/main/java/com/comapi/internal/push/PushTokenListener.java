package com.comapi.internal.push;

/**
 * Listener for refreshed FCM tokens.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface PushTokenListener {

    /**
     * Token was refreshed and should be send to backend.
     *
     * @param token New FCM token.
     */
    void onTokenRefresh(final String token);
}
