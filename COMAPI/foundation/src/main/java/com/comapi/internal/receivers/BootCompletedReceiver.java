package com.comapi.internal.receivers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Broadcast receiver for boot completed broadcasts to resume work after device restart.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * 31/03/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        //Nothing more to do.
    }
}