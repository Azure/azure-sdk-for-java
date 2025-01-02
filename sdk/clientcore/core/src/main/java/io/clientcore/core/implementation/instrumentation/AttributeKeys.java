// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation;

/**
 * Constants used as keys in semantic logging, tracing, metrics following
 * <a href="https://github.com/open-telemetry/semantic-conventions">OpenTelemetry semantic conventions</a>.
 * <p>
 * These keys unify how core logs HTTP requests, responses or anything
 * else and simplify telemetry analysis.
 * <p>
 * When reporting in client libraries, please do the best effort to stay consistent with these keys, but copy the value.
 */
public final class AttributeKeys {
    private AttributeKeys() {

    }

    /**
     * Key representing HTTP method.
     */
    public static final String HTTP_REQUEST_METHOD_KEY = "http.request.method";

    /**
     * Key representing try count, the value starts with {@code 0} on the first try
     * and should be an {@code int} number.
     */
    public static final String HTTP_REQUEST_RESEND_COUNT_KEY = "http.request.resend_count";

    /**
     * Key representing duration of call in milliseconds, the value should be a number.
     */
    public static final String HTTP_REQUEST_TIME_TO_HEADERS_KEY = "http.request.time_to_headers";

    /**
     * Key representing duration of call in milliseconds, the value should be a number.
     */
    public static final String HTTP_REQUEST_DURATION_KEY = "http.request.duration";

    /**
     * Key representing request URI.
     */
    public static final String URL_FULL_KEY = "url.full";

    /**
     * Key representing request body content length.
     */
    public static final String HTTP_REQUEST_BODY_SIZE_KEY = "http.request.body.size";

    /**
     * Key representing request body content length.
     */
    public static final String HTTP_RESPONSE_BODY_SIZE_KEY = "http.response.body.size";

    /**
     * Key representing request body. The value should be populated conditionally
     * if populated at all.
     */
    public static final String HTTP_REQUEST_BODY_KEY = "http.request.body";

    /**
     * Key representing response body. The value should be populated conditionally
     * if populated at all.
     */
    public static final String HTTP_RESPONSE_BODY_KEY = "http.request.body";

    /**
     * Key representing response status code. The value should be a number.
     */
    public static final String HTTP_RESPONSE_STATUS_CODE_KEY = "http.response.status_code";
}
