package com.comapi.internal;

import android.content.Context;
import android.support.annotation.NonNull;

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
 * Copyright (C) Donky Networks Ltd. All rights reserved.
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
