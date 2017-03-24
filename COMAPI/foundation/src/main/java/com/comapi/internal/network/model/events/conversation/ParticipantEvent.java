package com.comapi.internal.network.model.events.conversation;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import com.comapi.internal.network.model.events.Event;

/**
 * Event received trough socket. Base event related to conversation participants.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ParticipantEvent extends Event {

    @SerializedName("payload")
    protected PayloadParticipant payload;

    /**
     * User profile unique identifier
     *
     * @return User profile unique identifier
     */
    public String getProfileId() {
        return payload != null ? payload.getProfileId() : null;
    }

    /**
     * Role definition for this participant in this conversation
     *
     * @return Role definition for this participant in this conversation
     */
    @Nullable
    public String getRole() {
        return payload != null ? payload.getRole() : null;
    }

    /**
     * Conversation unique identifier
     *
     * @return Conversation unique identifier
     */
    public String getConversationId() {
        return payload != null ? payload.getConversationId() : null;
    }

    @Override
    public String toString() {
        return super.toString() +
                " | conversation = " + getConversationId() +
                " | profileId = " + getProfileId();
    }
}