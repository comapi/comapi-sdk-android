package com.comapi.internal.network.model.conversation;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Roles description for a conversation.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Roles {

    @SerializedName("owner")
    protected Role owner;

    @SerializedName("participant")
    protected Role participant;

    /**
     * Recommended constructor.
     *
     * @param owner       Role definition for the owner of the conversation.
     * @param participant Role definition for the participant of the conversation.
     */
    public Roles(@NonNull Role owner, @NonNull Role participant) {
        this.owner = owner;
        this.participant = participant;
    }

    /**
     * Get role definition for the owner of the conversation.
     *
     * @return Role definition for the owner of the conversation.
     */
    public Role getOwner() {
        return owner;
    }

    /**
     * Get role definition for the participant of the conversation.
     *
     * @return Role definition for the participant of the conversation.
     */
    public Role getParticipant() {
        return participant;
    }

}
