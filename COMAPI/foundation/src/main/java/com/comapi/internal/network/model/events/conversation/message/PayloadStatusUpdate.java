package com.comapi.internal.network.model.events.conversation.message;

import com.google.gson.annotations.SerializedName;

/**
 * Payload of the message status update socket event.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
class PayloadStatusUpdate {

    @SerializedName("messageId")
    protected String messageId;

    @SerializedName("conversationId")
    protected String conversationId;

    @SerializedName("profileId")
    protected String profileId;

    @SerializedName("timestamp")
    protected String timestamp;

    /**
     * Message unique identifier
     *
     * @return Message unique identifier
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Gets conversation id for which the message status was updated.
     *
     * @return Conversation id for which the message status was updated.
     */
    public String getConversationId() {
        return conversationId;
    }

    /**
     * Gets profile id of the user that updated the message status.
     *
     * @return Profile id of the user that updated the message status.
     */
    public String getProfileId() {
        return profileId;
    }

    /**
     * Gets time when the message status was updated.
     *
     * @return Time when the message status was updated.
     */
    public String getTimestamp() {
        return timestamp;
    }
}
