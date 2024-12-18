// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.logging;

import com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId;
import org.slf4j.MDC;
import reactor.util.annotation.Nullable;

// operation failure stats for a given 5-min window
// each instance represents a logical grouping of errors that a user cares about and can understand,
// e.g. sending telemetry to the portal, storing telemetry to disk, ...
public class OperationLogger {

    public static final OperationLogger NOOP = new OperationLogger(null);

    @Nullable
    private final AggregatingLogger aggregatingLogger;

    public OperationLogger(Class<?> source, String operation) {
        this(source, operation, 300);
    }

    // visible for testing
    OperationLogger(Class<?> source, String operation, int intervalSeconds) {
        this(new AggregatingLogger(source, operation, true, intervalSeconds));
    }

    private OperationLogger(@Nullable AggregatingLogger aggregatingLogger) {
        this.aggregatingLogger = aggregatingLogger;
    }

    public void recordSuccess() {
        if (aggregatingLogger != null) {
            aggregatingLogger.recordSuccess();
        }
    }

    // failureMessage should have low cardinality
    @SuppressWarnings("try")
    public void recordFailure(String failureMessage, AzureMonitorMsgId msgId) {
        if (aggregatingLogger != null) {
            try (MDC.MDCCloseable ignored = msgId.makeActive()) {
                aggregatingLogger.recordWarning(failureMessage);
            }
        }
    }

    // failureMessage should have low cardinality
    public void recordFailure(String failureMessage) {
        if (aggregatingLogger != null) {
            aggregatingLogger.recordWarning(failureMessage, null);
        }
    }

    public void recordFailure(String failureMessage, Throwable exception) {
        if (aggregatingLogger != null) {
            aggregatingLogger.recordWarning(failureMessage, exception);
        }
    }

    // failureMessage should have low cardinality
    @SuppressWarnings("try")
    public void recordFailure(String failureMessage, @Nullable Throwable exception, AzureMonitorMsgId msgId) {
        try (MDC.MDCCloseable ignored = msgId.makeActive()) {
            recordFailure(failureMessage, exception);
        }
    }
}
