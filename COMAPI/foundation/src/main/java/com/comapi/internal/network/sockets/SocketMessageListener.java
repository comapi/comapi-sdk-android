package com.comapi.internal.network.sockets;

/**
 * Listener for incoming socket messages.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
interface SocketMessageListener {

    /**
     * Called when socket message is received.
     *
     * @param text Socket string message.
     */
    void onMessage(String text);

}
