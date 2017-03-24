package com.comapi.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.comapi.Callback;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Adapter to change observables into callbacks. By default it subscribes on io thread and notifies on the main thread.
 * To change that behaviour ovveride CallbackAdapter#adapt(Observable, Callback) method and pass to SDK initialisation method.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class CallbackAdapter {

    /**
     * Changes observables into callbacks.
     *
     * @param subscriber Observable to subscribe to.
     * @param callback Callback where onError and onNext from Observable will be delivered
     * @param <T> Class of the result.
     */
    public <T> void adapt(@NonNull final Observable<T> subscriber, @Nullable final Callback<T> callback) {
        subscriber.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<T>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (callback != null) {
                            callback.error(e);
                        }
                    }

                    @Override
                    public void onNext(T result) {
                        if (callback != null) {
                            callback.success(result);
                        }
                    }
                });
    }
}