package com.comapi.internal.network;

import android.app.Application;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import com.comapi.APIConfig;
import com.comapi.Callback;
import com.comapi.ComapiAuthenticator;
import com.comapi.QueryBuilder;
import com.comapi.Session;
import com.comapi.internal.CallbackAdapter;
import com.comapi.internal.ComapiException;
import com.comapi.internal.ISessionListener;
import com.comapi.internal.data.DataManager;
import com.comapi.internal.data.SessionData;
import com.comapi.internal.helpers.APIHelper;
import com.comapi.internal.log.LogLevel;
import com.comapi.internal.log.Logger;
import com.comapi.internal.network.api.ComapiService;
import com.comapi.internal.network.api.RestApi;
import com.comapi.internal.network.api.RxComapiService;
import com.comapi.internal.network.model.conversation.ConversationCreate;
import com.comapi.internal.network.model.conversation.ConversationDetails;
import com.comapi.internal.network.model.conversation.ConversationUpdate;
import com.comapi.internal.network.model.conversation.Participant;
import com.comapi.internal.network.model.conversation.Scope;
import com.comapi.internal.network.model.messaging.EventsQueryResponse;
import com.comapi.internal.network.model.messaging.MessageSentResponse;
import com.comapi.internal.network.model.messaging.MessageStatusUpdate;
import com.comapi.internal.network.model.messaging.MessageToSend;
import com.comapi.internal.network.model.messaging.MessagesQueryResponse;
import com.comapi.internal.network.model.session.PushConfig;
import com.comapi.internal.network.sockets.SocketController;
import com.comapi.internal.network.sockets.SocketEventListener;
import com.comapi.internal.push.PushManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;


/**
 * Manages all service calls checking session state and redirecting to appropriate controllers.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class InternalService extends ServiceQueue implements ComapiService, RxComapiService {

    private final PushManager pushMgr;

    /**
     * Observables to callbacks adapter.
     */
    private final CallbackAdapter adapter;

    @SuppressWarnings("FieldCanBeLocal")
    private RestClient restClient;

    private SessionController sessionController;

    /**
     * App package name.
     */
    private final String packageName;

    /**
     * Recommended constructor.
     *
     * @param application Application instance.
     * @param adapter     Observables to callbacks adapter.
     * @param dataMgr     Internal data storage access.
     * @param pushMgr     Push messaging manager.
     * @param apiSpaceId  Comapi API Space.
     * @param packageName App package name.
     * @param log         Internal logger.
     */
    public InternalService(Application application, @NonNull CallbackAdapter adapter, @NonNull final DataManager dataMgr, PushManager pushMgr, String apiSpaceId, @NonNull final String packageName, @NonNull final Logger log) {
        super(application, apiSpaceId, dataMgr, log);
        this.adapter = adapter;
        this.pushMgr = pushMgr;
        this.packageName = packageName;
    }

    /**
     * Initialise REST API client.
     *
     * @param logLevelNet {@link LogLevel} of for the network library.
     * @param baseURIs    API baseURIs.
     * @return REST API client.
     */
    public RestApi initialiseRestClient(int logLevelNet, APIConfig.BaseURIs baseURIs) {

        AuthManager authManager = new AuthManager() {
            @Override
            protected Observable<SessionData> restartSession() {
                return reAuthenticate();
            }
        };

        restClient = new RestClient(new OkHttpAuthenticator(authManager), logLevelNet, baseURIs.getService().toString());
        service = restClient.getService();
        setService(service);
        return service;
    }

    /**
     * Initialise controller for creating and managing session.
     *
     * @param application          Application instance.
     * @param sessionCreateManager Manager for the process of creation of a new session
     * @param pushMgr              Push messaging manager.
     * @param state                SDK global state.
     * @param auth                 ComapiImplementation calls authentication request callback
     * @param restApi              Rest API definitions.
     * @param handler              Main thread handler.
     * @param sessionListener      Listener for new sessions.
     * @return Controller for creating and managing session.
     */
    public SessionController initialiseSessionController(Application application, @NonNull SessionCreateManager sessionCreateManager, PushManager pushMgr, @NonNull AtomicInteger state, @NonNull ComapiAuthenticator auth, @NonNull RestApi restApi, Handler handler, @NonNull final ISessionListener sessionListener) {
        sessionController = new SessionController(application, sessionCreateManager, pushMgr, state, dataMgr, auth, restApi, packageName, handler, log, getTaskQueue(), sessionListener);
        return sessionController;
    }

    /**
     * Initialise client for managing socket connections.
     *
     * @param sessionController Controller for creating and managing session.
     * @param listener          Listener for socket events.
     * @param baseURIs          APIs baseURIs.
     * @return Client for managing socket connections.
     */
    public SocketController initialiseSocketClient(@NonNull SessionController sessionController, SocketEventListener listener, APIConfig.BaseURIs baseURIs) {
        SocketController socketController = new SocketController(dataMgr, listener, log, baseURIs.getSocket(), baseURIs.getProxy());
        sessionController.setSocketController(socketController);
        if (isSessionValid()) {
            socketController.connectSocket();
        }
        return socketController;
    }

    /**
     * Create and start new Comapi session.
     *
     * @return True if session was started.
     */
    public Observable<Session> startSession() {
        return wrapObservable(sessionController.startSession().map(Session::new));
    }

    /**
     * Create and start new ComapiImplementation session.
     *
     * @param callback Callback to deliver new session instance.
     */
    public void startSession(Callback<Session> callback) {
        adapter.adapt(startSession(), callback);
    }

    /**
     * Ends currently active session.
     *
     * @return Observable to end current session.
     */
    public Observable<ComapiResult<Void>> endSession() {
        if (isSessionValid()) {
            return wrapObservable(sessionController.endSession().map(mapToComapiResult()));
        } else {
            //return Observable.onError(getSessionStateErrorDescription());
            return Observable.just(null);
        }
    }

    /**
     * Ends currently active session.
     */
    public void endSession(Callback<ComapiResult<Void>> callback) {
        adapter.adapt(endSession(), callback);
    }

    /**
     * Recreate session when authentication expires.
     *
     * @return Observable emitting new session.
     */
    public Observable<SessionData> reAuthenticate() {
        return wrapObservable(sessionController.reAuthenticate());
    }

    /**
     * Gets an observable task for push token registration. Will emit FCM push token for provided senderId.
     *
     * @return Observable task for push token registration.
     */
    public Observable<Pair<SessionData, ComapiResult<Void>>> updatePushToken() {

        final SessionData session = dataMgr.getSessionDAO().session();

        if (isSessionValid(session)) {

            return wrapObservable(Observable.create((Observable.OnSubscribe<String>) sub -> {
                String token = dataMgr.getDeviceDAO().device().getPushToken();
                if (TextUtils.isEmpty(token)) {
                    token = pushMgr.getPushToken();
                    if (!TextUtils.isEmpty(token)) {
                        dataMgr.getDeviceDAO().setPushToken(token);
                    }
                }
                sub.onNext(token);
                sub.onCompleted();
            }).concatMap(token -> service.updatePushToken(AuthManager.addAuthPrefix(session.getAccessToken()), apiSpaceId, session.getSessionId(), new PushConfig(packageName, token)).map(mapToComapiResult()))
                    .map(result -> new Pair<>(session, result)));
        } else {
            return Observable.error(getSessionStateErrorDescription());
        }
    }

    /**
     * Get profile details from the service.
     *
     * @param profileId Profile Id of the user.
     * @return Profile details from the service.
     */
    public Observable<ComapiResult<Map<String, Object>>> getProfile(@NonNull final String profileId) {

        final String token = getToken();

        if (sessionController.isCreatingSession()) {
            return getTaskQueue().queueGetProfile(profileId);
        } else if (TextUtils.isEmpty(token)) {
            return Observable.error(getSessionStateErrorDescription());
        } else {
            return doGetProfile(token, profileId);
        }
    }

    /**
     * Get profile details from the service.
     *
     * @param profileId Profile Id of the user.
     * @param callback  Callback to deliver new session instance.
     */
    public void getProfile(@NonNull final String profileId, @Nullable Callback<ComapiResult<Map<String, Object>>> callback) {
        adapter.adapt(getProfile(profileId), callback);
    }

    /**
     * Query user profiles on the services.
     *
     * @param queryString Query string. See https://www.npmjs.com/package/mongo-querystring for query syntax. You can use {@link QueryBuilder} helper class to construct valid query string.
     * @return Profiles detail from the service.
     */
    public Observable<ComapiResult<List<Map<String, Object>>>> queryProfiles(@NonNull final String queryString) {

        final String token = getToken();

        if (sessionController.isCreatingSession()) {
            return getTaskQueue().queueQueryProfiles(queryString);
        } else if (TextUtils.isEmpty(token)) {
            return Observable.error(getSessionStateErrorDescription());
        } else {
            return doQueryProfiles(token, queryString);
        }
    }

    /**
     * Query user profiles on the services.
     *
     * @param queryString Query string. See https://www.npmjs.com/package/mongo-querystring for query syntax. You can use {@link QueryBuilder} helper class to construct valid query string.
     * @param callback    Callback to deliver new session instance.
     */
    public void queryProfiles(@NonNull final String queryString, @Nullable Callback<ComapiResult<List<Map<String, Object>>>> callback) {
        adapter.adapt(queryProfiles(queryString), callback);
    }

    /**
     * Updates profile for an active session.
     *
     * @param profileDetails Profile details.
     * @param eTag           ETag for server to check if local version of the data is the same as the one the server side.
     * @return Observable with to perform update profile for current session.
     */
    public Observable<ComapiResult<Map<String, Object>>> updateProfile(@NonNull final Map<String, Object> profileDetails, final String eTag) {

        final String token = getToken();

        if (sessionController.isCreatingSession()) {
            return getTaskQueue().queueUpdateProfile(profileDetails, eTag);
        } else if (TextUtils.isEmpty(token)) {
            return Observable.error(getSessionStateErrorDescription());
        } else {
            return doUpdateProfile(token, dataMgr.getSessionDAO().session().getProfileId(), profileDetails, eTag);
        }
    }

    /**
     * Updates profile for an active session.
     *
     * @param profileDetails Profile details.
     * @param callback       Callback to deliver new session instance.
     */
    public void updateProfile(@NonNull final Map<String, Object> profileDetails, final String eTag, @Nullable Callback<ComapiResult<Map<String, Object>>> callback) {
        adapter.adapt(updateProfile(profileDetails, eTag), callback);
    }

    /**
     * Returns observable to create a conversation.
     *
     * @param request Request with conversation details to create.
     * @return Observable to to create a conversation.
     */
    public Observable<ComapiResult<ConversationDetails>> createConversation(@NonNull final ConversationCreate request) {

        final String token = getToken();

        if (sessionController.isCreatingSession()) {
            return getTaskQueue().queueCreateConversation(request);
        } else if (TextUtils.isEmpty(token)) {
            return Observable.error(getSessionStateErrorDescription());
        } else {
            return doCreateConversation(token, request);
        }
    }

    /**
     * Returns observable to create a conversation.
     *
     * @param request  Request with conversation details to create.
     * @param callback Callback to deliver new session instance.
     */
    public void createConversation(@NonNull final ConversationCreate request, @Nullable Callback<ComapiResult<ConversationDetails>> callback) {
        adapter.adapt(createConversation(request), callback);
    }

    /**
     * Returns observable to create a conversation.
     *
     * @param conversationId ID of a conversation to delete.
     * @param eTag           ETag for server to check if local version of the data is the same as the one the server side.
     * @return Observable to to create a conversation.
     */
    public Observable<ComapiResult<Void>> deleteConversation(@NonNull final String conversationId, final String eTag) {

        final String token = getToken();

        if (sessionController.isCreatingSession()) {
            return getTaskQueue().queueDeleteConversation(conversationId, eTag);
        } else if (TextUtils.isEmpty(token)) {
            return Observable.error(getSessionStateErrorDescription());
        } else {
            return doDeleteConversation(token, conversationId, eTag);
        }
    }

    /**
     * Returns observable to create a conversation.
     *
     * @param conversationId ID of a conversation to delete.
     * @param eTag           ETag for server to check if local version of the data is the same as the one the server side.
     * @param callback       Callback to deliver new session instance.
     */
    public void deleteConversation(@NonNull final String conversationId, final String eTag, @Nullable Callback<ComapiResult<Void>> callback) {
        adapter.adapt(deleteConversation(conversationId, eTag), callback);
    }

    /**
     * Returns observable to create a conversation.
     *
     * @param conversationId ID of a conversation to obtain.
     * @return Observable to to create a conversation.
     */
    public Observable<ComapiResult<ConversationDetails>> getConversation(@NonNull final String conversationId) {

        final String token = getToken();

        if (sessionController.isCreatingSession()) {
            return getTaskQueue().queueGetConversation(conversationId);
        } else if (TextUtils.isEmpty(token)) {
            return Observable.error(getSessionStateErrorDescription());
        } else {
            return doGetConversation(token, conversationId);
        }
    }

    /**
     * Returns observable to create a conversation.
     *
     * @param conversationId ID of a conversation to obtain.
     * @param callback       Callback to deliver new session instance.
     */
    public void getConversation(@NonNull final String conversationId, @Nullable Callback<ComapiResult<ConversationDetails>> callback) {
        adapter.adapt(getConversation(conversationId), callback);
    }

    /**
     * Returns observable to get all visible conversations.
     *
     * @param scope {@link Scope} of the query
     * @return Observable to to create a conversation.
     */
    public Observable<ComapiResult<List<ConversationDetails>>> getConversations(@NonNull final Scope scope) {

        final String token = getToken();

        if (sessionController.isCreatingSession()) {
            return getTaskQueue().queueGetConversations(scope);
        } else if (TextUtils.isEmpty(token)) {
            return Observable.error(getSessionStateErrorDescription());
        } else {
            return doGetConversations(token, dataMgr.getSessionDAO().session().getProfileId(), scope);
        }
    }

    /**
     * Returns observable to get all visible conversations.
     *
     * @param scope    {@link Scope} of the query
     * @param callback Callback to deliver new session instance.
     */
    public void getConversations(@NonNull final Scope scope, @Nullable Callback<ComapiResult<List<ConversationDetails>>> callback) {
        adapter.adapt(getConversations(scope), callback);
    }

    /**
     * Returns observable to update a conversation.
     *
     * @param conversationId ID of a conversation to update.
     * @param request        Request with conversation details to update.
     * @param eTag           Tag to specify local data version.
     * @return Observable to update a conversation.
     */
    public Observable<ComapiResult<ConversationDetails>> updateConversation(@NonNull final String conversationId, @NonNull final ConversationUpdate request, @Nullable final String eTag) {

        final String token = getToken();

        if (sessionController.isCreatingSession()) {
            return getTaskQueue().queueUpdateConversation(conversationId, request, eTag);
        } else if (TextUtils.isEmpty(token)) {
            return Observable.error(getSessionStateErrorDescription());
        } else {
            return doUpdateConversation(token, conversationId, request, eTag);
        }
    }

    /**
     * Returns observable to update a conversation.
     *
     * @param conversationId ID of a conversation to update.
     * @param request        Request with conversation details to update.
     * @param eTag           ETag for server to check if local version of the data is the same as the one the server side.
     * @param callback       Callback to deliver new session instance.
     */
    public void updateConversation(@NonNull final String conversationId, @NonNull final ConversationUpdate request, @NonNull final String eTag, @Nullable Callback<ComapiResult<ConversationDetails>> callback) {
        adapter.adapt(updateConversation(conversationId, request, eTag), callback);
    }

    /**
     * Returns observable to remove list of participants from a conversation.
     *
     * @param conversationId ID of a conversation to delete.
     * @param ids            List of participant ids to be removed.
     * @return Observable to remove list of participants from a conversation.
     */
    public Observable<ComapiResult<Void>> removeParticipants(@NonNull final String conversationId, @NonNull final List<String> ids) {

        final String token = getToken();

        if (sessionController.isCreatingSession()) {
            return getTaskQueue().queueRemoveParticipants(conversationId, ids);
        } else if (TextUtils.isEmpty(token)) {
            return Observable.error(getSessionStateErrorDescription());
        } else {
            return doRemoveParticipants(token, conversationId, ids);
        }
    }

    /**
     * Returns observable to remove list of participants from a conversation.
     *
     * @param conversationId ID of a conversation to delete.
     * @param ids            List of participant ids to be removed.
     * @param callback       Callback to deliver new session instance.
     */
    public void removeParticipants(@NonNull final String conversationId, @NonNull final List<String> ids, @Nullable Callback<ComapiResult<Void>> callback) {
        adapter.adapt(removeParticipants(conversationId, ids), callback);
    }

    /**
     * Returns observable to add a participant to.
     *
     * @param conversationId ID of a conversation to add a participant to.
     * @return Observable to get a list of conversation participants.
     */
    public Observable<ComapiResult<List<Participant>>> getParticipants(@NonNull final String conversationId) {

        final String token = getToken();

        if (sessionController.isCreatingSession()) {
            return getTaskQueue().queueGetParticipants(conversationId);
        } else if (TextUtils.isEmpty(token)) {
            return Observable.error(getSessionStateErrorDescription());
        } else {
            return doGetParticipants(token, conversationId);
        }
    }

    /**
     * Returns observable to add a participant to.
     *
     * @param conversationId ID of a conversation to add a participant to.
     * @param callback       Callback to deliver new session instance.
     */
    public void getParticipants(@NonNull final String conversationId, @Nullable Callback<ComapiResult<List<Participant>>> callback) {
        adapter.adapt(getParticipants(conversationId), callback);
    }

    /**
     * Returns observable to add a list of participants to a conversation.
     *
     * @param conversationId ID of a conversation to update.
     * @param participants   New conversation participants details.
     * @return Observable to add participants to a conversation.
     */
    public Observable<ComapiResult<Void>> addParticipants(@NonNull final String conversationId, @NonNull final List<Participant> participants) {

        final String token = getToken();

        if (sessionController.isCreatingSession()) {
            return getTaskQueue().queueAddParticipants(conversationId, participants);
        } else if (TextUtils.isEmpty(token)) {
            return Observable.error(getSessionStateErrorDescription());
        } else {
            return doAddParticipants(token, conversationId, participants);
        }
    }

    /**
     * Returns observable to add a list of participants to a conversation.
     *
     * @param conversationId ID of a conversation to update.
     * @param participants   New conversation participants details.
     * @param callback       Callback to deliver new session instance.
     */
    public void addParticipants(@NonNull final String conversationId, @NonNull final List<Participant> participants, @Nullable Callback<ComapiResult<Void>> callback) {
        adapter.adapt(addParticipants(conversationId, participants), callback);
    }

    /**
     * Send message to the chanel.
     *
     * @param conversationId ID of a conversation to send a message to.
     * @param message        Message to be send.
     * @return Observable to send message to a conversation.
     */
    public Observable<ComapiResult<MessageSentResponse>> sendMessage(@NonNull final String conversationId, @NonNull final MessageToSend message) {

        final String token = getToken();

        if (sessionController.isCreatingSession()) {
            return getTaskQueue().queueSendMessage(conversationId, message);
        } else if (TextUtils.isEmpty(token)) {
            return Observable.error(getSessionStateErrorDescription());
        } else {
            return doSendMessage(token, conversationId, message);
        }
    }

    /**
     * Send message to the chanel.
     *
     * @param conversationId ID of a conversation to send a message to.
     * @param message        Message to be send.
     * @param callback       Callback to deliver new session instance.
     */
    public void sendMessage(@NonNull final String conversationId, @NonNull final MessageToSend message, @Nullable Callback<ComapiResult<MessageSentResponse>> callback) {
        adapter.adapt(sendMessage(conversationId, message), callback);
    }

    /**
     * Send message to the chanel.
     *
     * @param conversationId ID of a conversation to send a message to.
     * @param body           Message body to be send.
     * @param callback       Callback to deliver new session instance.
     */
    public void sendMessage(@NonNull final String conversationId, @NonNull final String body, @Nullable Callback<ComapiResult<MessageSentResponse>> callback) {
        SessionData session = dataMgr.getSessionDAO().session();
        adapter.adapt(sendMessage(conversationId, APIHelper.createMessage(conversationId, body, session != null ? session.getProfileId() : null)), callback);
    }

    /**
     * Send message to the chanel.
     *
     * @param conversationId ID of a conversation to send a message to.
     * @param body           Message body to be send.
     * @return Observable to send message to a conversation.
     */
    public Observable<ComapiResult<MessageSentResponse>> sendMessage(@NonNull final String conversationId, @NonNull final String body) {
        SessionData session = dataMgr.getSessionDAO().session();
        return sendMessage(conversationId, APIHelper.createMessage(conversationId, body, session != null ? session.getProfileId() : null));
    }

    /**
     * Sets statuses for sets of messages.
     *
     * @param conversationId ID of a conversation to modify.
     * @param msgStatusList  List of status modifications.
     * @return Observable to modify message statuses.
     */
    public Observable<ComapiResult<Void>> updateMessageStatus(@NonNull final String conversationId, @NonNull final List<MessageStatusUpdate> msgStatusList) {

        final String token = getToken();

        if (sessionController.isCreatingSession()) {
            return getTaskQueue().queueUpdateMessageStatus(conversationId, msgStatusList);
        } else if (TextUtils.isEmpty(token)) {
            return Observable.error(getSessionStateErrorDescription());
        } else {
            return doUpdateMessageStatus(token, conversationId, msgStatusList);
        }
    }

    /**
     * Sets statuses for sets of messages.
     *
     * @param conversationId ID of a conversation to modify.
     * @param msgStatusList  List of status modifications.
     * @param callback       Callback to deliver new session instance.
     */
    public void updateMessageStatus(@NonNull final String conversationId, @NonNull final List<MessageStatusUpdate> msgStatusList, @Nullable Callback<ComapiResult<Void>> callback) {
        adapter.adapt(updateMessageStatus(conversationId, msgStatusList), callback);
    }

    /**
     * Query chanel events.
     *
     * @param conversationId ID of a conversation to query events in it.
     * @param from           ID of the event to start from.
     * @param limit          Limit of events to obtain in this call.
     * @return Observable to get events from a conversation.
     */
    public Observable<ComapiResult<EventsQueryResponse>> queryEvents(@NonNull final String conversationId, @NonNull final Long from, @NonNull final Integer limit) {

        final String token = getToken();

        if (sessionController.isCreatingSession()) {
            return getTaskQueue().queueQueryEvents(conversationId, from, limit);
        } else if (TextUtils.isEmpty(token)) {
            return Observable.error(getSessionStateErrorDescription());
        } else {
            return doQueryEvents(token, conversationId, from, limit);
        }
    }

    /**
     * Query chanel events.
     *
     * @param conversationId ID of a conversation to query events in it.
     * @param from           ID of the event to start from.
     * @param limit          Limit of events to obtain in this call.
     * @param callback       Callback to deliver new session instance.
     */
    public void queryEvents(@NonNull final String conversationId, @NonNull final Long from, @NonNull final Integer limit, @Nullable Callback<ComapiResult<EventsQueryResponse>> callback) {
        adapter.adapt(queryEvents(conversationId, from, limit), callback);
    }

    /**
     * Query chanel messages.
     *
     * @param conversationId ID of a conversation to query messages in it.
     * @param from           ID of the message to start from.
     * @param limit          Limit of events to obtain in this call.
     * @return Observable to get messages in a conversation.
     */
    public Observable<ComapiResult<MessagesQueryResponse>> queryMessages(@NonNull final String conversationId, final Long from, @NonNull final Integer limit) {

        final String token = getToken();

        if (sessionController.isCreatingSession()) {
            return getTaskQueue().queueQueryMessages(conversationId, from, limit);
        } else if (TextUtils.isEmpty(token)) {
            return Observable.error(getSessionStateErrorDescription());
        } else {
            return doQueryMessages(token, conversationId, from, limit);
        }
    }

    /**
     * Query chanel messages.
     *
     * @param conversationId ID of a conversation to query messages in it.
     * @param from           ID of the message to start from.
     * @param limit          Limit of events to obtain in this call.
     * @param callback       Callback to deliver new session instance.
     */
    public void queryMessages(@NonNull final String conversationId, final Long from, @NonNull final Integer limit, @Nullable Callback<ComapiResult<MessagesQueryResponse>> callback) {
        adapter.adapt(queryMessages(conversationId, from, limit), callback);
    }

    /**
     * Send 'user is typing'.
     *
     * @param conversationId ID of a conversation.
     * @return Observable to send event.
     */
    public Observable<ComapiResult<Void>> isTyping(@NonNull final String conversationId) {

        final String token = getToken();

        if (sessionController.isCreatingSession() || TextUtils.isEmpty(token)) {
            return Observable.error(getSessionStateErrorDescription());
        } else {
            return doIsTyping(token, conversationId);
        }
    }

    /**
     * Send 'user is typing' message for specified conversation.
     *
     * @param conversationId ID of a conversation.
     * @param callback       Callback to deliver result.
     */
    public void isTyping(@NonNull final String conversationId, @Nullable Callback<ComapiResult<Void>> callback) {
        adapter.adapt(isTyping(conversationId), callback);
    }

    /**
     * Is session successfully created.
     *
     * @return True if session is created.
     */
    private boolean isSessionValid() {
        return isSessionValid(dataMgr.getSessionDAO().session());
    }

    /**
     * Is session successfully created.
     *
     * @param session SessionData instance to validate.
     * @return True if session is created.
     */
    private boolean isSessionValid(final SessionData session) {
        return !sessionController.isCreatingSession() && session != null && session.getSessionId() != null && session.getAccessToken() != null;
    }

    /**
     * Creates exception for request that require a valid session but there is none.
     *
     * @return Exception
     */
    private ComapiException getSessionStateErrorDescription() {
        return new ComapiException("Session not started.");
    }

    /**
     * Gets the Facebook data-ref to create send to messenger button.
     *
     * @param callback Callback with the result.
     */
    @Override
    public void createFbOptInState(Callback<ComapiResult<String>> callback) {
        adapter.adapt(createFbOptInState(), callback);
    }

    /**
     * Gets the Facebook data-ref to create send to messenger button.
     *
     * @return @return Observable to get Facebook data-ref.
     */
    @Override
    public Observable<ComapiResult<String>> createFbOptInState() {

        final String token = getToken();

        if (sessionController.isCreatingSession()) {
            return getTaskQueue().createFbOptInState();
        } else if (TextUtils.isEmpty(token)) {
            return Observable.error(getSessionStateErrorDescription());
        } else {
            return doCreateFbOptInState(token);
        }
    }
}