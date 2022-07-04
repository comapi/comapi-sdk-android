package com.comapi;

import androidx.annotation.Nullable;

import org.json.JSONObject;

public class PushDetails {

    @Nullable
    private final String url;
    private final JSONObject data;
    private final boolean isClickRecorded;
    private final boolean isDeepLinkCalled;
    @Nullable
    private final Exception e;

    public PushDetails(@Nullable String url, @Nullable JSONObject data, boolean isClickRecorded, boolean isDeepLinkCalled, @Nullable Exception e) {
        this.url = url;
        this.data = data;
        this.isClickRecorded = isClickRecorded;
        this.isDeepLinkCalled = isDeepLinkCalled;
        this.e = e;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @Nullable
    public JSONObject getData() {
        return data;
    }

    public boolean isClickRecorded() {
        return isClickRecorded;
    }

    public boolean isDeepLinkCalled() {
        return isDeepLinkCalled;
    }

    @Nullable
    public Exception getException() {
        return e;
    }

    public String urlToHandle() {
        return  !isDeepLinkCalled && url != null && !url.isEmpty() ? url : null;
    }
}
