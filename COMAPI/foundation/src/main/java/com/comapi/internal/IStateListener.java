package com.comapi.internal;

import com.comapi.internal.network.model.events.SocketStartEvent;

/**
 * SDK state listener interface. Callbacks for events describing SDK state.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface IStateListener extends ISessionListener {

    /**
     * Socket started successfully.
     *
     * @param event Socket started successfully event.
     */
    void onSocketStart(SocketStartEvent event);

}