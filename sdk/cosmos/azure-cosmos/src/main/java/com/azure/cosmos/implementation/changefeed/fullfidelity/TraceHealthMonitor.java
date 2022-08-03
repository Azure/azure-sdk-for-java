// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.fullfidelity;

import com.azure.cosmos.implementation.changefeed.HealthMonitor;
import com.azure.cosmos.implementation.changefeed.HealthMonitoringRecord;
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
                logger.error("Unhealthiness detected in the operation {} for {}.", record.operation.name(), record.lease.getId(), record.throwable);
            }
        });
    }
}
