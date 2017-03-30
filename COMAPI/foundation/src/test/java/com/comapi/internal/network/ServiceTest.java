package com.comapi.internal.network;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.comapi.APIConfig;
import com.comapi.BuildConfig;
import com.comapi.GlobalState;
import com.comapi.QueryBuilder;
import com.comapi.StateListener;
import com.comapi.helpers.DataTestHelper;
import com.comapi.helpers.ResponseTestHelper;
import com.comapi.internal.CallbackAdapter;
import com.comapi.internal.ComapiException;
import com.comapi.internal.data.DataManager;
import com.comapi.internal.data.SessionData;
import com.comapi.internal.log.LogLevel;
import com.comapi.internal.log.LogManager;
import com.comapi.internal.log.Logger;
import com.comapi.internal.network.api.RestApi;
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
import com.comapi.internal.network.model.events.conversation.ParticipantUpdatedEvent;
import com.comapi.internal.network.model.events.conversation.message.MessageDeliveredEvent;
import com.comapi.internal.network.model.events.conversation.message.MessageReadEvent;
import com.comapi.internal.network.model.events.conversation.message.MessageSentEvent;
import com.comapi.internal.network.model.messaging.Alert;
import com.comapi.internal.network.model.messaging.MessageStatus;
import com.comapi.internal.network.model.messaging.MessageToSend;
import com.comapi.internal.network.model.messaging.OrphanedEvent;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import rx.Observable;
import rx.Observer;

import static com.comapi.helpers.DataTestHelper.API_SPACE_ID;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.robolectric.RuntimeEnvironment.application;

/**
 * Robolectric for Network setup.
 *
 * @author Marcin Swierczek
 *         Copyright (C) Donky Networks Ltd. All rights reserved.
 * @version 1.0.0
 * @since 1.0.0
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(manifest = "foundation/src/main/AndroidManifest.xml", sdk = Build.VERSION_CODES.M, constants = BuildConfig.class, packageName = "com.comapi")
public class ServiceTest {

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
    private APIConfig.BaseURIs baseURIs;
    private APIConfig apiConfig;
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

        service = new InternalService(application, new CallbackAdapter(), dataMgr, pushMgr, API_SPACE_ID, "packageName", log);

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
        }, log, new URI("ws://auth"), null));
    }

    @Test(expected = ComapiException.class)
    public void initialiseSessionController_wrongURI() throws Exception {
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

        service.startSession().toBlocking().forEach(response -> {
            assertTrue("someProfileId".equals(response.getProfileId()));
            assertNotNull(response.getProfileId());
            assertTrue(response.isSuccessfullyCreated());
        });
    }

    @Test
    public void endSession() throws Exception {

        MockResponse mr = new MockResponse();
        mr.setResponseCode(204);
        server.enqueue(mr);

        service.endSession().toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(204, response.getCode());
            assertNull(response.getResult());
        });
    }

    @Test(expected = RuntimeException.class)
    public void endSession_sessionCreateInProgress_shouldFail() throws Exception {
        isCreateSessionInProgress.set(true);
        endSession();
    }

    @Test(expected = RuntimeException.class)
    public void endSession_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        endSession();
    }

    @Test
    public void getProfile() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_profile_get.json", 200).addHeader("ETag", "eTag"));

        service.getProfile("profileId").toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getResult().get("id"));
            assertNotNull(response.getETag());
        });

    }

    @Test
    public void getProfile_sessionCreateInProgress() throws Exception {

        isCreateSessionInProgress.set(true);
        service.getProfile("profileId").timeout(3, TimeUnit.SECONDS).subscribe();
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = RuntimeException.class)
    public void getProfile_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        getProfile();
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

        service.getProfile("profileId").toBlocking().forEach(response -> {
            assertEquals(false, response.isSuccessful());
            assertEquals(401, response.getCode());
        });
    }

    @Test
    public void getProfile_copyResult() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_profile_get.json", 200).addHeader("ETag", "eTag"));

        service.getProfile("profileId").toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getResult().get("id"));
            assertNotNull(response.getETag());

            ComapiResult<Map<String, Object>> newResponse = new ComapiResult<>(response);
            assertEquals(newResponse.isSuccessful(), response.isSuccessful());
            assertEquals(newResponse.getCode(), response.getCode());
            assertEquals(newResponse.getResult().get("id"), response.getResult().get("id"));
            assertEquals(newResponse.getETag(), response.getETag());

            Map map = new HashMap<String, Object>();
            map.put("key", "value");

            ComapiResult newResponse2 = new ComapiResult<>(response, "replacement");
            assertEquals(newResponse2.isSuccessful(), response.isSuccessful());
            assertEquals(newResponse2.getCode(), response.getCode());
            assertEquals("replacement", newResponse2.getResult());
            assertEquals(newResponse2.getETag(), response.getETag());
        });

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

        service.queryProfiles(query).toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getResult().get(0).get("id"));
            assertNotNull(response.getETag());
        });
    }

    @Test
    public void queryProfile_sessionCreateInProgress() throws Exception {
        isCreateSessionInProgress.set(true);
        service.queryProfiles("query").timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = RuntimeException.class)
    public void queryProfile_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        queryProfile();
    }

    @Test
    public void queryProfile_serverError() throws Exception {

        MockResponse response = new MockResponse();
        response.setResponseCode(500);
        response.setHttp2ErrorCode(500);
        response.setBody("{\n" +
                "  \"key\": \"value\"\n" +
                "}");
        server.enqueue(response);

        String query = new QueryBuilder()
                .build();

        service.queryProfiles(query).toBlocking().forEach(result -> {
            assertEquals(false, result.isSuccessful());
            assertNotNull(result.getErrorBody());
        });
    }

    @Test
    public void updateProfile() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_profile_update.json", 200).addHeader("ETag", "eTag"));

        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        map.put("key2", 312);

        service.updateProfile(map, "eTag").toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getResult().get("id"));
            assertNotNull(response.getETag());
        });
    }

    @Test
    public void updateProfile_sessionCreateInProgress() throws Exception {
        isCreateSessionInProgress.set(true);
        service.updateProfile(new HashMap<>(), null).timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = ComapiException.class)
    public void updateProfile_sessionCreateInProgress_noToken() throws Exception {
        DataTestHelper.clearSessionData();
        isCreateSessionInProgress.set(false);
        service.updateProfile(new HashMap<>(), null).timeout(3, TimeUnit.SECONDS).toBlocking().subscribe();
    }

    @Test(expected = RuntimeException.class)
    public void updateProfile_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        queryProfile();
    }

    @Test
    public void isTyping() {
        server.enqueue(new MockResponse().setResponseCode(200));
        service.isTyping("conversationId").toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
        });
    }

    @Test
    public void isTyping_sessionCreateInProgress() throws Exception {
        isCreateSessionInProgress.set(true);
        service.isTyping("conversationId").timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        // Not adding
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = ComapiException.class)
    public void isTyping_sessionCreateInProgress_noToken() throws Exception {
        DataTestHelper.clearSessionData();
        isCreateSessionInProgress.set(false);
        service.isTyping("conversationId").timeout(3, TimeUnit.SECONDS).toBlocking().subscribe();
    }

    @Test(expected = RuntimeException.class)
    public void isTyping_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        isTyping();
    }

    @Test
    public void createConversation() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_conversation_create.json", 201).addHeader("ETag", "eTag"));

        com.comapi.internal.network.model.conversation.ConversationCreate conversation = com.comapi.internal.network.model.conversation.ConversationCreate.builder()
                .setId("0")
                .setPublic(false).build();

        service.createConversation(conversation).toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(201, response.getCode());
            assertNotNull(response.getResult().getDescription());
            assertNotNull(response.getResult().getId());
            assertNotNull(response.getResult().getName());
            assertNotNull(response.getResult().getRoles().getOwner());
            assertNotNull(response.getResult().getRoles().getParticipant());
            assertEquals(true, response.getResult().getRoles().getParticipant().getCanAddParticipants().booleanValue());
            assertEquals(true, response.getResult().getRoles().getParticipant().getCanRemoveParticipants().booleanValue());
            assertEquals(true, response.getResult().getRoles().getParticipant().getCanSend().booleanValue());
            assertEquals(true, response.getResult().getRoles().getOwner().getCanAddParticipants().booleanValue());
            assertEquals(true, response.getResult().getRoles().getOwner().getCanRemoveParticipants().booleanValue());
            assertEquals(true, response.getResult().getRoles().getOwner().getCanSend().booleanValue());
            assertNotNull(response.getETag());
        });
    }

    @Test
    public void createConversation_sessionCreateInProgress() throws Exception {
        isCreateSessionInProgress.set(true);
        service.createConversation(com.comapi.internal.network.model.conversation.ConversationCreate.builder().build()).timeout(3, TimeUnit.SECONDS).toBlocking().subscribe(getEmptyObserver());
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = RuntimeException.class)
    public void createConversation_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        createConversation();
    }

    @Test
    public void getConversation() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_conversation_get.json", 200).addHeader("ETag", "eTag"));

        service.getConversation("someId").toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getResult().getDescription());
            assertNotNull(response.getResult().getId());
            assertNotNull(response.getResult().getName());
            assertNotNull(response.getResult().getRoles().getOwner());
            assertNotNull(response.getResult().getRoles().getParticipant());
            assertEquals(true, response.getResult().getRoles().getParticipant().getCanAddParticipants().booleanValue());
            assertEquals(true, response.getResult().getRoles().getParticipant().getCanRemoveParticipants().booleanValue());
            assertEquals(true, response.getResult().getRoles().getParticipant().getCanSend().booleanValue());
            assertEquals(true, response.getResult().getRoles().getOwner().getCanAddParticipants().booleanValue());
            assertEquals(true, response.getResult().getRoles().getOwner().getCanRemoveParticipants().booleanValue());
            assertEquals(true, response.getResult().getRoles().getOwner().getCanSend().booleanValue());
            assertNotNull(response.getETag());
        });
    }

    @Test
    public void getConversation_sessionCreateInProgress() throws Exception {
        isCreateSessionInProgress.set(true);
        service.getConversation("someId").timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = RuntimeException.class)
    public void getConversation_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        getConversation();
    }

    @Test
    public void getConversations() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_conversations_get.json", 200).addHeader("ETag", "eTag"));

        service.getConversations(Scope.PARTICIPANT).toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getResult().get(0).getDescription());
            assertNotNull(response.getResult().get(0).getId());
            assertNotNull(response.getResult().get(0).getName());
            assertNotNull(response.getResult().get(0).getRoles().getOwner());
            assertNotNull(response.getResult().get(0).getRoles().getParticipant());
            assertEquals(true, response.getResult().get(0).getRoles().getParticipant().getCanAddParticipants().booleanValue());
            assertEquals(true, response.getResult().get(0).getRoles().getParticipant().getCanRemoveParticipants().booleanValue());
            assertEquals(true, response.getResult().get(0).getRoles().getParticipant().getCanSend().booleanValue());
            assertEquals(true, response.getResult().get(0).getRoles().getOwner().getCanAddParticipants().booleanValue());
            assertEquals(true, response.getResult().get(0).getRoles().getOwner().getCanRemoveParticipants().booleanValue());
            assertEquals(true, response.getResult().get(0).getRoles().getOwner().getCanSend().booleanValue());
            assertNotNull(response.getETag());
        });
    }

    @Test
    public void getConversations_sessionCreateInProgress() throws Exception {
        isCreateSessionInProgress.set(true);
        service.getConversations(Scope.PARTICIPANT).timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = RuntimeException.class)
    public void getConversations_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        getConversations();
    }

    @Test
    public void updateConversation() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_conversation_update.json", 200).addHeader("ETag", "eTag"));

        com.comapi.internal.network.model.conversation.ConversationUpdate conversation = com.comapi.internal.network.model.conversation.ConversationUpdate.builder()
                .setPublic(false).build();

        service.updateConversation("someId", conversation, "eTag").toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getResult().getDescription());
            assertNotNull(response.getResult().getId());
            assertNotNull(response.getResult().getName());
            assertNotNull(response.getResult().getRoles().getOwner());
            assertNotNull(response.getResult().getRoles().getParticipant());
            assertEquals(true, response.getResult().getRoles().getParticipant().getCanAddParticipants().booleanValue());
            assertEquals(true, response.getResult().getRoles().getParticipant().getCanRemoveParticipants().booleanValue());
            assertEquals(true, response.getResult().getRoles().getParticipant().getCanSend().booleanValue());
            assertEquals(true, response.getResult().getRoles().getOwner().getCanAddParticipants().booleanValue());
            assertEquals(true, response.getResult().getRoles().getOwner().getCanRemoveParticipants().booleanValue());
            assertEquals(true, response.getResult().getRoles().getOwner().getCanSend().booleanValue());
            assertNotNull(response.getETag());
        });
    }

    @Test
    public void updateConversation_sessionCreateInProgress() throws Exception {
        isCreateSessionInProgress.set(true);
        service.updateConversation("someId", com.comapi.internal.network.model.conversation.ConversationUpdate.builder().build(), "eTag").timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = RuntimeException.class)
    public void updateConversation_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        updateConversation();
    }

    @Test
    public void deleteConversation() throws Exception {

        MockResponse mr = new MockResponse();
        mr.setResponseCode(204);
        mr.addHeader("ETag", "eTag");
        server.enqueue(mr);

        service.deleteConversation("someId", "eTag").toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(204, response.getCode());
            assertNotNull(response.getETag());
        });
    }

    @Test
    public void deleteConversation_sessionCreateInProgress() {
        isCreateSessionInProgress.set(true);
        service.deleteConversation("someId", "eTag").timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = RuntimeException.class)
    public void deleteConversation_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        deleteConversation();
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

        service.removeParticipants("someId", participants).toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(204, response.getCode());
            assertNotNull(response.getETag());
        });
    }

    @Test
    public void removeParticipants_sessionCreateInProgress() throws Exception {
        isCreateSessionInProgress.set(true);
        service.removeParticipants("someId", new ArrayList<>()).timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = RuntimeException.class)
    public void removeParticipants_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        removeParticipants();
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

        service.addParticipants("someId", participants).toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(201, response.getCode());
            assertNotNull(response.getETag());
        });
    }

    @Test
    public void addParticipants_sessionCreateInProgress() throws Exception {
        isCreateSessionInProgress.set(true);
        service.addParticipants("someId", new ArrayList<>()).timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = RuntimeException.class)
    public void addParticipants_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        addParticipants();
    }

    @Test
    public void getParticipants() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_participants_get.json", 200).addHeader("ETag", "eTag"));

        service.getParticipants("someId").toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getETag());
            assertNotNull(response.getResult().get(0).getId());
            assertNotNull(response.getResult().get(0).getRole());
            assertNotNull(response.getResult().get(1).getId());
            assertNotNull(response.getResult().get(1).getRole());
            assertNotNull(response.getResult().get(2).getId());
            assertNotNull(response.getResult().get(2).getRole());
        });
    }

    @Test
    public void getParticipants_sessionCreateInProgress() throws Exception {
        isCreateSessionInProgress.set(true);
        service.getParticipants("someId").timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = RuntimeException.class)
    public void getParticipants_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        getParticipants();
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

        service.sendMessage("someId", msg).toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getETag());
            assertNotNull(response.getResult().getId());
        });
    }

    @Test
    public void sendMessage_simpleVersion() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_message_sent.json", 200).addHeader("ETag", "eTag"));

        service.sendMessage("someId", "body").toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getETag());
            assertNotNull(response.getResult().getId());
        });
    }

    @Test
    public void sendMessage_sessionCreateInProgress() throws Exception {
        isCreateSessionInProgress.set(true);
        service.sendMessage("someId", MessageToSend.builder().build()).timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = RuntimeException.class)
    public void sendMessage_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        sendMessage();
    }

    @Test
    public void updateMessageStatus() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_message_sent.json", 200).addHeader("ETag", "eTag"));

        List<com.comapi.internal.network.model.messaging.MessageStatusUpdate> update = new ArrayList<>();
        update.add(com.comapi.internal.network.model.messaging.MessageStatusUpdate.builder().setStatus(MessageStatus.read).addMessageId("someId").setTimestamp("time").build());
        update.add(com.comapi.internal.network.model.messaging.MessageStatusUpdate.builder().setStatus(MessageStatus.delivered).addMessageId("someId").setTimestamp("time").build());

        service.updateMessageStatus("someId", update).toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getETag());
        });
    }

    @Test
    public void updateMessageStatus_sessionCreateInProgress() {
        isCreateSessionInProgress.set(true);
        service.updateMessageStatus("someId", new ArrayList<>()).timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = RuntimeException.class)
    public void updateMessageStatus_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        updateMessageStatus();
    }

    @Test
    public void queryEvents() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_events_query.json", 200).addHeader("ETag", "eTag"));

        service.queryEvents("someId", 0L, 100).toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getETag());

            assertNotNull(response.getResult().getMessageRead().get(0).getEventId());
            assertNotNull(response.getResult().getMessageRead().get(0).getName());
            assertNotNull(response.getResult().getMessageRead().get(0).getConversationEventId());
            assertNotNull(response.getResult().getMessageRead().get(0).getMessageId());
            assertNotNull(response.getResult().getMessageRead().get(0).getConversationId());
            assertNotNull(response.getResult().getMessageRead().get(0).getProfileId());
            assertNotNull(response.getResult().getMessageRead().get(0).getTimestamp());

            assertNotNull(response.getResult().getMessageDelivered().get(0).getEventId());
            assertNotNull(response.getResult().getMessageDelivered().get(0).getName());
            assertNotNull(response.getResult().getMessageDelivered().get(0).getConversationEventId());
            assertNotNull(response.getResult().getMessageDelivered().get(0).getMessageId());
            assertNotNull(response.getResult().getMessageDelivered().get(0).getConversationId());
            assertNotNull(response.getResult().getMessageDelivered().get(0).getProfileId());
            assertNotNull(response.getResult().getMessageDelivered().get(0).getTimestamp());

            assertNotNull(response.getResult().getMessageSent().get(0).getEventId());
            assertNotNull(response.getResult().getMessageSent().get(0).getName());
            assertNotNull(response.getResult().getMessageSent().get(0).getConversationEventId());
            assertNotNull(response.getResult().getMessageSent().get(0).getMessageId());
            assertNotNull(response.getResult().getMessageSent().get(0).getAlert().getPlatforms().getFcm().get("notification"));
            assertNotNull(response.getResult().getMessageSent().get(0).getAlert().getPlatforms().getFcm().get("data"));
            assertNotNull(response.getResult().getMessageSent().get(0).getContext().getConversationId());
            assertNotNull(response.getResult().getMessageSent().get(0).getContext().getSentBy());
            assertNotNull(response.getResult().getMessageSent().get(0).getContext().getSentOn());
            assertNotNull(response.getResult().getMessageSent().get(0).getContext().getFromWhom().getId());
            assertNotNull(response.getResult().getMessageSent().get(0).getContext().getFromWhom().getName());
            assertNotNull(response.getResult().getMessageSent().get(0).getContext().getFromWhom().getName());
            assertNotNull(response.getResult().getMessageSent().get(0).getMetadata().get("key"));

            assertNotNull(response.getResult().getConversationDelete().get(0));

            assertNotNull(response.getResult().getConversationUnDelete().get(0));

            assertNotNull(response.getResult().getConversationUpdate().get(0));

            assertNotNull(response.getResult().getParticipantAdded().get(0));

            assertNotNull(response.getResult().getParticipantRemoved().get(0));

            assertNotNull(response.getResult().getParticipantUpdate().get(0));

            assertEquals(9, response.getResult().getCombinedSize());
        });
    }

    @Test
    public void queryEvents_sessionCreateInProgress() {

        isCreateSessionInProgress.set(true);
        service.queryEvents("someId", 0L, 100).timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = RuntimeException.class)
    public void queryEvents_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        queryEvents();
    }

    @Test
    public void queryMessages() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_message_query.json", 200).addHeader("ETag", "eTag"));

        service.queryMessages("someId", 0L, 100).toBlocking().forEach(response -> {

            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getETag());

            assertEquals(0, response.getResult().getEarliestEventId());
            assertEquals(true, response.getResult().getLatestEventId() > 0);

            assertNotNull(response.getResult().getMessages().get(0).getMessageId());
            assertNotNull(response.getResult().getMessages().get(0).getConversationId());
            assertNotNull(response.getResult().getMessages().get(0).getFromWhom().getId());
            assertNotNull(response.getResult().getMessages().get(0).getFromWhom().getName());
            assertNotNull(response.getResult().getMessages().get(0).getSentOn());
            assertNotNull(response.getResult().getMessages().get(0).getSentBy());
            assertNotNull(response.getResult().getMessages().get(0).getMetadata().get("key"));
            assertNotNull(response.getResult().getMessages().get(0).getStatusUpdate().get("userB").getStatus());
            assertNotNull(response.getResult().getMessages().get(0).getStatusUpdate().get("userB").getTimestamp());
            assertNotNull(response.getResult().getMessages().get(0).getParts().get(0).getName());
            assertEquals(true, response.getResult().getMessages().get(0).getParts().get(0).getSize() > 0);
            assertNotNull(response.getResult().getMessages().get(0).getParts().get(0).getType());
            assertNotNull(response.getResult().getMessages().get(0).getParts().get(0).getData());
            assertNotNull(response.getResult().getOrphanedEvents());
            assertNotNull(response.getResult().getOrphanedEvents());

            assertNotNull(response.getResult().getMessages().get(1).getParts().get(0).getUrl());
        });
    }


    @Test
    public void queryMessages_orphanedEvents() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_message_query_orphaned.json", 200).addHeader("ETag", "eTag"));

        service.queryMessages("someId", 0L, 100).toBlocking().forEach(response -> {

            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getETag());

            for (OrphanedEvent event : response.getResult().getOrphanedEvents()) {
                assertNotNull(event.getMessageId());
                assertNotNull(event.getConversationId());
                assertNotNull(event.getEventId());
                assertTrue(event.getConversationEventId() > 0);
                assertNotNull(event.getName());
                assertNotNull(event.getProfileId());
                assertNotNull(event.getTimestamp());
                assertTrue(event.isEventTypeDelivered() || event.isEventTypeRead());
            }
        });
    }

    @Test
    public void queryMessages_sessionCreateInProgress() throws Exception {
        isCreateSessionInProgress.set(true);
        service.queryMessages("someId", 0L, 0).timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = RuntimeException.class)
    public void queryMessages_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        queryMessages();
    }

    @Test
    public void createFbOptIn() throws Exception {

        String testResp = "data";

        server.enqueue(new MockResponse().setResponseCode(200).setBody(testResp));

        service.createFbOptInState().toBlocking().forEach(response -> {
            assertEquals(true, response.getResult().equals(testResp));
        });
    }

    @Test
    public void createFbOptIn_sessionCreateInProgress() throws Exception {
        isCreateSessionInProgress.set(true);
        service.createFbOptInState().timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = RuntimeException.class)
    public void createFbOptIn_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        createFbOptIn();
    }

    @Test
    public void updatePush() throws Exception {

        MockResponse mr = new MockResponse();
        mr.setResponseCode(200);
        mr.addHeader("ETag", "eTag");
        server.enqueue(mr);
        //new SessionData().setProfileId("id").setSessionId("id").setAccessToken("token").setExpiresOn(Long.MAX_VALUE)
        service.updatePushToken().toBlocking().forEach(response -> {
            assertEquals(true, response.second.isSuccessful());
            assertEquals(200, response.second.getCode());
            assertNotNull(response.second.getETag());
        });
    }

    @Test(expected = RuntimeException.class)
    public void updatePush_sessionCreateInProgress_shouldFail() throws Exception {
        isCreateSessionInProgress.set(true);
        updatePush();
    }

    @Test(expected = RuntimeException.class)
    public void updatePush_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        updatePush();
    }

    @After
    public void tearDown() throws Exception {
        DataTestHelper.clearDeviceData();
        DataTestHelper.clearSessionData();
        server.shutdown();
        pushMgr.unregisterPushReceiver(RuntimeEnvironment.application);
    }

    private Observer<ComapiResult<?>> getEmptyObserver() {

        return new Observer<ComapiResult<?>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(ComapiResult<?> mapComapiResult) {

            }
        };
    }
}
