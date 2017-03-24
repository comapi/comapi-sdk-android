package com.comapi.internal.network.model.session;

import com.google.gson.annotations.SerializedName;

/**
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class PushConfig {

    @SerializedName("fcm")
    private final FCM fcm;

    private class FCM {

        @SerializedName("package")
        String appPackage;

        @SerializedName("registrationId")
        String registrationId;

    }

    public PushConfig(final String appPackage, final String token) {
        fcm = new FCM();
        fcm.appPackage = appPackage;
        fcm.registrationId = token;
    }
}
