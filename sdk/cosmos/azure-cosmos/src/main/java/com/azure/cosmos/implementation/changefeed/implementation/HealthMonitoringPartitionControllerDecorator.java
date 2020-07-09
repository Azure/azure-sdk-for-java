// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.changefeed.HealthMonitor;
import com.azure.cosmos.implementation.changefeed.HealthMonitoringRecord;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.PartitionController;
import reactor.core.publisher.Mono;

/**
 * Monitors partition controller health.
 */
class HealthMonitoringPartitionControllerDecorator implements PartitionController {
    private final PartitionController inner;
    private final HealthMonitor monitor;

    public HealthMonitoringPartitionControllerDecorator(PartitionController inner, HealthMonitor monitor) {
        if (inner == null) {
            throw new IllegalArgumentException("inner");
        }

        if (monitor == null) {
            throw new IllegalArgumentException("monitor");
        }

        this.inner = inner;
        this.monitor = monitor;
    }

    @Override
    public Mono<Lease> addOrUpdateLease(Lease lease) {
        return this.inner.addOrUpdateLease(lease)
            .onErrorResume(throwable ->  {
                if (throwable instanceof CosmosException) {
                    // do nothing.
                } else {
                    monitor.inspect(new HealthMonitoringRecord(
                        HealthMonitoringRecord.HealthSeverity.INFORMATIONAL,
                        HealthMonitoringRecord.MonitoredOperation.ACQUIRE_LEASE,
                        lease, throwable));
                }
                return Mono.empty();
            });
    }

    @Override
    public Mono<Void> initialize() {
        return this.inner.initialize();
    }

    @Override
    public Mono<Void> shutdown() {
        return this.inner.shutdown();
    }
}
