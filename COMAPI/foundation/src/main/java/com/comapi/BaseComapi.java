package com.comapi;

import android.app.Application;
import android.text.TextUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Builder class for {@link ComapiClient} instance.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
class BaseComapi {

    /**
     * Version of the ComapiImpl SDK MAJOR.MINOR.PATCH.BUILD
     */
    private final static String SDK_VERSION = "VER_TO_REPLACE";

    private static final Set<String> apiSpaces = Collections.synchronizedSet(new HashSet<String>());

    static String checkIfCanInitialise(Application app, ComapiConfig config, boolean isShared) {
        if (app == null) {
            return "Null Application instance provided for COMAPI initialisation.";
        } else if (config == null || TextUtils.isEmpty(config.apiSpaceId)) {
            return "No API Space has been set for COMAPI initialisation.";
        } else if (config.authenticator == null) {
            return "No authenticator object has been set for COMAPI initialisation.";
        } else if (isShared && !apiSpaces.isEmpty()) {
            return "Shared instance can be initialised only once.";
        } else if (!apiSpaces.add(config.apiSpaceId)) {
            return "COMAPI initialised with this API space already.";
        }

        return null;
    }

    public static String getVersion() {
        return SDK_VERSION;
    }

    /**
     * Method used by tests to reset shared client instance.
     */
    static void reset() {
        apiSpaces.clear();
    }
}