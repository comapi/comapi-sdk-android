package com.comapi.internal.network.sockets;

/**
 * Definition of a strategy for socket reconnection when an error occurs.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
class RetryStrategy {

    private int maxRetries;

    private int maximumDelay;

    private int retries;

    /**
     * Recommended constructor.
     *
     * @param maxRetries MAx retries allowed for failing socket connection.
     */
    RetryStrategy(int maxRetries, int maximumDelay) {
        this.maxRetries = maxRetries;
        this.maximumDelay = maximumDelay;
    }

    /**
     * @return True if maximum of retries was not reached. Increments the retry counter.
     */
    boolean retry() {
        retries += 1;
        return retries <= maxRetries;
    }

    /**
     * Resets the retry counter.
     */
    void reset() {
        retries = 0;
    }

    /**
     * @return Delay before next retry in milliseconds.
     */
    long getDelay() {
        return Math.min(1000*retries, maximumDelay);
    }
}