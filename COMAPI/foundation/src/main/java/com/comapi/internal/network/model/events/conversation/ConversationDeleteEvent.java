package com.comapi.internal.network.model.events.conversation;

import com.google.gson.annotations.SerializedName;

/**
 * Event received trough socket. Conversation was deleted.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ConversationDeleteEvent extends ConversationEvent {

    public static final String TYPE = "conversation.delete";

    @SerializedName("payload")
    protected Payload payload;

    /**
     * Gets date when the conversation was deleted.
     *
     * @return Date when the conversation was deleted.
     */
    public String getDeletedOn() {
        return payload != null ? payload.date : null;
    }

    @Override
    public String toString() {
        return super.toString()+" | Conversation deleted on "+getDeletedOn();
    }

    private class Payload {

        @SerializedName("date")
        protected String date;
    }
}