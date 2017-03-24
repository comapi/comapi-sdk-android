package com.comapi.internal.log;

import com.comapi.internal.Parser;
import com.comapi.internal.helpers.DateHelper;

/**
 * Formats the log entry for file log appender.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
class FormatterFileLog extends Formatter {

    String formatMessage(int msgLogLevel, String msg, Throwable exception) {
        LogEntry entry = new LogEntry(msgLogLevel, msg, exception);
        Parser gson = new Parser();
        return gson.toJson(entry) + "\n";
    }

    /**
     * Class representing a log entry as a json.
     */
    private class LogEntry {

        final String time;

        final String level;

        final String msg;

        final String stacktrace;

        /**
         * Constructor for non-debug mode.
         *
         * @param msgLogLevel Level of the log message.
         * @param msg         Log message.
         * @param exception   Optional exception.
         */
        LogEntry(final int msgLogLevel, final String msg, final Throwable exception) {
            time = DateHelper.getCurrentUTC();
            level = getLevelTag(msgLogLevel);
            this.msg = msg;
            stacktrace = getStackTrace(exception);
        }
    }

    /**
     * Gets stacktrace as a String.
     *
     * @param exception Exception for which the stacktrace should be returned.
     * @return Stacktrace as a String.
     */
    private String getStackTrace(final Throwable exception) {

        if (exception != null) {

            StringBuilder sb = new StringBuilder();

            StackTraceElement[] stackTrace = exception.getStackTrace();
            for (StackTraceElement element : stackTrace) {
                sb.append(element.toString());
                sb.append('\n');
            }

            if (exception.getCause() != null) {

                StackTraceElement[] stackTraceCause = exception.getCause().getStackTrace();
                for (StackTraceElement element : stackTraceCause) {
                    sb.append(element.toString());
                    sb.append('\n');
                }

            }

            return sb.toString();
        }

        return null;
    }
}
