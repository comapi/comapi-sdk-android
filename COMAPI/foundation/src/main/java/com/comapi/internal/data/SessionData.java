package com.comapi.internal.data;

/**
 * Active session data.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SessionData {

    private String profileId;

    private String sessionId;

    private String accessToken;

    private long expiresOn;

    /**
     * Gets user unique identifier.
     *
     * @return User unique identifier.
     */
    public String getProfileId() {
        return profileId;
    }

    /**
     * Gets identifier for a session.
     *
     * @return Identifier for a session.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets session authorisation token for a session.
     *
     * @return Authorisation token for a session.
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Gets number of milliseconds since January 1, 1970 00:00:00 UTC in which the session is valid.
     *
     * @return Number of milliseconds since January 1, 1970 00:00:00 UTC in which the session is valid.
     */
    public long getExpiresOn() {
        return expiresOn;
    }

    /**
     * Sets user unique identifier.
     *
     * @param profileId User unique identifier.
     * @return {@link SessionData} instance with the new value.
     */
    public SessionData setProfileId(String profileId) {
        this.profileId = profileId;
        return this;
    }

    /**
     * Sets identifier for a session.
     *
     * @param sessionId Identifier for a session.
     */
    public SessionData setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    /**
     * Sets session authorisation token for a session.
     *
     * @param accessToken Authorisation token for a session.
     */
    public SessionData setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    /**
     * Sets number of milliseconds since January 1, 1970 00:00:00 UTC in which the session is valid.
     *
     * @param expiresOn Number of milliseconds since January 1, 1970 00:00:00 UTC in which the session is valid.
     */
    public SessionData setExpiresOn(long expiresOn) {
        this.expiresOn = expiresOn;
        return this;
    }
}