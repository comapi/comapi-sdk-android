package com.comapi.internal.network.model.messaging;

import com.comapi.internal.Parser;
import com.comapi.internal.network.model.events.Event;
import com.comapi.internal.network.model.events.conversation.ConversationDeleteEvent;
import com.comapi.internal.network.model.events.conversation.ConversationUndeleteEvent;
import com.comapi.internal.network.model.events.conversation.ConversationUpdateEvent;
import com.comapi.internal.network.model.events.conversation.ParticipantAddedEvent;
import com.comapi.internal.network.model.events.conversation.ParticipantRemovedEvent;
import com.comapi.internal.network.model.events.conversation.ParticipantUpdatedEvent;
import com.comapi.internal.network.model.events.conversation.message.MessageDeliveredEvent;
import com.comapi.internal.network.model.events.conversation.message.MessageReadEvent;
import com.comapi.internal.network.model.events.conversation.message.MessageSentEvent;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class EventsQueryResponse {

    private int combinedSize;

    private final List<MessageSentEvent> messageSent = new ArrayList<>();

    private final List<MessageDeliveredEvent> messageDelivered = new ArrayList<>();

    private final List<MessageReadEvent> messageRead = new ArrayList<>();

    private final List<ConversationUpdateEvent> conversationUpdate = new ArrayList<>();

    private final List<ConversationDeleteEvent> conversationDelete = new ArrayList<>();

    private final List<ConversationUndeleteEvent> conversationUnDelete = new ArrayList<>();

    private final List<ParticipantAddedEvent> participantAdded = new ArrayList<>();

    private final List<ParticipantUpdatedEvent> participantUpdate = new ArrayList<>();

    private final List<ParticipantRemovedEvent> participantRemoved = new ArrayList<>();

    public EventsQueryResponse(List<JsonObject> newEvents, Parser parser) {

        if (newEvents != null) {
            for (JsonObject event : newEvents) {
                parseEvent(event, parser);
            }
            calculateSize();
        }
    }

    private void calculateSize() {
        combinedSize = messageSent.size() +
                messageDelivered.size() +
                messageRead.size() +
                conversationDelete.size() +
                conversationUnDelete.size() +
                conversationUpdate.size() +
                participantAdded.size() +
                participantRemoved.size() +
                participantUpdate.size();
    }

    private void parseEvent(JsonObject event, Parser parser) {

        JsonElement nameJE = event.get(Event.KEY_NAME);

        if (nameJE != null) {

            String name = nameJE.getAsString();

            if (MessageSentEvent.TYPE.equals(name)) {
                messageSent.add(parser.parse(event, MessageSentEvent.class));
            } else if (MessageDeliveredEvent.TYPE.equals(name)) {
                messageDelivered.add(parser.parse(event, MessageDeliveredEvent.class));
            } else if (MessageReadEvent.TYPE.equals(name)) {
                messageRead.add(parser.parse(event, MessageReadEvent.class));
            } else if (ConversationUpdateEvent.TYPE.equals(name)) {
                conversationUpdate.add(parser.parse(event, ConversationUpdateEvent.class));
            } else if (ConversationDeleteEvent.TYPE.equals(name)) {
                conversationDelete.add(parser.parse(event, ConversationDeleteEvent.class));
            } else if (ConversationUndeleteEvent.TYPE.equals(name)) {
                conversationUnDelete.add(parser.parse(event, ConversationUndeleteEvent.class));
            } else if (ParticipantAddedEvent.TYPE.equals(name)) {
                participantAdded.add(parser.parse(event, ParticipantAddedEvent.class));
            } else if (ParticipantUpdatedEvent.TYPE.equals(name)) {
                participantUpdate.add(parser.parse(event, ParticipantUpdatedEvent.class));
            } else if (ParticipantRemovedEvent.TYPE.equals(name)) {
                participantRemoved.add(parser.parse(event, ParticipantRemovedEvent.class));
            }
        }
    }

    public List<MessageSentEvent> getMessageSent() {
        return messageSent;
    }

    public List<MessageDeliveredEvent> getMessageDelivered() {
        return messageDelivered;
    }

    public List<MessageReadEvent> getMessageRead() {
        return messageRead;
    }

    public List<ConversationUpdateEvent> getConversationUpdate() {
        return conversationUpdate;
    }

    public List<ConversationDeleteEvent> getConversationDelete() {
        return conversationDelete;
    }

    public List<ConversationUndeleteEvent> getConversationUnDelete() {
        return conversationUnDelete;
    }

    public List<ParticipantAddedEvent> getParticipantAdded() {
        return participantAdded;
    }

    public List<ParticipantUpdatedEvent> getParticipantUpdate() {
        return participantUpdate;
    }

    public List<ParticipantRemovedEvent> getParticipantRemoved() {
        return participantRemoved;
    }

    /**
     * Number of events in response.
     *
     * @return Number of events in response.
     */
    public int getCombinedSize() {
        return combinedSize;
    }

}
