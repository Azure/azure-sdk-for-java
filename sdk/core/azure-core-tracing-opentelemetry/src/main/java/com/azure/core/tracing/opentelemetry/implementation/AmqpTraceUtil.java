// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry.implementation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

public final class AmqpTraceUtil {

    private AmqpTraceUtil() { }

    /**
     * Parses an OpenTelemetry Status from AMQP Error Condition.
     *
     * @param span the span to set the status for.
     * @param statusMessage AMQP description for this error condition.
     * @param throwable the error occurred during response transmission (optional).
     * @return the corresponding OpenTelemetry {@link Span}.
     */
    public static Span parseStatusMessage(Span span, String statusMessage, Throwable throwable) {
        if (throwable != null) {
            span.recordException(throwable);
            return span.setStatus(StatusCode.ERROR);
        }
        if (statusMessage != null && "success".equalsIgnoreCase(statusMessage)) {
            // No error.
            return span.setStatus(StatusCode.OK);
        }
        if (statusMessage == null) {
            return span.setStatus(StatusCode.UNSET);
        }
        // return status with custom error condition message
        return span.setStatus(StatusCode.UNSET, statusMessage);
    }
}
