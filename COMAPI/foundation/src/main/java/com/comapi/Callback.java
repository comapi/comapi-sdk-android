package com.comapi;

/**
 * Callback interface.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface Callback<T> {

    void success(T result);

    void error(Throwable t);
}
