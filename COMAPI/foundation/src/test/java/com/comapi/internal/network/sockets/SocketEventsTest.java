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

import android.os.Build;

import com.comapi.BuildConfig;
import com.comapi.MessagingListener;
import com.comapi.ProfileListener;
import com.comapi.Session;
import com.comapi.StateListener;
import com.comapi.helpers.ResponseTestHelper;
import com.comapi.internal.IMessagingListener;
import com.comapi.internal.IProfileListener;
import com.comapi.internal.IStateListener;
import com.comapi.internal.ListenerListAdapter;
import com.comapi.internal.Parser;
import com.comapi.internal.log.LogManager;
import com.comapi.internal.log.Logger;
import com.comapi.internal.network.model.events.ProfileUpdateEvent;
import com.comapi.internal.network.model.events.SocketStartEvent;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Robolectric tests for parsing and dispatching events.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(manifest = "com.comapi.appmessaging/src/main/AndroidManifest.xml", sdk = Build.VERSION_CODES.M, constants = BuildConfig.class, packageName = "com.comapi")
public class SocketEventsTest {

    private SocketEventDispatcher dispatcher;

    private Receiver receiver;

    private IMessagingListener messagingListener;
    private IProfileListener profileListener;
    private IStateListener stateListener;

    @Before
    public void setUpComapi() throws Exception {
        LogManager mgr = new LogManager();
        receiver = new Receiver(new Logger(mgr, ""));

        messagingListener = new MessagingListener() {
        };
        profileListener = new ProfileListener() {
        };
        stateListener = new StateListener() {
        };

        receiver.addListener(messagingListener);
        receiver.addListener(profileListener);
        receiver.addListener(stateListener);
        dispatcher = new SocketEventDispatcher(receiver, new Parser()).setLogger(new Logger(mgr, ""));
    }

    @Test
    public void dispatchSent() throws IOException {

        String json = ResponseTestHelper.readFromFile(this, "message_sent.json");
        dispatcher.onMessage(json);
        assertEquals(true, receiver.sent.getName().equals(MessageSentEvent.TYPE));
        assertNotNull(receiver.sent.getEventId());
        assertEquals(true, receiver.sent.getConversationEventId() > 0);
        assertNotNull(receiver.sent.getMessageId());
        assertNotNull(receiver.sent.getAlert());
        assertNotNull(receiver.sent.getAlert().getPlatforms().getFcm().get("notification"));
        assertNotNull(receiver.sent.getAlert().getPlatforms().getFcm().get("data"));
        assertNotNull(receiver.sent.getContext().getConversationId());
        assertNotNull(receiver.sent.getContext().getSentBy());
        assertNotNull(receiver.sent.getContext().getSentOn());
        assertNotNull(receiver.sent.getContext().getFromWhom());
        assertNotNull(receiver.sent.getMetadata());
        assertNotNull(receiver.sent.getParts());
        assertNotNull(receiver.sent.getParts().get(0).getName());
        assertNotNull(receiver.sent.getParts().get(0).getData());
        assertEquals(true, receiver.sent.getParts().get(0).getSize() > 0);
        assertNotNull(receiver.sent.getParts().get(0).getType());
    }

    @Test
    public void dispatchSent_error() throws IOException {

        messagingListener = new IMessagingListener() {

            @Override
            public void onMessage(MessageSentEvent event) {
                throw new RuntimeException();
            }

            @Override
            public void onMessageDelivered(MessageDeliveredEvent event) {
                throw new RuntimeException();
            }

            @Override
            public void onMessageRead(MessageReadEvent event) {
                throw new RuntimeException();
            }

            @Override
            public void onParticipantAdded(ParticipantAddedEvent event) {
                throw new RuntimeException();
            }

            @Override
            public void onParticipantUpdated(ParticipantUpdatedEvent event) {
                throw new RuntimeException();
            }

            @Override
            public void onParticipantRemoved(ParticipantRemovedEvent event) {
                throw new RuntimeException();
            }

            @Override
            public void onConversationCreated(ConversationCreateEvent event) {
                throw new RuntimeException();
            }

            @Override
            public void onConversationUpdated(ConversationUpdateEvent event) {
                throw new RuntimeException();
            }

            @Override
            public void onConversationDeleted(ConversationDeleteEvent event) {
                throw new RuntimeException();
            }

            @Override
            public void onConversationUndeleted(ConversationUndeleteEvent event) {
                throw new RuntimeException();
            }

            @Override
            public void onParticipantIsTyping(ParticipantTypingEvent event) {
                throw new RuntimeException();
            }

            @Override
            public void onParticipantTypingOff(ParticipantTypingOffEvent event) {
                throw new RuntimeException();
            }
        };

        profileListener = new IProfileListener() {

            @Override
            public void onProfileUpdate(ProfileUpdateEvent event) {
                throw new RuntimeException();
            }
        };
        stateListener = new IStateListener() {
            @Override
            public void onSocketStart(SocketStartEvent event) {
                throw new RuntimeException();
            }

            @Override
            public void onSessionStart(Session session) {
                throw new RuntimeException();
            }
        };

        receiver.addListener(messagingListener);
        receiver.addListener(profileListener);
        receiver.addListener(stateListener);
        
        String json = ResponseTestHelper.readFromFile(this, "message_sent.json");
        dispatcher.onMessage(json);
        json = ResponseTestHelper.readFromFile(this, "message_delivered.json");
        dispatcher.onMessage(json);
        json = ResponseTestHelper.readFromFile(this, "message_read.json");
        dispatcher.onMessage(json);
        json = ResponseTestHelper.readFromFile(this, "socket_info.json");
        dispatcher.onMessage(json);
        json = ResponseTestHelper.readFromFile(this, "socket_info.json");
        dispatcher.onMessage(json);
        json = ResponseTestHelper.readFromFile(this, "participant_added.json");
        dispatcher.onMessage(json);
        json = ResponseTestHelper.readFromFile(this, "participant_updated.json");
        dispatcher.onMessage(json);
        json = ResponseTestHelper.readFromFile(this, "participant_removed.json");
        dispatcher.onMessage(json);
        json = ResponseTestHelper.readFromFile(this, "conversation_update.json");
        dispatcher.onMessage(json);
        json = ResponseTestHelper.readFromFile(this, "conversation_delete.json");
        dispatcher.onMessage(json);
        json = ResponseTestHelper.readFromFile(this, "conversation_delete.json");
        dispatcher.onMessage(json);
        json = ResponseTestHelper.readFromFile(this, "conversation_undelete.json");
        dispatcher.onMessage(json);
        json = ResponseTestHelper.readFromFile(this, "profile_update.json");
        dispatcher.onMessage(json);
        json = ResponseTestHelper.readFromFile(this, "is_typing.json");
        dispatcher.onMessage(json);
        json = ResponseTestHelper.readFromFile(this, "typing_off.json");
        dispatcher.onMessage(json);

        receiver.removeListener(messagingListener);
        receiver.removeListener(profileListener);
        receiver.removeListener(stateListener);
    }

    @Test
    public void dispatchDelivered() throws IOException {

        String json = ResponseTestHelper.readFromFile(this, "message_delivered.json");
        dispatcher.onMessage(json);
        assertEquals(true, receiver.delivered.getName().equals(MessageDeliveredEvent.TYPE));
        assertNotNull(receiver.delivered.getEventId());
        assertEquals(true, receiver.delivered.getConversationEventId() > 0);
        assertNotNull(receiver.delivered.getConversationId());
        assertNotNull(receiver.delivered.getMessageId());
        assertNotNull(receiver.delivered.getProfileId());
        assertNotNull(receiver.delivered.getTimestamp());
        assertNotNull(receiver.delivered.toString());
    }

    @Test
    public void dispatchRead() throws IOException {

        String json = ResponseTestHelper.readFromFile(this, "message_read.json");
        dispatcher.onMessage(json);
        assertEquals(true, receiver.read.getName().equals(MessageReadEvent.TYPE));
        assertNotNull(receiver.read.getEventId());
        assertEquals(true, receiver.read.getConversationEventId() > 0);
        assertNotNull(receiver.read.getConversationId());
        assertNotNull(receiver.read.getMessageId());
        assertNotNull(receiver.read.getProfileId());
        assertNotNull(receiver.read.getTimestamp());
        assertNotNull(receiver.read.toString());
    }

    @Test
    public void dispatchIsTyping() throws IOException {
        String json = ResponseTestHelper.readFromFile(this, "is_typing.json");
        dispatcher.onMessage(json);
        assertEquals(true, receiver.isTyping.getName().equals(ParticipantTypingEvent.TYPE));
        assertNotNull(receiver.isTyping.getEventId());
        assertNotNull(receiver.isTyping.getConversationId());
        assertNotNull(receiver.isTyping.getProfileId());
        assertNotNull(receiver.isTyping.toString());
    }

    @Test
    public void dispatchIsNotTyping() throws IOException {
        String json = ResponseTestHelper.readFromFile(this, "typing_off.json");
        dispatcher.onMessage(json);
        assertEquals(true, receiver.isNotTyping.getName().equals(ParticipantTypingOffEvent.TYPE));
        assertNotNull(receiver.isNotTyping.getEventId());
        assertNotNull(receiver.isNotTyping.getConversationId());
        assertNotNull(receiver.isNotTyping.getProfileId());
        assertNotNull(receiver.isNotTyping.toString());
    }

    @Test
    public void dispatchSocketStart() throws IOException {

        String json = ResponseTestHelper.readFromFile(this, "socket_info.json");
        dispatcher.onMessage(json);
        assertEquals(true, receiver.socketStart.getName().equals(SocketStartEvent.TYPE));
        assertNotNull(receiver.socketStart.getEventId());
        assertNotNull(receiver.socketStart.getSocketId());
        assertNotNull(receiver.socketStart.toString());
    }

    @Test
    public void dispatchParticipantAdded() throws IOException {

        String json = ResponseTestHelper.readFromFile(this, "participant_added.json");
        dispatcher.onMessage(json);
        assertNotNull(receiver.participantAdded.getEventId());
        assertEquals(true, receiver.participantAdded.getName().equals(ParticipantAddedEvent.TYPE));
        assertNotNull(receiver.participantAdded.getProfileId());
        assertEquals("owner", receiver.participantAdded.getRole());
        assertNotNull(receiver.participantAdded.toString());
    }

    @Test
    public void dispatchParticipantUpdated() throws IOException {

        String json = ResponseTestHelper.readFromFile(this, "participant_updated.json");
        dispatcher.onMessage(json);
        assertNotNull(receiver.participantUpdated.getEventId());
        assertEquals(true, receiver.participantUpdated.getName().equals(ParticipantUpdatedEvent.TYPE));
        assertNotNull(receiver.participantUpdated.getProfileId());
        assertEquals("owner", receiver.participantUpdated.getRole());
        assertNotNull(receiver.participantUpdated.toString());
    }

    @Test
    public void dispatchParticipantRemoved() throws IOException {

        String json = ResponseTestHelper.readFromFile(this, "participant_removed.json");
        dispatcher.onMessage(json);
        assertNotNull(receiver.participantRemoved.getEventId());
        assertEquals(true, receiver.participantRemoved.getName().equals(ParticipantRemovedEvent.TYPE));
        assertNotNull(receiver.participantRemoved.getProfileId());
        assertNotNull(receiver.participantRemoved.toString());
    }

    @Test
    public void dispatchConversationUpdated() throws IOException {

        String json = ResponseTestHelper.readFromFile(this, "conversation_update.json");
        dispatcher.onMessage(json);
        assertNotNull(receiver.conversationUpdate.getEventId());
        assertEquals("myNewConversation", receiver.conversationUpdate.getConversationName());
        assertEquals(true, receiver.conversationUpdate.getName().equals(ConversationUpdateEvent.TYPE));
        assertNotNull(receiver.conversationUpdate.getConversationId());
        assertEquals("myConversationDescription", receiver.conversationUpdate.getDescription());
        assertTrue(receiver.conversationUpdate.getRoles().getOwner().getCanAddParticipants());
        assertTrue(receiver.conversationUpdate.getRoles().getOwner().getCanSend());
        assertTrue(receiver.conversationUpdate.getRoles().getOwner().getCanRemoveParticipants());
        assertTrue(receiver.conversationUpdate.getRoles().getParticipant().getCanAddParticipants());
        assertTrue(receiver.conversationUpdate.getRoles().getParticipant().getCanSend());
        assertTrue(receiver.conversationUpdate.getRoles().getParticipant().getCanRemoveParticipants());
        assertNotNull(receiver.conversationUpdate.toString());
    }

    @Test
    public void dispatchConversationDeleted() throws IOException {

        String json = ResponseTestHelper.readFromFile(this, "conversation_delete.json");
        dispatcher.onMessage(json);
        assertNotNull(receiver.conversationDelete.getEventId());
        assertEquals(true, receiver.conversationDelete.getName().equals(ConversationDeleteEvent.TYPE));
        assertNotNull(receiver.conversationDelete.getDeletedOn());
        assertNotNull(receiver.conversationDelete.toString());
    }

    @Test
    public void dispatchConversationUnDeleted() throws IOException {

        String json = ResponseTestHelper.readFromFile(this, "conversation_undelete.json");
        dispatcher.onMessage(json);
        assertNotNull(receiver.conversationUndelete.getEventId());
        assertEquals(true, receiver.conversationUndelete.getName().equals(ConversationUndeleteEvent.TYPE));
        assertNotNull(receiver.conversationUndelete.getConversation().getId());
        assertNotNull(receiver.conversationUndelete.getConversation().getDescription());
        assertNotNull(receiver.conversationUndelete.getName());
        assertNotNull(receiver.conversationUndelete.getConversation().getRoles());
        assertNotNull(receiver.conversationUndelete.toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void dispatchProfileUpdate() throws IOException {

        String json = ResponseTestHelper.readFromFile(this, "profile_update.json");
        dispatcher.onMessage(json);
        assertEquals(true, receiver.profileUpdate.getName().equals(ProfileUpdateEvent.TYPE));
        assertNotNull(receiver.profileUpdate.getApiSpaceId());
        assertNotNull(receiver.profileUpdate.toString());
        assertNotNull(receiver.profileUpdate.getPublishedOn());
        assertNotNull(receiver.profileUpdate.getApiSpaceId());
        assertEquals("userABC", receiver.profileUpdate.getProfileId());
        assertEquals("user123", receiver.profileUpdate.getCreatedBy());
        assertEquals(2, receiver.profileUpdate.getRevision());
        assertEquals("John", receiver.profileUpdate.getPayload().get("forename"));
        assertEquals("Doe", receiver.profileUpdate.getPayload().get("surname"));
        assertEquals("0", ((Map<String, String>) receiver.profileUpdate.getPayload().get("address")).get("number"));
        assertEquals("postcode", ((Map<String, String>) receiver.profileUpdate.getPayload().get("address")).get("postcode"));
    }

    @Test
    public void dispatchSessionStart() throws IOException {
        receiver.onSessionStart(new Session(null));
        assertNotNull(receiver.sessionStart);
    }

    @After
    public void tearDown() throws Exception {
        receiver.removeListener(messagingListener);
        receiver.removeListener(profileListener);
        receiver.removeListener(stateListener);
    }

    public class Receiver extends ListenerListAdapter {

        SocketStartEvent socketStart;

        MessageSentEvent sent;

        MessageDeliveredEvent delivered;

        MessageReadEvent read;

        ParticipantAddedEvent participantAdded;

        ParticipantUpdatedEvent participantUpdated;

        ParticipantRemovedEvent participantRemoved;

        ConversationUpdateEvent conversationUpdate;

        ConversationDeleteEvent conversationDelete;

        ConversationUndeleteEvent conversationUndelete;

        ProfileUpdateEvent profileUpdate;

        Session sessionStart;

        ParticipantTypingEvent isTyping;

        ParticipantTypingOffEvent isNotTyping;

        /**
         * Recomended constructor.
         */
        public Receiver(Logger log) {
            super(log);
        }

        @Override
        public void onMessageSent(MessageSentEvent event) {
            super.onMessageSent(event);
            sent = event;
        }

        @Override
        public void onMessageDelivered(MessageDeliveredEvent event) {
            super.onMessageDelivered(event);
            delivered = event;
        }

        @Override
        public void onMessageRead(MessageReadEvent event) {
            super.onMessageRead(event);
            read = event;
        }

        @Override
        public void onSocketStarted(SocketStartEvent event) {
            super.onSocketStarted(event);
            socketStart = event;
        }

        @Override
        public void onParticipantAdded(ParticipantAddedEvent event) {
            super.onParticipantAdded(event);
            participantAdded = event;
        }

        @Override
        public void onParticipantUpdated(ParticipantUpdatedEvent event) {
            super.onParticipantUpdated(event);
            participantUpdated = event;
        }

        @Override
        public void onParticipantRemoved(ParticipantRemovedEvent event) {
            super.onParticipantRemoved(event);
            participantRemoved = event;
        }

        @Override
        public void onConversationUpdated(ConversationUpdateEvent event) {
            super.onConversationUpdated(event);
            conversationUpdate = event;
        }

        @Override
        public void onConversationDeleted(ConversationDeleteEvent event) {
            super.onConversationDeleted(event);
            conversationDelete = event;
        }

        @Override
        public void onConversationUndeleted(ConversationUndeleteEvent event) {
            super.onConversationUndeleted(event);
            conversationUndelete = event;
        }

        @Override
        public void onProfileUpdate(ProfileUpdateEvent event) {
            super.onProfileUpdate(event);
            profileUpdate = event;
        }

        @Override
        public void onSessionStart(Session session) {
            super.onSessionStart(session);
            sessionStart = session;
        }

        @Override
        public void onParticipantIsTyping(ParticipantTypingEvent event) {
            super.onParticipantIsTyping(event);
            isTyping = event;
        }

        @Override
        public void onParticipantTypingOff(ParticipantTypingOffEvent event) {
            super.onParticipantTypingOff(event);
            isNotTyping = event;
        }
    }
}