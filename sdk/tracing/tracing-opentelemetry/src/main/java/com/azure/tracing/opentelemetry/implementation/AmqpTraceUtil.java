// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tracing.opentelemetry.implementation;

import io.opencensus.trace.Status;

public final class AmqpTraceUtil {
    private static final Status STATUS_100 = Status.UNKNOWN.withDescription("amqp:not-found");
    private static final Status STATUS_101 = Status.UNKNOWN.withDescription("amqp:unauthorized-access");
    private static final Status STATUS_402 = Status.UNKNOWN.withDescription("amqp:resource-limit-exceeded");
    private static final Status STATUS_405 = Status.UNKNOWN.withDescription("amqp:not-allowed");
    private static final Status STATUS_406 = Status.UNKNOWN.withDescription("amqp:internal-error");
    private static final Status STATUS_407 = Status.UNKNOWN.withDescription("amqp:illegal-state");
    private static final Status STATUS_408 = Status.UNKNOWN.withDescription("amqp:not-implemented");
    private static final Status STATUS_409 = Status.UNKNOWN.withDescription("amqp:link:stolen");
    private static final Status STATUS_410 = Status.UNKNOWN.withDescription("amqp:link:message-size-exceeded");
    private static final Status STATUS_411 = Status.UNKNOWN.withDescription("amqp:link:detach-forced");
    private static final Status STATUS_413 = Status.UNKNOWN.withDescription("amqp:connection:forced");
    private static final Status STATUS_414 = Status.UNKNOWN.withDescription("com.microsoft:server-busy");
    private static final Status STATUS_415 = Status.UNKNOWN.withDescription("com.microsoft:argument-error");
    private static final Status STATUS_416 = Status.UNKNOWN.withDescription("com.microsoft:argument-out-of-range");
    private static final Status STATUS_417 = Status.UNKNOWN.withDescription("com.microsoft:entity-disabled");
    private static final Status STATUS_501 = Status.UNKNOWN.withDescription("com.microsoft:partition-not-owned");
    private static final Status STATUS_502 = Status.UNKNOWN.withDescription("com.microsoft:store-lock-lost");
    private static final Status STATUS_503 = Status.UNKNOWN.withDescription("com.microsoft:publisher-revoked");
    private static final Status STATUS_504 = Status.UNKNOWN.withDescription("com.microsoft:timeout");
    private static final Status STATUS_505 = Status.UNKNOWN.withDescription("com.microsoft:tracking-id");


    private AmqpTraceUtil() { }

    /**
     * Parse OpenTelemetry Status from Amqp Error Condition.
     *
     * <p>This method serves a default routine to map HTTP status code to Open Census Status. The
     * mapping is defined in <a
     * href="https://github.com/googleapis/googleapis/blob/master/google/rpc/code.proto">Google API
     * canonical error code</a>, and the behavior is defined in <a
     * href="https://github.com/census-instrumentation/opencensus-specs/blob/master/trace/HTTP.md">OpenTelemetry
     * Specs</a>.
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
            }
        }

        // No error.
        if (error == null && errorCondition.isEmpty()) {
            return Status.OK;
        }
        // Error Condition, try to parse the error message.
        switch (errorCondition) {
            case "NOT_FOUND":
                return STATUS_100;
            case "UNAUTHORIZED_ACCESS":
                return STATUS_101;
            case "RESOURCE_LIMIT_EXCEEDED":
                return STATUS_402;
            case "NOT_ALLOWED":
                return STATUS_405;
            case "INTERNAL_ERROR":
                return STATUS_406;
            case "ILLEGAL_STATE":
                return STATUS_407;
            case "NOT_IMPLEMENTED":
                return STATUS_408;
            case "LINK_STOLEN":
                return STATUS_409;
            case "LINK_PAYLOAD_SIZE_EXCEEDED":
                return STATUS_410;
            case "LINK_DETACH_FORCED":
                return STATUS_411;
            case "CONNECTION_FORCED":
                return STATUS_413;
            case "SERVER_BUSY_ERROR":
                return STATUS_414;
            case "ARGUMENT_ERROR":
                return STATUS_415;
            case "ARGUMENT_OUT_OF_RANGE_ERROR":
                return STATUS_416;
            case "ENTITY_DISABLED_ERROR":
                return STATUS_417;
            case "PARTITION_NOT_OWNED_ERROR":
                return STATUS_501;
            case "STORE_LOCK_LOST_ERROR":
                return STATUS_502;
            case "PUBLISHER_REVOKED_ERROR":
                return STATUS_503;
            case "TIMEOUT_ERROR":
                return STATUS_504;
            case "TRACKING_ID_PROPERTY":
                return STATUS_505;
            default:
                return Status.UNKNOWN.withDescription(message);
        }
    }
}

