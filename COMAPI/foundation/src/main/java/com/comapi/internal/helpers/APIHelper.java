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

package com.comapi.internal.helpers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.comapi.internal.network.model.messaging.MessageToSend;
import com.comapi.internal.network.model.messaging.Part;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to create basic message object to send.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public class APIHelper {

    /**
     * Convenience method to create a Message object with a single text body part.
     *
     * @param conversationId Unique conversation id.
     * @param body           Message text body.
     * @param title          Remote notification title.
     * @return Message object that can by sent to other participants of the conversation.
     */
    public static MessageToSend createMessage(@NonNull String conversationId, @NonNull String body, @Nullable String title) {

        Map<String, Object> fcm = new HashMap<>();
        String fcmMsg = !TextUtils.isEmpty(body) ? body : "attachments";
        fcm.put("notification", new Notification(title != null ? title : conversationId, fcmMsg, conversationId));
        Map<String, Object> apns = new HashMap<>();
        apns.put("alert", fcmMsg);

        MessageToSend.Builder builder = MessageToSend.builder();
        if (!TextUtils.isEmpty(body)) {
            Part bodyPart = Part.builder().setData(body).setName("body").setSize(body.length()).setType("text/plain").build();
            builder.addPart(bodyPart);
        }
        return builder.setAlert(fcm, apns).build();
    }

    /**
     * Class to parse remote notification info jason inside a Comapi message.
     */
    private static class Notification {

        @SerializedName("title")
        String title;
        @SerializedName("body")
        String body;
        @SerializedName("tag")
        String tag;

        /**
         * Recommended constructor.
         *
         * @param title Remote notification title.
         * @param body  Remote notification title.
         * @param tag   Remote notification tag identifing a message to avoid duplicates.
         */
        Notification(String title, String body, String tag) {
            this.title = title;
            this.body = body;
            this.tag = tag;
        }
    }
}