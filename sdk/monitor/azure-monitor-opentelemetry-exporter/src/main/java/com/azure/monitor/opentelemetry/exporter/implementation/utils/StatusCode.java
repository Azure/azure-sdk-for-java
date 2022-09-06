// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

public enum StatusCode {
    TEMPORARY_REDIRECT(307),
    PERMANENTY_REDIRECT(308),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    REQUEST_TIMEOUT(408),
    TOO_MANY_REQUESTS(429),
    INTERNAL_SERVER_ERROR(500),
    BAD_GATEWAY(502),
    SERVICE_UNAVAILABLE(503),
    GATEWAY_TIMEOUT(504);

    private final int code;

    StatusCode(int code) {
        this.code = code;
    }

    public static boolean isRetryable(int statusCode) {
        return statusCode == UNAUTHORIZED.code
            || statusCode == FORBIDDEN.code
            || statusCode == REQUEST_TIMEOUT.code
            || statusCode == TOO_MANY_REQUESTS.code
            || statusCode == INTERNAL_SERVER_ERROR.code
            || statusCode == BAD_GATEWAY.code
            || statusCode == SERVICE_UNAVAILABLE.code
            || statusCode == GATEWAY_TIMEOUT.code;
    }

    public static boolean isRedirect(int statusCode) {
        return statusCode == 307 || statusCode == 308;
    }
}
