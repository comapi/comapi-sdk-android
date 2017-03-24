package com.comapi.internal.network.model.events.conversation.message;

import com.google.gson.annotations.SerializedName;

import com.comapi.internal.network.model.events.Event;

/**
 * Base class for socket events related to a particonversation.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
class ConversationMessageEvent extends Event {

    @SerializedName("conversationEventId")
    protected long conversationEventId;

    /**
     * Gets id of an event related to particular conversation. It's unique per conversation. Corresponds to the number of event per conversation.
     *
     * @return The number of event per conversation.
     */
    public long getConversationEventId() {
        return conversationEventId;
    }

    @Override
    public String toString() {
        return " | conversationEventId " + conversationEventId;
    }
}