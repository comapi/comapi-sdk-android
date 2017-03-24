package com.comapi.internal.network.model.session;

import com.google.gson.annotations.SerializedName;

/**
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SessionCreateResponse {

    @SerializedName("token")
    private String token;

    @SerializedName("session")
    private Session session;

    /**
     * Access token
     *
     * @return Access token
     */
    public String getToken() {
        return token;
    }

    /**
     * New session data
     *
     * @return New session data
     */
    public Session getSession() {
        return session;
    }

    @Override
    public String toString() {
        return session.toString();
    }
}
