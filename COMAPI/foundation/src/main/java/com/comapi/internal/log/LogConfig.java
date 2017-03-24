package com.comapi.internal.log;

/**
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class LogConfig {

    private LogLevel consoleLevel;

    private LogLevel fileLevel;

    private LogLevel networkLevel;

    public LogConfig() {
        consoleLevel = LogLevel.WARNING;
        fileLevel = LogLevel.WARNING;
        networkLevel = LogLevel.WARNING;
    }

    private LogConfig(boolean isDebug) {
        if (isDebug) {
            consoleLevel = LogLevel.DEBUG;
            fileLevel = LogLevel.DEBUG;
            networkLevel = LogLevel.DEBUG;
        } else {
            consoleLevel = LogLevel.WARNING;
            fileLevel = LogLevel.WARNING;
            networkLevel = LogLevel.WARNING;
        }
    }

    public static LogConfig getDebugConfig() {
        return new LogConfig(true);
    }

    public static LogConfig getProductionConfig() {
        return new LogConfig(false);
    }

    public LogLevel getConsoleLevel() {
        return consoleLevel;
    }

    public LogConfig setConsoleLevel(LogLevel consoleLevel) {
        this.consoleLevel = consoleLevel;
        return this;
    }

    public LogLevel getFileLevel() {
        return fileLevel;
    }

    public LogConfig setFileLevel(LogLevel fileLevel) {
        this.fileLevel = fileLevel;
        return this;
    }

    public LogLevel getNetworkLevel() {
        return networkLevel;
    }

    public LogConfig setNetworkLevel(LogLevel networkLevel) {
        this.networkLevel = networkLevel;
        return this;
    }
}
