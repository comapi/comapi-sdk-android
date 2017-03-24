package com.comapi.internal.network.model.conversation;

import com.google.gson.annotations.SerializedName;

/**
 * Details of a conversation.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ConversationDetails extends ConversationBase {

    @SerializedName("isPublic")
    protected Boolean isPublic;

    /**
     * Check if the Conversation is public.
     *
     * @return True if Conversation is publicly accessible.
     */
    public Boolean isPublic() {
        return isPublic;
    }
}