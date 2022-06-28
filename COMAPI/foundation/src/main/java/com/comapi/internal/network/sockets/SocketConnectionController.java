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

package com.comapi.internal.network.sockets;

import android.os.Handler;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.comapi.internal.ListenerListAdapter;
import com.comapi.internal.NetworkConnectivityListener;
import com.comapi.internal.data.DataManager;
import com.comapi.internal.data.SessionData;
import com.comapi.internal.log.Logger;

import java.lang.ref.WeakReference;
import java.net.URI;

/**
 * Class to create and manage socket connection. Manages reties and responds to network connectivity changes if registered.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
class SocketConnectionController implements SocketStateListener, NetworkConnectivityListener {

    private final SocketFactory factory;

    private final DataManager dataMgr;

    private final ListenerListAdapter listener;

    private SocketInterface socket;

    private boolean isManagingReconnection;

    private boolean isNetworkUnavailable;

    private final RetryStrategy retryStrategy;

    private final Logger log;

    private Runnable runnable;

    private Handler handler;

    /**
     * Recommended constructor.
     *
     * @param handler       main thread handler to schedule reconnection.
     * @param dataMgr       Data Manager to obtain latest authentication token.
     * @param factory       Factory class to create SocketInterface instance.
     * @param listener      Listener adapter dispatching events to registered external listener objects.
     * @param retryStrategy Strategy for socket connection retries.
     */
    SocketConnectionController(@NonNull Handler handler, @NonNull DataManager dataMgr, @NonNull SocketFactory factory, @NonNull ListenerListAdapter listener, @NonNull RetryStrategy retryStrategy, @NonNull Logger log) {
        this.dataMgr = dataMgr;
        this.factory = factory;
        this.listener = listener;
        this.retryStrategy = retryStrategy;
        this.isManagingReconnection = false;
        this.isNetworkUnavailable = false;
        this.socket = null;
        this.log = log;
        this.handler = handler;
    }

    /**
     * Sets address (with port) on on which proxy operates.
     *
     * @param proxyAddress Address (with port) on on which proxy operates.
     */
    void setProxy(URI proxyAddress) {
        factory.setProxyAddress(proxyAddress);
    }

    /**
     * Start socket connection.
     */
    synchronized void connect() {

        if (socket != null) {
            socket.disconnect();
        }

        final String token = getToken();

        if (!TextUtils.isEmpty(token)) {

            socket = factory.createSocket(token, new WeakReference<>(this));

            if (socket != null) {
                socket.connect();
            }
        }
    }

    /**
     * Close socket connection.
     */
    void disconnect() {
        if (socket != null) {
            socket.disconnect();
            socket = null;
        }
    }

    /**
     * Gets Comapi access token.
     *
     * @return Comapi access token.
     */
    private String getToken() {

        SessionData session = dataMgr.getSessionDAO().session();
        if (session != null && session.getExpiresOn() > System.currentTimeMillis()) {
            return session.getAccessToken();
        }

        return null;
    }

    /**
     * Set reconnection policy.
     *
     * @param isManagingReconnection True if controller should be trying to reconnect socket.
     */
    void setManageReconnection(boolean isManagingReconnection) {
        this.isManagingReconnection = isManagingReconnection;
    }

    private boolean shouldReconnect() {
        return isManagingReconnection && !isNetworkUnavailable;
    }

    @Override
    public void onConnected() {
        listener.onSocketConnected();
        log.i("Socket connected.");
        retryStrategy.reset();
        removeScheduledReconnection();
    }

    @Override
    public void onDisconnected() {
        listener.onSocketDisconnected();
        log.i("Socket disconnected.");
        if (shouldReconnect()) {
            scheduleReconnection();
        }
    }

    @Override
    public void onError(String uriStr, URI proxyAddress, Exception exception) {
        log.w("Socket disconnected with error. " + (exception != null ? exception.getLocalizedMessage() : null));
        if (shouldReconnect()) {
            scheduleReconnection();
        }
    }

    @Override
    public void onNetworkActive() {
        if (isNetworkUnavailable) {
            isNetworkUnavailable = false;
            connect();
        }
    }

    @Override
    public void onNetworkUnavailable() {
        isNetworkUnavailable = true;
    }

    /**
     * Schedule socket connection retry.
     */
    private void scheduleReconnection() {

        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }

        runnable = () -> {
            if (shouldReconnect() && (socket == null || !socket.isOpen()) && retryStrategy.retry() && !isNetworkUnavailable) {
                log.d("Reconnecting socket");
                connect();
            }
        };
        long delay = retryStrategy.getDelay();
        handler.postDelayed(runnable, delay);
        log.d("Socket reconnection in " + delay / 1000 + " seconds.");
    }

    /**
     * Remove pending socket connection retry.
     */
    private void removeScheduledReconnection() {
        if (runnable != null) {
            handler.removeCallbacksAndMessages(runnable);
            runnable = null;
        }
    }
}
