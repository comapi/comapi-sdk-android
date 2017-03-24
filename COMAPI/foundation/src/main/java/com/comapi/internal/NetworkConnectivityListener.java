package com.comapi.internal;

/**
 * Listener interface for Network connectivity changes.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface NetworkConnectivityListener {

    /**
     * Internet connection established.
     */
    void onNetworkActive();

    /**
     * Internet connection lost.
     */
    void onNetworkUnavailable();

}