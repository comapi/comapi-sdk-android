package com.comapi.internal.network.model.messaging;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents message that can be published in the conversation.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class BaseMessage {

    @SerializedName("metadata")
    Map<String, Object> metadata;

    @SerializedName("parts")
    List<Part> parts;

    BaseMessage() {
        parts = new LinkedList<>();
    }

    /**
     * Custom message metadata (sent when client sends a message)
     *
     * @return Custom message metadata (sent when client sends a message)
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Parts of the message with data, type, name and size
     *
     * @return Parts of the message with data, type, name and size
     */
    public List<Part> getParts() {
        return parts;
    }
}