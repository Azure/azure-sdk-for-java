// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuickPulseMetrics {

    @JsonProperty(value = "Name")
    private final String name;

    @JsonProperty(value = "Value")
    private final double value;

    @JsonProperty(value = "Weight")
    private final int weight;

    public QuickPulseMetrics(String name, double value, int weight) {
        this.name = name;
        this.value = value;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public int getWeight() {
        return weight;
    }
}
