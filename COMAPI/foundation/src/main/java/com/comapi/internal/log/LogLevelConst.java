package com.comapi.internal.log;

/**
 * Level of logging that Comapi SDK will apply. Values stored here are used in Logger class to determine
 * if the message should be logged.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface LogLevelConst {

    /**
     * Will not log anything in this mode.
     */
    int OFF = 0;

    /**
     * Severe runtime exception.
     */
    int FATAL = 1;

    /**
     * Runtime errors or unexpected conditions.
     */
    int ERROR = 2;

    /**
     * Runtime situations that are undesirable or unexpected, but may not require intervention.
     */
    int WARNING = 3;

    /**
     * Interesting runtime events.
     */
    int INFO = 4;

    /**
     * Verbose runtime information for debugging purpose.
     */
    int DEBUG = 5;

}
