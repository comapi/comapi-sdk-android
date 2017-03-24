package com.comapi.internal.network.model.events.conversation;

import com.google.gson.annotations.SerializedName;

import com.comapi.internal.network.model.events.Event;

/**
 * Socket event base class for all events related to particular conversation.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
class ConversationEvent extends Event {

    @SerializedName("conversationId")
    protected String conversationId;

    /**
     * Gets conversation unique identifier.
     *
     * @return Conversation unique identifier.
     */
    public String getConversationId() {
        return conversationId;
    }

    @Override
    public String toString() {
        return super.toString() + " | conversationId " + conversationId;
    }
}