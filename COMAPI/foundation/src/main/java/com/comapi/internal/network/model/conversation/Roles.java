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

package com.comapi.internal.network.model.conversation;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Roles description for a conversation.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
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
