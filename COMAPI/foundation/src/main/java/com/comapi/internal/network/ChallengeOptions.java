package com.comapi.internal.network;

/**
 * Encapsulates the authentication challenge details that may be used to obtain the token from auth provider.
 *
 * Created by Marcin Swierczek
 * 12/04/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ChallengeOptions {

    private final String nonce;

    /**
     * Recommended constructor.
     *
     * @param nonce     Nonce for authentication challenge.
     */
    ChallengeOptions(String nonce) {
        this.nonce = nonce;
    }

    /**
     * Gets nonce for authentication challenge.
     *
     * @return Nonce for authentication challenge.
     */
    public String getNonce() {
        return nonce;
    }

}
