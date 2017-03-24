package com.comapi.internal.network.model.conversation;

import com.google.gson.annotations.SerializedName;

/**
 * Request to create a conversation.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ConversationBase {

    @SerializedName("id")
    protected String id;

    @SerializedName("name")
    protected String name;

    @SerializedName("description")
    protected String description;

    @SerializedName("roles")
    protected Roles roles;

    /**
     * Get ID of the Conversation.
     *
     * @return ID of the Conversation.
     */
    public String getId() {
        return id;
    }

    /**
     * Get name of the Conversation.
     *
     * @return Name of the Conversation.
     */
    public String getName() {
        return name;
    }

    /**
     * Get ID of the Conversation.
     *
     * @return ID of the Conversation.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get description of the Conversation.
     *
     * @return Description of the Conversation.
     */
    public Roles getRoles() {
        return roles;
    }
}
