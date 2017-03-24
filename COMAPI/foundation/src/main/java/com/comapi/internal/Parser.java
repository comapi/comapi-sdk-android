package com.comapi.internal;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Json parser wrapper.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Parser {

    private final Gson gson;

    /**
     * Recommended constructor.
     */
    public Parser() {
        gson = new Gson();
    }

    /**
     * Parse json to POJO.
     *
     * @param text  Json string.
     * @param clazz POJO Class.
     * @return POJO object.
     */
    public <T> T parse(String text, Class<T> clazz) {
        return gson.fromJson(text, clazz);
    }

    /**
     * Parse JsonObject to POJO.
     *
     * @param obj   JsonObject to parse.
     * @param clazz POJO Class.
     * @return POJO object.
     */
    public <T> T parse(JsonObject obj, Class<T> clazz) {
        return gson.fromJson(obj, clazz);
    }

    public String toJson(Object obj) {
        return gson.toJson(obj);
    }
}
