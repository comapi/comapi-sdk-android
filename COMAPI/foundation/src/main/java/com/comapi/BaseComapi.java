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
 */
class BaseComapi {

    /**
     * Version of the ComapiImpl SDK MAJOR.MINOR.PATCH.BUILD
     */
    private final static String SDK_VERSION = "1.4.0";

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