package com.comapi;

import com.comapi.internal.IStateListener;
import com.comapi.internal.network.model.events.SocketStartEvent;

/**
 * SDK state listener. Callbacks for events describing SDK state.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class StateListener implements IStateListener {

    /**
     * User authenticated session has been stared success.
     *
     * @param session New session details.
     */
    public void onSessionStart(Session session) {}

    /**
     * Socket started successfully.
     *
     * @param event Socket started successfully event.
     */
    public void onSocketStart(SocketStartEvent event) {}

}