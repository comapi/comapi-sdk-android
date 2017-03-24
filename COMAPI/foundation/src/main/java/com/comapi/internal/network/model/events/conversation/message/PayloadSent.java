package com.comapi.internal.network.model.events.conversation.message;

import com.comapi.internal.network.model.messaging.Alert;
import com.comapi.internal.network.model.messaging.MessageContext;
import com.comapi.internal.network.model.messaging.Part;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Payload of the {@link MessageSentEvent} event.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
class PayloadSent {

    @SerializedName("messageId")
    protected String messageId;

    @SerializedName("metadata")
    protected Map<String, Object> metadata;

    @SerializedName("context")
    protected MessageContext context;

    @SerializedName("parts")
    protected List<Part> parts;

    @SerializedName("alert")
    protected Alert alert;

    /**
     * Message unique identifier
     *
     * @return Message unique identifier
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Gets message metadata.
     *
     * @return Message metadata.
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Gets message context information.
     *
     * @return Message context information.
     */
    public MessageContext getContext() {
        return context;
    }

    /**
     * Gets message parts.
     *
     * @return Message parts.
     */
    public List<Part> getParts() {
        return parts;
    }

    /**
     * Gets message alert configuration.
     *
     * @return Message alert configuration.
     */
    public Alert getAlert() {
        return alert;
    }
}
