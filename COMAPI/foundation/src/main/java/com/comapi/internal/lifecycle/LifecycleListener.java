package com.comapi.internal.lifecycle;

import android.content.Context;

/**
 * Listener for application lifecycle.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface LifecycleListener {

    /**
     * Called when application was foregrounded.
     */
    void onForegrounded(Context context);

    /**
     * Called when application was backgrounded.
     */
    void onBackgrounded(Context context);
}
