package com.comapi;

import com.comapi.internal.network.AuthClient;
import com.comapi.internal.network.ChallengeOptions;

/**
 * Implementation of this interface will be used to obtain authentication token. Can be set when initialising.
 *
 * Created by Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class ComapiAuthenticator {

    /**
     * Timeout interval for authentication challenges in seconds.
     */
    private static final long TIMEOUT = 60 * 5;

    /**
     * This method will be called whenever SDK will try to authenticate user on the networkLevel.
     *
     * @param authClient       Pass the authentication token to {@link AuthClient#authenticateWithToken(String)} method
     * @param challengeOptions Encapsulates challenge details - nonce, and expected user id for the token.
     */
    public abstract void onAuthenticationChallenge(final AuthClient authClient, final ChallengeOptions challengeOptions);

    /**
     * Returns timeout interval for authentication challenges.
     *
     * @return Timeout interval for authentication challenges.
     */
    public long timeoutSeconds() {
        return TIMEOUT;
    }

}
