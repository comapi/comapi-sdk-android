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

package com.comapi.internal;

import android.content.Context;
import androidx.annotation.NonNull;

import com.comapi.ComapiClient;
import com.comapi.GlobalState;
import com.comapi.MessagingListener;
import com.comapi.ProfileListener;
import com.comapi.Session;
import com.comapi.StateListener;

import rx.Observable;

/**
 * Interface for {@link ComapiClient} and client extensions. Type parameter T is an interface for service API supported by this client instance.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public interface IClient<T> {

    /**
     * Gets the internal state of the SDK. Possible values in {@link GlobalState}.
     *
     * @return State of the ComapiImplementation SDK. Compare with values in {@link GlobalState}.
     */
    int getState();

    /**
     * Gets the active session data.
     *
     * @return Active session data.
     */
    Session getSession();

    T service();

    /**
     * Gets the content of internal log files.
     */
    Observable<String> getLogs();

    /**
     * Adds listener for messaging socket events.
     *
     * @param listener Adds listener for messaging socket events.
     */
    void addListener(MessagingListener listener);

    /**
     * Removes listener for messaging socket events.
     *
     * @param listener Listener for messaging socket events.
     */
    void removeListener(MessagingListener listener);

    /**
     * Adds listener for profile socket events.
     *
     * @param listener Listener for profile socket events.
     */
    void addListener(ProfileListener listener);

    /**
     * Remove listener for profile socket events.
     *
     * @param listener Listener for profile socket events.
     */
    void removeListener(ProfileListener listener);

    /**
     * Adds listener for state events.
     *
     * @param listener Listener for state events.
     */
    void addListener(StateListener listener);

    /**
     * Remove listener for state events.
     *
     * @param listener Listener for state events.
     */
    void removeListener(StateListener listener);

    void clean(@NonNull Context context);
}
