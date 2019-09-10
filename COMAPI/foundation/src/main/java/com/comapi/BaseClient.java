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
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Pair;

import com.comapi.internal.CallbackAdapter;
import com.comapi.internal.ComapiException;
import com.comapi.internal.IClient;
import com.comapi.internal.ListenerListAdapter;
import com.comapi.internal.data.DataManager;
import com.comapi.internal.data.SessionData;
import com.comapi.internal.lifecycle.LifeCycleController;
import com.comapi.internal.lifecycle.LifecycleListener;
import com.comapi.internal.log.LogConfig;
import com.comapi.internal.log.LogConstants;
import com.comapi.internal.log.LogManager;
import com.comapi.internal.log.Logger;
import com.comapi.internal.network.ComapiResult;
import com.comapi.internal.network.InternalService;
import com.comapi.internal.network.SessionController;
import com.comapi.internal.network.SessionCreateManager;
import com.comapi.internal.network.api.RestApi;
import com.comapi.internal.network.sockets.SocketController;
import com.comapi.internal.push.PushManager;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.functions.Func1;

/**
 * ComapiImpl Client implementation for foundation SDK. Handles initialisation and stores all internal objects.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public abstract class BaseClient<T> implements IClient<T> {

    /**
     * Global state of ComapiImpl SDK. Possible values in {@link GlobalState}
     */
    protected final AtomicInteger state;

    /**
     * Configuration to be use for this client instance.
     */
    private final ComapiConfig config;

    /**
     * Logger instance.
     */
    protected Logger log;

    // MANAGERS
    protected final LogManager logMgr;
    protected final DataManager dataMgr;
    protected PushManager pushMgr;

    /**
     * API
     */
    protected InternalService service;

    /**
     * Listeners for app backgrounded/foregrounded
     */
    protected CopyOnWriteArrayList<LifecycleListener> lifecycleListeners;

    /**
     * Adapter to dispatch events to multiple external listeners.
     */
    protected ListenerListAdapter listenerListAdapter;

    /**
     * Recommended constructor.
     *
     * @param config ComapiImpl configuration.
     */
    BaseClient(final ComapiConfig config) {
        state = new AtomicInteger(GlobalState.NOT_INITIALISED);
        this.config = config;
        logMgr = new LogManager();
        dataMgr = new DataManager();
        pushMgr = new PushManager();
        lifecycleListeners = new CopyOnWriteArrayList<>();
    }

    /**
     * Initialise ComapiImpl client instance.
     *
     * @param application Application context.
     * @param instance    Client instance.
     * @param adapter     Observables to callbacks adapter.
     * @return Observable returning client instance.
     */
    <E extends BaseClient> Observable<E> initialise(@NonNull final Application application, @NonNull final E instance, @NonNull final CallbackAdapter adapter) {

        if (state.compareAndSet(GlobalState.NOT_INITIALISED, GlobalState.INITIALISING)) {

            return init(application, adapter)
                    .concatMap((Func1<Boolean, Observable<SessionData>>) this::loadSession)
                    .doOnNext(session -> log.d(session != null ? "Comapi initialised with session profile id : " + session.getProfileId() : "Comapi initialisation with no session."))
                    .doOnError(e -> {
                        if (log != null) {
                            log.f("Error initialising ComapiImpl SDK. " + e.getMessage(), new ComapiException("Error initialising ComapiImpl SDK.", e));
                        }
                    })
                    .flatMap(session -> {
                        if (state.get() == GlobalState.SESSION_ACTIVE && config.isFcmEnabled()) {
                            return instance.service.updatePushToken()
                                    .doOnNext(sessionComapiResultPair -> log.d("Push token updated"))
                                    .doOnError(throwable -> log.f("Error updating push token", throwable))
                                    .map((Func1<Pair<SessionData, ComapiResult<Void>>, Object>) resultPair -> resultPair.first)
                                    .onErrorReturn((Func1<Throwable, SessionData>) throwable -> session);
                        }
                        return Observable.fromCallable(() -> session);
                    })
                    .map(result -> instance);

        } else if (state.get() >= GlobalState.INITIALISED) {
            return Observable.fromCallable(() -> instance);
        } else {
            return Observable.error(new ComapiException("Initialise in progress. Shouldn't be called twice. Ignoring."));
        }
    }

    /**
     * Performs basic initialisation.
     *
     * @param application Application application.
     * @param adapter     Observables to callbacks adapter.
     * @return Observable emitting True if initialisation ended successfully.
     */
    private Observable<Boolean> init(@NonNull final Application application, @NonNull final CallbackAdapter adapter) {

        final Looper mainLooper = Looper.getMainLooper();

        return Observable.create(sub -> {

            try {

                //logging
                final LogConfig logConfig = config.getLogConfig() != null ? config.getLogConfig() : LogConfig.getProductionConfig();
                logMgr.init(application.getApplicationContext(), logConfig.getConsoleLevel().getValue(), logConfig.getFileLevel().getValue(), config.getLogSizeLimit());
                log = new Logger(logMgr, LogConstants.TAG + "_" + BaseComapi.getVersion());
                log.i("Comapi SDK " + BaseComapi.getVersion() + " client " + this.hashCode() + " initialising on " + (Thread.currentThread() == Looper.getMainLooper().getThread() ? "main thread." : "background thread."));

                //public listeners
                listenerListAdapter = new ListenerListAdapter(log);
                listenerListAdapter.addListener(config.getMessagingListener());
                listenerListAdapter.addListener(config.getStateListener());
                listenerListAdapter.addListener(config.getProfileListener());

                //data
                dataMgr.init(application, config.getApiSpaceId(), log);
                dataMgr.getDeviceDAO().setApiSpaceId(config.getApiSpaceId());

                //push
                pushMgr.init(application.getApplicationContext(), new Handler(mainLooper), log, config.getPushTokenProvider(), token -> {
                    log.d("Refreshed push token is " + token);
                    if (!TextUtils.isEmpty(token)) {
                        dataMgr.getDeviceDAO().setPushToken(token);
                    }
                }, config.getPushMessageListener());

                //API baseURIs, proxy
                APIConfig.BaseURIs baseURIs = APIConfig.BaseURIs.build(config.getApiConfig(), config.getApiSpaceId(), log);
                if (baseURIs.getProxy() != null) {
                    log.i("Proxy address has been set for COMAPI initialisation.");
                }

                //services
                service = new InternalService(adapter, dataMgr, pushMgr, config.getApiSpaceId(), application.getPackageName(), log);
                RestApi restApi = service.initialiseRestClient(logConfig.getNetworkLevel().getValue(), baseURIs);
                SessionController sessionController = service.initialiseSessionController(
                        new SessionCreateManager(new AtomicBoolean()),
                        pushMgr,
                        state,
                        config.getAuthenticator(),
                        restApi, new Handler(mainLooper),
                        config.isFcmEnabled(),
                        listenerListAdapter);

                //sockets
                SocketController socketController = service.initialiseSocketClient(sessionController, listenerListAdapter, baseURIs);
                lifecycleListeners.add(socketController.createLifecycleListener());
                initialiseLifecycleObserver(application);

                pushMgr.setService(service);

                sub.onNext(state.compareAndSet(GlobalState.INITIALISING, GlobalState.INITIALISED));
                sub.onCompleted();

            } catch (Exception e) {
                state.compareAndSet(GlobalState.INITIALISING, GlobalState.NOT_INITIALISED);
                sub.onError(e);
            }
        });
    }

    /**
     * Register for application lifecycle callbacks.
     *
     * @param application Application instance.
     */
    private void initialiseLifecycleObserver(Application application) {
        LifeCycleController.registerLifeCycleObserver(application, new LifecycleListener() {
            @Override
            public void onForegrounded(Context context) {
                for (LifecycleListener listener : lifecycleListeners) {
                    listener.onForegrounded(context);
                }
            }

            @Override
            public void onBackgrounded(Context context) {
                for (LifecycleListener listener : lifecycleListeners) {
                    listener.onBackgrounded(context);
                }
            }
        });
    }

    /**
     * Gets the internal state of the SDK. Possible values in {@link GlobalState}.
     *
     * @return State of the ComapiImpl SDK. Compare with values in {@link GlobalState}.
     */
    @Override
    public int getState() {
        return state.get();
    }

    /**
     * Gets the active session data.
     *
     * @return Active session data.
     */
    @Override
    public Session getSession() {
        return state.get() > GlobalState.INITIALISING ? new Session(dataMgr.getSessionDAO().session()) : new Session();
    }

    abstract public T service();

    /**
     * Gets the content of internal log files.
     */
    @Override
    public Observable<String> getLogs() {
        return state.get() > GlobalState.INITIALISING ? logMgr.getLogs() : Observable.fromCallable(() -> null);
    }

    /**
     * Gets the content of internal log files merged into provided file.
     *
     * @param file File to merge internal logs into.
     * @return Observable returning file with merged internal logs.
     */
    public Observable<File> copyLogs(@NonNull File file) {
        return state.get() > GlobalState.INITIALISING ? logMgr.copyLogs(file) : Observable.fromCallable(() -> null);
    }

    @Override
    public void clean(@NonNull Context context) {
        pushMgr.unregisterPushReceiver(context);
    }

    /**
     * Loads local session state.
     *
     * @return Returns local session state.
     */
    private Observable<SessionData> loadSession(final Boolean initialised) {

        if (initialised) {

            final SessionData session = dataMgr.getSessionDAO().session();
            if (session != null) {
                if (session.getExpiresOn() > System.currentTimeMillis()) {
                    state.compareAndSet(GlobalState.INITIALISED, GlobalState.SESSION_ACTIVE);
                } else {
                    state.compareAndSet(GlobalState.INITIALISED, GlobalState.SESSION_OFF);
                    return service.reAuthenticate().onErrorReturn(throwable -> {
                        log.w("Authentication failure during init.");
                        return session;
                    });
                }
            }

            return Observable.create(sub -> {
                sub.onNext(session);
                sub.onCompleted();
            });
        }

        return Observable.just(null);
    }

    /**
     * Gets logger to access internal logs writer.
     *
     * @return Internal logger.
     */
    Logger getLogger() {
        return log;
    }

    /**
     * Adds listener for application lifecycle callbacks.
     *
     * @param listener Listener for application lifecycle callbacks.
     */
    void addLifecycleListener(LifecycleListener listener) {
        lifecycleListeners.add(listener);
    }


    /**
     * Adds listener for messaging socket events.
     *
     * @param listener Adds listener for messaging socket events.
     */
    @Override
    public void addListener(MessagingListener listener) {
        listenerListAdapter.addListener(listener);
    }

    /**
     * Removes listener for messaging socket events.
     *
     * @param listener Listener for messaging socket events.
     */
    @Override
    public void removeListener(MessagingListener listener) {
        listenerListAdapter.removeListener(listener);
    }

    /**
     * Adds listener for profile socket events.
     *
     * @param listener Listener for profile socket events.
     */
    @Override
    public void addListener(ProfileListener listener) {
        if (listener != null) {
            listenerListAdapter.addListener(listener);
        }
    }

    /**
     * Adds listener for profile socket events.
     *
     * @param listener Listener for profile socket events.
     */
    @Override
    public void removeListener(ProfileListener listener) {
        listenerListAdapter.removeListener(listener);
    }

    /**
     * Adds listener for profile socket events.
     *
     * @param listener Listener for profile socket events.
     */
    @Override
    public void addListener(StateListener listener) {
        if (listener != null) {
            listenerListAdapter.addListener(listener);
        }
    }

    /**
     * Adds listener for profile socket events.
     *
     * @param listener Listener for profile socket events.
     */
    @Override
    public void removeListener(StateListener listener) {
        listenerListAdapter.removeListener(listener);
    }
}