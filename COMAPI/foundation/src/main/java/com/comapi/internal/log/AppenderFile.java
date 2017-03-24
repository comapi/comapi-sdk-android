package com.comapi.internal.log;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Class to implement log output to console.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
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
     * File output stream to save Donky SDK logs.
     */
    private FileOutputStream outputStream;

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
        executor = Executors.newSingleThreadExecutor();
        if (logSizeLimit >= MIN_LOG_SIZE) {
            this.fileSizeLimitKb = logSizeLimit;
        } else {
            this.fileSizeLimitKb = DEFAULT_LOG_SIZE;
        }
    }

    @Override
    public void appendLog(final String clazz, final int logLevel, final String msg, final Throwable exception) {

        Context context = appContextRef.get();

        if (shouldAppend(logLevel) && context != null) {

            executor.submit(() -> {

                synchronized (sharedLock) {

                    try {

                        rollOverFiles();

                        outputStream = context.openFileOutput(name(1), Context.MODE_APPEND);

                        if (outputStream != null) {
                            outputStream.write(formatter.formatMessage(logLevel, msg, exception).getBytes(Charset.forName("UTF-8")));
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

                float fileSize = new File(dir, name(1)).length();
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

        return Observable.create(

                new Observable.OnSubscribe<String>() {

                    @Override
                    public void call(Subscriber<? super String> sub) {

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
                                    sub.onError(e);
                                } finally {
                                    sharedLock.notifyAll();
                                }
                            }
                        }

                        sub.onNext(sb.toString());
                        sub.onCompleted();
                    }
                }
        );
    }
}
