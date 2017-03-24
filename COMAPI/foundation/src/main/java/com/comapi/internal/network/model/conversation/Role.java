package com.comapi.internal.network.model.conversation;

import com.google.gson.annotations.SerializedName;

/**
 * Role that can be assign to a conversation participant.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Role {

    @SerializedName("canSend")
    private Boolean canSend;

    @SerializedName("canAddParticipants")
    private Boolean canAddParticipants;

    @SerializedName("canRemoveParticipants")
    private Boolean canRemoveParticipants;

    /**
     * Recommended constructor. By default Participant can send messages, can add participants, cannot remove participants.
     */
    public Role() {
        this.canSend = true;
        this.canAddParticipants = true;
        this.canRemoveParticipants = false;
    }

    /**
     * Check if participant with this role can send messages to the conversation.
     *
     * @return True if participant with this role can send messages to the conversation.
     */
    public Boolean getCanSend() {
        return canSend;
    }

    /**
     * Check if participant with this role can add participants to the conversation.
     *
     * @return True if participant with this role can add participants to the conversation.
     */
    public Boolean getCanAddParticipants() {
        return canAddParticipants;
    }

    /**
     * Check if participant with this role can remove participants from the conversation.
     *
     * @return True if participant with this role can remove participants from the conversation.
     */
    public Boolean getCanRemoveParticipants() {
        return canRemoveParticipants;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        Role role;

        Builder() {
            role = new Role();
        }

        public Role build() {
            return role;
        }

        /**
         * Participant with this role will be able to send messages to the conversation.
         *
         * @return Instance of the role that allows sending messages to the conversation.
         */
        public Builder setCanSend() {
            role.canSend = true;
            return this;
        }

        /**
         * Participant with this role will be able to add participants to the conversation.
         *
         * @return Instance of the role that allows adding participants to the conversation.
         */
        public Builder setCanAddParticipants() {
            role.canAddParticipants = true;
            return this;
        }

        /**
         * Participant with this role will be able to remove participants from the conversation.
         *
         * @return Instance of the role that allows removing participants from the conversation.
         */
        public Builder setCanRemoveParticipants() {
            role.canRemoveParticipants = true;
            return this;
        }
    }
}
