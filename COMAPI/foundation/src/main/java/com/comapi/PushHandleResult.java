package com.comapi;

import androidx.annotation.Nullable;

import org.json.JSONObject;

public class PushHandleResult extends PushDetails {

    private final boolean isClickRecorded;
    private final boolean isDeepLinkCalled;

    public PushHandleResult(@Nullable String url, @Nullable JSONObject data, boolean isClickRecorded, boolean isDeepLinkCalled) {
        super(url, data);
        this.isClickRecorded = isClickRecorded;
        this.isDeepLinkCalled = isDeepLinkCalled;
    }

    public boolean isClickRecorded() {
        return isClickRecorded;
    }

    public boolean isDeepLinkCalled() {
        return isDeepLinkCalled;
    }
}
