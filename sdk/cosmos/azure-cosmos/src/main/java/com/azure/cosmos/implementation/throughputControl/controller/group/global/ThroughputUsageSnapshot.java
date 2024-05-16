// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.group.global;

import java.time.Duration;
import java.time.Instant;

public class ThroughputUsageSnapshot {
    private final double throughputUsage;
    private final Instant time;
    private double weight;

    public ThroughputUsageSnapshot(double throughputUsage) {
        this.throughputUsage = throughputUsage;
        this.time = Instant.now();
    }

    public double getThroughputUsage() {
        return throughputUsage;
    }

    public Instant getTime() {
        return time;
    }

    public double getWeight() {
        return this.weight;
    }

    /**
     * The most recent should have higher weight,
     * so it makes higher impact on the final load factor of the client.
     *
     * @param startTime The start time.
     * @return The weight of the throughput usage snapshot.
     */
    public double calculateWeight(Instant startTime) {
        this.weight = Math.exp(Duration.between(time, startTime).getSeconds());
        return this.weight;
    }
}
