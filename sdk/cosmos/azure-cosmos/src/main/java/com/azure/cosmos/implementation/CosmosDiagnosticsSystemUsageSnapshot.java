// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.guava25.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * This class represents a snapshot of the system usage when processing the operation. It can be useful to investigate
 * whether client resource exhaustion caused high latency etc.
 */
public final class CosmosDiagnosticsSystemUsageSnapshot {
    private final String usedMemory;
    private final String availableMemory;
    private final String systemCpuLoad;
    private final int availableProcessors;

    private final ImmutableMap<String, Object> map;

    public CosmosDiagnosticsSystemUsageSnapshot(
        String systemCpuLoad,
        String usedMemory,
        String availableMemory,
        int availableProcessors) {

        checkNotNull(systemCpuLoad, "Argument 'systemCpuLoad' must not be null.");
        checkNotNull(usedMemory, "Argument 'usedMemory' must not be null.");
        checkNotNull(availableMemory, "Argument 'availableMemory' must not be null.");

        this.systemCpuLoad = systemCpuLoad;
        this.usedMemory = usedMemory;
        this.availableMemory = availableMemory;
        this.availableProcessors = availableProcessors;
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builderWithExpectedSize(4);
        builder.put("CPU", systemCpuLoad);
        builder.put("Memory used", usedMemory);
        builder.put("Memory available", availableMemory);
        builder.put("Processor count", availableProcessors);
        this.map = builder.build();
    }

    /**
     * Gets the current memory usage (JVM-wide)
     * @return the current memory usage (JVM-wide)
     */
    public String getUsedMemory() {
        return usedMemory;
    }

    /**
     * Gets the available memory (JVM-wide)
     * @return the available memory (JVM-wide)
     */
    public String getAvailableMemory() {
        return availableMemory;
    }

    /**
     * Gets the system-wide CPU usage for the last 60 seconds (in 5 second intervals)
     * @return the system-wide CPU usage for the last 60 seconds (in 5 second intervals)
     */
    public String getSystemCpuLoad() {
        return systemCpuLoad;
    }

    /**
     * Gets the number of processors available in the JVM
     * @return the number of processors available in the JVM
     */
    public int getAvailableProcessors() {
        return availableProcessors;
    }

    public Map<String, Object> toMap() { return this.map; }
}
