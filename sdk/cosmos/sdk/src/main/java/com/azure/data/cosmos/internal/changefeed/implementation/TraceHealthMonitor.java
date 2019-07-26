// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.internal.changefeed.HealthMonitor;
import com.azure.data.cosmos.internal.changefeed.HealthMonitoringRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Implementation for trace health monitor.
 */
class TraceHealthMonitor implements HealthMonitor {
    private final Logger logger = LoggerFactory.getLogger(TraceHealthMonitor.class);
    @Override
    public Mono<Void> inspect(HealthMonitoringRecord record) {
        return Mono.fromRunnable(() -> {
            if (record.getSeverity() == HealthMonitoringRecord.HealthSeverity.ERROR) {
                logger.error(String.format("Unhealthiness detected in the operation %s for %s.", record.operation.name(), record.lease.getId()), record.throwable);
            }
        });
    }
}
