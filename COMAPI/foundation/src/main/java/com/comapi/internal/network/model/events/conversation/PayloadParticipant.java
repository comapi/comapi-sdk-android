/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Comapi (trading name of Dynmark International Limited)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons
 * to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.comapi.internal.network.model.events.conversation;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Payload of an {@link ParticipantEvent}.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
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