package com.comapi.internal.push;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Service to handle Instance ID service token refresh notifications for FCM security.
 * Any app using FCM must include a class extending FirebaseInstanceIdService and implement onTokenRefresh().
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * 31/03/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class IDService extends FirebaseInstanceIdService {

    public static final String ACTION_REFRESH_PUSH = "com.comapi.push.refresh";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(ACTION_REFRESH_PUSH);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

}