package com.comapi.internal.network.model.messaging;

import com.google.gson.annotations.SerializedName;

/**
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Sender {

    @SerializedName("id")
    protected String id;

    @SerializedName("name")
    protected String name;

    /**
     * Message sender unique identifier
     *
     * @return Message sender unique identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Message sender name
     *
     * @return Message sender name
     */
    public String getName() {
        return name;
    }
}
