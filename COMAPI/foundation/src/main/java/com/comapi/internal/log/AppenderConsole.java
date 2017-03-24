package com.comapi.internal.log;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Class to implement log output to console.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
class AppenderConsole extends Appender {

    private final FormatterConsoleLog formatter;

    /**
     * Recommended constructor.
     *
     * @param logLevel  Logging level that should be used for console output. Messages with higher
     *                  level won't be displayed in logcat.
     * @param formatter Message formatter. Defines the format of the output.
     */
    AppenderConsole(final int logLevel, @NonNull final FormatterConsoleLog formatter) {
        super(logLevel);
        this.formatter = formatter;
    }

    @Override
    public void appendLog(String clazz, int msgLogLevel, String msg, Throwable exception) {

        if (shouldAppend(msgLogLevel)) {

            String log;

            switch (getLevel()) {

                //TODO
//                case LogLevelConst.DEBUG:
//                    log = formatter.formatMessage(msgLogLevel, clazz, msg);

                default:
                    log = formatter.formatMessage(msgLogLevel, clazz, msg);

            }

            switch (msgLogLevel) {

                case LogLevelConst.FATAL:
                    Log.e(LogConstants.TAG, log, exception);
                    break;

                case LogLevelConst.ERROR:
                    Log.e(LogConstants.TAG, log);
                    break;

                case LogLevelConst.WARNING:
                    Log.w(LogConstants.TAG, log);
                    break;

                case LogLevelConst.INFO:
                    Log.i(LogConstants.TAG, log);
                    break;

                case LogLevelConst.DEBUG:
                    Log.d(LogConstants.TAG, log);
                    break;

            }
        }
    }
}
