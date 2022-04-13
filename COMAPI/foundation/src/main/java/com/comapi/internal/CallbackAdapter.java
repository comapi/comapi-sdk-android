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

package com.comapi.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.comapi.Callback;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Adapter to change observables into callbacks. By default it subscribes on io thread and notifies on the main thread.
 * To change that behaviour override CallbackAdapter#adapt(Observable, Callback) method and pass to SDK initialisation method.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
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