package com.comapi;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.comapi.internal.CallbackAdapter;

/**
 * ComapiImpl Client implementation for foundation SDK. Handles initialisation and stores all internal objects.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
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
     */
    public void getLogs(Callback<String> callback) {
        adapter.adapt(super.getLogs(), callback);
    }

    @Override
    public void clean(@NonNull Context context) {
        super.clean(context);
    }
}