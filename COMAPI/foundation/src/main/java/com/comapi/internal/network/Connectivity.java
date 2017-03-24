package com.comapi.internal.network;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Helper to check internet connectivity.
 *
 * Created by Marcin Swierczek
 * 12/04/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Connectivity {

    public static boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}