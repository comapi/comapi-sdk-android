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

/**
 * Separates access to subsets of service APIs.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ServiceAccessor {

    private InternalService service;

    /**
     * Recommended constructor.
     *
     * @param service COMAPI service interface.
     */
    ServiceAccessor(@NonNull InternalService service) {
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
         * @param callback Callback with the result.
         */
        void startSession(@Nullable Callback<Session> callback);

        /**
         * Ends currently active session.
         *
         * @param callback Callback with the result.
         */
        void endSession(@Nullable Callback<ComapiResult<Void>> callback);
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
         * @param callback  Callback with the result.
         */
        void getProfile(@NonNull final String profileId, @Nullable Callback<ComapiResult<Map<String, Object>>> callback);

        /**
         * Query user profiles on the services.
         *
         * @param queryString Query string. See https://www.npmjs.com/package/mongo-querystring for query syntax. You can use {@link QueryBuilder} helper class to construct valid query string.
         * @param callback    Callback with the result.
         */
        void queryProfiles(@NonNull final String queryString, @Nullable Callback<ComapiResult<List<Map<String, Object>>>> callback);

        /**
         * Updates profile for an active session.
         *
         * @param profileDetails Profile details.
         * @param callback       Callback with the result.
         */
        void updateProfile(@NonNull final Map<String, Object> profileDetails, final String eTag, @Nullable Callback<ComapiResult<Map<String, Object>>> callback);
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
         * @param request  Request with conversation details to create.
         * @param callback Callback with the result.
         */
        void createConversation(@NonNull final ConversationCreate request, @Nullable Callback<ComapiResult<ConversationDetails>> callback);

        /**
         * Returns observable to create a conversation.
         *
         * @param conversationId ID of a conversation to delete.
         * @param eTag           Tag to specify local data version. Can be null.
         * @param callback       Callback with the result.
         */
        void deleteConversation(@NonNull final String conversationId, final String eTag, @Nullable Callback<ComapiResult<Void>> callback);

        /**
         * Returns observable to create a conversation.
         *
         * @param conversationId ID of a conversation to obtain.
         * @param callback       Callback with the result.
         */
        void getConversation(@NonNull final String conversationId, @Nullable Callback<ComapiResult<ConversationDetails>> callback);

        /**
         * Returns observable to get all visible conversations.
         *
         * @param scope    {@link Scope} of the query
         * @param callback Callback with the result.
         */
        void getConversations(@NonNull final Scope scope, @Nullable Callback<ComapiResult<List<ConversationDetails>>> callback);

        /**
         * Returns observable to update a conversation.
         *
         * @param eTag           Tag to specify local data version.
         * @param conversationId ID of a conversation to update.
         * @param request        Request with conversation details to update.
         * @param callback       Callback with the result.
         */
        void updateConversation(@NonNull final String conversationId, @NonNull final ConversationUpdate request, @NonNull final String eTag, @Nullable Callback<ComapiResult<ConversationDetails>> callback);

        /**
         * Returns observable to remove list of participants from a conversation.
         *
         * @param conversationId ID of a conversation to delete.
         * @param ids            List of participant ids to be removed.
         * @param callback       Callback with the result.
         */
        void removeParticipants(@NonNull final String conversationId, @NonNull final List<String> ids, @Nullable Callback<ComapiResult<Void>> callback);

        /**
         * Returns observable to add a participant to.
         *
         * @param conversationId ID of a conversation to add a participant to.
         * @param callback       Callback with the result.
         */
        void getParticipants(@NonNull final String conversationId, @Nullable Callback<ComapiResult<List<Participant>>> callback);

        /**
         * Returns observable to add a list of participants to a conversation.
         *
         * @param conversationId ID of a conversation to update.
         * @param participants   New conversation participants details.
         * @param callback       Callback with the result.
         */
        void addParticipants(@NonNull final String conversationId, @NonNull final List<Participant> participants, @Nullable Callback<ComapiResult<Void>> callback);

        /**
         * Send message to the conversation.
         *
         * @param conversationId ID of a conversation to send a message to.
         * @param message        Message to be send.
         * @param callback       Callback with the result.
         */
        void sendMessage(@NonNull final String conversationId, @NonNull final MessageToSend message, @Nullable Callback<ComapiResult<MessageSentResponse>> callback);

        /**
         * Send message to the chanel.
         *
         * @param conversationId ID of a conversation to send a message to.
         * @param body           Message body to be send.
         * @param callback       Callback with the result.
         */
        void sendMessage(@NonNull final String conversationId, @NonNull final String body, @Nullable Callback<ComapiResult<MessageSentResponse>> callback);

        /**
         * Sets statuses for sets of messages.
         *
         * @param conversationId ID of a conversation to modify.
         * @param msgStatusList  List of status modifications.
         * @param callback       Callback with the result.
         */
        void updateMessageStatus(@NonNull final String conversationId, @NonNull final List<MessageStatusUpdate> msgStatusList, @Nullable Callback<ComapiResult<Void>> callback);

        /**
         * Query conversation events.
         *
         * @param conversationId ID of a conversation to query events in it.
         * @param from           ID of the event to start from.
         * @param limit          Limit of events to obtain in this call.
         * @param callback       Callback with the result.
         */
        void queryEvents(@NonNull final String conversationId, @NonNull final Long from, @NonNull final Integer limit, @Nullable Callback<ComapiResult<EventsQueryResponse>> callback);

        /**
         * Query conversation messages.
         *
         * @param conversationId ID of a conversation to query messages in it.
         * @param from           ID of the message to start from.
         * @param limit          Limit of events to obtain in this call.
         * @param callback       Callback with the result.
         */
        void queryMessages(@NonNull final String conversationId, final Long from, @NonNull final Integer limit, @Nullable Callback<ComapiResult<MessagesQueryResponse>> callback);

        /**
         * Sends participant is typing in conversation event.
         *
         * @param conversationId ID of a conversation in which participant is typing a message.
         * @param callback       Callback with the result.
         */
        void isTyping(@NonNull final String conversationId, @Nullable Callback<ComapiResult<Void>> callback);
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
         * @param callback Callback with the result.
         */
        void createFbOptInState(@Nullable Callback<ComapiResult<String>> callback);
    }
}
