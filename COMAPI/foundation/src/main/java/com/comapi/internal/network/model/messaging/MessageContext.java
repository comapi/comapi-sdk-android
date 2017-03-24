package com.comapi.internal.network.model.messaging;

import com.google.gson.annotations.SerializedName;

/**
 * Context of received message.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MessageContext {

    @SerializedName("from")
    private Sender fromWhom;

    @SerializedName("sentBy")
    private String sentBy;

    @SerializedName("sentOn")
    private String sentOn;

    @SerializedName("conversationId")
    private String conversationId;

    /**
     * Message sender
     *
     * @return Message sender
     */
    public Sender getFromWhom() {
        return fromWhom;
    }

    /**
     * Message sender defined internally on server (shouldn't be visible inside the app)
     *
     * @return Message sender defined internally on server (shouldn't be visible inside the app)
     */
    public String getSentBy() {
        return sentBy;
    }

    /**
     * When the message was sent
     *
     * @return When the message was sent
     */
    public String getSentOn() {
        return sentOn;
    }

    /**
     * Conversation unique identifier
     *
     * @return Conversation unique identifier
     */
    public String getConversationId() {
        return conversationId;
    }
}
