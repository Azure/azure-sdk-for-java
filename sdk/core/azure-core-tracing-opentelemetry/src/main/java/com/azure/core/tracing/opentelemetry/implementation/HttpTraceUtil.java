// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry.implementation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

public final class HttpTraceUtil {
    private static final String STATUS_100 = "Continue";
    private static final String STATUS_101 = "Switching Protocols";
    private static final String STATUS_400 = "Bad Request";
    private static final String STATUS_401 = "Unauthorized";
    private static final String STATUS_402 = "Payment Required";
    private static final String STATUS_403 = "Forbidden";
    private static final String STATUS_404 = "Not Found";
    private static final String STATUS_405 = "Method Not Allowed";
    private static final String STATUS_406 = "Not Acceptable";
    private static final String STATUS_407 = "Proxy Authentication Required";
    private static final String STATUS_408 = "Request Time-out";
    private static final String STATUS_409 = "Conflict";
    private static final String STATUS_410 = "Gone";
    private static final String STATUS_411 = "Length Required";
    private static final String STATUS_412 = "Precondition Failed";
    private static final String STATUS_413 = "Request Entity Too Large";
    private static final String STATUS_414 = "Request-URI Too Large";
    private static final String STATUS_415 = "Unsupported Media Type";
    private static final String STATUS_416 = "Requested range not satisfiable";
    private static final String STATUS_417 = "Expectation Failed";
    private static final String STATUS_429 = "Too Many Requests";
    private static final String STATUS_500 = "Internal Server Error";
    private static final String STATUS_501 = "Not Implemented";
    private static final String STATUS_502 = "Bad Gateway";
    private static final String STATUS_503 = "Service Unavailable";
    private static final String STATUS_504 = "Gateway Timeout";
    private static final String STATUS_505 = "HTTP Version not supported";

    private HttpTraceUtil() { }

    /**
     * Parse OpenTelemetry Status from HTTP response status code.
     *
     * @param span the span to set the status on.
     * @param statusCode the HTTP response status code. {@code 0} means invalid response.
     * @param throwable the {@link Throwable} to propagate to the span.
     * @return the corresponding OpenTelemetry {@code Status}.
     */
    public static Span setSpanStatus(Span span, int statusCode, Throwable throwable) {
        String message = null;
        StatusCode openTelemetryStatusCode;

        if (throwable != null) {
            span.recordException(throwable);
        }

        // Good response from the server.
        if (statusCode >= 200 && statusCode < 400) {
            return span.setStatus(StatusCode.OK);
        }

        // Error status, try to parse the error message.
        switch (statusCode) {
            case 100:
                message = STATUS_100;
                openTelemetryStatusCode = StatusCode.OK;
                break;
            case 101:
                message = STATUS_101;
                openTelemetryStatusCode = StatusCode.UNSET;
                break;
            case 400:
                message = STATUS_400;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 401:
                message = STATUS_401;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 402:
                message = STATUS_402;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 403:
                message = STATUS_403;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 404:
                message = STATUS_404;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 405:
                message = STATUS_405;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 406:
                message = STATUS_406;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 407:
                message = STATUS_407;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 408:
                message = STATUS_408;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 409:
                message = STATUS_409;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 410:
                message = STATUS_410;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 411:
                message = STATUS_411;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 412:
                message = STATUS_412;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 413:
                message = STATUS_413;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 414:
                message = STATUS_414;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 415:
                message = STATUS_415;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 416:
                message = STATUS_416;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 417:
                message = STATUS_417;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 429:
                message = STATUS_429;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 500:
                message = STATUS_500;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 501:
                message = STATUS_501;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 502:
                message = STATUS_502;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 503:
                message = STATUS_503;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 504:
                message = STATUS_504;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            case 505:
                message = STATUS_505;
                openTelemetryStatusCode = StatusCode.ERROR;
                break;
            default:
                openTelemetryStatusCode = StatusCode.UNSET;
                break;
        }

        if (message == null) {
            return span.setStatus(openTelemetryStatusCode);
        }
        return span.setStatus(openTelemetryStatusCode, message);
    }
}

