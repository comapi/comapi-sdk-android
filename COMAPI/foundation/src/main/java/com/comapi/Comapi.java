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
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.comapi.internal.CallbackAdapter;

/**
 * Builder class for {@link ComapiClient} instance.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
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