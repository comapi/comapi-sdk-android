package com.comapi;

import com.comapi.internal.lifecycle.LifecycleListener;
import com.comapi.internal.log.Logger;
import com.comapi.internal.network.api.RxComapiService;

/**
 * Wrapper class for client to access methods needed by COMAPI extension modules. This class should be used only by COMAPI modules and doesn't contain supported public APIs.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ClientHelper {

    /**
     * Gets logger to access internal logs writer. Convenience method for chat layer.
     *
     * @param client COMAPI client.
     * @return Internal logger instance.
     */
    public static Logger getLogger(RxComapiClient client) {
        return client.getLogger();
    }

    /**
     * Adds listener for application lifecycle callbacks. Convenience method for chat layer.
     *
     * @param client COMAPI client.
     * @param listener Listener for application lifecycle callbacks.
     */
    public static void addLifecycleListener(RxComapiClient client, LifecycleListener listener) {
        client.addLifecycleListener(listener);
    }

    /**
     * Entire service interface. Convenience method for chat layer.
     *
     * @param client COMAPI client.
     * @return Service interface.
     */
    public static RxComapiService getComapiService(RxComapiClient client) {
        return client.getComapiService();
    }

    public static void resetShared() {
        RxComapi.reset();
    }
}
