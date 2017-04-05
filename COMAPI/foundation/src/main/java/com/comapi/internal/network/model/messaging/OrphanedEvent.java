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

package com.comapi.internal.network.model.messaging;

import com.google.gson.annotations.SerializedName;

/**
 * Orphaned event. Event returned in message query related to some message that was send later then messages obtained from the query. Should be used to update messages in previous message pages.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public class OrphanedEvent {

    private static final String KEY_NAME_DELIVERED = "delivered";

    private static final String KEY_NAME_READ = "read";

    @SerializedName("id")
    private int id;

    @SerializedName("data")
    private OrphanedEventData data;

    public class OrphanedEventData {

        @SerializedName("name")
        private String name;

        @SerializedName("payload")
        private OrphanedEventPayload payload;

        @SerializedName("eventId")
        private String eventId;

        @SerializedName("profileId")
        private String profileId;

    }

    public class OrphanedEventPayload {

        @SerializedName("messageId")
        private String messageId;

        @SerializedName("conversationId")
        private String conversationId;

        @SerializedName("profileId")
        private String profileId;

        @SerializedName("timestamp")
        private String timestamp;
    }

    /**
     * Conversation event id.
     *
     * @return Conversation event id.
     */
    public int getConversationEventId() {
        return id;
    }

    /**
     * Event name.
     *
     * @return Event name.
     */
    public String getName() {
        return data != null ? data.name : null;
    }

    /**
     * Unique event identifier.
     *
     * @return Unique event identifier.
     */
    public String getEventId() {
        return data != null ? data.eventId : null;
    }

    /**
     * Gets id of the updated message.
     *
     * @return Id of the updated message.
     */
    public String getMessageId() {
        return (data != null && data.payload != null) ? data.payload.messageId : null;
    }

    /**
     * Gets id of the conversation for which message was updated.
     *
     * @return Id of the updated message.
     */
    public String getConversationId() {
        return (data != null && data.payload != null) ? data.payload.conversationId : null;
    }

    /**
     * Gets profile id of the user that changed the message status.
     *
     * @return Profile id of the user that changed the message status.
     */
    public String getProfileId() {
        return (data != null && data.payload != null) ? data.payload.profileId : null;
    }

    /**
     * Gets time when the message status changed.
     *
     * @return Time when the message status changed.
     */
    public String getTimestamp() {
        return (data != null && data.payload != null) ? data.payload.timestamp : null;
    }

    /**
     * True if this event is of type message delivered.
     *
     * @return True if this event is of type message delivered.
     */
    public boolean isEventTypeDelivered() {
        return data != null && KEY_NAME_DELIVERED.equals(data.name);
    }

    /**
     * True if this event is of type message read.
     *
     * @return True if this event is of type message read.
     */
    public boolean isEventTypeRead() {
        return data != null && KEY_NAME_READ.equals(data.name);
    }
}