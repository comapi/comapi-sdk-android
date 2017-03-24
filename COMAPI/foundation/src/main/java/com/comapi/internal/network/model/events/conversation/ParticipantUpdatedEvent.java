package com.comapi.internal.network.model.events.conversation;

/**
 * Event received trough socket. Conversation participant was updated.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ParticipantUpdatedEvent extends ParticipantEvent {

    public static final String TYPE = "conversation.participantUpdated";

    @Override
    public String toString() {
        return super.toString();
    }
}