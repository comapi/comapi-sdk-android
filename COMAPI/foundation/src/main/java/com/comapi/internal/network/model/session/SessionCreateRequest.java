/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Comapi (trading name of Dynmark International Limited)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons
 * to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.comapi.internal.network.model.session;

import com.google.gson.annotations.SerializedName;

/**
 * Request object for creation of a new session on the network.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
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

    @SerializedName("push")
    private PushConfig push;

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

    public SessionCreateRequest setPush(PushConfig push) {
        this.push = push;
        return this;
    }
}
