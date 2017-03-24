package com.comapi.internal.log;

/**
 * Constants used in logs.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface LogConstants {

    /**
     * Default tag for {@link Logger}s
     */
    String TAG = "Comapi";

    /**
     * See {@link LogLevel#FATAL}
     */
    String TAG_FATAL = "[FATAL]";

    /**
     * See {@link LogLevel#ERROR}
     */
    String TAG_ERROR = "[ERROR]";

    /**
     * See {@link LogLevel#WARNING}
     */
    String TAG_WARNING = "[WARNING]";

    /**
     * See {@link LogLevel#INFO}
     */
    String TAG_INFO = "[INFO]";

    /**
     * See {@link LogLevel#DEBUG}
     */
    String TAG_DEBUG = "[DEBUG]";

}
