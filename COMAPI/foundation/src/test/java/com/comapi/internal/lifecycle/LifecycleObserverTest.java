package com.comapi.internal.lifecycle;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.comapi.BuildConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Robolectric tests for application lifecycle observer.
 *
 * @author Marcin Swierczek
 *         Copyright (C) Donky Networks Ltd. All rights reserved.
 * @version 1.0.0
 * @since 1.0.0
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(manifest = "foundation/src/main/AndroidManifest.xml", sdk = Build.VERSION_CODES.M, constants = BuildConfig.class, packageName = "com.comapi")
public class LifecycleObserverTest {

    private static final String FOREGROUNDED = "foregrounded";

    private static final String BACKGROUNDED = "backgrounded";

    private String state;

    private ActivityController<Activity> controller;

    @Before
    public void setUpComapi() throws Exception {


        LifeCycleController.registerLifeCycleObserver(RuntimeEnvironment.application, new LifecycleListener() {
            @Override
            public void onForegrounded(Context context) {
                assertNotNull(context);
                state = FOREGROUNDED;
            }

            @Override
            public void onBackgrounded(Context context) {
                assertNotNull(context);
                state = BACKGROUNDED;
            }
        });
        controller = Robolectric.buildActivity(Activity.class);
        state = null;
    }

    @Test
    public void testForegrounded() {
        controller.create().start().resume().get();
        assertEquals(FOREGROUNDED, state);
    }

    @Test
    public void testBackgrounded() throws InterruptedException {
        controller.create().start().resume().pause().stop().saveInstanceState(new Bundle()).destroy().get();
        Thread t = new Thread() {
            public void run() {
                try {
                    sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        t.join();
        assertEquals(BACKGROUNDED, state);
    }

    @After
    public void tearDown() throws Exception {

    }

}