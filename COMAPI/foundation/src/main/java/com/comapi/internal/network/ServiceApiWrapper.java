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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.comapi.QueryBuilder;
import com.comapi.internal.Parser;
import com.comapi.internal.log.Logger;
import com.comapi.internal.network.api.RestApi;
import com.comapi.internal.network.model.conversation.ConversationCreate;
import com.comapi.internal.network.model.conversation.ConversationDetails;
import com.comapi.internal.network.model.conversation.ConversationUpdate;
import com.comapi.internal.network.model.conversation.Conversation;
import com.comapi.internal.network.model.conversation.Participant;
import com.comapi.internal.network.model.conversation.Scope;
import com.comapi.internal.network.model.messaging.ConversationEventsResponse;
import com.comapi.internal.network.model.messaging.EventsQueryResponse;
import com.comapi.internal.network.model.messaging.MessageSentResponse;
import com.comapi.internal.network.model.messaging.MessageStatusUpdate;
import com.comapi.internal.network.model.messaging.MessageToSend;
import com.comapi.internal.network.model.messaging.MessagesQueryResponse;
import com.comapi.internal.network.model.messaging.UploadContentResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Response;
import rx.Observable;
import rx.functions.Func1;

/**
 * Basic wrapper around REST API. Sets schedulers and Comapi results.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
class ServiceApiWrapper extends ApiWrapper {

    private final Logger log;

    protected RestApi service;

    protected final String apiSpaceId;

    /**
     * Recommended constructor.
     *
     * @param apiSpaceId  Comapi Api Space in which the SDK operates.
     */
    ServiceApiWrapper(@NonNull String apiSpaceId, @NonNull Logger log) {
        this.apiSpaceId = apiSpaceId;
        this.log = log;
    }

    /**
     * Sets REST API definitions.
     *
     * @param service REST API.
     */
    protected void setService(RestApi service) {
        this.service = service;
    }

    /**
     * Send message to the chanel.
     *
     * @param token          Comapi access token.
     * @param conversationId ID of a conversation to send a message to.
     * @param message        Message to be send.
     * @return Observable to send message to a conversation.
     */
    Observable<ComapiResult<MessageSentResponse>> doSendMessage(@NonNull final String token, @NonNull final String conversationId, @NonNull final MessageToSend message) {
        return wrapObservable(service.sendMessage(AuthManager.addAuthPrefix(token), apiSpaceId, conversationId, message).map(mapToComapiResult()), log, "Sending message to conversation " + conversationId);
    }

    /**
     * Upload content data.
     *
     * @param token  Comapi access token.
     * @param folder Folder name to put the file in.
     * @param data   Content data.
     * @return Observable emitting details of uploaded content.
     */
    Observable<ComapiResult<UploadContentResponse>> doUploadContent(@NonNull final String token, @NonNull final String folder, @Nullable final String name, @NonNull final ContentData data) {
        return wrapObservable(service.uploadContent(AuthManager.addAuthPrefix(token), apiSpaceId, folder, name, data.getBody()).map(mapToComapiResult()), log, "Uploading content " + name);
    }

    /**
     * Get profile details from the service.
     *
     * @param token     Comapi access token.
     * @param profileId Profile Id of the user.
     * @return Profile details from the service.
     */
    Observable<ComapiResult<Map<String, Object>>> doGetProfile(@NonNull final String token, @NonNull final String profileId) {
        return wrapObservable(service.getProfile(AuthManager.addAuthPrefix(token), apiSpaceId, profileId).map(mapToComapiResult()), log, "Getting profile " + profileId);
    }

    /**
     * Query user profiles on the services.
     *
     * @param token       Comapi access token.
     * @param queryString Query string. See https://www.npmjs.com/package/mongo-querystring for query syntax. You can use {@link QueryBuilder} helper class to construct valid query string.
     * @return Profiles detail from the service.
     */
    Observable<ComapiResult<List<Map<String, Object>>>> doQueryProfiles(@NonNull final String token, @NonNull final String queryString) {
        final String uri = "/apispaces/" + apiSpaceId + "/profiles" + queryString;
        return wrapObservable(service.queryProfiles(AuthManager.addAuthPrefix(token), uri).map(mapToComapiResult()), log, "Query profiles " + queryString);
    }

    /**
     * Updates profile for an active session.
     *
     * @param token          Comapi access token.
     * @param profileDetails Profile details.
     * @return Observable with to perform update profile for current session.
     */
    Observable<ComapiResult<Map<String, Object>>> doUpdateProfile(@NonNull final String token, @NonNull final String profileId, @NonNull final Map<String, Object> profileDetails, final String eTag) {
        return wrapObservable(!TextUtils.isEmpty(eTag) ? service.updateProfile(AuthManager.addAuthPrefix(token), eTag, apiSpaceId, profileId, profileDetails).map(mapToComapiResult()) : service.updateProfile(AuthManager.addAuthPrefix(token), apiSpaceId, profileId, profileDetails)
                .map(mapToComapiResult()), log, "Updating profiles " + profileId);
    }

    /**
     * Patches profile for an active session.
     *
     * @param token          Comapi access token.
     * @param profileDetails Profile details.
     * @return Observable with to perform patch profile for current session.
     */
    Observable<ComapiResult<Map<String, Object>>> doPatchProfile(@NonNull final String token, @NonNull final String profileId, @NonNull final Map<String, Object> profileDetails, final String eTag) {
        return wrapObservable(!TextUtils.isEmpty(eTag) ? service.patchProfile(AuthManager.addAuthPrefix(token), eTag, apiSpaceId, profileId, profileDetails).map(mapToComapiResult()) : service.patchProfile(AuthManager.addAuthPrefix(token), apiSpaceId, profileId, profileDetails)
                .map(mapToComapiResult()), log, "Updating profiles " + profileId);
    }

    /**
     * Returns observable to create a conversation.
     *
     * @param token   Comapi access token.
     * @param request Request with conversation details to create.
     * @return Observable to to create a conversation.
     */
    Observable<ComapiResult<ConversationDetails>> doCreateConversation(@NonNull final String token, @NonNull final ConversationCreate request) {
        return wrapObservable(service.createConversation(AuthManager.addAuthPrefix(token), apiSpaceId, request)
                .map(mapToComapiResult()), log, "Creating conversation" + request.getName());
    }

    /**
     * Returns observable to create a conversation.
     *
     * @param token          Comapi access token.
     * @param conversationId ID of a conversation to delete.
     * @param eTag           Tag to specify local data version. Can be null.
     * @return Observable to to create a conversation.
     */
    Observable<ComapiResult<Void>> doDeleteConversation(@NonNull final String token, @NonNull final String conversationId, final String eTag) {
        return wrapObservable(!TextUtils.isEmpty(eTag) ? service.deleteConversation(AuthManager.addAuthPrefix(token), eTag, apiSpaceId, conversationId).map(mapToComapiResult()) : service.deleteConversation(AuthManager.addAuthPrefix(token), apiSpaceId, conversationId)
                .map(mapToComapiResult()), log, "Deleting conversation " + conversationId);
    }

    /**
     * Returns observable to create a conversation.
     *
     * @param token          Comapi access token.
     * @param conversationId ID of a conversation to obtain.
     * @return Observable to to create a conversation.
     */
    Observable<ComapiResult<ConversationDetails>> doGetConversation(@NonNull final String token, @NonNull final String conversationId) {
        return wrapObservable(service.getConversation(AuthManager.addAuthPrefix(token), apiSpaceId, conversationId).map(mapToComapiResult()), log, "Getting conversation" + conversationId);
    }

    /**
     * Returns observable to get all visible conversations.
     *
     * @param token Comapi access token.
     * @param scope {@link Scope} of the query
     * @return Observable to to create a conversation.
     */
    Observable<ComapiResult<List<Conversation>>> doGetConversations(@NonNull final String token, @NonNull final String profileId, @NonNull final Scope scope) {
        return wrapObservable(service.getConversations(AuthManager.addAuthPrefix(token), apiSpaceId, scope.getValue(), profileId).map(mapToComapiResult()), log, "Getting conversations " + profileId + " " + scope.name());
    }

    /**
     * Returns observable to update a conversation.
     *
     * @param token          Comapi access token.
     * @param eTag           Tag to specify local data version.
     * @param conversationId ID of a conversation to update.
     * @param request        Request with conversation details to update.
     * @return Observable to update a conversation.
     */
    Observable<ComapiResult<ConversationDetails>> doUpdateConversation(@NonNull final String token, @NonNull final String conversationId, @NonNull final ConversationUpdate request, final String eTag) {
        return wrapObservable(!TextUtils.isEmpty(eTag) ? service.updateConversation(AuthManager.addAuthPrefix(token), eTag, apiSpaceId, conversationId, request).map(mapToComapiResult()) : service.updateConversation(AuthManager.addAuthPrefix(token), apiSpaceId, conversationId, request)
                .map(mapToComapiResult()), log, "Updating conversation " + conversationId);
    }

    /**
     * Returns observable to remove list of participants from a conversation.
     *
     * @param token          Comapi access token.
     * @param conversationId ID of a conversation to delete.
     * @param ids            List of participant ids to be removed.
     * @return Observable to remove list of participants from a conversation.
     */
    Observable<ComapiResult<Void>> doRemoveParticipants(@NonNull final String token, @NonNull final String conversationId, @NonNull final List<String> ids) {
        return wrapObservable(service.deleteParticipants(AuthManager.addAuthPrefix(token), apiSpaceId, conversationId, ids).map(mapToComapiResult()), log, "Removing participants from " + conversationId);
    }

    /**
     * Returns observable to add a participant to.
     *
     * @param token          Comapi access token.
     * @param conversationId ID of a conversation to add a participant to.
     * @return Observable to get a list of conversation participants.
     */
    Observable<ComapiResult<List<Participant>>> doGetParticipants(@NonNull final String token, @NonNull final String conversationId) {
        return wrapObservable(service.getParticipants(AuthManager.addAuthPrefix(token), apiSpaceId, conversationId).map(mapToComapiResult()), log, "Getting participants for " + conversationId);
    }

    /**
     * Returns observable to add a list of participants to a conversation.
     *
     * @param token          Comapi access token.
     * @param conversationId ID of a conversation to update.
     * @param participants   New conversation participants details.
     * @return Observable to add participants to a conversation.
     */
    Observable<ComapiResult<Void>> doAddParticipants(@NonNull final String token, @NonNull final String conversationId, @NonNull final List<Participant> participants) {
        return wrapObservable(service.addParticipants(AuthManager.addAuthPrefix(token), apiSpaceId, conversationId, participants).map(mapToComapiResult()), log, "Adding participants to " + conversationId);
    }

    /**
     * Sets statuses for sets of messages.
     *
     * @param token          Comapi access token.
     * @param conversationId ID of a conversation to modify.
     * @param msgStatusList  List of status modifications.
     * @return Observable to modify message statuses.
     */
    Observable<ComapiResult<Void>> doUpdateMessageStatus(@NonNull final String token, @NonNull final String conversationId, @NonNull final List<MessageStatusUpdate> msgStatusList) {
        return wrapObservable(service.updateMessageStatus(AuthManager.addAuthPrefix(token), apiSpaceId, conversationId, msgStatusList).map(mapToComapiResult()), log, "Updating message status in conversation " + conversationId);
    }

    /**
     * Sets statuses a push message.
     *
     * @param token          Comapi access token.
     * @param messageId ID of a message.
     * @param status  new status.
     * @return Observable to modify message statuses.
     */
    Observable<ComapiResult<Void>> doUpdatePushMessageStatus(@NonNull final String token, @NonNull final String messageId, @NonNull final String status) {
        return wrapObservable(service.updatePushMessageStatus(AuthManager.addAuthPrefix(token), apiSpaceId, messageId, status).map(mapToComapiResult()), log, "Updating message " + messageId + " to "+ status);
    }

    /**
     * Query events.  Use {@link #doQueryConversationEvents(String, String, Long, Integer)} for better visibility of possible events.
     *
     * @param token          Comapi access token.
     * @param conversationId ID of a conversation to query events in it.
     * @param from           ID of the event to start from.
     * @param limit          Limit of events to obtain in this call.
     * @return Observable to get events in a conversation.
     */
    Observable<ComapiResult<EventsQueryResponse>> doQueryEvents(@NonNull final String token, @NonNull final String conversationId, @NonNull final Long from, @NonNull final Integer limit) {
        return addLogging(service.queryEvents(AuthManager.addAuthPrefix(token), apiSpaceId, conversationId, from, limit).map(mapToComapiResult()), log, "Querying events in " + conversationId)
                .flatMap(result -> {
                    EventsQueryResponse newResult = new EventsQueryResponse(result.getResult(), new Parser());
                    return wrapObservable(Observable.just(new ComapiResult<>(result, newResult)));
                });
    }

    /**
     * Query conversation events.
     *
     * @param token          Comapi access token.
     * @param conversationId ID of a conversation to query events in it.
     * @param from           ID of the event to start from.
     * @param limit          Limit of events to obtain in this call.
     * @return Observable to get events in a conversation.
     */
    Observable<ComapiResult<ConversationEventsResponse>> doQueryConversationEvents(@NonNull final String token, @NonNull final String conversationId, @NonNull final Long from, @NonNull final Integer limit) {
        return addLogging(service.queryEvents(AuthManager.addAuthPrefix(token), apiSpaceId, conversationId, from, limit).map(mapToComapiResult()), log, "Querying conversation events in " + conversationId)
                .flatMap(result -> {
                    ConversationEventsResponse newResult = new ConversationEventsResponse(result.getResult(), new Parser());
                    return wrapObservable(Observable.just(new ComapiResult<>(result, newResult)));
                });
    }

    /**
     * Query messages in a conversation.
     *
     * @param token          Comapi access token.
     * @param conversationId Id of the conversation.
     * @param from           Event id to start from when agregating messages.
     * @param limit          Limit of messages send in query response.
     * @return Observable to get messages in a conversation.
     */
    Observable<ComapiResult<MessagesQueryResponse>> doQueryMessages(@NonNull final String token, @NonNull final String conversationId, final Long from, @NonNull final Integer limit) {
        return wrapObservable(service.queryMessages(AuthManager.addAuthPrefix(token), apiSpaceId, conversationId, from, limit).map(mapToComapiResult()), log, "Querying messages in " + conversationId);
    }

    /**
     * Gets the Facebook data-ref to create send to messenger button.
     *
     * @param token Comapi access token.
     * @return @return Observable to get Facebook data-ref.
     */
    Observable<ComapiResult<String>> doCreateFbOptInState(@NonNull final String token) {
        return wrapObservable(service.createFbOptInState(AuthManager.addAuthPrefix(token), apiSpaceId, new Object()).map(mapToComapiResult()), log, "Creating fb opt in state.");
    }

    /**
     * Send information if user started or stopped typing message in a conversation.
     *
     * @param token          Comapi access token.
     * @param conversationId Id of the conversation.
     * @return Observable to send 'is typing' notification.
     */
    Observable<ComapiResult<Void>> doIsTyping(@NonNull final String token, @NonNull final String conversationId, final boolean isTyping) {
        if (isTyping) {
            return wrapObservable(service.isTyping(AuthManager.addAuthPrefix(token), apiSpaceId, conversationId).map(mapToComapiResult()), log, "Sending is typing.");
        } else {
            return wrapObservable(service.isNotTyping(AuthManager.addAuthPrefix(token), apiSpaceId, conversationId).map(mapToComapiResult()), log, "Sending is not typing");
        }
    }

    /**
     * Maps service response to Comapi result object.
     *
     * @param <E>nClass of the service call result.
     * @return Comapi result.
     */
    <E> Func1<Response<E>, ComapiResult<E>> mapToComapiResult() {
        return ComapiResult::new;
    }
}