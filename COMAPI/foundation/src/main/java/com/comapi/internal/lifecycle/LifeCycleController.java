package com.comapi.internal.lifecycle;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Observer for global application state. Invokes callback when application is being backgrounded or foregrounded ignoring Activities transitions.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class LifeCycleController {

    /**
     * When activities was paused but not resumed during MAX_ACTIVITY_TRANSITION_TIME_MS we treat that as closing app event.
     */
    private static final long MAX_ACTIVITY_TRANSITION_TIME_MS = 1000;

    /**
     * Flag to determine if app was in background (MAX_ACTIVITY_TRANSITION_TIME_MS passed between onPause and onResume for any Activity in app.
     */
    private final AtomicBoolean wasInBackground;

    private final AtomicBoolean isApplicationForegrounded;

    /**
     * Time when onPause of the Activity was called and the Timer is starting measuring transition time between Activities.
     */
    private final AtomicLong startTime;

    /**
     * Main SDK listener for background/foreground events.
     */
    private final LifecycleListener listener;

    /**
     * Timer to measure transition time between Activities.
     */
    private TimerTask activityTransitionTimerTask;

    /**
     * Private constructor.
     *
     * @param listener Listener for application lifecycle callbacks.
     */
    private LifeCycleController(@NonNull LifecycleListener listener) {
        this.listener = listener;
        isApplicationForegrounded = new AtomicBoolean(false);
        wasInBackground = new AtomicBoolean(true);
        startTime = new AtomicLong(0);
    }

    /**
     * Creates and registers application lifecycle listener.
     *
     * @param application {@link Application} instance.
     */
    public static void registerLifeCycleObserver(@NonNull Application application, @NonNull LifecycleListener listener) {
        application.registerActivityLifecycleCallbacks(new LifeCycleController(listener).createLifecycleCallback());
    }

    /**
     * Creates {@link Application.ActivityLifecycleCallbacks} listener that will respond to onResume and onPause of any Activity in the application
     * obtaining information if the Application is in the background or foreground.
     *
     * @return Application lifecycle callbacks to be registered on the system by the SDK.
     */
    private Application.ActivityLifecycleCallbacks createLifecycleCallback() {

        return new Application.ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                handleOnResume(activity.getApplicationContext());
            }

            @Override
            public void onActivityPaused(Activity activity) {
                handleOnPause(activity.getApplicationContext());
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        };
    }

    /**
     * React to activity onPause event.
     *
     * @param context Application context.
     */
    private void handleOnPause(Context context) {
        startActivityTransitionTimer(context);
    }

    /**
     * React to activity onResume event.
     *
     * @param context Application context.
     */
    private void handleOnResume(Context context) {

        if (wasInBackground.get()) {
            startTime.set(System.currentTimeMillis());
            isApplicationForegrounded.set(true);
            listener.onForegrounded(context);
        }

        stopActivityTransitionTimer();
    }

    /**
     * Start timer to measure time between onPause and onResume to determine if app was backgrounded/closed
     */
    private void startActivityTransitionTimer(final Context applicationContext) {

        Timer activityTransitionTimer = new Timer();

        activityTransitionTimerTask = new TimerTask() {

            public void run() {
                wasInBackground.set(true);
                startTime.set(0);
                isApplicationForegrounded.set(false);
                listener.onBackgrounded(applicationContext);
            }
        };

        activityTransitionTimer.schedule(activityTransitionTimerTask,
                MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    /**
     * Stop timer measuring time between onPause and onResume to determine if app was backgrounded/closed
     */
    private void stopActivityTransitionTimer() {

        if (activityTransitionTimerTask != null) {
            activityTransitionTimerTask.cancel();
        }
        wasInBackground.set(false);
    }
}
