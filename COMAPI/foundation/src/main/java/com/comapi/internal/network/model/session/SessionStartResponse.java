package com.comapi.internal.network.model.session;

import com.google.gson.annotations.SerializedName;

/**
 * Request to start the process for authenticating a new session.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SessionStartResponse {

    @SerializedName("authenticationId")
    private String authenticationId;

    @SerializedName("provider")
    private String provider;

    @SerializedName("nonce")
    private String nonce;

    @SerializedName("expiresOn")
    private String expiresOn;

    /**
     * Gets single use authentication process id to identify currently performed authentication.
     *
     * @return Authentication process id.
     */
    public String getAuthenticationId() {
        return authenticationId;
    }

    /**
     * Gets authorisation token provider.
     *
     * @return Authentication token provider.
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Gets single use authentication id for auth provider to identify token.
     *
     * @return Authentication nonce.
     */
    public String getNonce() {
        return nonce;
    }

    /**
     * Gets expiration time.
     *
     * @return Authentication process id.
     */
    public String getExpiresOn() {
        return expiresOn;
    }
}
