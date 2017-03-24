package com.comapi.internal.network.model.messaging;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Represents message that can be published on the chanel.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MessageToSend extends BaseMessage {

    @SerializedName("alert")
    private Alert alert;

    private MessageToSend() {
        super();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        MessageToSend message;

        Builder() {
            message = new MessageToSend();
        }

        /**
         * Sets metadata to the message.
         */
        public Builder setMetadata(Map<String, Object> metadata) {
            message.metadata = metadata;
            return this;
        }

        /**
         * Adds new part of the message.
         */
        public Builder addPart(Part part) {
            message.parts.add(part);
            return this;
        }

        /**
         * Sets push alert details.
         */
        public Builder setAlert(Map<String, Object> fcm, Map<String, Object> apns) {
            message.alert = new Alert(fcm, apns);
            return this;
        }

        public MessageToSend build() {
            return message;
        }
    }
}
