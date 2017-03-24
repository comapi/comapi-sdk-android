package com.comapi.internal.network.model.events.conversation;

/**
 * Event received trough socket. Conversation participant was added.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ParticipantAddedEvent extends ParticipantEvent {

    public static final String TYPE = "conversation.participantAdded";

    @Override
    public String toString() {
        return super.toString();
    }
}