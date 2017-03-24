package com.comapi.internal.network.model.messaging;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes the push alert that should be displayed on various platforms when the messages will be received.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Alert {

    @SerializedName("platforms")
    private PushPlatforms platforms;

    public PushPlatforms getPlatforms() {
        return platforms;
    }

    Alert(Map<String, Object> fcm, Map<String, Object> apns) {
        platforms = new PushPlatforms();
        platforms.setFcm(fcm);
        platforms.setApns(apns);
    }

    public static FCM fcmPushBuilder() {
        return new FCM();
    }

    public static class FCM {

        Map<String, Object> fcm;

        FCM() {
            fcm = new HashMap<>();
        }

        public FCM putData(Map<String, String> data) {
            fcm.put("data", data);
            return this;
        }

        public FCM putNotification(String title, String body) {
            fcm.put("notification", new Notification(title, body));
            return this;
        }

        public Map<String, Object> build() {
            return fcm;
        }

        private class Notification {

            @SerializedName("title")
            private String title;

            @SerializedName("body")
            private String body;

            private Notification(String title, String body) {
                this.title = title;
                this.body = body;
            }
        }
    }
}