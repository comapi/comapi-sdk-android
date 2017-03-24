package com.comapi.internal.log;

/**
 * Base formatter for log messages.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
class Formatter {

    /**
     * Gets the string representation of the log level.
     *
     * @param logLevel Level of the log message.
     * @return String representation of the log level.
     */
    static String getLevelTag(final int logLevel) {

        switch (logLevel) {

            case LogLevelConst.FATAL:
                return LogConstants.TAG_FATAL;

            case LogLevelConst.ERROR:
                return LogConstants.TAG_ERROR;

            case LogLevelConst.WARNING:
                return LogConstants.TAG_WARNING;

            case LogLevelConst.INFO:
                return LogConstants.TAG_INFO;

            case LogLevelConst.DEBUG:
                return LogConstants.TAG_DEBUG;

            default:
                return "[DEFAULT]";

        }
    }
}
