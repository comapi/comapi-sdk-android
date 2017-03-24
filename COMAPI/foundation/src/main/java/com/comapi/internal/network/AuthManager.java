package com.comapi.internal.network;

import com.comapi.internal.data.SessionData;

import rx.Observable;

/**
 * Interface to internally trigger session refresh when old session expires.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class AuthManager {

    public static final String AUTH_PREFIX = "Bearer ";

    /**
     * Create new session when the old one expires.
     *
     * @return Observable to obtain new session.
     */
    protected abstract Observable<SessionData> restartSession();

    /**
     * Adds appropriate prefix to access token.
     *
     * @param token Access token.
     * @return Access token with a prefix.
     */
    public static String addAuthPrefix(String token) {
        return AUTH_PREFIX + token;
    }
}
