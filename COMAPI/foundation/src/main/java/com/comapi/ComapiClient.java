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

package com.comapi;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.comapi.internal.CallbackAdapter;
import com.google.firebase.messaging.RemoteMessage;

import java.io.File;

/**
 * Comapi Client implementation for foundation SDK. Handles initialisation and stores all internal objects.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public class ComapiClient extends BaseClient<ServiceAccessor> {

    private CallbackAdapter adapter;

    /**
     * Recommended constructor.
     *
     * @param config Comapi configuration.
     */
    ComapiClient(final ComapiConfig config) {
        super(config);
    }

    /**
     * Initialise Comapi client instance.
     *
     * @param application Application context.
     */
    void initialise(@NonNull final Application application, CallbackAdapter adapter, Callback<ComapiClient> callback) {
        this.adapter = adapter;
        adapter.adapt(super.initialise(application, this, adapter), callback);
    }

    /**
     * Gets the internal state of the SDK. Possible values in {@link GlobalState}.
     *
     * @return State of the ComapiImpl SDK. Compare with values in {@link GlobalState}.
     */
    @Override
    public int getState() {
        return super.getState();
    }

    /**
     * Gets the active session data.
     *
     * @return Active session data.
     */
    @Override
    public Session getSession() {
        return super.getSession();
    }

    /**
     * Access to Comapi service APIs.
     *
     * @return Comapi service APIs.
     */
    public ServiceAccessor service() {
        return state.get() > GlobalState.INITIALISING ? new ServiceAccessor(service) : null;
    }

    /**
     * Gets the content of internal log files.
     *
     * @deprecated Use safer version - {@link ComapiClient#copyLogs(File)} instead.
     */
    @Deprecated
    public void getLogs(Callback<String> callback) {
        adapter.adapt(super.getLogs(), callback);
    }

    /**
     * Gets the content of internal log files merged into provided file.
     *
     * @param file     File to merge internal logs into.
     * @param callback Callback with a same file this time containing all the internal logs merged into.
     */
    public void copyLogs(@NonNull File file, Callback<File> callback) {
        adapter.adapt(super.copyLogs(file), callback);
    }

    @Override
    public void clean(@NonNull Context context) {
        super.clean(context);
    }

    /**
     * Handles click push notification tracking and opens deep link
     * @param activityContext pass calling activity here
     * @param i intent that is retrieved by getIntent() in onCreate or intent passed to onNewIntent lifecycle callbacks
     * @param startActivity should start activity from deep link url
     * @param callback Contains result of the push notification processing
     *                 getUrl - url passed as a deep link with the notification
     *                 isTrackingSuccessful - was call to record a click for analytics successful;
     *                 isStartActivitySuccessful - was starting activity from url successful
     */
    public void handlePush(Context activityContext, Intent i, boolean startActivity, Callback<PushHandleResult> callback) {
        adapter.adapt(super.handlePushNotification(activityContext, i, startActivity),callback);
    }

    public static void parsePushMessage(RemoteMessage message, Callback<PushDetails> callback) {
        CallbackAdapter adapter = new CallbackAdapter();
        adapter.adapt(BaseClient.parsePush(message), callback);
    }
}