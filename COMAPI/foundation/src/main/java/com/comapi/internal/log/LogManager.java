package com.comapi.internal.log;

import android.content.Context;
import android.support.annotation.NonNull;

import rx.Observable;

/**
 * Manager class for logging. Internal API for setting log levels and log messages.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class LogManager {

    private AppenderConsole aConsole;

    private AppenderFile aFile;

    /**
     * Initialise Logging manager.
     *
     * @param context      Application context.
     * @param levConsole   Log level threshold for console output.
     * @param levFile      Log level threshold for file output.
     * @param logSizeLimit Log files size limit.
     */
    public void init(@NonNull Context context, final int levConsole, final int levFile, int logSizeLimit) {
        if (levConsole != LogLevel.OFF.getValue()) {
            aConsole = new AppenderConsole(levConsole, new FormatterConsoleLog());
        }
        if (levFile != LogLevelConst.OFF) {
            aFile = new AppenderFile(context, levFile, new FormatterFileLog(), logSizeLimit);
        }
    }

    /**
     * Gets the content of internal log files.
     */
    public Observable<String> getLogs() {
        return aFile != null ? aFile.getLogs() : Observable.just(null);
    }

    /**
     * Logs message using defined appender.
     *
     * @param clazz     Name of the class from the which the log originate.
     * @param logLevel  Level of the log message.
     * @param msg       Message to be logged.
     * @param exception Optional exception to extract the stacktrace.
     */
    public void log(final String clazz, final int logLevel, final String msg, final Throwable exception) {
        if (aConsole != null) {
            aConsole.appendLog(clazz, logLevel, msg, exception);
        }
        if (aFile != null) {
            aFile.appendLog(clazz, logLevel, msg, exception);
        }
    }
}
