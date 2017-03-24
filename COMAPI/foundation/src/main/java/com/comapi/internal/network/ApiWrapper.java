package com.comapi.internal.network;

import android.app.Application;
import android.content.Context;

import java.lang.ref.WeakReference;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Basic wrapper around REST API. Adds connectivity check and sets schedulers.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
class ApiWrapper {

    private WeakReference<Context> appContextWeakRef;

    /**
     * Recommended constructor.
     *
     * @param application Application instance.
     */
    ApiWrapper(Application application) {
        appContextWeakRef = new WeakReference<>(application.getApplicationContext());
    }

    /**
     * Sets schedulers for API calls observables.
     *
     * @param obs Observable to set schedulers for.
     * @param <E> Class of the service call result.
     * @return Observable for API call.
     */
    <E> Observable<E> wrapObservable(Observable<E> obs) {
        return obs.subscribeOn(Schedulers.io()).observeOn(Schedulers.io());
    }
}