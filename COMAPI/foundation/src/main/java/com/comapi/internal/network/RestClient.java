package com.comapi.internal.network;

import android.support.annotation.NonNull;

import com.comapi.internal.log.LogLevelConst;
import com.comapi.internal.network.api.RestApi;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Perform initialisation of network clients and provides access to REST service.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * 31/03/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
class RestClient {

    /**
     * Service connection timeout.
     */
    private static final int CONNECT_TIMEOUT = 60;

    /**
     * Service read timeout.
     */
    private static final int READ_TIMEOUT = 90;

    /**
     * Service write timeout.
     */
    private static final int WRITE_TIMEOUT = 90;

    /**
     * REST API
     */
    private RestApi service;

    /**
     * Recommended constructor.
     *
     * @param logLevel Log level threshold for logging service requests and responses.
     * @param baseUrl  Base URL endpoint for services.
     */
    RestClient(final OkHttpAuthenticator authenticator, int logLevel, String baseUrl) {
        if (service == null) {
            createService(createOkHttpClient(authenticator, logLevel), baseUrl);
        }
    }

    /**
     * Gets the service APIs.
     *
     * @return Service APIs.
     */
    public RestApi getService() {
        return service;
    }

    /**
     * Create and configure OkHTTP client.
     *
     * @param authenticator Class to intercept unauthorised responses and authenticate.
     * @param logLevel      Log level threshold for logging service requests and responses.
     * @return OkHTTP client.
     */
    private OkHttpClient createOkHttpClient(OkHttpAuthenticator authenticator, int logLevel) {

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor(logLevel))
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .authenticator(authenticator);

        return builder.build();
    }

    /**
     * Create and configure HTTP interceptor that will log the bodies of requests and responses.
     *
     * @param logLevel Log level threshold for logging service requests and responses.
     * @return Interceptor to be used to configure the OkHTTP client.
     */
    private HttpLoggingInterceptor loggingInterceptor(int logLevel) {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();

        switch (logLevel) {
            case LogLevelConst.DEBUG:
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                break;
            default:
                interceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
                break;
        }

        return interceptor;
    }

    /**
     * Create Retrofit service for Comapi Network REST APIs.
     *
     * @param client REST service.
     */
    private void createService(@NonNull OkHttpClient client, String baseUrl) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(createGson()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build();

        service = retrofit.create(RestApi.class);
    }

    /**
     * Create Gson converter for the service.
     *
     * @return Gson converter.
     */
    private Gson createGson() {

        GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping().setLenient().

                addSerializationExclusionStrategy(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getAnnotation(SerializedName.class) == null;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                });

        return gsonBuilder.create();
    }

}
