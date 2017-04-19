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

package com.comapi.internal.network;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.comapi.APIConfig;
import com.comapi.BuildConfig;
import com.comapi.Callback;
import com.comapi.GlobalState;
import com.comapi.QueryBuilder;
import com.comapi.Session;
import com.comapi.StateListener;
import com.comapi.helpers.DataTestHelper;
import com.comapi.helpers.MockCallback;
import com.comapi.helpers.ResponseTestHelper;
import com.comapi.internal.CallbackAdapter;
import com.comapi.internal.ComapiException;
import com.comapi.internal.data.DataManager;
import com.comapi.internal.data.SessionData;
import com.comapi.internal.log.LogLevel;
import com.comapi.internal.log.LogManager;
import com.comapi.internal.log.Logger;
import com.comapi.internal.network.api.RestApi;
import com.comapi.internal.network.model.conversation.ConversationDetails;
import com.comapi.internal.network.model.conversation.Participant;
import com.comapi.internal.network.model.conversation.Scope;
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
import com.comapi.internal.network.model.messaging.Alert;
import com.comapi.internal.network.model.messaging.EventsQueryResponse;
import com.comapi.internal.network.model.messaging.MessageSentResponse;
import com.comapi.internal.network.model.messaging.MessageStatus;
import com.comapi.internal.network.model.messaging.MessageToSend;
import com.comapi.internal.network.model.messaging.MessagesQueryResponse;
import com.comapi.internal.network.model.messaging.Part;
import com.comapi.internal.network.sockets.SocketController;
import com.comapi.internal.network.sockets.SocketEventListener;
import com.comapi.internal.push.PushManager;
import com.comapi.mock.MockAuthenticator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static com.comapi.helpers.DataTestHelper.API_SPACE_ID;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.robolectric.RuntimeEnvironment.application;

/**
 * Tests for service APIs version with callbacks in InternalService class.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(manifest = "foundation/src/main/AndroidManifest.xml", sdk = Build.VERSION_CODES.M, constants = BuildConfig.class, packageName = "com.comapi")
public class ServiceCallbackTest {

    private static final long TIME_OUT = 10000;

    private MockWebServer server;

    private InternalService service;

    private MockAuthenticator authenticator;

    private AtomicInteger comapiState;

    private AtomicBoolean isCreateSessionInProgress;

    private SessionController sessionController;

    private String apiSpace;
    private DataManager dataMgr;
    private PushManager pushMgr;
    private RestApi restApi;

    private MockCallback listener;

    private CallbackAdapter callbackAdapter;
    private APIConfig apiConfig;
    private APIConfig.BaseURIs baseURIs;
    private Logger log;

    private static final int LIMIT = 1000;

    @Before
    public void setUp() throws Exception {

        server = new MockWebServer();
        server.start();
        apiConfig = new APIConfig().service(server.url("/").toString()).socket("ws://10.0.0.0");

        DataTestHelper.saveDeviceData();
        DataTestHelper.saveSessionData();

        comapiState = new AtomicInteger(GlobalState.INITIALISED);
        authenticator = new MockAuthenticator();

        LogManager logMgr = new LogManager();
        logMgr.init(application, LogLevel.DEBUG.getValue(), LogLevel.OFF.getValue(), LIMIT);
        log = new Logger(new LogManager(), "");
        dataMgr = new DataManager();
        dataMgr.init(application, API_SPACE_ID, new Logger(new LogManager(), ""));

        baseURIs = APIConfig.BaseURIs.build(apiConfig, API_SPACE_ID, log);

        pushMgr = new PushManager();
        pushMgr.init(application.getApplicationContext(), new Handler(Looper.getMainLooper()), log, () -> "fcm-token", token -> {
            log.d("Refreshed push token is " + token);
            if (!TextUtils.isEmpty(token)) {
                dataMgr.getDeviceDAO().setPushToken(token);
            }
        }, null);

        callbackAdapter = new CallbackAdapter() {
            public <T> void adapt(@NonNull final Observable<T> subscriber, @Nullable final Callback<T> callback) {
                subscriber.subscribeOn(Schedulers.io())
                        .subscribe(new Subscriber<T>() {

                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                if (callback != null) {
                                    callback.error(e);
                                }
                            }

                            @Override
                            public void onNext(T result) {
                                if (callback != null) {
                                    callback.success(result);
                                }
                            }
                        });
            }
        };

        service = new InternalService(application, callbackAdapter, dataMgr, pushMgr, API_SPACE_ID, "packageName", log);

        restApi = service.initialiseRestClient(LogLevel.DEBUG.getValue(), baseURIs);

        isCreateSessionInProgress = new AtomicBoolean();
        sessionController = service.initialiseSessionController(application, new SessionCreateManager(isCreateSessionInProgress), pushMgr, comapiState, authenticator, restApi, new Handler(Looper.getMainLooper()), new StateListener() {
        });
        sessionController.setSocketController(new SocketController(dataMgr, new SocketEventListener() {
            @Override
            public void onMessageSent(MessageSentEvent event) {

            }

            @Override
            public void onMessageDelivered(MessageDeliveredEvent event) {

            }

            @Override
            public void onMessageRead(MessageReadEvent event) {

            }

            @Override
            public void onSocketStarted(SocketStartEvent event) {

            }

            @Override
            public void onParticipantAdded(ParticipantAddedEvent event) {

            }

            @Override
            public void onParticipantUpdated(ParticipantUpdatedEvent event) {

            }

            @Override
            public void onParticipantRemoved(ParticipantRemovedEvent event) {

            }

            @Override
            public void onConversationUpdated(ConversationUpdateEvent event) {

            }

            @Override
            public void onConversationDeleted(ConversationDeleteEvent event) {

            }

            @Override
            public void onConversationUndeleted(ConversationUndeleteEvent event) {

            }

            @Override
            public void onProfileUpdate(ProfileUpdateEvent event) {

            }

            @Override
            public void onParticipantIsTyping(ParticipantTypingEvent event) {

            }

            @Override
            public void onParticipantTypingOff(ParticipantTypingOffEvent event) {

            }
        }, log, new URI("ws://auth"), null));
    }

    @Test(expected = ComapiException.class)
    public void initialiseSessionController_wrongURI() {
        APIConfig apiConfig = new APIConfig().service(server.url("/").toString()).socket("@@@@@");
        APIConfig.BaseURIs.build(apiConfig, API_SPACE_ID, log);
    }

    @Test
    public void createSession() throws Exception {

        DataTestHelper.clearSessionData();

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_create.json", 200).addHeader("ETag", "eTag"));
        MockResponse mr = new MockResponse().setResponseCode(200);
        server.enqueue(mr);

        final MockCallback<Session> listener = new MockCallback<>();

        service.startSession(listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertNotNull(listener.getResult().getProfileId());
        assertTrue(listener.getResult().isSuccessfullyCreated());
        assertNull(listener.getError());
    }

    @Test
    public void endSession() throws Exception {

        MockResponse mr = new MockResponse();
        mr.setResponseCode(204);
        server.enqueue(mr);

        final MockCallback<ComapiResult<Void>> listener = new MockCallback<>();

        service.endSession(listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertNotNull(listener.getResult());
        assertEquals(true, listener.getResult().isSuccessful());
        assertEquals(204, listener.getResult().getCode());
        assertNull(listener.getResult().getResult());
        assertNull(listener.getError());
    }

    @Test
    public void getProfile() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_profile_get.json", 200).addHeader("ETag", "eTag"));

        final MockCallback<ComapiResult<Map<String, Object>>> listener = new MockCallback<>();

        service.getProfile("profileId", listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertEquals(true, listener.getResult().isSuccessful());
        assertEquals(200, listener.getResult().getCode());
        assertNotNull(listener.getResult().getResult().get("id"));
        assertNotNull(listener.getResult().getETag());
    }

    @Test
    public void getProfile_unauthorised_retry3times_shouldFail() throws Exception {

        sessionController = new SessionController(application, new SessionCreateManager(isCreateSessionInProgress), pushMgr, comapiState, dataMgr, authenticator, restApi, "", new Handler(Looper.getMainLooper()), new Logger(new LogManager(), ""), null, new StateListener() {
        }) {
            @Override
            protected Observable<SessionData> reAuthenticate() {
                return Observable.just(new SessionData().setAccessToken(UUID.randomUUID().toString()).setExpiresOn(Long.MAX_VALUE).setProfileId("id").setSessionId("id"));
            }
        };

        comapiState.set(GlobalState.SESSION_ACTIVE);
        isCreateSessionInProgress.set(false);

        // Go through all 3 retries
        MockResponse mr = new MockResponse();
        mr.setResponseCode(401);
        server.enqueue(mr);
        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_create.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(new MockResponse().setResponseCode(200));
        server.enqueue(mr);
        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_create.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(new MockResponse().setResponseCode(200));
        server.enqueue(mr);
        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_create.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(new MockResponse().setResponseCode(200));
        server.enqueue(mr);

        final MockCallback<ComapiResult<Map<String, Object>>> listener = new MockCallback<>();

        service.getProfile("profileId", listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertEquals(false, listener.getResult().isSuccessful());
        assertEquals(401, listener.getResult().getCode());
        assertNull(listener.getError());
    }

    @Test
    public void queryProfile() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_profile_query.json", 200).addHeader("ETag", "eTag"));

        List<String> list = new ArrayList<>();
        list.add("");

        String query = new QueryBuilder()
                .addContains("", "")
                .addEndsWith("", "")
                .addEqual("", "")
                .addExists("")
                .addGreaterOrEqualThan("", "")
                .addLessOrEqualThan("", "")
                .addLessThan("", "")
                .addGreaterThan("", "")
                .addNotExists("")
                .addStartsWith("", "")
                .addUnequal("", "")
                .inArray("", list)
                .notInArray("", list)
                .build();

        assertNotNull(query);

        final MockCallback<ComapiResult<List<Map<String, Object>>>> listener = new MockCallback<>();

        service.queryProfiles(query, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertEquals(true, listener.getResult().isSuccessful());
        assertEquals(200, listener.getResult().getCode());
        assertNotNull(listener.getResult().getResult().get(0).get("id"));
        assertNotNull(listener.getResult().getETag());
    }

    @Test
    public void updateProfile() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_profile_update.json", 200).addHeader("ETag", "eTag"));

        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        map.put("key2", 312);

        final MockCallback<ComapiResult<Map<String, Object>>> listener = new MockCallback<>();

        //TODO NOT WORKING ETAG null
        service.updateProfile(map, "eTag", listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertEquals(true, listener.getResult().isSuccessful());
        assertEquals(200, listener.getResult().getCode());
        assertNotNull(listener.getResult().getResult().get("id"));
        assertNotNull(listener.getResult().getETag());
    }

    @Test
    public void createConversation() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_conversation_create.json", 201).addHeader("ETag", "eTag"));

        com.comapi.internal.network.model.conversation.ConversationCreate conversation = com.comapi.internal.network.model.conversation.ConversationCreate.builder()
                .setPublic(false).build();

        final MockCallback<ComapiResult<ConversationDetails>> listener = new MockCallback<>();

        service.createConversation(conversation, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertEquals(true, listener.getResult().isSuccessful());
        assertEquals(201, listener.getResult().getCode());
        assertNotNull(listener.getResult().getResult().getDescription());
        assertNotNull(listener.getResult().getResult().getId());
        assertNotNull(listener.getResult().getResult().getName());
        assertNotNull(listener.getResult().getResult().getRoles().getOwner());
        assertNotNull(listener.getResult().getResult().getRoles().getParticipant());
        assertEquals(true, listener.getResult().getResult().getRoles().getParticipant().getCanAddParticipants().booleanValue());
        assertEquals(true, listener.getResult().getResult().getRoles().getParticipant().getCanRemoveParticipants().booleanValue());
        assertEquals(true, listener.getResult().getResult().getRoles().getParticipant().getCanSend().booleanValue());
        assertEquals(true, listener.getResult().getResult().getRoles().getOwner().getCanAddParticipants().booleanValue());
        assertEquals(true, listener.getResult().getResult().getRoles().getOwner().getCanRemoveParticipants().booleanValue());
        assertEquals(true, listener.getResult().getResult().getRoles().getOwner().getCanSend().booleanValue());
        assertNotNull(listener.getResult().getETag());
    }

    @Test
    public void getConversation() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_conversation_get.json", 200).addHeader("ETag", "eTag"));

        final MockCallback<ComapiResult<ConversationDetails>> listener = new MockCallback<>();

        service.getConversation("someId", listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertEquals(true, listener.getResult().isSuccessful());
        assertEquals(200, listener.getResult().getCode());
        assertNotNull(listener.getResult().getResult().getDescription());
        assertNotNull(listener.getResult().getResult().getId());
        assertNotNull(listener.getResult().getResult().getName());
        assertNotNull(listener.getResult().getResult().getRoles().getOwner());
        assertNotNull(listener.getResult().getResult().getRoles().getParticipant());
        assertEquals(true, listener.getResult().getResult().getRoles().getParticipant().getCanAddParticipants().booleanValue());
        assertEquals(true, listener.getResult().getResult().getRoles().getParticipant().getCanRemoveParticipants().booleanValue());
        assertEquals(true, listener.getResult().getResult().getRoles().getParticipant().getCanSend().booleanValue());
        assertEquals(true, listener.getResult().getResult().getRoles().getOwner().getCanAddParticipants().booleanValue());
        assertEquals(true, listener.getResult().getResult().getRoles().getOwner().getCanRemoveParticipants().booleanValue());
        assertEquals(true, listener.getResult().getResult().getRoles().getOwner().getCanSend().booleanValue());
        assertNotNull(listener.getResult().getETag());

    }

    @Test
    public void getConversations() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_conversations_get.json", 200).addHeader("ETag", "eTag"));

        final MockCallback<ComapiResult<List<ConversationDetails>>> listener = new MockCallback<>();

        service.getConversations(Scope.PARTICIPANT, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertEquals(true, listener.getResult().isSuccessful());
        assertEquals(200, listener.getResult().getCode());
        assertNotNull(listener.getResult().getResult().get(0).getDescription());
        assertNotNull(listener.getResult().getResult().get(0).getId());
        assertNotNull(listener.getResult().getResult().get(0).getName());
        assertNotNull(listener.getResult().getResult().get(0).getRoles().getOwner());
        assertNotNull(listener.getResult().getResult().get(0).getRoles().getParticipant());
        assertEquals(true, listener.getResult().getResult().get(0).getRoles().getParticipant().getCanAddParticipants().booleanValue());
        assertEquals(true, listener.getResult().getResult().get(0).getRoles().getParticipant().getCanRemoveParticipants().booleanValue());
        assertEquals(true, listener.getResult().getResult().get(0).getRoles().getParticipant().getCanSend().booleanValue());
        assertEquals(true, listener.getResult().getResult().get(0).getRoles().getOwner().getCanAddParticipants().booleanValue());
        assertEquals(true, listener.getResult().getResult().get(0).getRoles().getOwner().getCanRemoveParticipants().booleanValue());
        assertEquals(true, listener.getResult().getResult().get(0).getRoles().getOwner().getCanSend().booleanValue());
        assertNotNull(listener.getResult().getETag());
    }

    @Test
    public void updateConversation() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_conversation_update.json", 200).addHeader("ETag", "eTag"));

        com.comapi.internal.network.model.conversation.ConversationUpdate conversation = com.comapi.internal.network.model.conversation.ConversationUpdate.builder()
                .setPublic(false).build();

        final MockCallback<ComapiResult<ConversationDetails>> listener = new MockCallback<>();

        service.updateConversation("someId", conversation, "eTag", listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertEquals(true, listener.getResult().isSuccessful());
        assertEquals(200, listener.getResult().getCode());
        assertNotNull(listener.getResult().getResult().getDescription());
        assertNotNull(listener.getResult().getResult().getId());
        assertNotNull(listener.getResult().getResult().getName());
        assertNotNull(listener.getResult().getResult().getRoles().getOwner());
        assertNotNull(listener.getResult().getResult().getRoles().getParticipant());
        assertEquals(true, listener.getResult().getResult().getRoles().getParticipant().getCanAddParticipants().booleanValue());
        assertEquals(true, listener.getResult().getResult().getRoles().getParticipant().getCanRemoveParticipants().booleanValue());
        assertEquals(true, listener.getResult().getResult().getRoles().getParticipant().getCanSend().booleanValue());
        assertEquals(true, listener.getResult().getResult().getRoles().getOwner().getCanAddParticipants().booleanValue());
        assertEquals(true, listener.getResult().getResult().getRoles().getOwner().getCanRemoveParticipants().booleanValue());
        assertEquals(true, listener.getResult().getResult().getRoles().getOwner().getCanSend().booleanValue());
        assertNotNull(listener.getResult().getETag());
    }

    @Test
    public void deleteConversation() throws Exception {

        MockResponse mr = new MockResponse();
        mr.setResponseCode(204);
        mr.addHeader("ETag", "eTag");
        server.enqueue(mr);

        final MockCallback<ComapiResult<Void>> listener = new MockCallback<>();

        service.deleteConversation("someId", "eTag", listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertEquals(true, listener.getResult().isSuccessful());
        assertEquals(204, listener.getResult().getCode());
        assertNotNull(listener.getResult().getETag());
    }

    @Test
    public void removeParticipants() throws Exception {

        MockResponse mr = new MockResponse();
        mr.setResponseCode(204);
        mr.addHeader("ETag", "eTag");
        server.enqueue(mr);

        List<String> participants = new ArrayList<>();
        participants.add("pA");
        participants.add("pB");

        final MockCallback<ComapiResult<Void>> listener = new MockCallback<>();

        service.removeParticipants("someId", participants, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertEquals(true, listener.getResult().isSuccessful());
        assertEquals(204, listener.getResult().getCode());
        assertNotNull(listener.getResult().getETag());
    }

    @Test
    public void addParticipants() throws Exception {

        MockResponse mr = new MockResponse();
        mr.setResponseCode(201);
        mr.addHeader("ETag", "eTag");
        server.enqueue(mr);

        List<Participant> participants = new ArrayList<>();
        participants.add(Participant.builder().setId("someId1").setIsParticipant().build());
        participants.add(Participant.builder().setId("someId2").setIsParticipant().build());

        final MockCallback<ComapiResult<Void>> listener = new MockCallback<>();

        service.addParticipants("someId", participants, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertEquals(true, listener.getResult().isSuccessful());
        assertEquals(201, listener.getResult().getCode());
        assertNotNull(listener.getResult().getETag());
    }

    @Test
    public void getParticipants() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_participants_get.json", 200).addHeader("ETag", "eTag"));

        final MockCallback<ComapiResult<List<Participant>>> listener = new MockCallback<>();

        service.getParticipants("someId", listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertEquals(true, listener.getResult().isSuccessful());
        assertEquals(200, listener.getResult().getCode());
        assertNotNull(listener.getResult().getETag());
        assertNotNull(listener.getResult().getResult().get(0).getId());
        assertNotNull(listener.getResult().getResult().get(0).getRole());
        assertNotNull(listener.getResult().getResult().get(1).getId());
        assertNotNull(listener.getResult().getResult().get(1).getRole());
        assertNotNull(listener.getResult().getResult().get(2).getId());
        assertNotNull(listener.getResult().getResult().get(2).getRole());
    }

    @Test
    public void sendMessage() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_message_sent.json", 200).addHeader("ETag", "eTag"));

        Map<String, Object> map = new HashMap<>();
        map.put("keyA", "valueA");

        Map<String, String> mapFcmData = new HashMap<>();
        mapFcmData.put("keyB", "valueB");

        MessageToSend msg = MessageToSend.builder()
                .setAlert(Alert.fcmPushBuilder().putData(mapFcmData).putNotification("title", "message").build(), new HashMap<>())
                .setMetadata(map)
                .addPart(Part.builder().setData("data").setName("name").setSize(81209).setType("type").setUrl("url").build()).build();

        final MockCallback<ComapiResult<MessageSentResponse>> listener = new MockCallback<>();

        service.sendMessage("someId", msg, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertEquals(true, listener.getResult().isSuccessful());
        assertEquals(200, listener.getResult().getCode());
        assertNotNull(listener.getResult().getETag());
        assertNotNull(listener.getResult().getResult().getId());
    }

    @Test
    public void sendMessage_simpleVersion() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_message_sent.json", 200).addHeader("ETag", "eTag"));

        final MockCallback<ComapiResult<MessageSentResponse>> listener = new MockCallback<>();

        service.sendMessage("someId", "body", listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertEquals(true, listener.getResult().isSuccessful());
        assertEquals(200, listener.getResult().getCode());
        assertNotNull(listener.getResult().getETag());
        assertNotNull(listener.getResult().getResult().getId());
    }

    @Test
    public void updateMessageStatus() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_message_sent.json", 200).addHeader("ETag", "eTag"));

        List<com.comapi.internal.network.model.messaging.MessageStatusUpdate> update = new ArrayList<>();
        update.add(com.comapi.internal.network.model.messaging.MessageStatusUpdate.builder().setStatus(MessageStatus.read).addMessageId("someId").setTimestamp("time").build());
        update.add(com.comapi.internal.network.model.messaging.MessageStatusUpdate.builder().setStatus(MessageStatus.delivered).addMessageId("someId").setTimestamp("time").build());

        final MockCallback<ComapiResult<Void>> listener = new MockCallback<>();

        service.updateMessageStatus("someId", update, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertEquals(true, listener.getResult().isSuccessful());
        assertEquals(200, listener.getResult().getCode());
        assertNotNull(listener.getResult().getETag());
    }

    @Test
    public void queryEvents() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_events_query.json", 200).addHeader("ETag", "eTag"));

        final MockCallback<ComapiResult<EventsQueryResponse>> listener = new MockCallback<>();

        service.queryEvents("someId", 0L, 100, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertEquals(true, listener.getResult().isSuccessful());
        assertEquals(200, listener.getResult().getCode());
        assertNotNull(listener.getResult().getETag());

        assertNotNull(listener.getResult().getResult().getMessageRead().get(0).getEventId());
        assertNotNull(listener.getResult().getResult().getMessageRead().get(0).getName());
        assertNotNull(listener.getResult().getResult().getMessageRead().get(0).getConversationEventId());
        assertNotNull(listener.getResult().getResult().getMessageRead().get(0).getMessageId());
        assertNotNull(listener.getResult().getResult().getMessageRead().get(0).getConversationId());
        assertNotNull(listener.getResult().getResult().getMessageRead().get(0).getProfileId());
        assertNotNull(listener.getResult().getResult().getMessageRead().get(0).getTimestamp());

        assertNotNull(listener.getResult().getResult().getMessageDelivered().get(0).getEventId());
        assertNotNull(listener.getResult().getResult().getMessageDelivered().get(0).getName());
        assertNotNull(listener.getResult().getResult().getMessageDelivered().get(0).getConversationEventId());
        assertNotNull(listener.getResult().getResult().getMessageDelivered().get(0).getMessageId());
        assertNotNull(listener.getResult().getResult().getMessageDelivered().get(0).getConversationId());
        assertNotNull(listener.getResult().getResult().getMessageDelivered().get(0).getProfileId());
        assertNotNull(listener.getResult().getResult().getMessageDelivered().get(0).getTimestamp());

        assertNotNull(listener.getResult().getResult().getMessageSent().get(0).getEventId());
        assertNotNull(listener.getResult().getResult().getMessageSent().get(0).getName());
        assertNotNull(listener.getResult().getResult().getMessageSent().get(0).getConversationEventId());
        assertNotNull(listener.getResult().getResult().getMessageSent().get(0).getMessageId());
        assertNotNull(listener.getResult().getResult().getMessageSent().get(0).getAlert().getPlatforms().getFcm().get("notification"));
        assertNotNull(listener.getResult().getResult().getMessageSent().get(0).getAlert().getPlatforms().getFcm().get("data"));
        assertNotNull(listener.getResult().getResult().getMessageSent().get(0).getContext().getConversationId());
        assertNotNull(listener.getResult().getResult().getMessageSent().get(0).getContext().getSentBy());
        assertNotNull(listener.getResult().getResult().getMessageSent().get(0).getContext().getSentOn());
        assertNotNull(listener.getResult().getResult().getMessageSent().get(0).getContext().getFromWhom().getId());
        assertNotNull(listener.getResult().getResult().getMessageSent().get(0).getContext().getFromWhom().getName());
        assertNotNull(listener.getResult().getResult().getMessageSent().get(0).getContext().getFromWhom().getName());
        assertNotNull(listener.getResult().getResult().getMessageSent().get(0).getMetadata().get("key"));

        assertNotNull(listener.getResult().getResult().getConversationDelete().get(0));

        assertNotNull(listener.getResult().getResult().getConversationUnDelete().get(0));

        assertNotNull(listener.getResult().getResult().getConversationUpdate().get(0));

        assertNotNull(listener.getResult().getResult().getParticipantAdded().get(0));

        assertNotNull(listener.getResult().getResult().getParticipantRemoved().get(0));

        assertNotNull(listener.getResult().getResult().getParticipantUpdate().get(0));

        assertEquals(9, listener.getResult().getResult().getCombinedSize());
    }

    @Test
    public void queryMessages() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_message_query.json", 200).addHeader("ETag", "eTag"));

        final MockCallback<ComapiResult<MessagesQueryResponse>> listener = new MockCallback<>();

        service.queryMessages("someId", 0L, 100, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertEquals(true, listener.getResult().isSuccessful());
        assertEquals(200, listener.getResult().getCode());
        assertNotNull(listener.getResult().getETag());

        assertEquals(0, listener.getResult().getResult().getEarliestEventId());
        assertEquals(true, listener.getResult().getResult().getLatestEventId() > 0);

        assertNotNull(listener.getResult().getResult().getMessages().get(0).getMessageId());
        assertNotNull(listener.getResult().getResult().getMessages().get(0).getConversationId());
        assertNotNull(listener.getResult().getResult().getMessages().get(0).getFromWhom().getId());
        assertNotNull(listener.getResult().getResult().getMessages().get(0).getFromWhom().getName());
        assertNotNull(listener.getResult().getResult().getMessages().get(0).getSentOn());
        assertNotNull(listener.getResult().getResult().getMessages().get(0).getSentBy());
        assertNotNull(listener.getResult().getResult().getMessages().get(0).getMetadata().get("key"));
        assertNotNull(listener.getResult().getResult().getMessages().get(0).getStatusUpdate().get("userB").getStatus());
        assertNotNull(listener.getResult().getResult().getMessages().get(0).getStatusUpdate().get("userB").getTimestamp());
        assertNotNull(listener.getResult().getResult().getMessages().get(0).getParts().get(0).getName());
        assertEquals(true, listener.getResult().getResult().getMessages().get(0).getParts().get(0).getSize() > 0);
        assertNotNull(listener.getResult().getResult().getMessages().get(0).getParts().get(0).getType());
        assertNotNull(listener.getResult().getResult().getMessages().get(0).getParts().get(0).getData());
        assertNotNull(listener.getResult().getResult().getOrphanedEvents());
        assertNotNull(listener.getResult().getResult().getOrphanedEvents());

        assertNotNull(listener.getResult().getResult().getMessages().get(1).getParts().get(0).getUrl());
    }

    @Test
    public void isTyping() throws InterruptedException {

        server.enqueue(new MockResponse().setResponseCode(200));

        final MockCallback<ComapiResult<Void>> listener = new MockCallback<>();

        service.isTyping("conversationId", listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }
        assertEquals(true, listener.getResult().isSuccessful());
        assertEquals(200, listener.getResult().getCode());
    }

    @Test
    public void isNotTyping() throws InterruptedException {

        server.enqueue(new MockResponse().setResponseCode(200));

        final MockCallback<ComapiResult<Void>> listener = new MockCallback<>();

        service.isTyping("conversationId", false, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }
        assertEquals(true, listener.getResult().isSuccessful());
        assertEquals(200, listener.getResult().getCode());
    }

    @Test
    public void createFbOptIn() throws Exception {

        final String testResp = "data";

        server.enqueue(new MockResponse().setResponseCode(200).setBody(testResp));

        final MockCallback<ComapiResult<String>> listener = new MockCallback<>();

        service.createFbOptInState(listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertEquals(true, listener.getResult().getResult().equals(testResp));
    }

    @After
    public void tearDown() throws Exception {
        DataTestHelper.clearDeviceData();
        DataTestHelper.clearSessionData();
        server.shutdown();
        pushMgr.unregisterPushReceiver(RuntimeEnvironment.application);
    }
}
