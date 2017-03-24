package com.comapi.internal.network.model.messaging;

import com.google.gson.annotations.SerializedName;

/**
 * Represents services response to sending a new message.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MessageSentResponse {

    @SerializedName("id")
    private String id;

    public String getId() {
        return id;
    }
}