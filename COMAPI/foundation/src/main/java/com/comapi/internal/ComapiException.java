package com.comapi.internal;

/**
 * Represents exceptions thrown inside Comapi SDK.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ComapiException extends RuntimeException {

    /**
     * Constructor to wrap the initial exception.
     *
     * @param msg   Error message.
     * @param cause Initial exception.
     */
    public ComapiException(String msg, Throwable cause) {
        super(msg);
        this.initCause(cause);
    }

    /**
     * Constructor to wrap the initial exception.
     *
     * @param msg Error message.
     */
    public ComapiException(String msg) {
        super(msg);
    }
}
