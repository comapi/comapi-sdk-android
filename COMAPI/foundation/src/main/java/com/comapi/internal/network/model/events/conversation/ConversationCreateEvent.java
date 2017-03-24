package com.comapi.internal.network.model.events.conversation;

import com.comapi.internal.network.model.conversation.ConversationDetails;
import com.google.gson.annotations.SerializedName;

/**
 * Event received trough socket. Conversation was created.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ConversationCreateEvent extends ConversationEvent {

    public static final String TYPE = "conversation.create";

    @SerializedName("payload")
    protected ConversationDetails payload;

    /**
     * Gets details of a created conversation
     *
     * @return Conversation details.
     */
    public ConversationDetails getConversation() {
        return payload;
    }

    @Override
    public String toString() {
        return super.toString() +
                " | conversationId = " + payload.getId() +
                " | name = " + payload.getName();
    }
}