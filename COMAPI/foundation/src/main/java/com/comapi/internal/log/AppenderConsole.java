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
import android.util.Log;

/**
 * Class to implement log output to console.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
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
    public void appendLog(String tag, int msgLogLevel, String msg, Throwable exception) {

        if (shouldAppend(msgLogLevel)) {

            final String log = formatter.formatMessage(msgLogLevel, tag, msg);

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
