package com.comapi.internal;

import com.comapi.internal.network.model.events.ProfileUpdateEvent;

/**
 * Listener interface for messaging events.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface IProfileListener {

    /**
     * Dispatch profile update event.
     *
     * @param event Event to dispatch.
     */
    void onProfileUpdate(ProfileUpdateEvent event);

}