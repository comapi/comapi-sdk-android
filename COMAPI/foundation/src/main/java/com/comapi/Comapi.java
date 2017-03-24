package com.comapi;

import android.app.Application;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.comapi.internal.CallbackAdapter;

/**
 * Builder class for {@link ComapiClient} instance.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Comapi extends BaseComapi {

    /**
     * Shared client instance.
     */
    private static volatile ComapiClient instance;

    /**
     * Build SDK client. Can be called only once in onCreate callback on Application class.
     */
    public static void initialise(@NonNull Application app, @NonNull ComapiConfig config, Callback<ComapiClient> callback) {

        String error = checkIfCanInitialise(app, config, false);
        if (!TextUtils.isEmpty(error) && callback != null) {
            callback.error(new Exception(error));
        }

        ComapiClient client = new ComapiClient(config);
        CallbackAdapter adapter = config.getCallbackAdapter() != null ? config.getCallbackAdapter() : new CallbackAdapter();
        client.initialise(app, adapter, callback);
    }

    /**
     * Build SDK client. Can be called only once in onCreate callback on Application class.
     */
    public static void initialiseShared(@NonNull Application app, @NonNull ComapiConfig config, Callback<ComapiClient> callback) {

        String error = checkIfCanInitialise(app, config, false);
        if (!TextUtils.isEmpty(error) && callback != null) {
            callback.error(new Exception(error));
        }

        ComapiClient client = createShared(config);
        CallbackAdapter adapter = config.getCallbackAdapter() != null ? config.getCallbackAdapter() : new CallbackAdapter();
        client.initialise(app, adapter, callback);
    }

    /**
     * Get global singleton of {@link ComapiClient}.
     *
     * @return Singleton of {@link ComapiClient}
     */
    public static ComapiClient createShared(@NonNull ComapiConfig config) {

        if (instance == null) {
            synchronized (ComapiClient.class) {
                if (instance == null) {
                    instance = new ComapiClient(config);
                }
            }
        }

        return instance;
    }

    /**
     * Get global singleton of {@link ComapiClient}.
     *
     * @return Singleton of {@link ComapiClient}
     */
    public static ComapiClient getShared() {

        if (instance == null) {
            synchronized (ComapiClient.class) {
                if (instance == null) {
                    throw new RuntimeException("Comapi Client singleton has not been initialised.");
                }
            }
        }

        return instance;
    }

    /**
     * Method used by tests to reset shared client instance.
     */
    static void reset() {
        BaseComapi.reset();
        instance = null;
    }
}