/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.changefeed.internal;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.changefeed.HealthMonitor;
import com.azure.data.cosmos.changefeed.HealthMonitoringRecord;
import com.azure.data.cosmos.changefeed.Lease;
import com.azure.data.cosmos.changefeed.PartitionController;
import reactor.core.publisher.Mono;

/**
 * Monitors partition controller health.
 */
public class HealthMonitoringPartitionControllerDecorator implements PartitionController {
    private final PartitionController inner;
    private final HealthMonitor monitor;

    public HealthMonitoringPartitionControllerDecorator(PartitionController inner, HealthMonitor monitor) {
        if (inner == null) throw new IllegalArgumentException("inner");
        if (monitor == null) throw new IllegalArgumentException("monitor");

        this.inner = inner;
        this.monitor = monitor;
    }

    @Override
    public Mono<Lease> addOrUpdateLease(Lease lease) {
        return this.inner.addOrUpdateLease(lease)
            .onErrorResume(throwable ->  {
                if (throwable instanceof CosmosClientException) {
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
