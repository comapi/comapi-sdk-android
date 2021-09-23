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

package com.comapi;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.comapi.internal.log.LogLevelConst;
import com.comapi.internal.log.LogManager;
import com.comapi.internal.log.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ALoggingTest {

    private static final String errorMsg = "@error@";
    private static final String wrnMsg = "@wrn@";
    private static final String infoMsg = "@info@";
    private static final String debugMsg = "@debug@";
    private static final String fatalMsg = "@fatal@";

    private static final int LIMIT = 1000;

    @Test
    public void errorLogLevel() throws IOException, InterruptedException {

        clearLogcat();

        LogManager mgr = new LogManager();
        mgr.init(InstrumentationRegistry.getTargetContext(), LogLevelConst.ERROR, LogLevelConst.ERROR, LIMIT);
        Logger log = new Logger(mgr, "ALoggingTest");

        String id = UUID.randomUUID().toString();
        logMessages(log, id);

        String logStr = getLogcat();
        validateLogs(logStr, id, true, true, false, false, false);

        loadAndValidateLogs(mgr, id, true, true, false, false, false);

    }

    @Test
    public void warningLogLevel() throws IOException, InterruptedException {

        clearLogcat();

        LogManager mgr = new LogManager();
        mgr.init(InstrumentationRegistry.getTargetContext(), LogLevelConst.WARNING, LogLevelConst.WARNING, LIMIT);
        Logger log = new Logger(mgr, "ALoggingTest");

        String id = UUID.randomUUID().toString();
        logMessages(log, id);

        String logStr = getLogcat();
        validateLogs(logStr, id, true, true, true, false, false);

        loadAndValidateLogs(mgr, id, true, true, true, false, false);

    }

    @Test
    public void infoLogLevel() throws IOException, InterruptedException {

        clearLogcat();

        LogManager mgr = new LogManager();
        mgr.init(InstrumentationRegistry.getTargetContext(), LogLevelConst.INFO, LogLevelConst.INFO, LIMIT);
        Logger log = new Logger(mgr, "ALoggingTest");

        String id = UUID.randomUUID().toString();
        logMessages(log, id);

        String logStr = getLogcat();
        validateLogs(logStr, id, true, true, true, true, false);

        loadAndValidateLogs(mgr, id, true, true, true, true, false);

    }

    @Test
    public void debugLogLevel() throws IOException, InterruptedException {

        clearLogcat();

        LogManager mgr = new LogManager();
        mgr.init(InstrumentationRegistry.getTargetContext(), LogLevelConst.DEBUG, LogLevelConst.DEBUG, LIMIT);
        Logger log = new Logger(mgr, "ALoggingTest");

        String id = UUID.randomUUID().toString();
        logMessages(log, id);

        String logStr = getLogcat();
        validateLogs(logStr, id, true, true, true, true, true);

        loadAndValidateLogs(mgr, id, true, true, true, true, true);

    }

    @Test
    public void fatalLogLevel() throws IOException, InterruptedException {

        clearLogcat();

        LogManager mgr = new LogManager();
        mgr.init(InstrumentationRegistry.getTargetContext(), LogLevelConst.FATAL, LogLevelConst.FATAL, LIMIT);
        Logger log = new Logger(mgr, "ALoggingTest");

        String id = UUID.randomUUID().toString();
        logMessages(log, id);

        String logStr = getLogcat();
        validateLogs(logStr, id, true, false, false, false, false);

        loadAndValidateLogs(mgr, id, true, false, false, false, false);

    }

    @Test
    public void offLogLevel() throws IOException, InterruptedException {

        clearLogcat();

        LogManager mgr = new LogManager();
        mgr.init(InstrumentationRegistry.getTargetContext(), LogLevelConst.OFF, LogLevelConst.OFF, LIMIT);
        Logger log = new Logger(mgr, "ALoggingTest");

        String id = UUID.randomUUID().toString();
        logMessages(log, id);

        String logStr = getLogcat();
        validateLogs(logStr, id, false, false, false, false, false);

        assertNull(mgr.getLogs().toBlocking().single());

    }

    @Test
    public void testRollOver() throws InterruptedException {

        LogManager mgr = new LogManager();
        mgr.init(InstrumentationRegistry.getTargetContext(), LogLevelConst.DEBUG, LogLevelConst.DEBUG, LIMIT);
        Logger log = new Logger(mgr, "ALoggingTest");

        // Private config in AppenderFile
        int maxFiles = 2;

        for (int i = 0; i < 1000; i++) {
            logMessages(log, "Some very very very long text, ok maybe not so long.");
        }

        final Object obj = new Object();

        synchronized (obj) {
            obj.wait(10000);
        }

        Context context = InstrumentationRegistry.getTargetContext();
        File dir = context.getFilesDir();

        for (int i = maxFiles; i >= 1; i--) {
            File file = new File(dir, name(i));
            assertEquals(true, file.exists());
            double length = Math.floor(file.length() / 1024.0f - 1);
            assertTrue(length <= LIMIT);
        }
    }

    private void loadAndValidateLogs(LogManager mgr, final String id, boolean isFatal, boolean isError, boolean isWarning, boolean isInfo, boolean isDebug) throws InterruptedException {

        String logs = mgr.getLogs().toBlocking().single();

        assertNotNull(logs);
        assertEquals(isFatal, logs.contains(fatalMsg + id));
        assertEquals(isError, logs.contains(errorMsg + id));
        assertEquals(isWarning, logs.contains(wrnMsg + id));
        assertEquals(isInfo, logs.contains(infoMsg + id));
        assertEquals(isDebug, logs.contains(debugMsg + id));

    }

    private String name(int index) {
        // Private config in AppenderFile
        String LOG_FILE_NAME = "comapi_logs_";
        return LOG_FILE_NAME + index + ".log";
    }

    private void validateLogs(String logs, String id, boolean isFatal, boolean isError, boolean isWarning, boolean isInfo, boolean isDebug) throws IOException {

        assertNotNull(logs);
        assertEquals(isFatal, logs.contains(fatalMsg + id));
        assertEquals(isError, logs.contains(errorMsg + id));
        assertEquals(isWarning, logs.contains(wrnMsg + id));
        assertEquals(isInfo, logs.contains(infoMsg + id));
        assertEquals(isDebug, logs.contains(debugMsg + id));

    }

    private void logMessages(Logger log, String id) {
        log.e(errorMsg + id);
        log.w(wrnMsg + id);
        log.i(infoMsg + id);
        log.d(debugMsg + id);
        log.f(fatalMsg + id, new Exception("Test exception"));
    }

    private void clearLogcat() throws IOException {
        Runtime.getRuntime().exec("logcat -c");

        Process process = Runtime.getRuntime().exec("logcat -c");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()), 1024);
        String line = bufferedReader.readLine();
        assertNull(line);
    }

    private String getLogcat() throws IOException {

        Process process = Runtime.getRuntime().exec("logcat -d");
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        StringBuilder log = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            log.append(line);
        }

        return log.toString();
    }

}