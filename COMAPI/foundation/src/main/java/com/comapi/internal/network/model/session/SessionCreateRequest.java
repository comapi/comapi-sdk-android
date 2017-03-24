package com.comapi.internal.network.model.session;

import com.google.gson.annotations.SerializedName;

/**
 * Request object for creation of a new session on the network.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SessionCreateRequest {

    @SerializedName("authenticationId")
    private String authenticationId;

    @SerializedName("authenticationToken")
    private String authenticationToken;

    @SerializedName("deviceId")
    private String deviceId;

    @SerializedName("platform")
    private String platform;

    @SerializedName("platformVersion")
    private String platformVersion;

    @SerializedName("sdkType")
    private String sdkType;

    @SerializedName("sdkVersion")
    private String sdkVersion;

    /**
     * Recommended constructor.
     *
     * @param authId Auth process id.
     * @param token  Auth token.
     */
    public SessionCreateRequest(String authId, String token) {
        authenticationId = authId;
        authenticationToken = token;
    }

    /**
     * Sets device id.
     *
     * @param deviceId Device id.
     * @return Instance of {@link SessionCreateRequest} with the new value.
     */
    public SessionCreateRequest setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    /**
     * Sets platform name.
     *
     * @param platform Platform name.
     * @return Instance of {@link SessionCreateRequest} with the new value.
     */
    public SessionCreateRequest setPlatform(String platform) {
        this.platform = platform;
        return this;
    }

    /**
     * Sets platform version.
     *
     * @param platformVersion Platform version.
     * @return Instance of {@link SessionCreateRequest} with the new value.
     */
    public SessionCreateRequest setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
        return this;
    }

    /**
     * Sets SDK type.
     *
     * @param sdkType SDK type.
     * @return Instance of {@link SessionCreateRequest} with the new value.
     */
    public SessionCreateRequest setSdkType(String sdkType) {
        this.sdkType = sdkType;
        return this;
    }

    /**
     * Sets SDK version.
     *
     * @param sdkVersion SDK version.
     * @return Instance of {@link SessionCreateRequest} with the new value.
     */
    public SessionCreateRequest setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
        return this;
    }
}
