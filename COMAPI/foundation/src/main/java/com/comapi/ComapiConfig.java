package com.comapi;

import com.comapi.internal.IMessagingListener;

/**
 * Class describing the initial configuration for a {@link ComapiClient} instance.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ComapiConfig extends BaseConfig<ComapiConfig> {

    private IMessagingListener messagingListener;

    /**
     * Gets Comapi message listener.
     *
     * @return Comapi message listener.
     */
    IMessagingListener getMessagingListener() {
        return messagingListener;
    }

    /**
     * Sets Comapi message listener.
     *
     * @param messagingListener Comapi Socket events listener.
     * @return Builder instance with new value set.
     */
    public <E extends MessagingListener> ComapiConfig messagingListener(E messagingListener) {
        this.messagingListener = messagingListener;
        return getThis();
    }

    @Override
    protected ComapiConfig getThis() {
        return this;
    }
}