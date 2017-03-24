package com.comapi.internal.network.model.events.conversation;

import com.comapi.internal.network.model.conversation.ConversationDetails;
import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ConversationUndeleteEvent extends ConversationEvent {

    public static final String TYPE = "conversation.undelete";

    @SerializedName("payload")
    protected ConversationDetails payload;

    /**
     * Gets details of undeleted conversation.
     *
     * @return Details of undeleted conversation.
     */
    public ConversationDetails getConversation() {
        return payload;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}