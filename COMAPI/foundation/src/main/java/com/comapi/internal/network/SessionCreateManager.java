package com.comapi.internal.network;

import android.support.annotation.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class to manage intermediate communication state.
 */
public class SessionCreateManager {

    final private Object lock = new Object();

    final private AtomicBoolean isCreatingSession;

    private String sessionAuthId;

    /**
     * Recommended constructor.
     *
     * @param isCreatingSession Instance of 'is creating session' flag.
     */
    public SessionCreateManager(@NonNull final AtomicBoolean isCreatingSession) {
        this.isCreatingSession = isCreatingSession;
        this.isCreatingSession.set(false);
    }

    /**
     * Set internal state to 'is creating new session'
     *
     * @return True if 'is creating new session' state was successfully set.
     */
    boolean setStart() {
        return isCreatingSession.compareAndSet(false, true);
    }

    /**
     * Sets the authentication process completed and reset manager state.
     */
    void setStop() {
        synchronized (lock) {
            isCreatingSession.set(false);
            sessionAuthId = null;
            lock.notifyAll();
        }
    }

    /**
     * Sets id of current authentication process.
     *
     * @param sessionAuthId id of current authentication process.
     */
    void setId(String sessionAuthId) {
        synchronized (lock) {
            if (isCreatingSession.get()) {
                this.sessionAuthId = sessionAuthId;
            }
            lock.notifyAll();
        }
    }

    /**
     * Gets auth id for creating session process.
     *
     * @return Auth id for creating session process.
     */
    String getSessionAuthId() {
        return sessionAuthId;
    }

    /**
     * check if SDK is creating new session.
     *
     * @return True if SDK is currently creating new session.
     */
    boolean getIsCreatingSession() {
        return isCreatingSession.get();
    }
}
