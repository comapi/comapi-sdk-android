package com.comapi.internal.network.model.session;

import com.google.gson.annotations.SerializedName;

/**
 * Session data received from Comapi services.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Session {

    @SerializedName("id")
    private String id;

    @SerializedName("profileId")
    private String profileId;

    @SerializedName("state")
    private String state;

    @SerializedName("expiresOn")
    private String expiresOn;

    @Override
    public String toString() {
        return "[session = " + id + " | " + state + " | profile = " + profileId + " | expires = " + expiresOn + "]";
    }

    /**
     * Gets session id.
     *
     * @return session id.
     */
    public String getSessionId() {
        return id;
    }

    /**
     * Gets user id for the session.
     *
     * @return User id.
     */
    public String getProfileId() {
        return profileId;
    }

    /**
     * Gets session state.
     *
     * @return Session state.
     */
    public String getState() {
        return state;
    }

    /**
     * Gets session expiration date.
     *
     * @return Session expiration date.
     */
    public String getExpiresOn() {
        return expiresOn;
    }
}
