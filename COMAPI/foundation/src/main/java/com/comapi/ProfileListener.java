package com.comapi;

import com.comapi.internal.IProfileListener;
import com.comapi.internal.network.model.events.ProfileUpdateEvent;

/**
 * Profile updates listener. Callbacks for events updating profile details.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class ProfileListener implements IProfileListener {

    /**
     * Updates of registered user profile.
     *
     * @param event Profile update event.
     */
    public void onProfileUpdate(ProfileUpdateEvent event) {}

}