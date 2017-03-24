package com.comapi.internal.log;

/**
 * Interface to be implemented by all log output implementations.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
abstract class Appender {

    /**
     * Log level used by the Appender. Possible values are defined in {@link LogLevelConst} class.
     */
    private int level = LogLevelConst.ERROR;

    /**
     * Recommended constructor.
     *
     * @param logLevel Initial log level, should be one of {@link LogLevelConst}.
     */
    Appender(final int logLevel) {
        level = logLevel;
    }

    /**
     * Write the log message to some output implementation.
     *
     * @param clazz     Name of the class that sent the log message.
     * @param logLevel  Log level, should be one of {@link LogLevelConst}.
     * @param msg       Message to be logged.
     * @param exception Optional exception to log the stacktrace.
     */
    abstract void appendLog(final String clazz, final int logLevel, final String msg, final Throwable exception);

    /**
     * Should this appender output log.
     *
     * @param logLevel Log level for a message to append.
     * @return True if the appender should append the message to output.
     */
    boolean shouldAppend(final int logLevel) {
        return level >= logLevel;
    }

    /**
     * Sets log level for log appender implementation. Only logs with {@link LogLevelConst} above it will be appended.
     *
     * @param logLevel Log level for console log appender implementation.
     */
    public void setLevel(int logLevel) {
        level = logLevel;
    }

    /**
     * Gets log level set for appender implementation.
     *
     * @return Log level set for appender implementation.
     */
    protected int getLevel() {
        return level;
    }
}