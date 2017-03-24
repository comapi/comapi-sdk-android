package com.comapi.mock;

import com.comapi.ComapiAuthenticator;
import com.comapi.internal.network.ChallengeOptions;
import com.comapi.internal.network.AuthClient;

/**
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MockAuthenticator extends ComapiAuthenticator {

    private boolean shouldCrash = false;

    private boolean shouldReturnNullToken = false;

    @Override
    public void onAuthenticationChallenge(AuthClient authClient, ChallengeOptions challengeOptions) {
        if (shouldReturnNullToken) {
            authClient.authenticateWithToken(null);
        } else if (shouldCrash) {
            throw new RuntimeException();
        } else {
            authClient.authenticateWithToken("auth-token");
        }
    }

    public void setShouldCrash(boolean shouldCrash) {
        this.shouldCrash = shouldCrash;
    }

    public void setShouldReturnNullToken(boolean shouldReturnNullToken) {
        this.shouldReturnNullToken = shouldReturnNullToken;
    }
}
