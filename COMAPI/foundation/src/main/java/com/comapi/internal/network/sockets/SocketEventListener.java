package com.comapi.internal.network.sockets;

import com.comapi.internal.network.model.events.ProfileUpdateEvent;
import com.comapi.internal.network.model.events.SocketStartEvent;
import com.comapi.internal.network.model.events.conversation.ConversationDeleteEvent;
import com.comapi.internal.network.model.events.conversation.ConversationUndeleteEvent;
import com.comapi.internal.network.model.events.conversation.ConversationUpdateEvent;
import com.comapi.internal.network.model.events.conversation.ParticipantAddedEvent;
import com.comapi.internal.network.model.events.conversation.ParticipantRemovedEvent;
import com.comapi.internal.network.model.events.conversation.ParticipantTypingEvent;
import com.comapi.internal.network.model.events.conversation.ParticipantUpdatedEvent;
import com.comapi.internal.network.model.events.conversation.message.MessageDeliveredEvent;
import com.comapi.internal.network.model.events.conversation.message.MessageReadEvent;
import com.comapi.internal.network.model.events.conversation.message.MessageSentEvent;

/**
 * Listener for socket events.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface SocketEventListener {

    /**
     * Dispatch conversation message event.
     *
     * @param event Event to dispatch.
     */
    void onMessageSent(MessageSentEvent event);

    /**
     * Dispatch conversation message update event.
     *
     * @param event Event to dispatch.
     */
    void onMessageDelivered(MessageDeliveredEvent event);

    /**
     * Dispatch conversation message update event.
     *
     * @param event Event to dispatch.
     */
    void onMessageRead(MessageReadEvent event);

    /**
     * Dispatch socket info event.
     *
     * @param event Event to dispatch.
     */
    void onSocketStarted(SocketStartEvent event);

    /**
     * Dispatch participant added to a conversation event.
     *
     * @param event Event to dispatch.
     */
    void onParticipantAdded(ParticipantAddedEvent event);

    /**
     * Dispatch participant updated event.
     *
     * @param event Event to dispatch.
     */
    void onParticipantUpdated(ParticipantUpdatedEvent event);

    /**
     * Dispatch participant removed event.
     *
     * @param event Event to dispatch.
     */
    void onParticipantRemoved(ParticipantRemovedEvent event);

    /**
     * Dispatch conversation updated event.
     *
     * @param event Event to dispatch.
     */
    void onConversationUpdated(ConversationUpdateEvent event);

    /**
     * Dispatch conversation deleted event.
     *
     * @param event Event to dispatch.
     */
    void onConversationDeleted(ConversationDeleteEvent event);

    /**
     * Dispatch conversation restored event.
     *
     * @param event Event to dispatch.
     */
    void onConversationUndeleted(ConversationUndeleteEvent event);

    /**
     * Dispatch profile update event.
     *
     * @param event Event to dispatch.
     */
    void onProfileUpdate(ProfileUpdateEvent event);

    /**
     * Dispatch participant is typing.
     *
     * @param event Event to dispatch.
     */
    void onParticipantIsTyping(ParticipantTypingEvent event);
}
