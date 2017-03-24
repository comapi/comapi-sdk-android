package com.comapi.internal.log;

import android.support.annotation.NonNull;

/**
 * Helper class for all log messages in Comapi SDK. Controls the logging level and triggers any additional tasks performed during logging.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Logger {

    private final String TAG;

    private final LogManager logMgr;

    public Logger(@NonNull LogManager logMgr, @NonNull String tag) {
        this.logMgr = logMgr;
        TAG = tag;
    }

    /**
     * Logs message log level has been set at least to {@link LogLevel#INFO}.
     *
     * @param msg Message to be logged
     */
    public void i(final String msg) {
        logMgr.log(TAG, LogLevelConst.INFO, msg, null);
    }

    /**
     * Logs message log level has been set at least to {@link LogLevel#WARNING}.
     *
     * @param msg Message to be logged
     */
    public void w(final String msg) {
        logMgr.log(TAG, LogLevelConst.WARNING, msg, null);
    }

    /**
     * Logs message log level has been set at least to {@link LogLevel#ERROR}.
     *
     * @param msg Message to be logged
     */
    public void e(final String msg) {
        logMgr.log(TAG, LogLevelConst.ERROR, msg, null);
    }

    /**
     * Logs message log level has been set at least to {@link LogLevel#FATAL}.
     *
     * @param msg       Message to be logged
     * @param exception Optional exception to be logged.
     */
    public void f(final String msg, Throwable exception) {
        logMgr.log(TAG, LogLevelConst.FATAL, msg, exception);
    }

    /**
     * Logs message log level has been set at least to {@link LogLevel#DEBUG}.
     *
     * @param msg Message to be logged
     */
    public void d(final String msg) {
        logMgr.log(TAG, LogLevelConst.DEBUG, msg, null);
    }

}
