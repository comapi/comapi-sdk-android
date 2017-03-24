package com.comapi.internal.network.sockets;

import java.net.URI;

/**
 * Callbacks for socket connectivity changes.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
interface SocketStateListener {

    /**
     * Socket has been connected.
     */
    void onConnected();

    /**
     * Socket has been disconnected.
     */
    void onDisconnected();

    /**
     * Error occurred when connecting the socket.
     *
     * @param hostAddress  Host address for socket connection.
     * @param proxyAddress Proxy address if present.
     * @param exception    Exception that was thrown when SDK tried to connect.
     */
    void onError(String hostAddress, URI proxyAddress, Exception exception);
}