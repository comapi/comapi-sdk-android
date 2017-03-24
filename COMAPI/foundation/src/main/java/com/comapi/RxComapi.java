package com.comapi;

import android.app.Application;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.comapi.internal.CallbackAdapter;

import rx.Observable;

/**
 * Builder class for {@link ComapiClient} instance.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RxComapi extends BaseComapi {

    /**
     * Shared client instance.
     */
    private static volatile RxComapiClient instance;

    /**
     * Build SDK client. Can be called only once in onCreate callback on Application class.
     */
    public static Observable<RxComapiClient> initialise(@NonNull Application app, @NonNull ComapiConfig config) {

        String error = checkIfCanInitialise(app, config, false);
        if (!TextUtils.isEmpty(error)) {
            return Observable.error(new Exception(error));
        }

        RxComapiClient client = new RxComapiClient(config);
        return client.initialise(app, config.getCallbackAdapter() != null ? config.getCallbackAdapter() : new CallbackAdapter(), client);
    }

    /**
     * Build SDK client. Can be called only once in onCreate callback on Application class.
     */
    public static Observable<RxComapiClient> initialiseShared(@NonNull Application app, @NonNull ComapiConfig config) {
        String error = checkIfCanInitialise(app, config, true);
        if (error != null) {
            return Observable.error(new Exception(error));
        }

        RxComapiClient client = createShared(config);
        return client.initialise(app, config.getCallbackAdapter() != null ? config.getCallbackAdapter() : new CallbackAdapter(), client);
    }

    /**
     * Get global singleton of {@link ComapiClient}.
     *
     * @return Singleton of {@link ComapiClient}
     */
    public static RxComapiClient createShared(@NonNull ComapiConfig config) {

        if (instance == null) {
            synchronized (ComapiClient.class) {
                if (instance == null) {
                    instance = new RxComapiClient(config);
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
    public static RxComapiClient getShared() {

        if (instance == null) {
            synchronized (RxComapiClient.class) {
                if (instance == null) {
                    throw new RuntimeException("ComapiImpl Client singleton has not been initialised.");
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
