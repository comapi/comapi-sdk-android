package com.comapi.internal.network.model.conversation;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * Conversation details to update.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ConversationUpdate extends ConversationBase {

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        ConversationUpdate conversation;

        public Builder() {
            conversation = new ConversationUpdate();
        }

        /**
         * Set name for the Conversation.
         *
         * @param name Conversation name.
         * @return Instance with new name.
         */
        public Builder setName(String name) {
            conversation.name = name;
            return this;
        }

        /**
         * Set description for the Conversation.
         *
         * @param description Conversation description.
         * @return Instance with new description.
         */
        public Builder setDescription(String description) {
            conversation.description = description;
            return this;
        }

        /**
         * Set participant role definitions for the Conversation.
         *
         * @param roles Participant roles.
         * @return Instance with new participant roles.
         */
        public Builder setRoles(Roles roles) {
            conversation.roles = roles;
            return this;
        }

        /**
         * Set public access to the Conversation.
         *
         * @param isPublic True if Conversation is publicly accessible.
         * @return Instance with new value set.
         */
        public Builder setPublic(Boolean isPublic) {
            conversation.isPublic = isPublic;
            return this;
        }

        public ConversationUpdate build() {

            if (TextUtils.isEmpty(conversation.name)) {
                conversation.name = UUID.randomUUID().toString();
            }
            if (conversation.isPublic == null) {
                conversation.isPublic = false;
            }
            if (conversation.roles == null) {
                conversation.roles = new Roles(new Role(), new Role());
            } else {
                if (conversation.roles.getOwner() == null) {
                    conversation.roles.owner = new Role();
                }
                if (conversation.roles.getParticipant() == null) {
                    conversation.roles.participant = new Role();
                }
            }

            return conversation;
        }
    }
}
