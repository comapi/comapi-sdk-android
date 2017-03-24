package com.comapi;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.comapi.internal.ComapiException;
import com.comapi.internal.log.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

/**
 * Class to aggregate and validate APIs endpoints.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class APIConfig {

    private final static String DEFAULT_HOST = "api.comapi.com";

    private final static String DEFAULT_SERVICE_SCHEMA = "https://";

    private final static String DEFAULT_SOCKET_SCHEMA = "wss://";

    private String serviceHost;
    private String socketHost;
    private String proxyHost;

    public APIConfig service(@NonNull String host) {
        serviceHost = host;
        return this;
    }

    public APIConfig socket(@NonNull String host) {
        socketHost = host;
        return this;
    }

    public APIConfig proxy(@NonNull String host) {
        proxyHost = host;
        return this;
    }

    public static class BaseURIs {

        private static final MessageFormat socketURI = new MessageFormat("/apispaces/{0}/socket");

        private URI service;

        private URI socket;

        private URI proxy;

        public static BaseURIs build(APIConfig config, @NonNull String apiSpaceId, @NonNull Logger log) {

            BaseURIs baseURIs = new BaseURIs();

            try {

                if (config != null && !TextUtils.isEmpty(config.serviceHost)) {
                    while (config.serviceHost.endsWith("/") && config.serviceHost.length() > 0) {
                        config.serviceHost = config.serviceHost.substring(0, config.serviceHost.length() - 1);
                    }
                    baseURIs.service = new URI(config.serviceHost).parseServerAuthority();

                } else {
                    baseURIs.service = new URI(DEFAULT_SERVICE_SCHEMA + DEFAULT_HOST);
                }

                final String uriStr = socketURI.format(new Object[]{apiSpaceId});

                if (config != null && !TextUtils.isEmpty(config.socketHost)) {
                    while (config.socketHost.endsWith("/") && config.socketHost.length() > 0) {
                        config.socketHost = config.socketHost.substring(0, config.socketHost.length() - 1);
                    }
                    baseURIs.socket = new URI(config.socketHost + uriStr).parseServerAuthority();
                } else {
                    baseURIs.socket = new URI(DEFAULT_SOCKET_SCHEMA + DEFAULT_HOST + uriStr);
                }

                if (config != null && !TextUtils.isEmpty(config.proxyHost)) {
                    baseURIs.proxy = new URI(config.proxyHost).parseServerAuthority();
                }


            } catch (URISyntaxException e) {
                log.f("Provided api configuration is wrong.", e);
            }

            if (!baseURIs.validate(log)) {
                throw new ComapiException("Wrong API baseURIs.");
            }

            return baseURIs;
        }

        private boolean validate(Logger log) {
            return !(service == null || socket == null) && (validateURI(service, log) && validateURI(socket, log) && validateURI(proxy, log));
        }

        private boolean validateURI(URI uri, Logger log) {
            if (uri != null) {
                if (TextUtils.isEmpty(uri.getHost()) || TextUtils.isEmpty(uri.getScheme())) {
                    log.w("Error validating api config uri." + uri.toString());
                    return false;
                }
            }

            return true;
        }

        public URI getService() {
            return service;
        }

        public URI getSocket() {
            return socket;
        }

        public URI getProxy() {
            return proxy;
        }
    }
}
