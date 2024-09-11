// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.instrumentation;

import com.azure.core.amqp.exception.AmqpException;
import reactor.core.Exceptions;
import reactor.core.publisher.Signal;

import java.time.Duration;
import java.time.Instant;

public final class InstrumentationUtils {
    // Attribute names based on OpenTelemetry specification
    // https://github.com/open-telemetry/semantic-conventions/blob/main/docs/messaging/messaging-spans.md
    public static final String SERVER_ADDRESS = "server.address";
    public static final String ERROR_TYPE = "error.type";
    public static final String MESSAGING_BATCH_MESSAGE_COUNT = "messaging.batch.message_count";
    public static final String MESSAGING_DESTINATION_NAME = "messaging.destination.name";
    public static final String MESSAGING_DESTINATION_PARTITION_ID = "messaging.destination.partition.id";
    public static final String MESSAGING_OPERATION_NAME = "messaging.operation.name";
    public static final String MESSAGING_OPERATION_TYPE = "messaging.operation.type";
    public static final String MESSAGING_SYSTEM = "messaging.system";
    public static final String MESSAGING_CONSUMER_GROUP_NAME = "messaging.consumer.group.name";
    public static final String MESSAGING_EVENTHUBS_MESSAGE_ENQUEUED_TIME = "messaging.eventhubs.message.enqueued_time";

    // metrics
    public static final String MESSAGING_CLIENT_PUBLISHED_MESSAGES = "messaging.client.published.messages";
    public static final String MESSAGING_CLIENT_CONSUMED_MESSAGES = "messaging.client.consumed.messages";
    public static final String MESSAGING_CLIENT_OPERATION_DURATION = "messaging.client.operation.duration";
    public static final String MESSAGING_PROCESS_DURATION = "messaging.process.duration";

    // custom metrics
    public static final String MESSAGING_EVENTHUBS_CONSUMER_LAG = "messaging.eventhubs.consumer.lag";

    // constant attribute values
    public static final String MESSAGING_SYSTEM_VALUE = "eventhubs";
    public static final String CANCELLED_ERROR_TYPE_VALUE = "cancelled";

    // context propagation constants
    public static final String TRACEPARENT_KEY = "traceparent";
    public static final String DIAGNOSTIC_ID_KEY = "Diagnostic-Id";

    public static String getErrorType(Signal<?> signal) {
        // this method should only be called for complete or error signals
        if (signal.isOnComplete()) {
            return null;
        }

        return getErrorType(signal.getThrowable());
    }

    public static String getErrorType(Throwable error) {
        if (error == null) {
            return null;
        }

        error = Exceptions.unwrap(error);

        if (error instanceof AmqpException && ((AmqpException) error).getErrorCondition() != null) {
            return ((AmqpException) error).getErrorCondition().getErrorCondition();
        }

        return error.getClass().getName();
    }

    public static Throwable unwrap(Throwable error) {
        error = Exceptions.unwrap(error);

        if (error instanceof AmqpException && error.getCause() != null) {
            return error.getCause();
        }

        return error;
    }

    public static double getDurationInSeconds(Instant startTime) {
        long durationNanos = Duration.between(startTime, Instant.now()).toNanos();
        if (durationNanos < 0) {
            // we use this method to get lag, so need to take care of time skew on different machines
            return 0d;
        }
        return durationNanos / 1_000_000_000d;
    }

    public static String getOperationType(OperationName name) {
        switch (name) {
            case SEND:
                return "publish";
            case RECEIVE:
                return "receive";
            case CHECKPOINT:
                return "settle";
            case PROCESS:
                return "process";
            default:
                return null;
        }
    }

    private InstrumentationUtils() {
    }
}
