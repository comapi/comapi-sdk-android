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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;

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
import com.comapi.internal.push.PushDataKeys;
import com.comapi.internal.push.PushManager;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

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

    protected Observable<PushHandleResult> handlePushNotification(Context activityContext, Intent i, boolean startActivity) {
        if (i.hasExtra(PushDataKeys.KEY_PUSH_DEEP_LINK)) {
            JSONObject deepLinkData;
            try {
                deepLinkData = new JSONObject((String) i.getSerializableExtra(PushDataKeys.KEY_PUSH_DEEP_LINK));
                if (deepLinkData.has(PushDataKeys.KEY_PUSH_URL)) {
                    String url = deepLinkData.getString(PushDataKeys.KEY_PUSH_URL);
                    Observable<Boolean> tracking;
                    if (deepLinkData.has(PushDataKeys.KEY_PUSH_TRACKING_URL)) {
                        String trackingUrl = deepLinkData.getString(PushDataKeys.KEY_PUSH_TRACKING_URL);
                        tracking = service.sendClickData(trackingUrl);
                    } else {
                        tracking = Observable.fromCallable(() -> false);
                    }
                    return Observable.fromCallable(() -> {
                        if (startActivity) {
                            Intent intent = new Intent();
                            intent.setData(Uri.parse(url));
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.addCategory(Intent.CATEGORY_BROWSABLE);
                            try {
                                activityContext.startActivity(intent);
                                return true;
                            } catch (ActivityNotFoundException e) {
                                return false;
                            }
                        } else {
                            return false;
                        }

                    }).flatMap(isStartActivitySuccessful -> tracking.map(isTrackingSuccessful -> new PushHandleResult(url, null, isTrackingSuccessful, isStartActivitySuccessful)));
                }
            } catch (Exception e) {
                log.f(e.getMessage(), e);
                return Observable.error(e);
            }
        } else if (i.hasExtra(PushDataKeys.KEY_PUSH_DATA)) {
            try {
                JSONObject data = new JSONObject((String) i.getSerializableExtra(PushDataKeys.KEY_PUSH_DATA));
                return  Observable.fromCallable(() -> new PushHandleResult(null, data, false, false));
            } catch (Exception e) {
                return  Observable.error(e);
            }
        }

        return  Observable.fromCallable(() -> new PushHandleResult(null, null, false, false));
    }

    static protected Observable<PushDetails> parsePush(RemoteMessage message) {
        RemoteMessage.Notification n = message.getNotification();
        if (message.getData().containsKey(PushDataKeys.KEY_PUSH_DEEP_LINK)) {
            String deepLinkDataJson = message.getData().get(PushDataKeys.KEY_PUSH_DEEP_LINK);
            try {
                assert deepLinkDataJson != null;
                JSONObject deepLinkData = new JSONObject(deepLinkDataJson);
                if (deepLinkData.has(PushDataKeys.KEY_PUSH_URL)) {
                    String url = deepLinkData.getString(PushDataKeys.KEY_PUSH_URL);
                    return  Observable.fromCallable(() -> new PushDetails(url, null));
                }
            } catch (Exception e) {
                return Observable.error(e);
            }
        } else if (message.getData().containsKey(PushDataKeys.KEY_PUSH_DATA)) {
            String dataJson = message.getData().get(PushDataKeys.KEY_PUSH_DATA);
            try {
                assert dataJson != null;
                JSONObject data = new JSONObject(dataJson);
                return  Observable.fromCallable(() -> new PushDetails(null, data));
            } catch (Exception e) {
                return Observable.error(e);
            }
        }
        return  Observable.fromCallable(() -> new PushDetails(null, null));
    }
}