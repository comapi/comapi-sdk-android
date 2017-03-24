package com.comapi.internal.network.model.messaging;

import com.comapi.internal.helpers.DateHelper;
import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.Set;

/**
 * Describes messages status modification.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MessageStatusUpdate extends BaseMessageStatus {

    @SerializedName("messageIds")
    private Set<String> messageIds;

    public MessageStatusUpdate() {
        messageIds = new HashSet<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        MessageStatusUpdate update;

        Builder() {
            update = new MessageStatusUpdate();
        }

        /**
         * Adds message id for a message which status should be changed.
         */
        public Builder addMessageId(String messageId) {
            update.messageIds.add(messageId);
            return this;
        }

        /**
         * Sets message ids for a message which status should be changed.
         */
        public Builder setMessagesIds(Set<String> messageIds) {
            update.messageIds = messageIds;
            return this;
        }

        /**
         * Sets new message status.
         */
        public <E extends MessageStatus> Builder setStatus(E status) {
            update.status = status.name();
            return this;
        }

        /**
         * Sets timestamp.
         */
        public Builder setTimestamp(String timestamp) {
            update.timestamp = timestamp;
            return this;
        }

        public MessageStatusUpdate build() {

            if (update.timestamp == null) {
                update.timestamp = DateHelper.getCurrentUTC();
            }

            return update;
        }
    }
}
