// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.tracing.sleuth.implementation;

import org.springframework.cloud.sleuth.Span;

public final class HttpTraceUtil {

    /**
     * Parse Spring Cloud Sleuth Status from HTTP response status code.
     *
     * @param span the span to set the status on.
     * @param statusCode the HTTP response status code. {@code 0} means invalid response.
     * @param throwable the {@link Throwable} to propagate to the span.
     * @return the corresponding span.
     */
    public static Span setSpanStatus(Span span, int statusCode, Throwable throwable) {
        if (throwable != null) {
            return span.error(throwable);
        }

        // Good response from the server.
        if (statusCode >= 200 && statusCode < 400) {
            return span;
        }

        // Error status, try to parse the error status.
        HttpStatus status = HttpStatus.resolve(statusCode);
        if (status != null) {
            return span.tag("http.status_message", status.getReasonPhrase());
        }
        return span;
    }
}

