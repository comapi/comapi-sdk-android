package com.comapi.internal.network.sockets;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;

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
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
class SocketConnectionController implements SocketStateListener, NetworkConnectivityListener {

    private final SocketFactory factory;

    private final DataManager dataMgr;

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
     * @param handler       main thread handler to schedule reconections.
     * @param dataMgr       Data Manager to obtain latest authentication token.
     * @param factory       Factory class to create SocketInterface instance.
     * @param retryStrategy Strategy for socket connection retries.
     */
    SocketConnectionController(@NonNull Handler handler, @NonNull DataManager dataMgr, @NonNull SocketFactory factory, @NonNull RetryStrategy retryStrategy, @NonNull Logger log) {
        this.dataMgr = dataMgr;
        this.factory = factory;
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
        log.i("Socket connected.");
        retryStrategy.reset();
        removeScheduledReconnection();
    }

    @Override
    public void onDisconnected() {
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
