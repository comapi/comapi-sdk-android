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
 * Participant of the conversation.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public class Participant {

    public static final String OWNER = "owner";

    public static final String PARTICIPANT = "participant";

    @SerializedName("id")
    private String id;

    @SerializedName("role")
    private String role;

    /**
     * Unique profile identifier.
     *
     * @return Unique profile identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Participant roles in conversation.
     *
     * @return Participant roles in conversation.
     */
    public String getRole() {
        return role;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        Participant participant;

        Builder() {
            participant = new Participant();
        }

        /**
         * Set the Id of the conversation participant.
         *
         * @param id Id of the conversation participant.
         * @return Instance with new value.
         */
        public Builder setId(@NonNull String id) {
            participant.id = id;
            return this;
        }

        /**
         * Set the {@link Participant#OWNER} role for this Participant.
         *
         * @return Instance with {@link Participant#OWNER} role.
         */
        public Builder setIsOwner() {
            participant.role = OWNER;
            return this;
        }

        /**
         * Set the {@link Participant#PARTICIPANT} role for this Participant.
         *
         * @return Instance with {@link Participant#PARTICIPANT} role.
         */
        public Builder setIsParticipant() {
            participant.role = PARTICIPANT;
            return this;
        }

        public Participant build() {

            if (participant.role == null) {
                participant.role = PARTICIPANT;
            }

            return participant;
        }
    }
}
