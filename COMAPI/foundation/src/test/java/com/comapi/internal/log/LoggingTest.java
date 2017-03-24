package com.comapi.internal.log;

import android.app.Application;
import android.os.Build;

import com.comapi.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

/**
 * Example Robolectric tests to check test automation scripts
 *
 * @author Marcin Swierczek
 *         Copyright (C) Donky Networks Ltd. All rights reserved.
 * @version 1.0.0
 * @since 1.0.0
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(manifest = "foundation/src/main/AndroidManifest.xml", sdk = Build.VERSION_CODES.M, constants = BuildConfig.class, packageName = "com.comapi")
public class LoggingTest {

    private static final String errorMsg = "@error@";
    private static final String wrnMsg = "@wrn@";
    private static final String infoMsg = "@info@";
    private static final String debugMsg = "@debug@";
    private static final String fatalMsg = "@fatal@";

    private static final int LIMIT = 1000;

    private static ByteArrayOutputStream baos;
    private static PrintStream ps;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

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
}