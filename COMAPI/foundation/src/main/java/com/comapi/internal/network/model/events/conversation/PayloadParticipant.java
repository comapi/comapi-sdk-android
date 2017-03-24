package com.comapi.internal.network.model.events.conversation;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Payload of an {@link ParticipantEvent}.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class PayloadParticipant {

    @SerializedName("profileId")
    protected String profileId;

    @SerializedName("role")
    protected String role;

    @SerializedName("conversationId")
    protected String conversationId;

    /**
     * Gets participants profile id.
     *
     * @return Participants profile id.
     */
    public String getProfileId() {
        return profileId;
    }

    /**
     * Role definition for this participant in this conversation
     *
     * @return Role definition for this participant in this conversation
     */
    @Nullable
    public String getRole() {
        return role;
    }

    /**
     * Gets conversation unique id.
     *
     * @return Conversation unique id.
     */
    public String getConversationId() {
        return conversationId;
    }
}