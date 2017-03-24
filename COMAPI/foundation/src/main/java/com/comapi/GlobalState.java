package com.comapi;

/**
 * Describes the state of COMAPI SDK.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface GlobalState {

    /**
     * COMAPI SDK has not been initialised.
     */
    int NOT_INITIALISED = 1;

    /**
     * COMAPI SDK is initialising.
     */
    int INITIALISING = 2;

    /**
     * COMAPI SDK has been initialised but not registered.
     */
    int INITIALISED = 3;

    /**
     * COMAPI SDK has been initialised and session is loaded but is not active.
     */
    int SESSION_OFF = 4;

    /**
     * COMAPI SDK is creating and/or authenticating session.
     */
    int SESSION_STARTING = 5;

    /**
     * COMAPI SDK has active session.
     */
    int SESSION_ACTIVE = 6;

}
