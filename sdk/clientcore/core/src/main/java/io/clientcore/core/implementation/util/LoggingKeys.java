// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.util;

/**
 * Constants used as keys in semantic logging. Logging keys unify how core logs HTTP requests, responses or anything
 * else and simplify log analysis.
 * <p>
 * When logging in client libraries, please do the best effort to stay consistent with these keys, but copy the value.
 */
public final class LoggingKeys {
    private LoggingKeys() {

    }

    /**
     * Key representing HTTP method.
     */
    public static final String HTTP_METHOD_KEY = "method";

    /**
     * Key representing try count, the value starts with {@code 0} on the first try
     * and should be an {@code int} number.
     */
    public static final String TRY_COUNT_KEY = "tryCount";

    /**
     * Key representing time from request start to the moment response (headers and response code) were received in milliseconds,
     * the value should be a number.
     * <p>
     * Depending on the implementation and content type, this time may include time to receive the body.
     */
    public static final String TIME_TO_HEADERS_MS = "timeToHeadersMs";

    /**
     * Key representing duration of call in milliseconds, the value should be a number.
     * <p>
     * This time represents the most accurate duration that logging policy can record.
     * <p>
     * If exception was thrown, this time represents time to exception.
     * If response was received and body logging is disabled, it represents time to get the response (headers and status code).
     * If response was received and body logging is enabled, it represents time-to-last-byte (or, if response was closed before
     * body was fully received, time to closure).
     */
    public static final String DURATION_MS_KEY = "durationMs";

    /**
     * Key representing URI request was redirected to.
     */
    public static final String REDIRECT_URI_KEY = "redirectUri";

    /**
     * Key representing request URI.
     */
    public static final String URI_KEY = "uri";

    /**
     * Key representing request body content length.
     */
    public static final String REQUEST_CONTENT_LENGTH_KEY = "requestContentLength";

    /**
     * Key representing response body content length.
     */
    public static final String RESPONSE_CONTENT_LENGTH_KEY = "responseContentLength";

    /**
     * Key representing request body. The value should be populated conditionally
     * if populated at all.
     */
    public static final String BODY_KEY = "body";

    /**
     * Key representing response status code. The value should be a number.
     */
    public static final String STATUS_CODE_KEY = "statusCode";
}
