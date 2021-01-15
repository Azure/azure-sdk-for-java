// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.cpu;

import com.azure.cosmos.implementation.guava25.base.Preconditions;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class CpuLoadHistory {
    private static final String EMPTY = "empty";
    final List<CpuLoad> cpuLoad;
    final Duration monitoringInterval;
    final AtomicReference<Boolean> cpuOverload = new AtomicReference<>();
    private String cachedToString;

    public CpuLoadHistory(List<CpuLoad> cpuLoad, Duration monitoringInterval) {
        Preconditions.checkNotNull(cpuLoad, "cpuLoad");
        this.cpuLoad = cpuLoad;
        Preconditions.checkArgument(!monitoringInterval.isZero(), "monitoringInterval is zero");

        this.monitoringInterval = monitoringInterval;
    }

    public boolean isCpuOverloaded() {
        if (cpuOverload.get() == null) {
            cpuOverload.set(isCpuOverloadInternal());
        }

        return cpuOverload.get();
    }

    @Override
    public String toString() {
        if (cachedToString == null) {
            cachedToString = toStringInternal();
        }

        return cachedToString;
    }

    private String toStringInternal() {
        if (cpuLoad == null || cpuLoad.isEmpty()) {
            return EMPTY;
        }

        return String.join(", ", cpuLoad.stream().map(c -> c.toString()).collect(Collectors.toList()));
    }

    Instant getLastTimestamp() {
        return this.cpuLoad.get(this.cpuLoad.size() - 1).timestamp;
    }

    private boolean isCpuOverloadInternal() {
        for (int index = 0; index < this.cpuLoad.size(); ++index) {
            if ((double) this.cpuLoad.get(index).value > 90.0) {
                return true;
            }
        }

        // This signal is fragile, because the timestamps come from
        // a non-monotonic clock that might have gotten adjusted by
        // e.g. NTP.
        for (int index = 0; index < this.cpuLoad.size() - 1; ++index) {
            long totalMilliseconds =
                this.cpuLoad.get(index + 1).timestamp.toEpochMilli() - this.cpuLoad.get(index).timestamp.toEpochMilli();
            if (totalMilliseconds > 1.5 * this.monitoringInterval.toMillis()) {
                return true;
            }
        }
        return false;
    }
}
