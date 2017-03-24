package com.comapi.internal.helpers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.comapi.internal.network.model.messaging.MessageToSend;
import com.comapi.internal.network.model.messaging.Part;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to create basic message object to send.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class APIHelper {

    public static MessageToSend createMessage(@NonNull String conversationId, @NonNull String body, @Nullable String title) {

        Part bodyPart = Part.builder().setData(body).setName("body").setSize(body.length()).setType("text/plain").build();

        Map<String, Object> fcm = new HashMap<>();
        fcm.put("notification", new Notification(title != null ? title : conversationId, body, conversationId));
        Map<String, Object> apns = new HashMap<>();
        apns.put("alert", body);

        return MessageToSend.builder().addPart(bodyPart).setAlert(fcm, apns).build();
    }

    private static class Notification {

        @SerializedName("title")
        String title;
        @SerializedName("body")
        String body;
        @SerializedName("tag")
        String tag;

        public Notification(String title, String body, String tag) {
            this.title = title;
            this.body = body;
            this.tag = tag;
        }
    }
}