package com.comapi;

import android.text.TextUtils;

import com.comapi.internal.data.SessionData;

/**
 * Active session data.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Session {

    private String profileId;

    private boolean hasRequiredFields;

    public Session(SessionData data) {
        if (data != null) {
            this.hasRequiredFields = !TextUtils.isEmpty(data.getProfileId()) && data.getExpiresOn() > 0 && !TextUtils.isEmpty(data.getAccessToken()) && !TextUtils.isEmpty(data.getSessionId());
            this.profileId = data.getProfileId();
        } else {
            this.hasRequiredFields = false;
        }
    }

    /**
     * Gets user unique identifier.
     *
     * @return User unique identifier.
     */
    public String getProfileId() {
        return profileId;
    }

    /**
     * Check if this session valid and didn't expired.
     *
     * @return Is this session valid and didn't expired.
     */
    public boolean isSuccessfullyCreated() {
        return hasRequiredFields;
    }
}