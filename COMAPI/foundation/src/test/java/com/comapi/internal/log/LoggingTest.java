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

import android.app.Application;
import android.os.Build;

import com.comapi.BuildConfig;
import com.comapi.helpers.FileHelper;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Example Robolectric tests to check test automation scripts
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.M, constants = BuildConfig.class, packageName = "com.comapi")
public class LoggingTest {

    private static final String errorMsg = "@error@";
    private static final String wrnMsg = "@wrn@";
    private static final String infoMsg = "@info@";
    private static final String debugMsg = "@debug@";
    private static final String fatalMsg = "@fatal@";

    private static final int LIMIT = 1000;

    private static ByteArrayOutputStream baos;
    private static PrintStream ps;

    private final Object obj = new Object();

    @Before
    public void setUp() throws Exception {
        baos = new ByteArrayOutputStream();
        ps = new PrintStream(baos, true, "utf-8");
    }

    @Test
    public void fatalLogLevel() throws IOException, InterruptedException {

        ShadowLog.stream = ps;

        LogManager mgr = new LogManager();
        mgr.init(RuntimeEnvironment.application, LogLevelConst.FATAL, LogLevelConst.FATAL, LIMIT);
        Logger log = new Logger(mgr, "LoggingTest");

        String id = UUID.randomUUID().toString();
        logAll(id, log);
        assertConsole(LogLevelConst.FATAL, id);
        assertFile(LogLevelConst.FATAL, id, mgr);
    }

    @Test
    public void errorLogLevel() throws IOException, InterruptedException {
        ShadowLog.stream = ps;

        LogManager mgr = new LogManager();
        mgr.init(RuntimeEnvironment.application, LogLevelConst.ERROR, LogLevelConst.ERROR, LIMIT);
        Logger log = new Logger(mgr, "LoggingTest");

        String id = UUID.randomUUID().toString();
        logAll(id, log);
        assertConsole(LogLevelConst.ERROR, id);
        assertFile(LogLevelConst.ERROR, id, mgr);
    }

    @Test
    public void warningLogLevel() throws InterruptedException {
        ShadowLog.stream = ps;

        LogManager mgr = new LogManager();
        mgr.init(RuntimeEnvironment.application, LogLevelConst.WARNING, LogLevelConst.WARNING, LIMIT);
        Logger log = new Logger(mgr, "LoggingTest");

        String id = UUID.randomUUID().toString();
        logAll(id, log);
        assertConsole(LogLevelConst.WARNING, id);
        assertFile(LogLevelConst.WARNING, id, mgr);
    }

    @Test
    public void infoLogLevel() throws InterruptedException {
        ShadowLog.stream = ps;

        LogManager mgr = new LogManager();
        mgr.init(RuntimeEnvironment.application, LogLevelConst.INFO, LogLevelConst.INFO, LIMIT);
        Logger log = new Logger(mgr, "LoggingTest");

        String id = UUID.randomUUID().toString();
        logAll(id, log);
        assertConsole(LogLevelConst.INFO, id);
        assertFile(LogLevelConst.INFO, id, mgr);
    }

    @Test
    public void debugLogLevel() throws InterruptedException {
        ShadowLog.stream = ps;

        LogManager mgr = new LogManager();
        mgr.init(RuntimeEnvironment.application, LogLevelConst.DEBUG, LogLevelConst.DEBUG, LIMIT);
        Logger log = new Logger(mgr, "LoggingTest");

        String id = UUID.randomUUID().toString();
        logAll(id, log);
        assertConsole(LogLevelConst.DEBUG, id);
        assertFile(LogLevelConst.DEBUG, id, mgr);
    }

    @Test
    public void offLogLevel() throws InterruptedException {
        ShadowLog.stream = ps;

        LogManager mgr = new LogManager();
        mgr.init(RuntimeEnvironment.application, LogLevelConst.OFF, LogLevelConst.OFF, LIMIT);
        Logger log = new Logger(mgr, "LoggingTest");

        String id = UUID.randomUUID().toString();
        logAll(id, log);
        assertConsole(LogLevelConst.OFF, id);
        assertFile(LogLevelConst.OFF, id, mgr);
    }

    @Test
    public void testRollOver() throws InterruptedException {

        // having some troubles with file writing mocks on windows and probably renameTo method
        Assume.assumeFalse(isWindows());

        // Remove shadow consoleLevel stream to avoid out of memory exception in test
        ShadowLog.stream = null;

        // Private config in AppenderFile
        int maxFiles = 2;
        int LOG_FILE_SIZE_LIMIT_KB = LIMIT;

        LogManager mgr = new LogManager();
        mgr.init(RuntimeEnvironment.application, LogLevelConst.DEBUG, LogLevelConst.DEBUG, LOG_FILE_SIZE_LIMIT_KB);
        Logger log = new Logger(mgr, "LoggingTest");

        for (int i = 0; i < 10000; i++) {
            logAll("Some very very very long text, ok maybe not so long.", log);
        }

        synchronized (obj) {
            obj.wait(20000);
        }

        Application app = RuntimeEnvironment.application;
        File dir = app.getFilesDir();

        for (int i = maxFiles; i > 1; i--) {
            File file = new File(dir, name(i));
            assertEquals(true, file.exists());
            float length = file.length() / 1024.0f;
            assertEquals(true, length >= LOG_FILE_SIZE_LIMIT_KB);
        }

        File file = new File(dir, name(1));
        assertEquals(true, file.exists());
        float length = file.length() / 1024.0f;
        assertEquals(true, length < 2 * LOG_FILE_SIZE_LIMIT_KB);

    }

    @Test
    public void testCopyLogs() throws InterruptedException, IOException {

        String fileName = UUID.randomUUID().toString();
        File file0 = new File(RuntimeEnvironment.application.getFilesDir(), fileName);
        //noinspection ResultOfMethodCallIgnored

        LogManager mgr = new LogManager();
        mgr.init(RuntimeEnvironment.application, LogLevelConst.OFF, LogLevelConst.DEBUG, 10000);
        Logger log = new Logger(mgr, "LoggingTest");

        log.i("test001");
        log.w("test002");
        log.e("test003");
        log.f("test004", new RuntimeException("e001"));
        log.d("test005");
        File file = mgr.copyLogs(file0).toBlocking().first();
        String text = FileHelper.readFile(file);

        assertNotNull(text);
        assertTrue(text.contains("test005"));
        assertTrue(text.contains("test002"));
        assertTrue(text.contains("test001"));
        assertTrue(text.contains("test003"));
        assertTrue(text.contains("test004"));
        assertTrue(text.contains("com.comapi.internal.log.LoggingTest.testCopyLogs"));
        //noinspection ResultOfMethodCallIgnored
        file0.delete();
    }

    private String name(int index) {
        // Private config in AppenderFile
        String LOG_FILE_NAME = "comapi_logs_";
        return LOG_FILE_NAME + index + ".log";
    }

    private void logAll(String id, Logger log) {
        log.e(errorMsg + id);
        log.w(wrnMsg + id);
        log.i(infoMsg + id);
        log.d(debugMsg + id);
        Exception exception = new Exception("Test exception", new Exception("Test cause"));
        log.f(fatalMsg + id, exception);
    }

    private void assertConsole(int level, String id) {
        String logCat = baos.toString();
        assertLogged(level, id, logCat);
    }

    private void assertFile(final int level, final String id, LogManager mgr) throws InterruptedException {
        mgr.getLogs().forEach(logs -> assertLogged(level, id, logs));
    }

    private void assertLogged(int level, String id, String logs) {

        switch (level) {
            case LogLevelConst.FATAL:
                assertEquals(true, logs.contains(fatalMsg + id));
                assertEquals(false, logs.contains(errorMsg + id));
                assertEquals(false, logs.contains(wrnMsg + id));
                assertEquals(false, logs.contains(infoMsg + id));
                assertEquals(false, logs.contains(debugMsg + id));
                break;
            case LogLevelConst.ERROR:
                assertEquals(true, logs.contains(fatalMsg + id));
                assertEquals(true, logs.contains(errorMsg + id));
                assertEquals(false, logs.contains(wrnMsg + id));
                assertEquals(false, logs.contains(infoMsg + id));
                assertEquals(false, logs.contains(debugMsg + id));
                break;
            case LogLevelConst.WARNING:
                assertEquals(true, logs.contains(fatalMsg + id));
                assertEquals(true, logs.contains(errorMsg + id));
                assertEquals(true, logs.contains(wrnMsg + id));
                assertEquals(false, logs.contains(infoMsg + id));
                assertEquals(false, logs.contains(debugMsg + id));
                break;
            case LogLevelConst.INFO:
                assertEquals(true, logs.contains(fatalMsg + id));
                assertEquals(true, logs.contains(errorMsg + id));
                assertEquals(true, logs.contains(wrnMsg + id));
                assertEquals(true, logs.contains(infoMsg + id));
                assertEquals(false, logs.contains(debugMsg + id));
                break;
            case LogLevelConst.DEBUG:
                assertEquals(true, logs.contains(fatalMsg + id));
                assertEquals(true, logs.contains(errorMsg + id));
                assertEquals(true, logs.contains(wrnMsg + id));
                assertEquals(true, logs.contains(infoMsg + id));
                assertEquals(true, logs.contains(debugMsg + id));
                break;
            case LogLevelConst.OFF:
                if (logs != null) {
                    assertEquals(false, logs.contains(fatalMsg + id));
                    assertEquals(false, logs.contains(errorMsg + id));
                    assertEquals(false, logs.contains(wrnMsg + id));
                    assertEquals(false, logs.contains(infoMsg + id));
                    assertEquals(false, logs.contains(debugMsg + id));
                }
                break;
        }
    }

    public boolean isWindows() {
        return System.getProperty( "os.name" ).startsWith( "Windows" );
    }
}