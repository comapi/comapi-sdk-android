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

package com.comapi.internal.network;

import androidx.annotation.Nullable;

import com.comapi.internal.Parser;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Response;

/**
 * Comapi service response result.
 *
 * @author Marcin Swierczek
 * @since 1.0.0
 */
public class ComapiResult<T> {

    private T result;

    private final String eTag;

    private final boolean isSuccessful;

    private final int code;

    private String errorBody;

    private final String message;

    /**
     * Constructor for testing purpose.
     *
     * @param result       Call result body.
     * @param isSuccessful True if the call was successful.
     * @param eTag         ETag for tracking remote data version.
     * @param code         Code of the service response.
     * @param message      Message in the response.
     * @param errorBody    Error details.
     */
    protected ComapiResult(T result, boolean isSuccessful, String eTag, int code, String message, String errorBody) {
        this.result = result;
        this.eTag = eTag;
        this.isSuccessful = isSuccessful;
        this.code = code;
        this.message = message;
        this.errorBody = errorBody;
    }

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
        } catch (Exception e) {
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

    /**
     * Get API call validation failures details.
     *
     * @return List of validation failures returned from services. Can be null.
     */
    public @Nullable
    List<ComapiValidationFailure> getValidationFailures() {
        if (errorBody != null && !errorBody.isEmpty()) {
            ComapiValidationFailures failures = null;
            try {
                failures = new Parser().parse(errorBody, ComapiValidationFailures.class);
            } catch (Exception e) {
                return null;
            }
            return failures.validationFailures;
        }

        return null;
    }

    class ComapiValidationFailures {

        @SerializedName("validationFailures")
        private List<ComapiValidationFailure> validationFailures;

        public List<ComapiValidationFailure> getValidationFailures() {
            return validationFailures;
        }
    }

    public class ComapiValidationFailure {

        @SerializedName("paramName")
        private String paramName;

        @SerializedName("message")
        private String message;

        /**
         * Name of the JSON parameter that failed the check on the server side.
         *
         * @return Name of the JSON parameter.
         */
        public String getParamName() {
            return paramName;
        }

        /**
         * Cause of API call validation failure returned from the services.
         *
         * @return Cause of API call validation failure.
         */
        public String getMessage() {
            return message;
        }
    }
}