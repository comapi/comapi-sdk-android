package com.comapi.internal.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Data Access Object for {@link SessionData}.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SessionDAO extends BaseDAO {

    private static final String fileNamePrefix = "profile.";

    private static final String KEY_PROFILE_ID = "pId";

    private static final String KEY_SESSION_ID = "sId";

    private static final String KEY_ACCESS_TOKEN = "sT";

    private static final String KEY_EXPIRES_ON = "exp";

    private static final Object sharedLock = new Object();

    /**
     * Recommended constructor.
     *
     * @param context Application context.
     */
    SessionDAO(final Context context, final String suffix) {
        super(context, fileNamePrefix+suffix);
    }

    /**
     * Loads active session details from internal storage.
     *
     * @return Loaded session details.
     */
    private SessionData loadSession() {

        synchronized (sharedLock) {
            SharedPreferences sharedPreferences = getSharedPreferences();
            String id = sharedPreferences.getString(KEY_PROFILE_ID, null);
            if (!TextUtils.isEmpty(id)) {
                sharedLock.notifyAll();
                return new SessionData()
                        .setProfileId(id)
                        .setSessionId(sharedPreferences.getString(KEY_SESSION_ID, null))
                        .setAccessToken(sharedPreferences.getString(KEY_ACCESS_TOKEN, null))
                        .setExpiresOn(sharedPreferences.getLong(KEY_EXPIRES_ON, 0));
            }
            sharedLock.notifyAll();
        }

        return null;
    }

    /**
     * Gets currently active session.
     *
     * @return Currently active session.
     */
    public SessionData session() {
        return loadSession();
    }

    /**
     * Deletes currently saved session.
     *
     * @return SessionId for the deleted session or null if no session was active.
     */
    public String clearSession() {

        synchronized (sharedLock) {
            SessionData session = loadSession();
            String id = session != null ? session.getSessionId() : null;
            clearAll();
            sharedLock.notifyAll();
            return id;
        }
    }

    /**
     * Creates session if no session is active.
     *
     * @return True if session was created. If false there is a opened session already. End this session first.
     */
    public boolean startSession() {

        synchronized (sharedLock) {
            SessionData session = loadSession();
            if (isSessionActive(session)) {
                sharedLock.notifyAll();
                return false;
            } else {
                clearAll();
            }
            sharedLock.notifyAll();
        }

        return true;
    }

    private boolean isSessionActive(SessionData session) {
        if (session != null) {
            if (TextUtils.isEmpty(session.getProfileId()) || TextUtils.isEmpty(session.getSessionId()) || TextUtils.isEmpty(session.getAccessToken()) || session.getExpiresOn() < System.currentTimeMillis()) {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    /**
     * Updates session details obtained frm the services.
     *
     * @return True if session was updated. If false the update is for a different user or session is not started.
     */
    public boolean updateSessionDetails(final SessionData session) {

        synchronized (sharedLock) {
            if (session != null) {
                SharedPreferences.Editor editor = getSharedPreferences().edit();
                editor.putString(KEY_PROFILE_ID, session.getProfileId());
                editor.putString(KEY_SESSION_ID, session.getSessionId());
                editor.putString(KEY_ACCESS_TOKEN, session.getAccessToken());
                editor.putLong(KEY_EXPIRES_ON, session.getExpiresOn());
                boolean isUpdated = editor.commit();
                sharedLock.notifyAll();
                return isUpdated;
            }
            sharedLock.notifyAll();
        }

        return false;
    }
}
