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
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Robolectric tests for application lifecycle observer.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.M, constants = BuildConfig.class, packageName = "com.comapi")
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