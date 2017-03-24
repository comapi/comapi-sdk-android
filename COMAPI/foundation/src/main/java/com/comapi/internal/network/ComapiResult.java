package com.comapi.internal.network;

import java.io.IOException;

import retrofit2.Response;

/**
 * Comapi service response result.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ComapiResult<T> {

    private T result;

    private final String eTag;

    private final boolean isSuccessful;

    private final int code;

    private String errorBody;

    private final String message;

    /**
     * Recommended constructor. Translates Retrofit to Comapi service response.
     *
     * @param response Retrofit service response.
     */
    ComapiResult(Response<T> response) {
        result = response.body();
        eTag = response.headers().get("ETag");
        isSuccessful = response.isSuccessful();
        code = response.code();
        message = response.message();
        try {
            if (response.errorBody() != null) {
                errorBody = response.errorBody().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Copy constructor.
     *
     * @param result Comapi result to copy from.
     */
    @SuppressWarnings("unchecked")
    ComapiResult(ComapiResult<T> result) {
        eTag = result.getETag();
        isSuccessful = result.isSuccessful();
        code = result.getCode();
        message = result.getMessage();
        errorBody = result.errorBody;
        this.result = result.getResult();
    }

    /**
     * Copy constructor replacing result object with new one.
     *
     * @param result            Comapi result with values to copy to a new instance.
     * @param resultReplacement Result object that should replace the one from old Comapi result.
     */
    @SuppressWarnings("unchecked")
    public ComapiResult(ComapiResult result, T resultReplacement) {
        this.result = resultReplacement;
        eTag = result.getETag();
        isSuccessful = result.isSuccessful();
        code = result.getCode();
        message = result.getMessage();
        errorBody = result.errorBody;
    }

    /**
     * The deserialized response body of a {@linkplain #isSuccessful() successful} response.
     */
    public T getResult() {
        return result;
    }

    /**
     * Returns true if {@link #getCode()} ()} is in the range [200..300).
     */
    public boolean isSuccessful() {
        return isSuccessful;
    }

    /**
     * HTTP status code.
     */
    public int getCode() {
        return code;
    }

    /**
     * ETag describing version of the data.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * HTTP status message or null if unknown.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets service call error details.
     *
     * @return Service call error details.
     */
    public String getErrorBody() {
        return errorBody;
    }
}
