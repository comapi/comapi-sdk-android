package com.comapi.internal.network.model.events.conversation.message;

import com.google.gson.annotations.SerializedName;

/**
 * Base class for socket events. Delivers information about message status
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MessageUpdateEvent extends ConversationMessageEvent {

    @SerializedName("payload")
    protected PayloadStatusUpdate payload;

    /**
     * Gets id of the updated message.
     *
     * @return Id of the updated message.
     */
    public String getMessageId() {
        return payload != null ? payload.messageId : null;
    }

    /**
     * Gets id of the conversation for which message was updated.
     *
     * @return Id of the updated message.
     */
    public String getConversationId() {
        return payload != null ? payload.conversationId : null;
    }

    /**
     * Gets profile id of the user that changed the message status.
     *
     * @return Profile id of the user that changed the message status.
     */
    public String getProfileId() {
        return payload != null ? payload.profileId : null;
    }

    /**
     * Gets time when the message status changed.
     *
     * @return Time when the message status changed.
     */
    public String getTimestamp() {
        return payload != null ? payload.timestamp : null;
    }

    @Override
    public String toString() {
        return super.toString()+
                " | conversationId = " + conversationEventId +
                " | profileId = " + getProfileId() +
                " | messageId = " + getMessageId();
    }
}