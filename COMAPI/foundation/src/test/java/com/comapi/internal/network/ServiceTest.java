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
import com.comapi.internal.ListenerListAdapter;
import com.comapi.internal.data.DataManager;
import com.comapi.internal.data.SessionData;
import com.comapi.internal.log.LogLevel;
import com.comapi.internal.log.LogManager;
import com.comapi.internal.log.Logger;
import com.comapi.internal.network.api.RestApi;
import com.comapi.internal.network.model.conversation.Participant;
import com.comapi.internal.network.model.conversation.Scope;
import com.comapi.internal.network.model.messaging.Alert;
import com.comapi.internal.network.model.messaging.MessageReceived;
import com.comapi.internal.network.model.messaging.MessageStatus;
import com.comapi.internal.network.model.messaging.MessageToSend;
import com.comapi.internal.network.model.messaging.OrphanedEvent;
import com.comapi.internal.network.model.messaging.Part;
import com.comapi.internal.network.model.profile.ComapiProfile;
import com.comapi.internal.network.sockets.SocketController;
import com.comapi.internal.push.PushManager;
import com.comapi.mock.MockAuthenticator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
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
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.robolectric.RuntimeEnvironment.application;

/**
 * Robolectric for Network setup.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.M, constants = BuildConfig.class, packageName = "com.comapi")
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

        service = new InternalService(new CallbackAdapter(), dataMgr, pushMgr, API_SPACE_ID, "packageName", log);

        restApi = service.initialiseRestClient(LogLevel.DEBUG.getValue(), baseURIs);

        isCreateSessionInProgress = new AtomicBoolean();
        sessionController = service.initialiseSessionController(new SessionCreateManager(isCreateSessionInProgress), pushMgr, comapiState, authenticator, restApi, new Handler(Looper.getMainLooper()), true, new StateListener() {
        });
        sessionController.setSocketController(new SocketController(dataMgr, new ListenerListAdapter(log), log, new URI("ws://auth"), null));
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
    public void getProfileWithDefaults() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_profile_get.json", 200).addHeader("ETag", "eTag"));

        service.getProfileServiceWithDefaults().getProfile("profileId").toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertEquals("id", response.getResult().get("id"));
            assertEquals("firstName", response.getResult().getFirstName());
            assertEquals("lastName", response.getResult().getLastName());
            assertEquals("email", response.getResult().getEmail());
            assertEquals("gender", response.getResult().getGender());
            assertEquals("phoneNumber", response.getResult().getPhoneNumber());
            assertEquals("phoneNumberCountryCode", response.getResult().getPhoneNumberCountryCode());
            assertEquals("profilePicture", response.getResult().getProfilePicture());
            assertEquals("custom", response.getResult().get("custom"));
            assertNotNull(response.getResult().toString());
            assertNotNull(response.getETag());
        });
    }

    @Test
    public void profileToString() {

        ComapiProfile profile = new ComapiProfile();
        for (int i=0; i<100; i++) {
            profile.add(String.valueOf(i), "x");
        }

        assertNotNull(profile.toString());
        assertFalse(profile.toString().endsWith("..."));

        for (int i=100; i<10000; i++) {
            profile.add(String.valueOf(i), "x");
        }

        assertNotNull(profile.toString());
        assertTrue(profile.toString().length() < 1010);
        assertTrue(profile.toString().endsWith("..."));
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

        sessionController = new SessionController(new SessionCreateManager(isCreateSessionInProgress), pushMgr, comapiState, dataMgr, authenticator, restApi, "", new Handler(Looper.getMainLooper()), new Logger(new LogManager(), ""), null, true, new StateListener() {
        }) {
            @Override
            protected Observable<SessionData> reAuthenticate() {
                return Observable.just(new SessionData().setAccessToken(UUID.randomUUID().toString()).setExpiresOn(Long.MAX_VALUE).setProfileId("id").setSessionId("id"));
            }
        };

        comapiState.set(GlobalState.SESSION_ACTIVE);
        isCreateSessionInProgress.set(false);

        // Go through all 3 retries
        server.enqueue(new MockResponse().setResponseCode(401));
        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_create.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(new MockResponse().setResponseCode(401));
        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_create.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(new MockResponse().setResponseCode(401));
        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_start.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_session_create.json", 200).addHeader("ETag", "eTag"));
        server.enqueue(new MockResponse().setResponseCode(401));

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
    public void queryProfileWithDefaults() throws Exception {

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

        service.getProfileServiceWithDefaults().queryProfiles(query).toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());

            ComapiProfile profile = null;
            for (ComapiProfile p : response.getResult()) {
                if (p.getId().equals("id")) {
                    profile = p;
                    break;
                }
            }

            if (profile != null) {
                assertEquals("firstName", profile.getFirstName());
                assertEquals("lastName", profile.getLastName());
                assertEquals("email", profile.getEmail());
                assertEquals("gender", profile.getGender());
                assertEquals("phoneNumber", profile.getPhoneNumber());
                assertEquals("phoneNumberCountryCode", profile.getPhoneNumberCountryCode());
                assertEquals("profilePicture", profile.getProfilePicture());
                assertEquals("custom", profile.get("custom"));
                assertNotNull(response.getETag());
            } else {
                fail("no profile with id = id");
            }
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
        response.setResponseCode(400);
        response.setHttp2ErrorCode(400);
        response.setBody("{\"validationFailures\":[{\"paramName\":\"someParameter\",\"message\":\"details\"}]}");
        server.enqueue(response);

        String query = new QueryBuilder()
                .build();

        service.queryProfiles(query).toBlocking().forEach(result -> {
            assertEquals(false, result.isSuccessful());
            assertNotNull(result.getErrorBody());
            List<ComapiResult<List<Map<String, Object>>>.ComapiValidationFailure> failures = result.getValidationFailures();
            assertNotNull(failures);
            assertEquals(1, failures.size());
            assertEquals("details", failures.get(0).getMessage());
            assertEquals("someParameter", failures.get(0).getParamName());
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
    public void updateProfileWithDefaults() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_profile_update.json", 200).addHeader("ETag", "eTag"));

        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        map.put("key2", 312);

        service.getProfileServiceWithDefaults().updateProfile(new ComapiProfile(map), "eTag").toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertEquals("firstName", response.getResult().getFirstName());
            assertEquals("lastName", response.getResult().getLastName());
            assertEquals("email", response.getResult().getEmail());
            assertEquals("gender", response.getResult().getGender());
            assertEquals("phoneNumber", response.getResult().getPhoneNumber());
            assertEquals("phoneNumberCountryCode", response.getResult().getPhoneNumberCountryCode());
            assertEquals("profilePicture", response.getResult().getProfilePicture());
            assertEquals("custom", response.getResult().get("custom"));
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
    public void patchProfile() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_profile_patch.json", 200).addHeader("ETag", "eTag"));

        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        map.put("key2", 312);

        service.patchMyProfile(map, "eTag").toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getResult().get("id"));
            assertNotNull(response.getETag());
        });
    }

    @Test
    public void patchProfileWithDefaults() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_profile_patch.json", 200).addHeader("ETag", "eTag"));

        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        map.put("key2", 312);

        service.getProfileServiceWithDefaults().patchMyProfile(new ComapiProfile(map), "eTag").toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertEquals("firstName", response.getResult().getFirstName());
            assertEquals("lastName", response.getResult().getLastName());
            assertEquals("email", response.getResult().getEmail());
            assertEquals("gender", response.getResult().getGender());
            assertEquals("phoneNumber", response.getResult().getPhoneNumber());
            assertEquals("phoneNumberCountryCode", response.getResult().getPhoneNumberCountryCode());
            assertEquals("profilePicture", response.getResult().getProfilePicture());
            assertEquals("custom", response.getResult().get("custom"));
            assertNotNull(response.getETag());
        });
    }

    @Test
    public void patchProfile2() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_profile_patch.json", 200).addHeader("ETag", "eTag"));

        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        map.put("key2", 312);

        service.patchProfile("someId", map, null).toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getResult().get("id"));
            assertNotNull(response.getETag());
        });
    }

    @Test
    public void patchProfileWithDefaults2() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_profile_patch.json", 200).addHeader("ETag", "eTag"));

        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        map.put("key2", 312);

        service.getProfileServiceWithDefaults().patchProfile("someId", new ComapiProfile(map), null).toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getResult().get("id"));
            assertEquals("firstName", response.getResult().getFirstName());
            assertEquals("lastName", response.getResult().getLastName());
            assertEquals("email", response.getResult().getEmail());
            assertEquals("gender", response.getResult().getGender());
            assertEquals("phoneNumber", response.getResult().getPhoneNumber());
            assertEquals("phoneNumberCountryCode", response.getResult().getPhoneNumberCountryCode());
            assertEquals("profilePicture", response.getResult().getProfilePicture());
            assertEquals("custom", response.getResult().get("custom"));
            assertNotNull(response.getETag());
        });
    }

    @Test
    public void patchProfile_sessionCreateInProgress() throws Exception {
        isCreateSessionInProgress.set(true);
        service.patchMyProfile(new HashMap<>(), null).timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test
    public void patchProfile2_sessionCreateInProgress() throws Exception {
        isCreateSessionInProgress.set(true);
        service.patchProfile("someId", new HashMap<>(), null).timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = ComapiException.class)
    public void patchProfile_sessionCreateInProgress_noToken() throws Exception {
        DataTestHelper.clearSessionData();
        isCreateSessionInProgress.set(false);
        service.patchMyProfile(new HashMap<>(), null).timeout(3, TimeUnit.SECONDS).toBlocking().subscribe();
    }

    @Test(expected = ComapiException.class)
    public void patchProfile2_sessionCreateInProgress_noToken() throws Exception {
        DataTestHelper.clearSessionData();
        isCreateSessionInProgress.set(false);
        service.patchProfile("someId", new HashMap<>(), null).timeout(3, TimeUnit.SECONDS).toBlocking().subscribe();
    }

    @Test(expected = RuntimeException.class)
    public void patchProfile_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        patchProfile();
    }

    @Test(expected = RuntimeException.class)
    public void patchProfile2_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        patchProfile2();
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
    public void isNotTyping() {

        server.enqueue(new MockResponse().setResponseCode(200));
        service.isTyping("conversationId", false).toBlocking().forEach(response -> {
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

        service.isTyping("conversationId", false).timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        // Not adding
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = ComapiException.class)
    public void isTyping_sessionCreateInProgress_noToken() throws Exception {
        DataTestHelper.clearSessionData();
        isCreateSessionInProgress.set(false);
        service.isTyping("conversationId").timeout(3, TimeUnit.SECONDS).toBlocking().subscribe();
    }

    @Test(expected = ComapiException.class)
    public void isNotTyping_sessionCreateInProgress_noToken() throws Exception {
        DataTestHelper.clearSessionData();
        isCreateSessionInProgress.set(false);
        service.isTyping("conversationId", false).timeout(3, TimeUnit.SECONDS).toBlocking().subscribe();
    }

    @Test(expected = RuntimeException.class)
    public void isTyping_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        isTyping();
    }

    @Test(expected = RuntimeException.class)
    public void isNotTyping_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        isNotTyping();
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
    public void getConversationsExtended() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_conversations_get_ext.json", 200).addHeader("ETag", "eTag"));

        service.getConversations(false).toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());

            assertEquals("eTag", response.getResult().get(0).getETag());
            assertEquals("eTag", response.getResult().get(1).getETag());
            assertEquals(Long.valueOf(24), response.getResult().get(0).getLatestSentEventId());
            assertNull(response.getResult().get(1).getLatestSentEventId());
            assertEquals(Integer.valueOf(2), response.getResult().get(0).getParticipantCount());
            assertEquals(Integer.valueOf(1), response.getResult().get(1).getParticipantCount());

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
    public void getConversationsExtended_sessionCreateInProgress() throws Exception {
        isCreateSessionInProgress.set(true);
        service.getConversations(true).timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = RuntimeException.class)
    public void getConversationsExtended_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        getConversationsExtended();
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
            assertNotNull(response.getResult().getEventId());
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
            assertNotNull(response.getResult().getEventId());
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
    public void queryConversationEvents() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_conversation_events_query.json", 200).addHeader("ETag", "eTag"));

        service.queryConversationEvents("someId", 0L, 100).toBlocking().forEach(response -> {
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
        });
    }

    @Test
    public void queryConversationEvents_sessionCreateInProgress() {

        isCreateSessionInProgress.set(true);
        service.queryConversationEvents("someId", 0L, 100).timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = RuntimeException.class)
    public void queryConversationEvents_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        queryConversationEvents();
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

            for (MessageReceived mr : response.getResult().getMessages()) {
                assertNotNull(mr.getMessageId());
                assertNotNull(mr.getSentEventId());
                assertNotNull(mr.getConversationId());
                assertNotNull(mr.getFromWhom().getId());
                assertNotNull(mr.getFromWhom().getName());
                assertNotNull(mr.getSentOn());
                assertNotNull(mr.getSentBy());
                assertNotNull(mr.getMetadata().get("key"));
                assertNotNull(mr.getStatusUpdate().get("userB").getStatus());
                assertNotNull(mr.getStatusUpdate().get("userB").getTimestamp());
                assertNotNull(mr.getParts().get(0).getName());
                assertEquals(true, mr.getParts().get(0).getSize() > 0);
                assertNotNull(mr.getParts().get(0).getType());
                assertNotNull(mr.getParts().get(0).getData());
                assertNotNull(response.getResult().getOrphanedEvents());
                assertNotNull(response.getResult().getOrphanedEvents());
                assertNotNull(mr.getParts().get(0).getUrl());
            }
        });
    }

    @Test
    public void queryMessages_broken() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_message_query_broken.json", 200).addHeader("ETag", "eTag"));

        service.queryMessages("someId", 0L, 100).toBlocking().forEach(response -> {

            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getETag());

            for (MessageReceived mr : response.getResult().getMessages()) {
                mr.getMessageId();
                mr.getSentEventId();
                mr.getConversationId();
                mr.getFromWhom();
                mr.getSentOn();
                mr.getSentBy();
                mr.getMetadata();
                mr.getStatusUpdate();
                mr.getParts();
            }
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
    public void queryMessages_brokenOrphanedEvents() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_message_query_orphaned_broken.json", 200).addHeader("ETag", "eTag"));

        service.queryMessages("someId", 0L, 100).toBlocking().forEach(response -> {

            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getETag());

            for (OrphanedEvent event : response.getResult().getOrphanedEvents()) {
                event.getMessageId();
                event.getConversationId();
                event.getEventId();
                event.getConversationEventId();
                event.getName();
                event.getProfileId();
                event.getTimestamp();
                event.isEventTypeDelivered();
                event.isEventTypeRead();
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

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void uploadContent_file() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_upload_content.json", 200).addHeader("ETag", "eTag"));

        File file = new File(RuntimeEnvironment.application.getFilesDir(), "testFile");
        file.setReadable(true);
        file.createNewFile();

        service.uploadContent("folder", ContentData.create(file, "mime_type", "name")).toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getETag());
            assertEquals("id", response.getResult().getId());
            assertEquals("folder", response.getResult().getFolder());
            assertEquals(2662193, response.getResult().getSize().longValue());
            assertEquals("fullURL", response.getResult().getUrl());
            assertEquals("image/jpeg", response.getResult().getType());
        });
    }

    @Test
    public void uploadContent_string() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_upload_content.json", 200).addHeader("ETag", "eTag"));

        service.uploadContent("folder", ContentData.create("string", "mime_type", "name")).toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getETag());
            assertEquals("id", response.getResult().getId());
            assertEquals("folder", response.getResult().getFolder());
            assertEquals(2662193, response.getResult().getSize().longValue());
            assertEquals("fullURL", response.getResult().getUrl());
            assertEquals("image/jpeg", response.getResult().getType());
        });
    }

    @Test
    public void uploadContent_bytes() throws Exception {

        server.enqueue(ResponseTestHelper.createMockResponse(this, "rest_upload_content.json", 200).addHeader("ETag", "eTag"));

        service.uploadContent("folder", ContentData.create(new byte[0], "mime_type", "name")).toBlocking().forEach(response -> {
            assertEquals(true, response.isSuccessful());
            assertEquals(200, response.getCode());
            assertNotNull(response.getETag());
            assertEquals("id", response.getResult().getId());
            assertEquals("folder", response.getResult().getFolder());
            assertEquals(2662193, response.getResult().getSize().longValue());
            assertEquals("fullURL", response.getResult().getUrl());
            assertEquals("image/jpeg", response.getResult().getType());
        });
    }

    @Test
    public void uploadContent_sessionCreateInProgress() throws Exception {
        isCreateSessionInProgress.set(true);
        service.uploadContent("folder", ContentData.create("", "mime_type", "name")).timeout(3, TimeUnit.SECONDS).subscribe(getEmptyObserver());
        assertEquals(1, service.getTaskQueue().queue.size());
        isCreateSessionInProgress.set(false);
        service.getTaskQueue().executePending();
        assertEquals(0, service.getTaskQueue().queue.size());
    }

    @Test(expected = RuntimeException.class)
    public void uploadContent_noSession_shouldFail() throws Exception {
        DataTestHelper.clearSessionData();
        uploadContent_string();
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
