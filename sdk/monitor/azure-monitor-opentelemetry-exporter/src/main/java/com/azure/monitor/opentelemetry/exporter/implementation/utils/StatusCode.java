/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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
