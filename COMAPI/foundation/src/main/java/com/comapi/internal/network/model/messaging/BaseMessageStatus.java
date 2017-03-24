package com.comapi.internal.network.model.messaging;

import com.google.gson.annotations.SerializedName;


/**
 * Describes messages status.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class BaseMessageStatus {

    @SerializedName("status")
    protected String status;

    @SerializedName("timestamp")
    protected String timestamp;

    /**
     * Message status 'delivered' or 'read'
     *
     * @return Message status 'delivered' or 'read'
     */
    public String getStatus() {
        return status;
    }

    /**
     * When the message status was changed
     *
     * @return When the message status was changed
     */
    public String getTimestamp() {
        return timestamp;
    }
}
