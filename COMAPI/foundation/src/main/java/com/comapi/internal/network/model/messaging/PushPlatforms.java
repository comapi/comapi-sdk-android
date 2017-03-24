package com.comapi.internal.network.model.messaging;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Wrapper for various push configurations.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class PushPlatforms {

    @SerializedName("apns")
    private Map<String, Object> apns;

    @SerializedName("fcm")
    private Map<String, Object> fcm;

    /**
     * Gets definition of APNS notification
     *
     * @return Definition of APNS notification
     */
    public Map<String, Object> getApns() {
        return apns;
    }

    /**
     * Gets definition of FCM notification
     *
     * @return Definition of FCM notification
     */
    public Map<String, Object> getFcm() {
        return fcm;
    }

    /**
     * Set definition of APNS notification
     *
     * @param apns Definition of APNS notification
     */
    void setApns(Map<String, Object> apns) {
        this.apns = apns;
    }

    /**
     * Set definition of FCM notification
     *
     * @param fcm Definition of APNS notification
     */
    void setFcm(Map<String, Object> fcm) {
        this.fcm = fcm;
    }
}
