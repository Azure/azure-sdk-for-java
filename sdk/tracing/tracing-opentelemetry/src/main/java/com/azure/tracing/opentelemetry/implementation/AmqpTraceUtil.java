// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tracing.opentelemetry.implementation;

import io.opencensus.trace.Status;

public final class AmqpTraceUtil {

    private AmqpTraceUtil() { }

    /**
     * Parse OpenTelemetry Status from Amqp Error Condition.
     *
     * @param errorCondition AMQP header value for this error condition.
     * @param error the error occurred during response transmission (optional).
     * @return the corresponding OpenTelemetry {@code Status}.
     */
    public static Status parseErrorCondition(String errorCondition, Throwable error) {
        String message = null;

        if (error != null) {
            message = error.getMessage();
            if (message == null) {
                message = error.getClass().getSimpleName();
                return Status.UNKNOWN.withDescription(message);
            }
        }

        // No error.
        if (error == null && errorCondition.isEmpty()) {
            return Status.OK;
        }

        // return status with custom error condition message
        return Status.UNKNOWN.withDescription(errorCondition);
    }
}

