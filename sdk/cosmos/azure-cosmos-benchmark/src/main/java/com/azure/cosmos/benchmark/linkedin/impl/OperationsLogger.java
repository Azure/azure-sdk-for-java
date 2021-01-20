// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl;

import com.azure.cosmos.benchmark.linkedin.impl.models.CollectionKey;
import com.google.common.base.Preconditions;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Logger class for logging operation specific information
 */
public class OperationsLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationsLogger.class);

    private final Duration _minLatencyForPrintingDebugInfo;

    public OperationsLogger(final Duration minLatencyForPrintingDebugInfo) {
        Preconditions.checkArgument(
            Objects.nonNull(minLatencyForPrintingDebugInfo) && minLatencyForPrintingDebugInfo.toMillis() > 10,
            "The minimum latency for printing debug info should be non-null and greater than 10ms atleast");
        _minLatencyForPrintingDebugInfo = minLatencyForPrintingDebugInfo;
    }

    public <K> void logDebugInfo(final String operationName, final K keys, final CollectionKey collectionKey,
        final long requestDurationInMilliseconds, final String activityId, final String requestDiagnostics) {
        if (requestDurationInMilliseconds <= _minLatencyForPrintingDebugInfo.toMillis()) {
            return;
        }

        LOGGER.info(
            "{} took more than: {} ms. Total duration: {}. Collection name: {}. Key: {}. ActivityId: {}. Diagnostics {}.",
            operationName, _minLatencyForPrintingDebugInfo.toMillis(), requestDurationInMilliseconds, collectionKey,
            keys, activityId, Optional.ofNullable(requestDiagnostics).orElse("No diagnostic information available"));
    }
}
