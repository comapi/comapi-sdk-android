package com.comapi.internal.network.model.events.conversation;

import com.comapi.internal.network.model.conversation.ConversationBase;
import com.google.gson.annotations.SerializedName;

import com.comapi.internal.network.model.conversation.Roles;

/**
 * Event received trough socket. Conversation was updated.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ConversationUpdateEvent extends ConversationEvent {

    public static final String TYPE = "conversation.update";

    @SerializedName("payload")
    protected ConversationBase payload;

    /**
     * Get name of the Conversation.
     *
     * @return Name of the Conversation.
     */
    public String getConversationName() {
        return payload != null ? payload.getName() : null;
    }

    /**
     * Get ID of the Conversation.
     *
     * @return ID of the Conversation.
     */
    public String getDescription() {
        return payload != null ? payload.getDescription() : null;
    }

    /**
     * Get description of the Conversation.
     *
     * @return Description of the Conversation.
     */
    public Roles getRoles() {
        return payload != null ? payload.getRoles() : null;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}