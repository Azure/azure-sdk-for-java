// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.group.global;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ThroughputGlobalControlClientItem extends ThroughputGlobalControlItem {

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonProperty(value = "initializeTime", required = true)
    private Instant initializeTime;

    @JsonProperty(value = "loadFactor", required = true)
    private double loadFactor;

    /**
     * Constructor used for Json deserialization
     */
    public ThroughputGlobalControlClientItem() {

    }

    public ThroughputGlobalControlClientItem(
        String id,
        String partitionKeyValue,
        double loadFactor,
        Duration clientItemExpireInterval) {
        super(id, partitionKeyValue);

        this.loadFactor = loadFactor;
        this.initializeTime = Instant.now();
        this.setTtl((int)clientItemExpireInterval.getSeconds());
    }

    public Instant getInitializeTime() {
        return initializeTime;
    }

    public double getLoadFactor() {
        return loadFactor;
    }

    public void setLoadFactor(double loadFactor) {
        this.loadFactor = loadFactor;
    }
}
