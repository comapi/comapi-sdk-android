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

import com.comapi.internal.data.DataManager;
import com.comapi.internal.log.Logger;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.AsyncSubject;

/**
 * Holds pending API calls queue used when session is being authenticated and service is unavailable.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
class ServiceQueue extends ServiceApiWrapper {

    final DataManager dataMgr;

    protected final Logger log;

    private final TaskQueue taskQueue;

    /**
     * Recommended constructor.
     *
     * @param apiSpaceId  Comapi Api Space in which SDK operates.
     * @param dataMgr     Manager for internal data storage.
     * @param log         Internal logger.
     */
    ServiceQueue(@NonNull String apiSpaceId, @NonNull  DataManager dataMgr, @NonNull  Logger log) {
        super(apiSpaceId, log);
        this.dataMgr = dataMgr;
        this.log = log;
        taskQueue = new TaskQueue();
    }

    /**
     * Gets session access token.
     *
     * @return Session access token.
     */
    protected String getToken() {
        return dataMgr.getSessionDAO().session() != null ? dataMgr.getSessionDAO().session().getAccessToken() : null;
    }

    /**
     * Gets service calls queue.
     *
     * @return Service calls queue.
     */
    TaskQueue getTaskQueue() {
        return taskQueue;
    }

    /**
     * Observables queue wrapper to store pending service calls.
     */
    class TaskQueue {

        ConcurrentLinkedQueue<AsyncSubject<String>> queue = new ConcurrentLinkedQueue<>();

        Observable<ComapiResult<MessageSentResponse>> queueSendMessage(@NonNull final String conversationId, @NonNull final MessageToSend message) {

            return createNewTask()
                    .flatMap(new Func1<String, Observable<ComapiResult<MessageSentResponse>>>() {
                        @Override
                        public Observable<ComapiResult<MessageSentResponse>> call(String token) {
                            log.d("doSendMessage called from the service queue. " + queue.size() + " requests still pending.");
                            return doSendMessage(token, conversationId, message);
                        }
                    })
                    .doOnCompleted(this::executePending);

        }

        Observable<ComapiResult<UploadContentResponse>> queueUploadContent(@NonNull final String folder, @NonNull final ContentData body) {

            return createNewTask()
                    .flatMap(new Func1<String, Observable<ComapiResult<UploadContentResponse>>>() {
                        @Override
                        public Observable<ComapiResult<UploadContentResponse>> call(String token) {
                            log.d("doUploadContent called from the service queue. " + queue.size() + " requests still pending.");
                            return doUploadContent(token, folder, body.getName(), body);
                        }
                    })
                    .doOnCompleted(this::executePending);
        }

        Observable<ComapiResult<Map<String, Object>>> queueGetProfile(String profileId) {

            return createNewTask()
                    .flatMap(new Func1<String, Observable<ComapiResult<Map<String, Object>>>>() {
                        @Override
                        public Observable<ComapiResult<Map<String, Object>>> call(String token) {
                            log.d("doGetProfile called from the service queue. " + queue.size() + " requests still pending.");
                            return doGetProfile(token, profileId);
                        }
                    })
                    .doOnCompleted(this::executePending);
        }

        Observable<ComapiResult<List<Map<String, Object>>>> queueQueryProfiles(String queryString) {

            return createNewTask()
                    .flatMap(new Func1<String, Observable<ComapiResult<List<Map<String, Object>>>>>() {
                        @Override
                        public Observable<ComapiResult<List<Map<String, Object>>>> call(String token) {
                            log.d("doQueryProfiles called from the service queue. " + queue.size() + " requests still pending.");
                            return doQueryProfiles(token, queryString);
                        }
                    })
                    .doOnCompleted(this::executePending);
        }

        Observable<ComapiResult<Map<String, Object>>> queueUpdateProfile(Map<String, Object> profileDetails, String eTag) {

            return createNewTask()
                    .flatMap(new Func1<String, Observable<ComapiResult<Map<String, Object>>>>() {
                        @Override
                        public Observable<ComapiResult<Map<String, Object>>> call(String token) {
                            log.d("doUpdateProfile called from the service queue. " + queue.size() + " requests still pending.");
                            return doUpdateProfile(token, dataMgr.getSessionDAO().session().getProfileId(), profileDetails, eTag);
                        }
                    })
                    .doOnCompleted(this::executePending);
        }

        Observable<ComapiResult<Map<String, Object>>> queuePatchProfile(Map<String, Object> profileDetails, String eTag) {

            return createNewTask()
                    .flatMap(new Func1<String, Observable<ComapiResult<Map<String, Object>>>>() {
                        @Override
                        public Observable<ComapiResult<Map<String, Object>>> call(String token) {
                            log.d("doPatchProfile called from the service queue. " + queue.size() + " requests still pending.");
                            return doPatchProfile(token, dataMgr.getSessionDAO().session().getProfileId(), profileDetails, eTag);
                        }
                    })
                    .doOnCompleted(this::executePending);
        }

        Observable<ComapiResult<ConversationDetails>> queueCreateConversation(ConversationCreate request) {

            return createNewTask()
                    .flatMap(new Func1<String, Observable<ComapiResult<ConversationDetails>>>() {
                        @Override
                        public Observable<ComapiResult<ConversationDetails>> call(String token) {
                            log.d("doCreateConversation called from the service queue. " + queue.size() + " requests still pending.");
                            return doCreateConversation(token, request);
                        }
                    })
                    .doOnCompleted(this::executePending);
        }

        Observable<ComapiResult<Void>> queueDeleteConversation(String conversationId, String eTag) {

            return createNewTask()
                    .flatMap(new Func1<String, Observable<ComapiResult<Void>>>() {
                        @Override
                        public Observable<ComapiResult<Void>> call(String token) {
                            log.d("doDeleteConversation called from the service queue. " + queue.size() + " requests still pending.");
                            return doDeleteConversation(token, conversationId, eTag);
                        }
                    })
                    .doOnCompleted(this::executePending);
        }

        Observable<ComapiResult<ConversationDetails>> queueGetConversation(String conversationId) {

            return createNewTask()
                    .flatMap(new Func1<String, Observable<ComapiResult<ConversationDetails>>>() {
                        @Override
                        public Observable<ComapiResult<ConversationDetails>> call(String token) {
                            log.d("doGetConversation called from the service queue. " + queue.size() + " requests still pending.");
                            return doGetConversation(token, conversationId);
                        }
                    })
                    .doOnCompleted(this::executePending);
        }

        Observable<ComapiResult<List<ConversationDetails>>> queueGetConversations(Scope scope) {

            return createNewTask()
                    .flatMap(new Func1<String, Observable<ComapiResult<List<ConversationDetails>>>>() {
                        @Override
                        public Observable<ComapiResult<List<ConversationDetails>>> call(String token) {
                            log.d("doGetConversations called from the service queue. " + queue.size() + " requests still pending.");
                            return doGetConversations(token, dataMgr.getSessionDAO().session().getProfileId(), scope).map(result -> {
                                List<ConversationDetails> newList = new ArrayList<>();
                                List<Conversation> oldList = result.getResult();
                                if (oldList != null && !oldList.isEmpty()) {
                                    newList.addAll(oldList);
                                }
                                return new ComapiResult<>(result, newList);
                            });
                        }
                    })
                    .doOnCompleted(this::executePending);
        }

        Observable<ComapiResult<List<Conversation>>> queueGetConversationsExt(Scope scope) {

            return createNewTask()
                    .flatMap(new Func1<String, Observable<ComapiResult<List<Conversation>>>>() {
                        @Override
                        public Observable<ComapiResult<List<Conversation>>> call(String token) {
                            log.d("doGetConversations called from the service queue. " + queue.size() + " requests still pending.");
                            return doGetConversations(token, dataMgr.getSessionDAO().session().getProfileId(), scope);
                        }
                    })
                    .doOnCompleted(this::executePending);
        }

        Observable<ComapiResult<ConversationDetails>> queueUpdateConversation(String conversationId, ConversationUpdate request, String eTag) {

            return createNewTask()
                    .flatMap(new Func1<String, Observable<ComapiResult<ConversationDetails>>>() {
                        @Override
                        public Observable<ComapiResult<ConversationDetails>> call(String token) {
                            log.d("doUpdateConversation called from the service queue. " + queue.size() + " requests still pending.");
                            return doUpdateConversation(token, conversationId, request, eTag);
                        }
                    })
                    .doOnCompleted(this::executePending);
        }

        Observable<ComapiResult<Void>> queueRemoveParticipants(String conversationId, List<String> ids) {

            return createNewTask()
                    .flatMap(new Func1<String, Observable<ComapiResult<Void>>>() {
                        @Override
                        public Observable<ComapiResult<Void>> call(String token) {
                            log.d("doRemoveParticipants called from the service queue. " + queue.size() + " requests still pending.");
                            return doRemoveParticipants(token, conversationId, ids);
                        }
                    })
                    .doOnCompleted(this::executePending);
        }

        Observable<ComapiResult<List<Participant>>> queueGetParticipants(String conversationId) {

            return createNewTask()
                    .flatMap(new Func1<String, Observable<ComapiResult<List<Participant>>>>() {
                        @Override
                        public Observable<ComapiResult<List<Participant>>> call(String token) {
                            log.d("doGetParticipants called from the service queue. " + queue.size() + " requests still pending.");
                            return doGetParticipants(token, conversationId);
                        }
                    })
                    .doOnCompleted(this::executePending);
        }

        Observable<ComapiResult<Void>> queueAddParticipants(String conversationId, List<Participant> participants) {

            return createNewTask()
                    .flatMap(new Func1<String, Observable<ComapiResult<Void>>>() {
                        @Override
                        public Observable<ComapiResult<Void>> call(String token) {
                            log.d("doAddParticipants called from the service queue. " + queue.size() + " requests still pending.");
                            return doAddParticipants(token, conversationId, participants);
                        }
                    })
                    .doOnCompleted(this::executePending);
        }

        Observable<ComapiResult<Void>> queueUpdateMessageStatus(String conversationId, List<MessageStatusUpdate> msgStatusList) {

            return createNewTask()
                    .flatMap(new Func1<String, Observable<ComapiResult<Void>>>() {
                        @Override
                        public Observable<ComapiResult<Void>> call(String token) {
                            log.d("doUpdateMessageStatus called from the service queue. " + queue.size() + " requests still pending.");
                            return doUpdateMessageStatus(token, conversationId, msgStatusList);
                        }
                    })
                    .doOnCompleted(this::executePending);
        }

        Observable<ComapiResult<Void>> queueUpdatePushMessageStatus(String messageId, String status) {

            return createNewTask()
                    .flatMap((Func1<String, Observable<ComapiResult<Void>>>) token -> {
                        log.d("doUpdatePushMessageStatus called from the service queue. " + queue.size() + " requests still pending.");
                        return doUpdatePushMessageStatus(token, messageId, status);
                    })
                    .doOnCompleted(this::executePending);
        }

        Observable<ComapiResult<EventsQueryResponse>> queueQueryEvents(String conversationId, Long from, Integer limit) {

            return createNewTask()
                    .flatMap(new Func1<String, Observable<ComapiResult<EventsQueryResponse>>>() {
                        @Override
                        public Observable<ComapiResult<EventsQueryResponse>> call(String token) {
                            log.d("doQueryEvents called from the service queue. " + queue.size() + " requests still pending.");
                            return doQueryEvents(token, conversationId, from, limit);
                        }
                    })
                    .doOnCompleted(this::executePending);
        }

        Observable<ComapiResult<ConversationEventsResponse>> queueQueryConversationEvents(String conversationId, Long from, Integer limit) {

            return createNewTask()
                    .flatMap(new Func1<String, Observable<ComapiResult<ConversationEventsResponse>>>() {
                        @Override
                        public Observable<ComapiResult<ConversationEventsResponse>> call(String token) {
                            log.d("doQueryEvents called from the service queue. " + queue.size() + " requests still pending.");
                            return doQueryConversationEvents(token, conversationId, from, limit);
                        }
                    })
                    .doOnCompleted(this::executePending);
        }

        Observable<ComapiResult<MessagesQueryResponse>> queueQueryMessages(String conversationId, Long from, Integer limit) {

            return createNewTask()
                    .flatMap(new Func1<String, Observable<ComapiResult<MessagesQueryResponse>>>() {
                        @Override
                        public Observable<ComapiResult<MessagesQueryResponse>> call(String token) {
                            log.d("doQueryMessages called from the service queue. " + queue.size() + " requests still pending.");
                            return doQueryMessages(token, conversationId, from, limit);
                        }
                    })
                    .doOnCompleted(this::executePending);
        }

        Observable<ComapiResult<String>> createFbOptInState() {

            return createNewTask()
                    .flatMap(new Func1<String, Observable<ComapiResult<String>>>() {
                        @Override
                        public Observable<ComapiResult<String>> call(String token) {
                            log.d("createFbOptInState called from the service queue. " + queue.size() + " requests still pending.");
                            return doCreateFbOptInState(token);
                        }
                    })
                    .doOnCompleted(this::executePending);
        }

        /**
         * Executes pending service calls.
         */
        void executePending() {
            AsyncSubject<String> next = queue.poll();
            if (next != null) {
                next.onNext(dataMgr.getSessionDAO().session().getAccessToken());
                next.onCompleted();
            }
        }

        /**
         * Creates AsyncSubject representing new pending task in a queue. Will be mapped to pending service call and blocked till session controller finishes authentication process.
         *
         * @return New pending task.
         */
        private AsyncSubject<String> createNewTask() {
            AsyncSubject<String> subject = AsyncSubject.create();
            queue.add(subject);
            return subject;
        }
    }
}