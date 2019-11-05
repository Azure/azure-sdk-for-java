// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry.implementation;

import io.opentelemetry.trace.Status;

public final class AmqpTraceUtil {

    private AmqpTraceUtil() { }

    /**
     * Parses an OpenTelemetry Status from AMQP Error Condition.
     *
     * @param statusMessage AMQP description for this error condition.
     * @param error the error occurred during response transmission (optional).
     * @return the corresponding OpenTelemetry {@link Status}.
     */
    public static Status parseStatusMessage(String statusMessage, Throwable error) {
        if (error != null) {
            final String message = error.getMessage();

            return message != null
                ? Status.UNKNOWN.withDescription(message)
                : Status.UNKNOWN.withDescription(error.getClass().getSimpleName());

        }
        if (statusMessage != null && "success".equalsIgnoreCase(statusMessage)) {
            // No error.
            return Status.OK;
        }
        // return status with custom error condition message
        return Status.UNKNOWN.withDescription(statusMessage);
    }
}
