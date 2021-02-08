// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.group.global;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ThroughputGlobalControlClientItem extends ThroughputGlobalControlItem {

    @JsonProperty(value = "initializeTime", required = true)
    private String initializeTime;

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
        this.initializeTime = ZonedDateTime.now(ZoneId.of("UTC")).toString();
        this.setTtl((int)clientItemExpireInterval.getSeconds());
    }

    public String getInitializeTime() {
        return initializeTime;
    }

    public void setInitializeTime(String initializeTime) {
        this.initializeTime = initializeTime;
    }

    public double getLoadFactor() {
        return loadFactor;
    }

    public void setLoadFactor(double loadFactor) {
        this.loadFactor = loadFactor;
    }
}
