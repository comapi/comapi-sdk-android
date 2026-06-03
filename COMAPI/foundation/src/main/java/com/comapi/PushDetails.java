package com.comapi;

import androidx.annotation.Nullable;

import org.json.JSONObject;

public class PushDetails {

    @Nullable
    private final String url;
    @Nullable
    private final JSONObject data;
    private final boolean ddOriginated;

    public PushDetails(@Nullable String url, @Nullable JSONObject data) {
        this(url, data, false);
    }

    public PushDetails(@Nullable String url, @Nullable JSONObject data, boolean ddOriginated) {
        this.url = url;
        this.data = data;
        this.ddOriginated = ddOriginated;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @Nullable
    public JSONObject getData() {
        return data;
    }

    public boolean isDdOriginated() {
        return ddOriginated;
    }
}
