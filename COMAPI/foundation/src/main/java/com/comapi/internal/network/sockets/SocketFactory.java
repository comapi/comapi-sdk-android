package com.comapi.internal.network.sockets;

import android.support.annotation.NonNull;

import com.comapi.internal.log.Logger;
import com.comapi.internal.network.AuthManager;
import com.neovisionaries.ws.client.ProxySettings;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Factory to create sockets.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
class SocketFactory {

    /**
     * Ping server interval
     */
    private static final long PING_INTERVAL = 1000 * 60;

    /**
     * Timeout for a socket connection attempt.
     */
    private static final int TIMEOUT = 5000;

    /**
     * Socket URI
     */
    private final URI uri;

    private final Logger log;

    private final SocketMessageListener messageListener;

    private URI proxyAddress;

    /**
     * Recommended constructor.
     *
     * @param uri             Socket connection URI.
     * @param messageListener Listener for incoming socket messages.
     * @param log             Internal logger.
     */
    SocketFactory(@NonNull URI uri, @NonNull SocketMessageListener messageListener, @NonNull Logger log) {
        this.uri = uri;
        this.messageListener = messageListener;
        this.log = log;
    }

    /**
     * Sets address for a proxy if used.
     *
     * @param proxyAddress Proxy address.
     */
    void setProxyAddress(URI proxyAddress) {
        this.proxyAddress = proxyAddress;
    }

    /**
     * Creates and configures web socket instance.
     *
     * @param token                      Authentication token.
     * @param stateListenerWeakReference Weak reference to socket connection state callbacks.
     */
    SocketInterface createSocket(@NonNull final String token, @NonNull final WeakReference<SocketStateListener> stateListenerWeakReference) {

        WebSocket socket = null;

        WebSocketFactory factory = new WebSocketFactory();

        // Configure proxy if provided
        if (proxyAddress != null) {
            ProxySettings settings = factory.getProxySettings();
            settings.setServer(proxyAddress);
        }

        try {
            socket = factory.createSocket(uri, TIMEOUT);
        } catch (IOException e) {
            log.f("Creating socket failed", e);
        }

        if (socket != null) {

            String header = AuthManager.AUTH_PREFIX + token;
            socket.addHeader("Authorization", header);

            socket.addListener(createWebSocketAdapter(stateListenerWeakReference));

            socket.setPingInterval(PING_INTERVAL);

            return new SocketWrapperImpl(socket);
        }

        return null;
    }

    /**
     * Create adapter for websocket library events.
     *
     * @param stateListenerWeakReference Listener for socket state changes.
     * @return Adapter for websocket library events.
     */
    protected WebSocketAdapter createWebSocketAdapter(@NonNull final WeakReference<SocketStateListener> stateListenerWeakReference) {

        return new WebSocketAdapter() {

            @Override
            public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                super.onConnected(websocket, headers);
                final SocketStateListener stateListener = stateListenerWeakReference.get();
                if (stateListener != null) {
                    stateListener.onConnected();
                }
            }

            @Override
            public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
                super.onConnectError(websocket, exception);
                final SocketStateListener stateListener = stateListenerWeakReference.get();
                if (stateListener != null) {
                    stateListener.onError(uri.getRawPath(), proxyAddress, exception);
                }
            }

            @Override
            public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
                final SocketStateListener stateListener = stateListenerWeakReference.get();
                if (stateListener != null) {
                    stateListener.onDisconnected();
                }
            }

            @Override
            public void onTextMessage(WebSocket websocket, String text) throws Exception {
                super.onTextMessage(websocket, text);
                log.d("Socket message received = " + text);
                messageListener.onMessage(text);
            }

            @Override
            public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
                super.onBinaryMessage(websocket, binary);
                log.d("Socket binary message received.");
            }
        };
    }

    /**
     * Implementation of socket connection interface wrapping websocket client.
     */
    private class SocketWrapperImpl implements SocketInterface {

        private final WebSocket websocket;

        SocketWrapperImpl(@NonNull WebSocket websocket) {
            this.websocket = websocket;
        }

        @Override
        public void connect() {
            websocket.connectAsynchronously();
        }

        @Override
        public void disconnect() {
            websocket.disconnect();
        }

        @Override
        public boolean isOpen() {
            return websocket.isOpen();
        }
    }
}
