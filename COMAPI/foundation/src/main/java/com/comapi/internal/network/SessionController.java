package com.comapi.internal.network;

import android.app.Application;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import com.comapi.ComapiAuthenticator;
import com.comapi.GlobalState;
import com.comapi.RxComapi;
import com.comapi.Session;
import com.comapi.internal.ComapiException;
import com.comapi.internal.ISessionListener;
import com.comapi.internal.data.DataManager;
import com.comapi.internal.data.SessionData;
import com.comapi.internal.helpers.DateHelper;
import com.comapi.internal.helpers.DeviceHelper;
import com.comapi.internal.log.Logger;
import com.comapi.internal.network.api.RestApi;
import com.comapi.internal.network.model.session.PushConfig;
import com.comapi.internal.network.model.session.SessionCreateRequest;
import com.comapi.internal.network.model.session.SessionCreateResponse;
import com.comapi.internal.network.sockets.SocketController;
import com.comapi.internal.push.PushManager;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Response;
import rx.Observable;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Controls authentication of the session.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SessionController extends ApiWrapper {

    /**
     * Minimal time between automatic SDK re-authentications.
     */
    private static final long MINIMAL_REAUTHENTICATION_SCHEDULE_SEC = 60;

    /**
     * Global state of Comapi SDK. Possible values in {@link GlobalState}
     */
    private final AtomicInteger state;

    //Managers
    private final DataManager dataMgr;
    private final PushManager pushMgr;
    private final SessionCreateManager sessionCreateManager;

    /**
     * Authenticator to set a response for authentication challenges.
     */
    private final ComapiAuthenticator auth;

    /**
     * Access to REST service.
     */
    private final RestApi service;

    /**
     * Api Space in which SDK operates.
     */
    private final String apiSpaceId;

    /**
     * Application package name.
     */
    private final String packageName;

    /**
     * Logger instance.
     */
    private final Logger log;

    /**
     * Controls socket connections.
     */
    private SocketController socketController;

    private Handler handler;

    /**
     * Service calls queue used when SDK is re-authenticating.
     */
    private final ServiceQueue.TaskQueue taskQueue;

    /**
     * Listener for new sessions.
     */
    private final ISessionListener stateListener;

    /**
     * Recommended constructor.
     *
     * @param application          Application instance.
     * @param sessionCreateManager Manager of session create intermediate states.
     * @param pushMgr              Manager for push messaging.
     * @param state                Instance of the global SDK state.
     * @param dataMgr              Manager of internal data storage.
     * @param auth                 SDK authenticator.
     * @param service              Service APIs.
     * @param packageName          App package name.
     * @param handler              Main thread handler.
     * @param log                  Internal logger.
     * @param taskQueue            Service calls queue used when SDK is re-authenticating.
     * @param stateListener        Listener for new sessions.
     */
    SessionController(@NonNull Application application,
                      @NonNull final SessionCreateManager sessionCreateManager,
                      @NonNull PushManager pushMgr,
                      @NonNull final AtomicInteger state,
                      @NonNull final DataManager dataMgr,
                      @NonNull final ComapiAuthenticator auth,
                      @NonNull RestApi service,
                      @NonNull final String packageName,
                      @NonNull Handler handler,
                      @NonNull final Logger log,
                      @NonNull ServiceQueue.TaskQueue taskQueue,
                      @Nullable final ISessionListener stateListener) {

        super(application);
        this.state = state;
        this.dataMgr = dataMgr;
        this.pushMgr = pushMgr;
        this.auth = auth;
        this.service = service;
        this.log = log;
        this.packageName = packageName;
        this.apiSpaceId = dataMgr.getDeviceDAO().device().getApiSpaceId();
        this.sessionCreateManager = sessionCreateManager;
        this.handler = handler;
        this.stateListener = stateListener;
        this.taskQueue = taskQueue;

        SessionData session = dataMgr.getSessionDAO().session();
        if (session != null) {
            long expOn = session.getExpiresOn();
            if (expOn > System.currentTimeMillis()) {
                scheduleNextAuthentication(expOn);
                if (this.stateListener != null) {
                    this.stateListener.onSessionStart(new Session(session));
                }
            }
        }
    }

    /**
     * Create and start new Comapi session.
     *
     * @return True if session was started.
     */
    Observable<SessionData> startSession() {

        if ((state.get() >= GlobalState.INITIALISED && dataMgr.getSessionDAO().startSession())) {

            state.set(GlobalState.SESSION_STARTING);

            return doStartSessionServiceCalls(dataMgr.getDeviceDAO().device().getDeviceId())
                    .map(sessionCreateResponse -> new SessionData()
                            .setProfileId(sessionCreateResponse.getSession().getProfileId())
                            .setSessionId(sessionCreateResponse.getSession().getSessionId())
                            .setAccessToken(sessionCreateResponse.getToken())
                            .setExpiresOn(DateHelper.getUTCMilliseconds(sessionCreateResponse.getSession().getExpiresOn()))
                    )
                    .doOnNext(session -> {
                        state.compareAndSet(GlobalState.SESSION_STARTING, GlobalState.SESSION_ACTIVE);
                        dataMgr.getSessionDAO().updateSessionDetails(session);
                        socketController.connectSocket();
                        scheduleNextAuthentication(session.getExpiresOn());
                        taskQueue.executePending();
                        if (stateListener != null) {
                            stateListener.onSessionStart(new Session(session));
                        }
                    })
                    .doOnError(throwable -> state.compareAndSet(GlobalState.SESSION_STARTING, GlobalState.SESSION_OFF))
                    .concatMap(session -> updatePushToken(session)
                            .doOnNext(pushUpdateResult -> {
                                if (!pushUpdateResult.second.isSuccessful()) {
                                    log.e("Failed to update push token on the server. " + pushUpdateResult.second.message());
                                } else {
                                    log.i("Push token updated on the server.");
                                }
                            })
                            .doOnError(throwable -> log.f("Failed to update push token on the server.", throwable))
                            .map(sessionResultPair -> sessionResultPair.first));
        }

        return Observable.error(new ComapiException("Session already started or SDK not initialised. Stop the active session first. [" + state.get() + "]"));
    }

    void scheduleNextAuthentication(long nextRestartTime) {

        final long delay = (nextRestartTime - System.currentTimeMillis());
        log.d("Scheduling next authentication to " + DateHelper.getUTC(nextRestartTime) +
                " Device UTC time is " + DateHelper.getUTC(System.currentTimeMillis()) +
                " Authenticating automatically in " + TimeUnit.MILLISECONDS.toMinutes(delay) + " minutes.");
        handler.postDelayed(() -> reAuthenticate().subscribe(session -> {
            log.d("Successfully authenticated according to schedule");
        }, throwable -> {
            log.f("Failed to authenticate according to schedule.", throwable);
        }), delay);
    }

    /**
     * Observable for a task to start a new session.
     *
     * @param deviceId  Device Id.
     * @return Observable for a task to start a new session.
     */
    private Observable<SessionCreateResponse> doStartSessionServiceCalls(@NonNull final String deviceId) {

        if (sessionCreateManager.setStart()) {

            return service.startSession(apiSpaceId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .map(startResponse -> {
                        sessionCreateManager.setId(startResponse.getAuthenticationId());
                        return new ChallengeOptions(startResponse.getNonce());
                    })
                    .takeWhile(challengeOptions -> challengeOptions != null)
                    .concatMap(new Func1<ChallengeOptions, Observable<String>>() {
                        @Override
                        public Observable<String> call(ChallengeOptions challengeOptions) {
                            return getAuthToken(challengeOptions)
                                    .timeout(auth.timeoutSeconds(), TimeUnit.SECONDS)
                                    .retryWhen(errors -> errors.zipWith(Observable.range(1, 3), (n, i) -> {
                                        if (i >= 3) {
                                            //noinspection ThrowableResultOfMethodCallIgnored
                                            Exceptions.propagate(n);
                                        }
                                        return i;
                                    }))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(Schedulers.io());
                        }
                    })
                    .doOnNext(token -> log.d("Received 3rd party auth token: " + token))
                    .map(token -> getSessionCreateRequest(token, sessionCreateManager.getSessionAuthId(), deviceId))
                    .concatMap(sessionCreateRequest -> service.createSession(apiSpaceId, sessionCreateRequest)
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io())
                            .doOnNext(sessionCreateResponse -> log.i("Starting session successful: " + sessionCreateResponse.toString()))
                    ).doOnNext(response -> {
                        log.i("Starting session has been completed successfully.");
                        sessionCreateManager.setStop();
                    })
                    .doOnError((e) -> {
                        log.f("Starting session has been completed with error. ", e);
                        sessionCreateManager.setStop();
                    });
        } else {
            return Observable.error(new ComapiException("Session start in progress."));
        }
    }

    /**
     * Gets request for creating new session.
     *
     * @param token         Authentication token.
     * @param sessionAuthId Authentication id for the currently running task.
     * @param deviceId      Device ID.
     * @return Request for creating new session.
     */
    private SessionCreateRequest getSessionCreateRequest(String token, String sessionAuthId, String deviceId) {
        return new SessionCreateRequest(sessionAuthId, token)
                .setDeviceId(deviceId)
                .setPlatform(DeviceHelper.PLATFORM)
                .setPlatformVersion(Build.VERSION.RELEASE)
                .setSdkType(DeviceHelper.SDK_TYPE)
                .setSdkVersion(RxComapi.getVersion());
    }

    /**
     * Ends currently active session.
     *
     * @return Observable to end current session.
     */
    Observable<Response<Void>> endSession() {
        final int oldState = state.getAndSet(GlobalState.INITIALISING);
        handler.removeCallbacksAndMessages(null);
        SessionData session = dataMgr.getSessionDAO().session();
        return service.endSession(AuthManager.addAuthPrefix(session.getAccessToken()), apiSpaceId, session.getSessionId())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(voidResponse -> {
                    dataMgr.getSessionDAO().clearSession();
                    socketController.disconnectSocket();
                    state.compareAndSet(GlobalState.INITIALISING, GlobalState.INITIALISED);
                })
                .doOnError(voidResponse -> state.set(oldState));
    }

    /**
     * Gets an observable task for push token registration. Will emit FCM push token for provided senderId.
     *
     * @return Observable task for push token registration.
     */
    private Observable<Pair<SessionData, Response<Void>>> updatePushToken(SessionData session) {

        return Observable.create((Observable.OnSubscribe<String>) sub -> {
            String token = dataMgr.getDeviceDAO().device().getPushToken();
            if (TextUtils.isEmpty(token)) {
                token = pushMgr.getPushToken();
                if (!TextUtils.isEmpty(token)) {
                    dataMgr.getDeviceDAO().setPushToken(token);
                }
            }
            sub.onNext(token);
            sub.onCompleted();
        }).concatMap(token -> service.updatePushToken(AuthManager.addAuthPrefix(session.getAccessToken()), apiSpaceId, session.getSessionId(), new PushConfig(packageName, token)))
                .map(result -> new Pair<>(session, result));
    }

    /**
     * Re-authenticate when token expired.
     *
     * @return Observable returning new Comapi session.
     */
    protected Observable<SessionData> reAuthenticate() {

        if (state.get() < GlobalState.SESSION_OFF) {
            return Observable.error(new ComapiException("Session not started yet."));
        }

        socketController.disconnectSocket();

        return wrapObservable(doStartSessionServiceCalls(dataMgr.getDeviceDAO().device().getDeviceId()))
                .map(sessionCreateResponse -> new SessionData()
                        .setProfileId(sessionCreateResponse.getSession().getProfileId())
                        .setSessionId(sessionCreateResponse.getSession().getSessionId())
                        .setAccessToken(sessionCreateResponse.getToken())
                        .setExpiresOn(DateHelper.getUTCMilliseconds(sessionCreateResponse.getSession().getExpiresOn()))
                )
                .doOnNext(session -> {
                    dataMgr.getSessionDAO().updateSessionDetails(session);
                    state.compareAndSet(GlobalState.SESSION_STARTING, GlobalState.SESSION_ACTIVE);
                    socketController.connectSocket();
                    scheduleNextAuthentication(Math.max(session.getExpiresOn(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(MINIMAL_REAUTHENTICATION_SCHEDULE_SEC)));
                    taskQueue.executePending();
                    if (stateListener != null) {
                        stateListener.onSessionStart(new Session(session));
                    }
                })
                .doOnError(throwable -> {
                    state.compareAndSet(GlobalState.SESSION_STARTING, GlobalState.SESSION_OFF);
                    sessionCreateManager.setStop();
                })
                .concatMap(newSession ->
                        Observable.zip(
                                Observable.just(newSession),
                                service.updatePushToken(AuthManager.addAuthPrefix(newSession.getAccessToken()), apiSpaceId, newSession.getSessionId(), new PushConfig(packageName, dataMgr.getDeviceDAO().device().getPushToken())),
                                (session, voidResult) -> session));
    }

    /**
     * Challenge authentication. Integrator should provide auth token from authentication services provider and pass it to AuthClient instance.
     *
     * @param authClient       Authentication client instance that implements reaction to a given auth token.
     * @param challengeOptions Encapsulates challenge details - nonce, and expected user id for the token.
     */
    private void challengeAuthentication(final AuthClient authClient, final ChallengeOptions challengeOptions) {
        try {
            auth.onAuthenticationChallenge(authClient, challengeOptions);
        } catch (Exception e) {
            //noinspection ThrowableResultOfMethodCallIgnored
            Exceptions.propagate(new ComapiException("Exception thrown when obtaining auth token from 3rd party.", e));
        }
    }

    /**
     * Observable for a task to obtain an authentication token.
     *
     * @param challengeOptions Encapsulates challenge details - nonce, and expected user id for the token.
     * @return Observable for a task to obtain an authentication token.
     */
    private Observable<String> getAuthToken(final ChallengeOptions challengeOptions) {

        return Observable.create(sub -> {
            try {
                challengeAuthentication(token -> {
                    if (!TextUtils.isEmpty(token)) {
                        sub.onNext(token);
                        sub.onCompleted();
                    } else {
                        sub.onError(new ComapiException("Null authentication token received from 3rd party."));
                    }
                }, challengeOptions);
            } catch (Throwable e) {
                sub.onError(e);
            }
        });
    }

    /**
     * Checks if SDK is currently authenticating a new session.
     *
     * @return True if SDK is currently authenticating a new session.
     */
    boolean isCreatingSession() {
        return sessionCreateManager.getIsCreatingSession();
    }

    void setSocketController(SocketController socketController) {
        this.socketController = socketController;
    }
}
