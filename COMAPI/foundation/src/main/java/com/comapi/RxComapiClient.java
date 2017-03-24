package com.comapi;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.comapi.internal.CallbackAdapter;
import com.comapi.internal.lifecycle.LifecycleListener;
import com.comapi.internal.log.Logger;
import com.comapi.internal.network.api.RxComapiService;

import rx.Observable;

/**
 * ComapiImpl Client implementation for foundation SDK. Handles initialisation and stores all internal objects.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RxComapiClient extends BaseClient<RxServiceAccessor> {

    /**
     * Recommended constructor.
     *
     * @param config ComapiImpl configuration.
     */
    RxComapiClient(final ComapiConfig config) {
        super(config);
    }

    /**
     * Initialise ComapiImpl client instance.
     *
     * @param application Application context.
     * @param adapter Observables to callbacks adapter.
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
     */
    @Override
    public Observable<String> getLogs() {
        return super.getLogs();
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
    Logger getLogger() {
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

    RxComapiService getComapiService() {
        return service;
    }
}