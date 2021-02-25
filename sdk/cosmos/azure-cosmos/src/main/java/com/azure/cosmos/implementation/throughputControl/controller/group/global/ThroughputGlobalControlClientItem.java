// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.group.global;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.Duration;
import java.time.Instant;

public class ThroughputGlobalControlClientItem extends ThroughputGlobalControlItem {

    @JsonProperty(value = "initializeTime", required = true)
    private String initializeTime;

    @JsonProperty(value = "loadFactor", required = true)
    private double loadFactor;

    @JsonProperty(value = "allocatedThroughput", required = true)
    private double allocatedThroughput;

    /**
     * Constructor used for Json deserialization
     */
    public ThroughputGlobalControlClientItem() {

    }

    public ThroughputGlobalControlClientItem(
        String id,
        String partitionKeyValue,
        double loadFactor,
        double allocatedThroughput,
        Duration clientItemExpireInterval) {
        super(id, partitionKeyValue);

        this.loadFactor = loadFactor;
        this.allocatedThroughput = allocatedThroughput;
        this.initializeTime = Instant.now().toString();
        this.setTtl((int)clientItemExpireInterval.getSeconds());
    }

    public String getInitializeTime() {
        return initializeTime;
    }

    public double getLoadFactor() {
        return loadFactor;
    }

    public void setLoadFactor(double loadFactor) {
        this.loadFactor = loadFactor;
    }

    public double getAllocatedThroughput() {
        return allocatedThroughput;
    }

    public void setAllocatedThroughput(double allocatedThroughput) {
        this.allocatedThroughput = allocatedThroughput;
    }
}
