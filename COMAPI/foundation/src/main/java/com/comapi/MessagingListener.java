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

package com.comapi;

import com.comapi.internal.IMessagingListener;
import com.comapi.internal.network.model.events.conversation.ConversationCreateEvent;
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

/**
 * Listener for messaging events.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public abstract class MessagingListener implements IMessagingListener {

    /**
     * Dispatch conversation message event.
     *
     * @param event Event to dispatch.
     */
    public void onMessage(MessageSentEvent event) {}

    /**
     * Dispatch conversation message update event.
     *
     * @param event Event to dispatch.
     */
    public void onMessageDelivered(MessageDeliveredEvent event) {}

    /**
     * Dispatch conversation message update event.
     *
     * @param event Event to dispatch.
     */
    public void onMessageRead(MessageReadEvent event) {}

    /**
     * Dispatch participant added to a conversation event.
     *
     * @param event Event to dispatch.
     */
    public void onParticipantAdded(ParticipantAddedEvent event) {}

    /**
     * Dispatch participant updated event.
     *
     * @param event Event to dispatch.
     */
    public void onParticipantUpdated(ParticipantUpdatedEvent event) {}

    /**
     * Dispatch participant removed event.
     *
     * @param event Event to dispatch.
     */
    public void onParticipantRemoved(ParticipantRemovedEvent event) {}

    /**
     * Dispatch conversation created event.
     *
     * @param event Event to dispatch.
     */
    public void onConversationCreated(ConversationCreateEvent event) {}

    /**
     * Dispatch conversation updated event.
     *
     * @param event Event to dispatch.
     */
    public void onConversationUpdated(ConversationUpdateEvent event) {}

    /**
     * Dispatch conversation deleted event.
     *
     * @param event Event to dispatch.
     */
    public void onConversationDeleted(ConversationDeleteEvent event) {}

    /**
     * Dispatch conversation restored event.
     *
     * @param event Event to dispatch.
     */
    public void onConversationUndeleted(ConversationUndeleteEvent event) {}

    /**
     * Dispatch participant is typing event.
     *
     * @param event Event to dispatch.
     */
    public void onParticipantIsTyping(ParticipantTypingEvent event) {}

    /**
     * Dispatch participant stopped typing event.
     *
     * @param event Event to dispatch.
     */
    public void onParticipantTypingOff(ParticipantTypingOffEvent event) {}
}
