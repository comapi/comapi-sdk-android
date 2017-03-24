package com.comapi.internal.network.model.events.conversation.message;

import com.comapi.internal.network.model.messaging.Part;
import com.google.gson.annotations.SerializedName;

import com.comapi.internal.network.model.messaging.Alert;
import com.comapi.internal.network.model.messaging.MessageContext;

import java.util.List;
import java.util.Map;

/**
 * Event received trough socket. Message was sent to all participant in a conversation.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MessageSentEvent extends ConversationMessageEvent {

    public static final String TYPE = "conversationMessage.sent";

    @SerializedName("payload")
    protected PayloadSent payload;

    /**
     * Message unique identifier
     *
     * @return Message unique identifier
     */
    public String getMessageId() {
        return payload != null ? payload.messageId : null;
    }

    /**
     * Custom message metadata (sent when client sends a message)
     *
     * @return Custom message metadata (sent when client sends a message)
     */
    public Map<String, Object> getMetadata() {
        return payload != null ? payload.metadata : null;
    }

    /**
     * Message context
     *
     * @return Message context
     */
    public MessageContext getContext() {
        return payload != null ? payload.context : null;
    }

    /**
     * Parts of the message with data, type, name and size
     *
     * @return Parts of the message with data, type, name and size
     */
    public List<Part> getParts() {
        return payload != null ? payload.parts : null;
    }

    /**
     * Alert definitions with FCM and APNS push platforms
     *
     * @return Alert definitions with FCM and APNS push platforms
     */
    public Alert getAlert() {
        return payload != null ? payload.alert : null;
    }

    @Override
    public String toString() {
        return super.toString() +
                " | messageId = " + payload.getMessageId();
    }
}