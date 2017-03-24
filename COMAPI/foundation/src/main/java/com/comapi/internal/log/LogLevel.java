package com.comapi.internal.log;

/**
 * Level of logging that Comapi SDK will apply. Values stored here are used in {@link Logger} class to determine
 * if the message should be logged.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public enum LogLevel {

    /**
     * Will not log anything in this mode.
     */
    OFF(LogLevelConst.OFF),

    /**
     * Severe runtime exception.
     */
    FATAL(LogLevelConst.FATAL),

    /**
     * Runtime errors or unexpected conditions.
     */
    ERROR(LogLevelConst.ERROR),

    /**
     * Runtime situations that are undesirable or unexpected, but may not require intervention.
     */
    WARNING(LogLevelConst.WARNING),

    /**
     * Interesting runtime events.
     */
    INFO(LogLevelConst.INFO),

    /**
     * Verbose runtime information for debugging purpose.
     */
    DEBUG(LogLevelConst.DEBUG);

    private final int value;

    LogLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
