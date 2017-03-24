package com.comapi.internal.network;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;


/**
 * Internal interceptor for OkHttp authentication challenges.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
class OkHttpAuthenticator implements Authenticator {

    private final static int MAX_AUTH_COUNT = 3;

    private final AuthManager mgr;

    /**
     * Recommended constructor.
     *
     * @param mgr Authentication manager
     */
    OkHttpAuthenticator(AuthManager mgr) {
        this.mgr = mgr;
    }

    @Override
    public Request authenticate(Route route, Response response) {

        if (responseCount(response) >= MAX_AUTH_COUNT) {
            return null; // If we've failed N times, give up.
        }

        //Obtain new Comapi access token and retry request with updated header.

        String credential = mgr.restartSession().toBlocking().single().getAccessToken();

        if (!credential.equals(response.request().header("Authorization"))) {

            return response.request().newBuilder()
                    .header("Authorization", AuthManager.addAuthPrefix(credential))
                    .build();
        }

        return null;
    }

    /**
     * Counts how many times we made this service call.
     *
     * @param response Service call response.
     * @return How many responses we received for this service call.
     */
    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}