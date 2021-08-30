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
import androidx.annotation.NonNull;

import com.comapi.internal.CallbackAdapter;
import com.comapi.internal.lifecycle.LifecycleListener;
import com.comapi.internal.log.Logger;
import com.comapi.internal.network.api.RxComapiService;

import java.io.File;

import rx.Observable;

/**
 * ComapiImpl Client implementation for foundation SDK. Handles initialisation and stores all internal objects.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public class RxComapiClient extends BaseClient<RxServiceAccessor> {

    /**
     * Recommended constructor.
     *
     * @param config ComapiImpl configuration.
     */
    protected RxComapiClient(final ComapiConfig config) {
        super(config);
    }

    /**
     * Initialise ComapiImpl client instance.
     *
     * @param application Application context.
     * @param adapter     Observables to callbacks adapter.
     * @return Observable returning client instance.
     */
    <T extends RxComapiClient> Observable<T> initialise(@NonNull final Application application, CallbackAdapter adapter, @NonNull final T instance) {
        return super.initialise(application, instance, adapter);
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
    public RxServiceAccessor service() {
        return state.get() > GlobalState.INITIALISING ? new RxServiceAccessor(service) : null;
    }

    /**
     * Gets the content of internal log files.
     *
     * @deprecated Use safer version - {@link this#copyLogs(File)} instead.
     */
    @Override
    @Deprecated
    public Observable<String> getLogs() {
        return super.getLogs();
    }

    /**
     * Gets the content of internal log files merged into provided file.
     *
     * @param file File to merge internal logs into.
     * @return Observable with a same file this time containing all the internal logs merged into.
     */
    public Observable<File> copyLogs(@NonNull File file) {
        return super.copyLogs(file);
    }

    @Override
    public void clean(@NonNull Context context) {
        pushMgr.unregisterPushReceiver(context);
    }

    /**
     * Gets logger to access internal logs writer.
     *
     * @return Internal logger.
     */
    protected Logger getLogger() {
        return super.getLogger();
    }

    /**
     * Adds listener for application lifecycle callbacks.
     *
     * @param listener Listener for application lifecycle callbacks.
     */
    void addLifecycleListener(LifecycleListener listener) {
        super.addLifecycleListener(listener);
    }

    protected RxComapiService getComapiService() {
        return service;
    }
}