package com.comapi.internal.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.comapi.internal.NetworkConnectivityListener;

import java.lang.ref.WeakReference;

/**
 * Broadcast receiver for network connectivity changes.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class InternetConnectionReceiver extends BroadcastReceiver {

    private final WeakReference<NetworkConnectivityListener> listenerWeakReference;

    /**
     * Recommended constructor.
     *
     * @param listener Listener for connectivity changes.
     */
    public InternetConnectionReceiver(NetworkConnectivityListener listener) {
        listenerWeakReference = new WeakReference<>(listener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            final NetworkConnectivityListener listener = listenerWeakReference.get();
            if (listener != null) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    listener.onNetworkActive();
                } else {
                    listener.onNetworkUnavailable();
                }
            }
        }
    }
}