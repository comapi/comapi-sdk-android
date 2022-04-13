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

package com.comapi.internal.log;

import androidx.annotation.NonNull;

/**
 * Helper class for all log messages in Comapi SDK. Controls the logging level and triggers any additional tasks performed during logging.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public class Logger {

    private final String tag;

    private final LogManager logMgr;

    /**
     * Recommended constructor.
     *
     * @param logMgr Log manager instance.
     * @param tag Tag to be placed as a prefix of all logs.
     */
    public Logger(@NonNull LogManager logMgr, @NonNull String tag) {
        this.logMgr = logMgr;
        this.tag = tag;
    }

    /**
     * Logs message log level has been set at least to {@link LogLevel#INFO}.
     *
     * @param msg Message to be logged
     */
    public void i(final String msg) {
        logMgr.log(tag, LogLevelConst.INFO, msg, null);
    }

    /**
     * Logs message log level has been set at least to {@link LogLevel#WARNING}.
     *
     * @param msg Message to be logged
     */
    public void w(final String msg) {
        logMgr.log(tag, LogLevelConst.WARNING, msg, null);
    }

    /**
     * Logs message log level has been set at least to {@link LogLevel#ERROR}.
     *
     * @param msg Message to be logged
     */
    public void e(final String msg) {
        logMgr.log(tag, LogLevelConst.ERROR, msg, null);
    }

    /**
     * Logs message log level has been set at least to {@link LogLevel#FATAL}.
     *
     * @param msg       Message to be logged
     * @param exception Optional exception to be logged.
     */
    public void f(final String msg, Throwable exception) {
        logMgr.log(tag, LogLevelConst.FATAL, msg, exception);
    }

    /**
     * Logs message log level has been set at least to {@link LogLevel#DEBUG}.
     *
     * @param msg Message to be logged
     */
    public void d(final String msg) {
        logMgr.log(tag, LogLevelConst.DEBUG, msg, null);
    }

    /**
     * Clone Logger instance with additional tag suffix to include with every log entry. Eg. with SDK name and version.
     *
     * @param tagSuffix Tag suffix to include with every log entry.
     * @return New logger instance.
     */
    public Logger clone(@NonNull final String tagSuffix) {
        return new Logger(logMgr, tag + "|" + tagSuffix);
    }
}
