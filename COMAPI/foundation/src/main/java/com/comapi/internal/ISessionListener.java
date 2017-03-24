package com.comapi.internal;

import com.comapi.Session;

/**
 * SDK session state listener interface. Callbacks for events describing session state.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface ISessionListener {

    /**
     * User authenticated session has been stared success.
     *
     * @param session New session details.
     */
    void onSessionStart(Session session);

}