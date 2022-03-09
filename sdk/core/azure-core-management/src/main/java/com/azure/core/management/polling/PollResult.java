// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.polling;

import com.azure.core.http.HttpHeaders;

import java.util.Objects;

/**
 * Type representing result (succeeded or failed result) of a long-running-poll operation.
 * @param <T> the type of the value
 */
public final class PollResult<T> {
    private final T value;
    private final Error error;

    /**
     * Creates PollResult.
     *
     * @param value the service returned value for a succeeded poll operation
     */
    public PollResult(T value) {
        this.value = value;
        this.error = null;
    }

    /**
     * Creates PollResult.
     *
     * @param error describes the error that poll-operation received from the service
     */
    public PollResult(Error error) {
        this.error = error;
        this.value = null;
    }

    /**
     * Get the service returned value when poll-operation succeeded.
     *
     * @return the value
     */
    public T getValue() {
        return this.value;
    }

    /**
     * Get the error description when poll-operation receives error from service.
     *
     * @return the error
     */
    public Error getError() {
        return this.error;
    }

    /**
     * Type represents the error that poll-operation received from the service.
     */
    public static class Error {
        private final String message;
        private final int responseStatusCode;
        private final String responseBody;
        private final HttpHeaders responseHeaders;

        /**
         * Creates Error.
         *
         * @param message the error message
         * @param responseStatusCode the http status code associated with the error
         * @param responseHeaders the http response headers associated with the error
         * @param responseBody the http response body associated with the error
         */
        public Error(String message, int responseStatusCode, HttpHeaders responseHeaders, String responseBody) {
            this.message = Objects.requireNonNull(message, "'message' cannot be null.");
            this.responseStatusCode = responseStatusCode;
            this.responseBody = responseBody;
            this.responseHeaders = responseHeaders;
        }

        /**
         * @return the error message
         */
        public String getMessage() {
            return this.message;
        }

        /**
         * @return the response status code associated with the error
         */
        public int getResponseStatusCode() {
            return this.responseStatusCode;
        }

        /**
         * @return the response body associated with the error
         */
        public String getResponseBody() {
            return this.responseBody;
        }

        /**
         * @return the response headers associated with the error
         */
        public HttpHeaders getResponseHeaders() {
            return responseHeaders;
        }
    }
}
