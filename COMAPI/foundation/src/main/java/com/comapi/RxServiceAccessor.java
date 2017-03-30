package com.comapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.comapi.internal.network.ComapiResult;
import com.comapi.internal.network.InternalService;
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

import java.util.List;
import java.util.Map;

import rx.Observable;

/**
 * Separates access to subsets of service APIs.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RxServiceAccessor {

    private InternalService service;

    /**
     * Recommended constructor.
     *
     * @param service COMAPI service interface.
     */
    public RxServiceAccessor(@NonNull InternalService service) {
        this.service = service;
    }

    /**
     * Access COMAPI Service messaging APIs.
     *
     * @return COMAPI Service messaging APIs.
     */
    public MessagingService messaging() {
        return service;
    }

    /**
     * Access COMAPI Service profile APIs.
     *
     * @return COMAPI Service profile APIs.
     */
    public ProfileService profile() {
        return service;
    }

    /**
     * Access COMAPI Service session management APIs.
     *
     * @return COMAPI Service session management APIs.
     */
    public SessionService session() {
        return service;
    }

    /**
     * Access COMAPI Service support of various messaging channels.
     *
     * @return COMAPI Service support of various messaging channels.
     */
    public ChannelsService channels() {
        return service;
    }

    /**
     * Public interface to access ComapiImpl services limited to session management functionality.
     *
     * @author Marcin Swierczek
     * @since 1.0.0
     * Copyright (C) Donky Networks Ltd. All rights reserved.
     */
    public interface SessionService {

        /**
         * Create and start new ComapiImpl session.
         *
         * @return True if session was started.
         */
        Observable<Session> startSession();

        /**
         * Ends currently active session.
         *
         * @return Observable to end current session.
         */
        Observable<ComapiResult<Void>> endSession();
    }

    /**
     * Public interface to access ComapiImpl services limited to user profiles functionality.
     *
     * @author Marcin Swierczek
     * @since 1.0.0
     * Copyright (C) Donky Networks Ltd. All rights reserved.
     */
    public interface ProfileService {

        /**
         * Get profile details from the service.
         *
         * @param profileId Profile Id of the user.
         * @return Profile details from the service.
         */
        Observable<ComapiResult<Map<String, Object>>> getProfile(@NonNull final String profileId);

        /**
         * Query user profiles on the services.
         *
         * @param queryString Query string. See https://www.npmjs.com/package/mongo-querystring for query syntax. You can use {@link QueryBuilder} helper class to construct valid query string.
         * @return Profiles detail from the service.
         */
        Observable<ComapiResult<List<Map<String, Object>>>> queryProfiles(@NonNull final String queryString);

        /**
         * Updates profile for an active session.
         *
         * @param profileDetails Profile details.
         * @return Observable with to perform update profile for current session.
         */
        Observable<ComapiResult<Map<String, Object>>> updateProfile(@NonNull final Map<String, Object> profileDetails, final String eTag);
    }

    /**
     * Public interface to access ComapiImpl services limited to messaging functionality.
     *
     * @author Marcin Swierczek
     * @since 1.0.0
     * Copyright (C) Donky Networks Ltd. All rights reserved.
     */
    public interface MessagingService {

        /**
         * Returns observable to create a conversation.
         *
         * @param request Request with conversation details to create.
         * @return Observable to to create a conversation.
         */
        Observable<ComapiResult<ConversationDetails>> createConversation(@NonNull final ConversationCreate request);

        /**
         * Returns observable to create a conversation.
         *
         * @param conversationId ID of a conversation to delete.
         * @param eTag           Tag to specify local data version. Can be null.
         * @return Observable to to create a conversation.
         */
        Observable<ComapiResult<Void>> deleteConversation(@NonNull final String conversationId, final String eTag);

        /**
         * Returns observable to create a conversation.
         *
         * @param conversationId ID of a conversation to obtain.
         * @return Observable to to create a conversation.
         */
        Observable<ComapiResult<ConversationDetails>> getConversation(@NonNull final String conversationId);

        /**
         * Returns observable to get all visible conversations.
         *
         * @param scope {@link Scope} of the query
         * @return Observable to to create a conversation.
         */
        Observable<ComapiResult<List<ConversationDetails>>> getConversations(@NonNull final Scope scope);

        /**
         * Returns observable to update a conversation.
         *
         * @param conversationId ID of a conversation to update.
         * @param request        Request with conversation details to update.
         * @param eTag           Tag to specify local data version.
         * @return Observable to update a conversation.
         */
        Observable<ComapiResult<ConversationDetails>> updateConversation(@NonNull final String conversationId, @NonNull final ConversationUpdate request, @Nullable final String eTag);

        /**
         * Returns observable to remove list of participants from a conversation.
         *
         * @param conversationId ID of a conversation to delete.
         * @param ids            List of participant ids to be removed.
         * @return Observable to remove list of participants from a conversation.
         */
        Observable<ComapiResult<Void>> removeParticipants(@NonNull final String conversationId, @NonNull final List<String> ids);

        /**
         * Returns observable to add a participant to.
         *
         * @param conversationId ID of a conversation to add a participant to.
         * @return Observable to get a list of conversation participants.
         */
        Observable<ComapiResult<List<Participant>>> getParticipants(@NonNull final String conversationId);

        /**
         * Returns observable to add a list of participants to a conversation.
         *
         * @param conversationId ID of a conversation to update.
         * @param participants   New conversation participants details.
         * @return Observable to add participants to a conversation.
         */
        Observable<ComapiResult<Void>> addParticipants(@NonNull final String conversationId, @NonNull final List<Participant> participants);

        /**
         * Send message to the conversation.
         *
         * @param conversationId ID of a conversation to send a message to.
         * @param message        Message to be send.
         * @return Observable to send message to a conversation.
         */
        Observable<ComapiResult<MessageSentResponse>> sendMessage(@NonNull final String conversationId, @NonNull final MessageToSend message);

        /**
         * Send message to the chanel.
         *
         * @param conversationId ID of a conversation to send a message to.
         * @param body           Message body to be send.
         * @return Observable to send message to a conversation.
         */
        Observable<ComapiResult<MessageSentResponse>> sendMessage(@NonNull final String conversationId, @NonNull final String body);

        /**
         * Sets statuses for sets of messages.
         *
         * @param conversationId ID of a conversation to modify.
         * @param msgStatusList  List of status modifications.
         * @return Observable to modify message statuses.
         */
        Observable<ComapiResult<Void>> updateMessageStatus(@NonNull final String conversationId, @NonNull final List<MessageStatusUpdate> msgStatusList);

        /**
         * Query conversation events.
         *
         * @param conversationId ID of a conversation to query events in it.
         * @param from           ID of the event to start from.
         * @param limit          Limit of events to obtain in this call.
         * @return Observable to get events from a conversation.
         */
        Observable<ComapiResult<EventsQueryResponse>> queryEvents(@NonNull final String conversationId, @NonNull final Long from, @NonNull final Integer limit);

        /**
         * Query conversation messages.
         *
         * @param conversationId ID of a conversation to query messages in it.
         * @param from           ID of the message to start from.
         * @param limit          Limit of events to obtain in this call.
         * @return Observable to get messages in a conversation.
         */
        Observable<ComapiResult<MessagesQueryResponse>> queryMessages(@NonNull final String conversationId, final Long from, @NonNull final Integer limit);

        /**
         * Sends participant is typing in conversation event.
         *
         * @param conversationId ID of a conversation in which participant is typing a message.
         * @return Observable to send event.
         */
        Observable<ComapiResult<Void>> isTyping(@NonNull final String conversationId);
    }

    /**
     * Public interface for support of various messaging channels.
     *
     * @author Marcin Swierczek
     * @since 1.0.0
     * Copyright (C) Donky Networks Ltd. All rights reserved.
     */
    public interface ChannelsService {

        /**
         * Gets the Facebook data-ref to create send to messenger button.
         *
         * @return @return Observable to get Facebook data-ref.
         */
        Observable<ComapiResult<String>> createFbOptInState();
    }
}
