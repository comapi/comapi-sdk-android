package com.comapi.internal.network.sockets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.comapi.internal.Parser;
import com.comapi.internal.data.DataManager;
import com.comapi.internal.log.Logger;
import com.comapi.internal.receivers.InternetConnectionReceiver;
import com.comapi.internal.lifecycle.LifecycleListener;

import java.net.URI;

/**
 * Class to create connections. Registers for application background/foreground state changes and network connectivity changes.
 * Socket will disconnect when app is in background and reconnect when in foreground or network connection has been restored.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SocketController {

    private final DataManager dataMgr;

    private SocketConnectionController socketConnection;

    private BroadcastReceiver receiver;

    private final Logger log;

    private final URI socketURI;

    private final URI proxyURI;

    private boolean isForegrounded;

    private final SocketEventListener listener;

    private final Object lock;

    /**
     * Recommended constructor.
     *
     * @param dataMgr   Manager of internal data storage.
     * @param listener  Listener for socket events.
     * @param log       Internal logger.
     * @param socketURI Socket URI.
     * @param proxyURI  Proxy URI
     */
    public SocketController(@NonNull DataManager dataMgr, SocketEventListener listener, @NonNull Logger log, @NonNull URI socketURI, URI proxyURI) {
        this.lock = new Object();
        this.dataMgr = dataMgr;
        this.listener = listener;
        this.log = log;
        this.socketURI = socketURI;
        this.proxyURI = proxyURI;
        this.isForegrounded = true;
    }

    /**
     * Create and connect socket.
     */
    public void connectSocket() {

        synchronized (lock) {
            if (isForegrounded) {
                if (socketConnection == null) {
                    SocketFactory factory = new SocketFactory(socketURI, new SocketEventDispatcher(listener, new Parser()).setLogger(log), log);
                    socketConnection = new SocketConnectionController(new Handler(Looper.getMainLooper()), dataMgr, factory, new RetryStrategy(60, 60000), log);
                    socketConnection.setProxy(proxyURI);
                    socketConnection.connect();

                } else {
                    socketConnection.connect();
                }
                socketConnection.setManageReconnection(true);
            }
            lock.notifyAll();
        }
    }

    /**
     * Disconnect socket.
     */
    public void disconnectSocket() {
        synchronized (lock) {
            if (socketConnection != null) {
                socketConnection.setManageReconnection(false);
                socketConnection.disconnect();
            }
            lock.notifyAll();
        }
    }

    /**
     * creates application lifecycle and network connectivity callbacks.
     *
     * @return Application lifecycle and network connectivity callbacks.
     */
    public LifecycleListener createLifecycleListener() {
        return new LifecycleListener() {

            @Override
            public void onForegrounded(Context context) {
                synchronized (lock) {
                    if (!isForegrounded) {
                        isForegrounded = true;
                        connectSocket();
                        if (receiver == null) {
                            receiver = new InternetConnectionReceiver(socketConnection);
                        }
                        context.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                    }
                    lock.notifyAll();
                }
            }

            @Override
            public void onBackgrounded(Context context) {
                synchronized (lock) {
                    if (isForegrounded) {
                        isForegrounded = false;
                        disconnectSocket();
                        if (receiver != null && !isForegrounded) {
                            context.unregisterReceiver(receiver);
                        }
                    }
                    lock.notifyAll();
                }
            }
        };
    }

    boolean isAllowedToConnect() {
        return isForegrounded;
    }
}
