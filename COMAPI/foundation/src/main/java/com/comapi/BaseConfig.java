package com.comapi;

import com.comapi.internal.CallbackAdapter;
import com.comapi.internal.IStateListener;
import com.comapi.internal.log.LogConfig;
import com.comapi.internal.push.PushMessageListener;
import com.comapi.internal.IProfileListener;
import com.comapi.internal.push.PushTokenProvider;

/**
 * Base class for Comapi configuration and setup.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class BaseConfig<T extends BaseConfig<T>> {

    protected LogConfig logConfig;

    protected ComapiAuthenticator authenticator;

    protected PushMessageListener pushMessageListener;

    protected String apiSpaceId;

    protected IStateListener stateListener;

    protected IProfileListener profileListener;

    protected CallbackAdapter callbackAdapter;

    protected int logSizeLimit;

    protected APIConfig apiConfig;

    private PushTokenProvider pushTokenProvider;

    /**
     * Gets Comapi ApiSpace identifier.
     *
     * @return Comapi ApiSpace identifier.
     */
    String getApiSpaceId() {
        return apiSpaceId;
    }

    /**
     * Gets Comapi logging configuration.
     *
     * @return Comapi logging configuration.
     */
    LogConfig getLogConfig() {
        return logConfig;
    }

    /**
     * Gets Authenticator to set a response for authentication challenges.
     *
     * @return Authenticator to set a response for authentication challenges.
     */
    ComapiAuthenticator getAuthenticator() {
        return authenticator;
    }

    /**
     * Gets Comapi message listener.
     *
     * @return Comapi message listener.
     */
    PushMessageListener getPushMessageListener() {
        return pushMessageListener;
    }

    /**
     * Gets listener for SDK state changes.
     *
     * @return Listener for SDK state changes.
     */
    protected IStateListener getStateListener() {
        return stateListener;
    }

    /**
     * Gets listener for profile changes.
     *
     * @return Listener for profile changes.
     */
    protected IProfileListener getProfileListener() {
        return profileListener;
    }

    /**
     * Gets observables to callbacks adapter.
     *
     * @return Observables to callbacks adapter.
     */
    protected CallbackAdapter getCallbackAdapter() {
        return callbackAdapter;
    }

    /**
     * Gets total file size limit for internal logs.
     *
     * @return Total file size limit for internal logs.
     */
    int getLogSizeLimit() {
        return logSizeLimit;
    }

    /**
     * Gets API URLs.
     *
     * @return API URLs.
     */
    APIConfig getApiConfig() {
        return apiConfig;
    }

    /**
     * Sets method of obtaining push token. Used for mocking FCM.
     *
     * @return Provides push token.
     */
    PushTokenProvider getPushTokenProvider() {
        return pushTokenProvider;
    }

    /**
     * Sets Comapi App Space identifier.
     *
     * @param id Comapi ApiSpace identifier.
     * @return BaseURIs instance with new value set.
     */
    public T apiSpaceId(String id) {
        this.apiSpaceId = id;
        return getThis();
    }

    /**
     * Sets Comapi logging configuration.
     *
     * @param logConfig Comapi logging configuration.
     * @return BaseURIs instance with new value set.
     */
    public T logConfig(LogConfig logConfig) {
        this.logConfig = logConfig;
        return getThis();
    }

    /**
     * Sets Comapi message listener.
     *
     * @param pushMessageListener Push message listener.
     * @return BaseURIs instance with new value set.
     */
    public T pushMessageListener(PushMessageListener pushMessageListener) {
        this.pushMessageListener = pushMessageListener;
        return getThis();
    }

    /**
     * Sets Authenticator to set a response for authentication challenges.
     *
     * @param authenticator Authenticator to set a response for authentication challenges.
     * @return BaseURIs instance with new value set.
     */
    public T authenticator(ComapiAuthenticator authenticator) {
        this.authenticator = authenticator;
        return getThis();
    }

    /**
     * Sets listener for SDK state changes.
     *
     * @param stateListener {@link StateListener} callback for state changes.
     * @param <E>           Extends {@link StateListener}
     * @return BaseURIs instance with new value set.
     */
    public <E extends StateListener> T stateListener(E stateListener) {
        this.stateListener = stateListener;
        return getThis();
    }

    /**
     * Sets listener for profile changes.
     *
     * @param profileListener {@link ProfileListener} callback for profile changes.
     * @param <E>             Extends {@link ProfileListener}
     * @return BaseURIs instance with new value set.
     */
    public <E extends ProfileListener> T profileListener(E profileListener) {
        this.profileListener = profileListener;
        return getThis();
    }

    /**
     * Sets total file size limit for internal logs.
     *
     * @param limit Total file size limit for internal logs.
     * @return BaseURIs instance with new value set.
     */
    public T logSizeLimitKilobytes(int limit) {
        this.logSizeLimit = limit;
        return getThis();
    }

    /**
     * Sets API URLs.
     *
     * @param apiConfig API URLs configuration.
     * @return BaseURIs instance with new value set.
     */
    public T apiConfiguration(APIConfig apiConfig) {
        this.apiConfig = apiConfig;
        return getThis();
    }

    /**
     * Sets method of obtaining push token. Used for mocking FCM.
     *
     * @param pushTokenProvider Provides push token.
     * @return BaseURIs instance with new value set.
     */
    T pushTokenProvider(PushTokenProvider pushTokenProvider) {
        this.pushTokenProvider = pushTokenProvider;
        return getThis();
    }

    /**
     * Sets observables to callbacks adapter. By Overriding CallbackAdapter#adapt(Observable, Callback) method you can change
     * e.g. the threads on which SDK subscribe to observable when callback APIs version are being called. By default they subscribe on
     * io thread and notify on the main thread.
     *
     * @param callbackAdapter {@link CallbackAdapter} instance for APIs version with callbacks.
     * @param <E>             Extends {@link CallbackAdapter}
     * @return BaseURIs instance with new value set.
     */
    public <E extends CallbackAdapter> T overrideCallbackAdapter(E callbackAdapter) {
        this.callbackAdapter = callbackAdapter;
        return getThis();
    }

    protected abstract T getThis();
}
