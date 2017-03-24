package com.comapi.internal.network.model.messaging;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Represents message query response from services.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MessagesQueryResponse {

    @SerializedName("latestEventId")
    private int latestEventId;

    @SerializedName("earliestEventId")
    private int earliestEventId;

    @SerializedName("messages")
    private List<MessageReceived> messages;

    @SerializedName("orphanedEvents")
    private List<OrphanedEvent> orphanedEvents;

    public int getLatestEventId() {
        return latestEventId;
    }

    public int getEarliestEventId() {
        return earliestEventId;
    }

    public List<MessageReceived> getMessages() {
        return messages;
    }

    public List<OrphanedEvent> getOrphanedEvents() {
        return orphanedEvents;
    }
}
