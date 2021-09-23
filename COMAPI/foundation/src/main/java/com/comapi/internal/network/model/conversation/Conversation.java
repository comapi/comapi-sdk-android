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

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Details of a conversation.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public class Conversation extends ConversationDetails {

    @SerializedName("_etag")
    protected String eTag;

    @SerializedName("participantCount")
    protected Integer participantCount;

    @SerializedName("latestSentEventId")
    protected Long latestSentEventId;

    /**
     * Gets ETag to compare if local version of the data is the same as the one the server side.
     *
     * @return ETag to compare if local version of the data is the same as the one the server side.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Gets number of participant in conversation.
     *
     * @return Number of participant in conversation.
     */
    public Integer getParticipantCount() {
        return participantCount;
    }

    /**
     * Gets latest event id of sent message in the conversation. If null there are no messages in the conversation.
     *
     * @return Latest event id of sent message in the conversation
     */
    public
    @Nullable
    Long getLatestSentEventId() {
        return latestSentEventId;
    }
}