package com.comapi.helpers;

import com.comapi.Callback;

/**
 * Mocked callbacks for unit tests for synchronisation purpose.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MockCallback<T> implements Callback<T> {

    T result;

    Throwable error;

    @Override
    public void success(T result) {

        this.result = result;

        synchronized (this) {
            notifyAll();
        }

    }

    @Override
    public void error(Throwable t) {

        this.error = t;

        synchronized (this) {
            notifyAll();
        }
    }

    public T getResult() {
        return result;
    }

    public Throwable getError() {
        return error;
    }
}
