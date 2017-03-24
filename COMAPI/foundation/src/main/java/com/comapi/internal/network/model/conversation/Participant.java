package com.comapi.internal.network.model.conversation;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Participant of the conversation.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
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
