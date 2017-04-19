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

package com.comapi.internal.network.sockets;

import android.os.Handler;
import android.os.Looper;

import com.comapi.internal.Parser;
import com.comapi.internal.log.Logger;
import com.comapi.internal.network.model.events.Event;
import com.comapi.internal.network.model.events.ProfileUpdateEvent;
import com.comapi.internal.network.model.events.SocketStartEvent;
import com.comapi.internal.network.model.events.conversation.ConversationDeleteEvent;
import com.comapi.internal.network.model.events.conversation.ConversationUndeleteEvent;
import com.comapi.internal.network.model.events.conversation.ConversationUpdateEvent;
import com.comapi.internal.network.model.events.conversation.ParticipantAddedEvent;
import com.comapi.internal.network.model.events.conversation.ParticipantRemovedEvent;
import com.comapi.internal.network.model.events.conversation.ParticipantTypingEvent;
import com.comapi.internal.network.model.events.conversation.ParticipantTypingOffEvent;
import com.comapi.internal.network.model.events.conversation.ParticipantUpdatedEvent;
import com.comapi.internal.network.model.events.conversation.message.MessageDeliveredEvent;
import com.comapi.internal.network.model.events.conversation.message.MessageReadEvent;
import com.comapi.internal.network.model.events.conversation.message.MessageSentEvent;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Categorises, and dispatches socket events to message bus.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public class SocketEventDispatcher implements SocketMessageListener {

    private final Parser parser;

    private Logger log;

    private final SocketEventListener listener;

    private Handler handler;

    /**
     * Recommended constructor.
     *
     * @param listener Listener for all socket events.
     * @param parser   Json parser implantation.
     */
    public SocketEventDispatcher(SocketEventListener listener, Parser parser) {
        this.listener = listener;
        this.parser = parser;
        this.handler = new Handler(Looper.getMainLooper());
    }

    /**
     * Sets internal logger.
     * @param log Internal logger.
     * @return Adapter for text messages coming trough websocket.
     */
    public SocketEventDispatcher setLogger(Logger log) {
        this.log = log;
        return this;
    }

    @Override
    public void onMessage(String text) {

        if (listener != null) {

            JsonObject event = parser.parse(text, JsonObject.class);

            if (event != null) {

                JsonElement nameElement = event.get(Event.KEY_NAME);

                if (nameElement != null) {

                    String name = nameElement.getAsString();

                    if (MessageSentEvent.TYPE.equals(name)) {
                        onMessageSent(parser.parse(event, MessageSentEvent.class));
                    } else if (MessageDeliveredEvent.TYPE.equals(name)) {
                        onMessageDelivered(parser.parse(event, MessageDeliveredEvent.class));
                    } else if (MessageReadEvent.TYPE.equals(name)) {
                        onMessageRead(parser.parse(event, MessageReadEvent.class));
                    } else if (ParticipantAddedEvent.TYPE.equals(name)) {
                        onParticipantAdded(parser.parse(event, ParticipantAddedEvent.class));
                    } else if (ParticipantUpdatedEvent.TYPE.equals(name)) {
                        onParticipantUpdated(parser.parse(event, ParticipantUpdatedEvent.class));
                    } else if (ParticipantRemovedEvent.TYPE.equals(name)) {
                        onParticipantRemoved(parser.parse(event, ParticipantRemovedEvent.class));
                    } else if (ConversationUpdateEvent.TYPE.equals(name)) {
                        onConversationUpdated(parser.parse(event, ConversationUpdateEvent.class));
                    } else if (ConversationDeleteEvent.TYPE.equals(name)) {
                        onConversationDeleted(parser.parse(event, ConversationDeleteEvent.class));
                    } else if (ConversationUndeleteEvent.TYPE.equals(name)) {
                        onConversationUndeleted(parser.parse(event, ConversationUndeleteEvent.class));
                    } else if (SocketStartEvent.TYPE.equals(name)) {
                        onSocketStarted(parser.parse(event, SocketStartEvent.class));
                    } else if (ProfileUpdateEvent.TYPE.equals(name)) {
                        onProfileUpdate(parser.parse(event, ProfileUpdateEvent.class));
                    } else if (ParticipantTypingEvent.TYPE.equals(name)) {
                        onParticipantIsTyping(parser.parse(event, ParticipantTypingEvent.class));
                    } else if (ParticipantTypingOffEvent.TYPE.equals(name)) {
                        onParticipantTypingOff(parser.parse(event, ParticipantTypingOffEvent.class));
                    }
                }
            }
        }
    }

    /**
     * Dispatch conversation participant is typing event.
     *
     * @param event Event to dispatch.
     */
    private void onParticipantIsTyping(ParticipantTypingEvent event) {
        handler.post(() -> listener.onParticipantIsTyping(event));
        log("Event published " + event.toString());
    }

    /**
     * Dispatch conversation participant stopped typing event.
     *
     * @param event Event to dispatch.
     */
    private void onParticipantTypingOff(ParticipantTypingOffEvent event) {
        handler.post(() -> listener.onParticipantTypingOff(event));
        log("Event published " + event.toString());
    }

    /**
     * Dispatch profile update event.
     *
     * @param event Event to dispatch.
     */
    private void onProfileUpdate(ProfileUpdateEvent event) {
        handler.post(() -> listener.onProfileUpdate(event));
        log("Event published " + event.toString());
    }

    /**
     * Dispatch conversation message event.
     *
     * @param event Event to dispatch.
     */
    private void onMessageSent(MessageSentEvent event) {
        handler.post(() -> listener.onMessageSent(event));
        log("Event published " + event.toString());
    }

    /**
     * Dispatch conversation message update event.
     *
     * @param event Event to dispatch.
     */
    void onMessageDelivered(MessageDeliveredEvent event) {
        handler.post(() -> listener.onMessageDelivered(event));
        log("Event published " + event.toString());
    }

    /**
     * Dispatch conversation message update event.
     *
     * @param event Event to dispatch.
     */
    void onMessageRead(MessageReadEvent event) {
        handler.post(() -> listener.onMessageRead(event));
        log("Event published " + event.toString());
    }

    /**
     * Dispatch socket info event.
     *
     * @param event Event to dispatch.
     */
    private void onSocketStarted(SocketStartEvent event) {
        handler.post(() -> listener.onSocketStarted(event));
        log("Event published " + event.toString());
    }

    /**
     * Dispatch participant added to a conversation event.
     *
     * @param event Event to dispatch.
     */
    private void onParticipantAdded(ParticipantAddedEvent event) {
        handler.post(() -> listener.onParticipantAdded(event));
        log("Event published " + event.toString());
    }

    /**
     * Dispatch participant updated event.
     *
     * @param event Event to dispatch.
     */
    private void onParticipantUpdated(ParticipantUpdatedEvent event) {
        handler.post(() -> listener.onParticipantUpdated(event));
        log("Event published " + event.toString());
    }

    /**
     * Dispatch participant removed event.
     *
     * @param event Event to dispatch.
     */
    private void onParticipantRemoved(ParticipantRemovedEvent event) {
        handler.post(() -> listener.onParticipantRemoved(event));
        log("Event published " + event.toString());
    }

    /**
     * Dispatch conversation updated event.
     *
     * @param event Event to dispatch.
     */
    private void onConversationUpdated(ConversationUpdateEvent event) {
        handler.post(() -> listener.onConversationUpdated(event));
        log("Event published " + event.toString());
    }

    /**
     * Dispatch conversation deleted event.
     *
     * @param event Event to dispatch.
     */
    private void onConversationDeleted(ConversationDeleteEvent event) {
        handler.post(() -> listener.onConversationDeleted(event));
        log("Event published " + event.toString());
    }

    /**
     * Dispatch conversation restored event.
     *
     * @param event Event to dispatch.
     */
    private void onConversationUndeleted(ConversationUndeleteEvent event) {
        handler.post(() -> listener.onConversationUndeleted(event));
        log("Event published " + event.toString());
    }

    void log(String message) {
        if (log != null) {
            log.d(message);
        }
    }
}