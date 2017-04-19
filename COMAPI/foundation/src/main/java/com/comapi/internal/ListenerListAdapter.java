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

package com.comapi.internal;

import com.comapi.Session;
import com.comapi.internal.log.Logger;
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
import com.comapi.internal.network.sockets.SocketEventListener;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Adapts single internal callbacks to external lists of callbacks.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public class ListenerListAdapter implements SocketEventListener, ISessionListener {

    private CopyOnWriteArrayList<IMessagingListener> messagingListeners;

    private CopyOnWriteArrayList<IStateListener> stateListeners;

    private CopyOnWriteArrayList<IProfileListener> profileEventListeners;

    private Logger log;

    /**
     * Recomended constructor.
     *
     * @param log Logger instance.
     */
    public ListenerListAdapter(Logger log) {
        this.messagingListeners = new CopyOnWriteArrayList<>();
        this.stateListeners = new CopyOnWriteArrayList<>();
        this.profileEventListeners = new CopyOnWriteArrayList<>();
        this.log = log;
    }

    @Override
    public void onSessionStart(Session session) {
        for (IStateListener listener : stateListeners) {
            try {
                listener.onSessionStart(session);
            } catch (Exception e) {
                logError(e, "Session start");
            }
        }
    }

    @Override
    public void onMessageSent(MessageSentEvent event) {
        for (IMessagingListener listener : messagingListeners) {
            try {
                listener.onMessage(event);
            } catch (Exception e) {
                logError(e, "message "+event.getMessageId());
            }
        }
    }

    /**
     * Dispatch conversation message update event.
     *
     * @param event Event to dispatch.
     */
    @Override
    public void onMessageDelivered(MessageDeliveredEvent event) {
        for (IMessagingListener listener : messagingListeners) {
            try {
                listener.onMessageDelivered(event);
            } catch (Exception e) {
                logError(e, "message delivered "+event.getMessageId());
            }
        }
    }

    /**
     * Dispatch conversation message update event.
     *
     * @param event Event to dispatch.
     */
    @Override
    public void onMessageRead(MessageReadEvent event) {
        for (IMessagingListener listener : messagingListeners) {
            try {
                listener.onMessageRead(event);
            } catch (Exception e) {
                logError(e, "message read "+event.getMessageId());
            }
        }
    }

    @Override
    public void onSocketStarted(SocketStartEvent event) {
        for (IStateListener listener : stateListeners) {
            try {
                listener.onSocketStart(event);
            } catch (Exception e) {
                logError(e, "socket started");
            }
        }
    }

    @Override
    public void onParticipantAdded(ParticipantAddedEvent event) {
        for (IMessagingListener listener : messagingListeners) {
            try {
                listener.onParticipantAdded(event);
            } catch (Exception e) {
                logError(e, "participant "+event.getProfileId()+" to "+event.getConversationId());
            }
        }
    }

    @Override
    public void onParticipantUpdated(ParticipantUpdatedEvent event) {
        for (IMessagingListener listener : messagingListeners) {
            try {
                listener.onParticipantUpdated(event);
            } catch (Exception e) {
                logError(e, "participant updated "+event.getProfileId()+" to "+event.getConversationId());
            }
        }
    }

    @Override
    public void onParticipantRemoved(ParticipantRemovedEvent event) {
        for (IMessagingListener listener : messagingListeners) {
            try {
                listener.onParticipantRemoved(event);
            } catch (Exception e) {
                logError(e, "participant removed "+event.getProfileId()+" to "+event.getConversationId());
            }
        }
    }

    @Override
    public void onConversationUpdated(ConversationUpdateEvent event) {
        for (IMessagingListener listener : messagingListeners) {
            try {
                listener.onConversationUpdated(event);
            } catch (Exception e) {
                logError(e, "conversation "+event.getConversationId()+" updated");
            }
        }
    }

    @Override
    public void onConversationDeleted(ConversationDeleteEvent event) {
        for (IMessagingListener listener : messagingListeners) {
            try {
                listener.onConversationDeleted(event);
            } catch (Exception e) {
                logError(e, "conversation "+event.getConversationId()+" deleted");
            }
        }
    }

    @Override
    public void onConversationUndeleted(ConversationUndeleteEvent event) {
        for (IMessagingListener listener : messagingListeners) {
            try {
                listener.onConversationUndeleted(event);
            } catch (Exception e) {
                logError(e, "conversation "+event.getConversationId()+" restored");
            }
        }
    }

    @Override
    public void onProfileUpdate(ProfileUpdateEvent event) {
        for (IProfileListener listener : profileEventListeners) {
            try {
                listener.onProfileUpdate(event);
            } catch (Exception e) {
                logError(e, "profile "+event.getProfileId()+" updated");
            }
        }
    }

    @Override
    public void onParticipantIsTyping(ParticipantTypingEvent event) {
        for (IMessagingListener listener : messagingListeners) {
            try {
                listener.onParticipantIsTyping(event);
            } catch (Exception e) {
                logError(e, "is typing");
            }
        }
    }

    @Override
    public void onParticipantTypingOff(ParticipantTypingOffEvent event) {
        for (IMessagingListener listener : messagingListeners) {
            try {
                listener.onParticipantTypingOff(event);
            } catch (Exception e) {
                logError(e, "is typing");
            }
        }
    }

    /**
     * Adds {@link IMessagingListener} to external callbacks that should be invoked when internal event was raised.
     *
     * @param listener {@link IMessagingListener} to external callbacks that should be invocked when internal event was raised.
     */
    public void addListener(IMessagingListener listener) {
        if (listener != null) {
            messagingListeners.add(listener);
        }
    }

    /**
     * Removes {@link IMessagingListener} from external callbacks that should be invoked when internal event was raised.
     *
     * @param listener {@link IMessagingListener} from external callbacks that should be invoked when internal event was raised.
     */
    public void removeListener(IMessagingListener listener) {
        messagingListeners.remove(listener);
    }

    /**
     * Adds {@link IStateListener} to external callbacks that should be invoked when internal event was raised.
     *
     * @param listener {@link IStateListener} to external callbacks that should be invocked when internal event was raised.
     */
    public void addListener(IStateListener listener) {
        if (listener != null) {
            stateListeners.add(listener);
        }
    }

    /**
     * Removes {@link IStateListener} from external callbacks that should be invoked when internal event was raised.
     *
     * @param listener {@link IStateListener} from external callbacks that should be invoked when internal event was raised.
     */
    public void removeListener(IStateListener listener) {
        stateListeners.remove(listener);
    }

    /**
     * Adds {@link IProfileListener} to external callbacks that should be invoked when internal event was raised.
     *
     * @param listener {@link IProfileListener} to external callbacks that should be invocked when internal event was raised.
     */
    public void addListener(IProfileListener listener) {
        if (listener != null) {
            profileEventListeners.add(listener);
        }
    }

    /**
     * Removes {@link IProfileListener} from external callbacks that should be invoked when internal event was raised.
     *
     * @param listener {@link IProfileListener} from external callbacks that should be invoked when internal event was raised.
     */
    public void removeListener(IProfileListener listener) {
        profileEventListeners.remove(listener);
    }

    private void logError(Exception e, String details) {
        log.f("Couldn't deliver event ("+details+") Exception in external callback implementation.", e);
    }
}
