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

package com.comapi.internal.network.api;

import com.comapi.internal.network.model.conversation.ConversationCreate;
import com.comapi.internal.network.model.conversation.ConversationDetails;
import com.comapi.internal.network.model.conversation.ConversationUpdate;
import com.comapi.internal.network.model.conversation.Conversation;
import com.comapi.internal.network.model.messaging.MessageStatusUpdate;
import com.comapi.internal.network.model.messaging.UploadContentResponse;
import com.comapi.internal.network.model.session.PushConfig;
import com.comapi.internal.network.model.session.SessionStartResponse;
import com.google.gson.JsonObject;

import com.comapi.internal.network.model.conversation.Participant;
import com.comapi.internal.network.model.messaging.MessageSentResponse;
import com.comapi.internal.network.model.messaging.MessageToSend;
import com.comapi.internal.network.model.messaging.MessagesQueryResponse;
import com.comapi.internal.network.model.session.SessionCreateRequest;
import com.comapi.internal.network.model.session.SessionCreateResponse;

import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

/**
 * REST api.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public interface RestApi {

    /*
     * SESSION APIs
     */

    @Headers({"Accept: application/json"})
    @GET("/apispaces/{apiSpaceId}/sessions/start")
    Observable<SessionStartResponse> startSession(@Path("apiSpaceId") String apiSpaceId);

    @Headers({"Accept: application/json"})
    @POST("/apispaces/{apiSpaceId}/sessions")
    Observable<SessionCreateResponse> createSession(@Path("apiSpaceId") String apiSpaceId, @Body SessionCreateRequest request);

    @Headers({"Accept: application/json"})
    @DELETE("/apispaces/{apiSpaceId}/sessions/{sessionId}")
    Observable<Response<Void>> endSession(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Path("sessionId") String sessionId);

    @Headers({"Accept: application/json"})
    @GET("/apispaces/{apiSpaceId}/sessions/{sessionId}")
    Observable<SessionCreateResponse> getSession(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Path("sessionId") String sessionId);

    @Headers({"Accept: application/json"})
    @PUT("/apispaces/{apiSpaceId}/sessions/{sessionId}/push")
    Observable<Response<Void>> updatePushToken(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Path("sessionId") String sessionId, @Body PushConfig pushConfig);

    /*
     * PROFILE APIs
     */

    @Headers({"Accept: application/json"})
    @GET("/apispaces/{apiSpaceId}/profiles/{id}")
    Observable<Response<Map<String, Object>>> getProfile(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Path("id") String id);

    @Headers({"Accept: application/json"})
    @GET
    Observable<Response<List<Map<String, Object>>>> queryProfiles(@Header("Authorization") String authorization, @Url String url);

    @Headers({"Accept: application/json"})
    @PUT("/apispaces/{apiSpaceId}/profiles/{id}")
    Observable<Response<Map<String, Object>>> updateProfile(@Header("Authorization") String authorization, @Header("If-Match") String eTag, @Path("apiSpaceId") String apiSpaceId, @Path("id") String id, @Body Map<String, Object> profileUpdate);

    @Headers({"Accept: application/json"})
    @PUT("/apispaces/{apiSpaceId}/profiles/{id}")
    Observable<Response<Map<String, Object>>> updateProfile(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Path("id") String id, @Body Map<String, Object> profileUpdate);

    @Headers({"Accept: application/json"})
    @PATCH("/apispaces/{apiSpaceId}/profiles/{id}")
    Observable<Response<Map<String, Object>>> patchProfile(@Header("Authorization") String authorization, @Header("If-Match") String eTag, @Path("apiSpaceId") String apiSpaceId, @Path("id") String id, @Body Map<String, Object> profileUpdate);

    @Headers({"Accept: application/json"})
    @PATCH("/apispaces/{apiSpaceId}/profiles/{id}")
    Observable<Response<Map<String, Object>>> patchProfile(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Path("id") String id, @Body Map<String, Object> profileUpdate);

    /*
     * conversations
     */

    @Headers({"Accept: application/json"})
    @POST("/apispaces/{apiSpaceId}/conversations")
    Observable<Response<ConversationDetails>> createConversation(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Body ConversationCreate request);

    @Headers({"Accept: application/json"})
    @DELETE("/apispaces/{apiSpaceId}/conversations/{conversationId}")
    Observable<Response<Void>> deleteConversation(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Path("conversationId") String conversationId);

    @Headers({"Accept: application/json"})
    @DELETE("/apispaces/{apiSpaceId}/conversations/{conversationId}")
    Observable<Response<Void>> deleteConversation(@Header("Authorization") String authorization, @Header("If-Match") String eTag, @Path("apiSpaceId") String apiSpaceId, @Path("conversationId") String conversationId);

    @Headers({"Accept: application/json"})
    @GET("/apispaces/{apiSpaceId}/conversations/{conversationId}")
    Observable<Response<ConversationDetails>> getConversation(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Path("conversationId") String conversationId);

    @Headers({"Accept: application/json"})
    @GET("/apispaces/{apiSpaceId}/conversations")
    Observable<Response<List<Conversation>>> getConversations(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Query("scope") String scope, @Query("profileId") String profileId);

    @Headers({"Accept: application/json"})
    @PUT("/apispaces/{apiSpaceId}/conversations/{conversationId}")
    Observable<Response<ConversationDetails>> updateConversation(@Header("Authorization") String authorization, @Header("If-Match") String eTag, @Path("apiSpaceId") String apiSpaceId, @Path("conversationId") String conversationId, @Body ConversationUpdate conversationUpdate);

    @Headers({"Accept: application/json"})
    @PUT("/apispaces/{apiSpaceId}/conversations/{conversationId}")
    Observable<Response<ConversationDetails>> updateConversation(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Path("conversationId") String conversationId, @Body ConversationUpdate conversationUpdate);

    @Headers({"Accept: application/json"})
    @POST("/apispaces/{apiSpaceId}/conversations/{conversationId}/typing")
    Observable<Response<Void>> isTyping(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Path("conversationId") String conversationId);

    @Headers({"Accept: application/json"})
    @DELETE("/apispaces/{apiSpaceId}/conversations/{conversationId}/typing")
    Observable<Response<Void>> isNotTyping(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Path("conversationId") String conversationId);

    /*
     * participants
     */

    @Headers({"Accept: application/json"})
    @POST("/apispaces/{apiSpaceId}/conversations/{conversationId}/participants")
    Observable<Response<Void>> addParticipants(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Path("conversationId") String conversationId, @Body List<Participant> request);

    @Headers({"Accept: application/json"})
    @DELETE("/apispaces/{apiSpaceId}/conversations/{conversationId}/participants")
    Observable<Response<Void>> deleteParticipants(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Path("conversationId") String conversationId, @Query("id") List<String> ids);

    @Headers({"Accept: application/json"})
    @GET("/apispaces/{apiSpaceId}/conversations/{conversationId}/participants")
    Observable<Response<List<Participant>>> getParticipants(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Path("conversationId") String conversationId);

    /*
     * MESSAGING
     */

    @Headers({"Accept: application/json"})
    @POST("/apispaces/{apiSpaceId}/conversations/{conversationId}/messages")
    Observable<Response<MessageSentResponse>> sendMessage(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Path("conversationId") String conversationId, @Body MessageToSend message);

    @Headers({"Accept: application/json"})
    @POST("/apispaces/{apiSpaceId}/conversations/{conversationId}/messages/statusupdates")
    Observable<Response<Void>> updateMessageStatus(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Path("conversationId") String conversationId, @Body List<MessageStatusUpdate> messageStatusList);

    @Headers({"Accept: application/json"})
    @GET("/apispaces/{apiSpaceId}/conversations/{conversationId}/messages")
    Observable<Response<MessagesQueryResponse>> queryMessages(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Path("conversationId") String conversationId, @Query("from") final Long from, @Query("limit") final Integer limit);

    @Headers({"Accept: application/json"})
    @GET("/apispaces/{apiSpaceId}/conversations/{conversationId}/events")
    Observable<Response<List<JsonObject>>> queryEvents(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Path("conversationId") String conversationId, @Query("from") final Long from, @Query("limit") final Integer limit);

    /*
     * Content
     */
    @POST("/apispaces/{apiSpaceId}/content")
    Observable<Response<UploadContentResponse>> uploadContent(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Query("folder") String folder, @Header("content-filename") String name, @Body RequestBody body);

    /*
     * FB
     */
    @Headers({"Accept: application/json"})
    @POST("/apispaces/{apiSpaceId}/channels/facebook/state")
    Observable<Response<String>> createFbOptInState(@Header("Authorization") String authorization, @Path("apiSpaceId") String apiSpaceId, @Body Object body);

}