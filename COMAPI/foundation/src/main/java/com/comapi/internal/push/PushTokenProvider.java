package com.comapi.internal.push;

/**
 * Interface for a method of obtaining push token. Allows dependency injection for mocking FCM.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface PushTokenProvider {

    /**
     * Gets Push token.
     * @return Push token.
     */
    String getPushToken();
}