package com.comapi.internal.network.model.events.conversation;

import com.comapi.internal.network.model.events.Event;
import com.google.gson.annotations.SerializedName;

/**
 * Event received trough socket. Participant in a conversation is typing.
 *
 * @author Marcin Swierczek
 *         Copyright (C) Donky Networks Ltd. All rights reserved.
 * @version 1.0.0
 * @since 1.0.1
 */
public class ParticipantTypingEvent extends Event {

    public static final String TYPE = "conversation.participantTyping";

    @SerializedName("payload")
    private Payload payload;

    /**
     * Get conversation id in which participant is typing.
     *
     * @return Conversation id in which participant is typing.
     */
    public String getConversationId() {
        return payload != null ? payload.conversationId : null;
    }

    /**
     * Get profile id of an participant who is typing new message.
     *
     * @return Profile id of an participant who is typing new message.
     */
    public String getProfileId() {
        return payload != null ? payload.profileId : null;
    }

    private class Payload {

        @SerializedName("conversationId")
        private String conversationId;

        @SerializedName("profileId")
        private String profileId;

    }
}