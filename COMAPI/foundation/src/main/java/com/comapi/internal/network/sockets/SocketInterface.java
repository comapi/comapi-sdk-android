package com.comapi.internal.network.sockets;

/**
 * Socket connection interface.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
interface SocketInterface {

    /**
     * Connect socket.
     */
    void connect();

    /**
     * Disconnect socket.
     */
    void disconnect();

    /**
     * @return True if connection is open.
     */
    boolean isOpen();

}