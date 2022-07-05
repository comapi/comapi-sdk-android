package com.comapi;

import androidx.annotation.Nullable;

import org.json.JSONObject;

public class PushDetails {

    @Nullable
    private final String url;
    @Nullable
    private final JSONObject data;

    public PushDetails(@Nullable String url, @Nullable JSONObject data) {
        this.url = url;
        this.data = data;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @Nullable
    public JSONObject getData() {
        return data;
    }
}
