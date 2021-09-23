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

import android.content.Context;
import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.exceptions.Exceptions;
import rx.schedulers.Schedulers;

/**
 * Class to implement log output to console.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
class AppenderFile extends Appender {

    private static final int MIN_LOG_SIZE = 500;

    private static final int DEFAULT_LOG_SIZE = 1000;

    /**
     * Object to lock the thread on.
     */
    private static final Object sharedLock = new Object();

    /**
     * Maximum number of files in which the logs are stored.
     */
    private final int maxFiles = 2;

    /**
     * Limit of the file size. When exceeded the files will be rolled over.
     */
    private int fileSizeLimitKb;

    /**
     * Log files name prefix.
     */
    private static final String LOG_FILE_NAME = "comapi_logs_";

    private final WeakReference<Context> appContextRef;

    /**
     * Formats the log entry.
     */
    private final FormatterFileLog formatter;

    /**
     * File input stream to load Donky SDK logs.
     */
    private FileInputStream inputStream;

    /**
     * Work thread executor.
     */
    private final ExecutorService executor;

    /**
     * Recommended constructor.
     *
     * @param appContext   Application Context.
     * @param logLevel     Logging level that should be used for console output. Messages with higher
     *                     level won't be displayed in logcat.
     * @param formatter    Message formatter. Defines the format of the output.
     * @param logSizeLimit Size limit for internal log files
     */
    AppenderFile(@NonNull final Context appContext, final int logLevel, @NonNull final FormatterFileLog formatter, int logSizeLimit) {
        super(logLevel);
        this.appContextRef = new WeakReference<>(appContext);
        this.formatter = formatter;
        this.executor = Executors.newSingleThreadExecutor();
        if (logSizeLimit >= MIN_LOG_SIZE) {
            this.fileSizeLimitKb = logSizeLimit;
        } else {
            this.fileSizeLimitKb = DEFAULT_LOG_SIZE;
        }
    }

    @Override
    public void appendLog(final String tag, final int logLevel, final String msg, final Throwable exception) {

        Context context = appContextRef.get();

        if (shouldAppend(logLevel) && context != null) {

            executor.submit(() -> {

                synchronized (sharedLock) {

                    try {

                        rollOverFiles();

                        File mainFile = new File(context.getFilesDir(), name(1));
                        if (!mainFile.exists()) {
                            mainFile.createNewFile();
                        }
                        if (mainFile.exists() && mainFile.canWrite()) {
                            Writer outputStream = new BufferedWriter(new FileWriter(mainFile, true), 1024);
                            outputStream.write(formatter.formatMessage(logLevel, tag, msg, exception));
                            outputStream.flush();
                            outputStream.close();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        sharedLock.notifyAll();
                    }
                }
            });
        }
    }

    /**
     * Gets the content of internal log files.
     */
    public Observable<String> getLogs() {

        return loadLogsObservable()
                .subscribeOn(Schedulers.from(executor));
    }

    /**
     * Keep the maximum size of log files close to maximum value.
     * Logs are cached if the size of the currently used log file exceeds max value.
     */
    private void rollOverFiles() {

        Context context = appContextRef.get();

        if (context != null) {

            File dir = context.getFilesDir();

            File mainFile = new File(dir, name(1));

            if (mainFile.exists()) {

                float fileSize = mainFile.length();
                fileSize = fileSize / 1024.0f; //In kilobytes

                if (fileSize > fileSizeLimitKb) {

                    File file;
                    File target;

                    file = new File(dir, name(maxFiles));
                    if (file.exists()) {
                        file.delete();
                    }

                    for (int i = maxFiles - 1; i > 0; i--) {
                        file = new File(dir, name(i));
                        if (file.exists()) {
                            target = new File(dir, name(i + 1));
                            file.renameTo(target);
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the name of log file with a given index.
     *
     * @param index Index of log file.
     * @return Name of log file.
     */
    private String name(int index) {
        return LOG_FILE_NAME + index + ".log";
    }

    /**
     * Gets rx observable that reads the log files and appends to string.
     *
     * @return Observable that reads the log files and appends to string.
     */
    private Observable<String> loadLogsObservable() {

        return Observable.fromCallable(() -> {

            StringBuilder sb = new StringBuilder();

            Context context = appContextRef.get();

            if (context != null) {

                synchronized (sharedLock) {

                    try {

                        File dir = context.getFilesDir();

                        String line;

                        for (int i = 1; i <= maxFiles; i++) {

                            File file = new File(dir, name(i));

                            if (file.exists()) {
                                inputStream = new FileInputStream(file);
                                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                while ((line = reader.readLine()) != null) {
                                    sb.append(line).append('\n');
                                }
                                reader.close();
                            }
                        }
                    } catch (Exception e) {
                        throw Exceptions.propagate(e);
                    } finally {
                        sharedLock.notifyAll();
                    }
                }
            }

            return sb.toString();
        });
    }

    /**
     * Create observable returning same file that was passed as a parameter but with merged content of internal log files.
     *
     * @param mergedFile Instance of a file to merge logs into.
     * @return Observable returning an instance of a file with logs merged into.
     */
    Observable<File> mergeLogs(@NonNull File mergedFile) {

        return Observable.fromCallable(() -> {
            synchronized (sharedLock) {
                try {
                    mergeFiles(mergedFile);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    sharedLock.notifyAll();
                }
            }
            return mergedFile;
        }).subscribeOn(Schedulers.from(executor));
    }

    /**
     * Merged content of internal log files.
     *
     * @param mergedFile Instance of a file to merge logs into.
     */
    private void mergeFiles(@NonNull File mergedFile) throws IOException {

        Context context = appContextRef.get();

        if (context != null) {

            FileWriter fw;
            BufferedWriter bw;
            fw = new FileWriter(mergedFile, true);
            bw = new BufferedWriter(fw);

            File dir = context.getFilesDir();
            for (int i = 1; i <= maxFiles; i++) {

                File file = new File(dir, name(i));
                if (file.exists()) {

                    FileInputStream fis;
                    try {

                        fis = new FileInputStream(file);
                        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                        //noinspection TryFinallyCanBeTryWithResources
                        try {
                            String aLine;
                            while ((aLine = br.readLine()) != null) {
                                bw.write(aLine);
                                bw.newLine();
                            }
                        } finally {
                            br.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            bw.close();
        }
    }
}