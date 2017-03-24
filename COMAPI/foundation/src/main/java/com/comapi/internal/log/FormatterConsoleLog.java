package com.comapi.internal.log;

/**
 * Formats the log entry for console log appender.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
class FormatterConsoleLog extends Formatter {

    /**
     * Formats log message for console output. Short version.
     *
     * @param msgLogLevel Message log level.
     * @param clazz       Class name in which message was logged.
     * @param msg         Log message.
     * @return Formatted log message.
     */
    String formatMessage(int msgLogLevel, String clazz, String msg) {
        return getLevelTag(msgLogLevel) + "[" + clazz + "]: " + msg;
    }
}
