package com.comapi.internal.network;

/**
 * Interface for a client class to obtain ID token from the integrator to be used to authenticate user on the networkLevel.
 *
 * Created by Marcin Swierczek
 * 12/04/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface AuthClient {

    /**
     * Sets the auth ID token to be used to authenticate user on the networkLevel.
     *
     * @param token Auth ID token
     */
    void authenticateWithToken(String token);

}