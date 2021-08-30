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

package com.comapi.internal.network;

import androidx.annotation.NonNull;

import com.comapi.internal.log.Logger;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Basic wrapper around REST API. Adds connectivity check and sets schedulers.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
class ApiWrapper {

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

    /**
     * Sets schedulers for API calls observables and adds logging.
     *
     * @param obs Observable to set schedulers for.
     * @param log Logger instance.
     * @param <E> Class of the service call result.
     * @return Observable for API call.
     */
    <E> Observable<E> wrapObservable(@NonNull Observable<E> obs, @NonNull Logger log, final String msg) {
        return obs.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                .doOnNext(r -> log(log, (ComapiResult) r, msg))
                .doOnError(t -> log(log, t, msg));
    }

    /**
     * Add logging to observable.
     *
     * @param obs Observable to add logging for.
     * @param log Logger instance.
     * @param msg Message with which the log should start with.
     * @param <E> Class of the service call result.
     * @return Observable for API call.
     */
    <E> Observable<E> addLogging(@NonNull Observable<E> obs, @NonNull Logger log, final String msg) {
        return obs.doOnNext(r -> log(log, (ComapiResult) r, msg))
                .doOnError(t -> log(log, t, msg));
    }

    /**
     * Log comapi result details.
     *
     * @param log Logger instance.
     * @param r   Service call result.
     * @param msg Message with which the log should start with.
     */
    private void log(@NonNull Logger log, @NonNull ComapiResult r, String msg) {
        if (!r.isSuccessful()) {
            log.e(msg + ". Error calling services" + " (" + r.getCode() + "). " + r.getMessage() + ". " + r.getErrorBody());
        }
    }

    /**
     * Log exception details.
     *
     * @param log Logger instance.
     * @param t   Throwable with error details.
     * @param msg Message with which the log should start with.
     */
    private void log(@NonNull Logger log, @NonNull Throwable t, String msg) {
        log.f(msg + ". Error calling services. " + t.getLocalizedMessage(), t);
    }
}