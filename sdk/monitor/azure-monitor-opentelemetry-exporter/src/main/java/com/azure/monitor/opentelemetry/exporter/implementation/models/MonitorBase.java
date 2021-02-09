// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Data struct to contain only C section with custom fields. */
@Fluent
public final class MonitorBase {
    /*
     * Name of item (B section) if any. If telemetry data is derived straight
     * from this, this should be null.
     */
    @JsonProperty(value = "baseType")
    private String baseType;

    /*
     * The data payload for the telemetry request
     */
    @JsonProperty(value = "baseData")
    private MonitorDomain baseData;

    /**
     * Get the baseType property: Name of item (B section) if any. If telemetry data is derived straight from this, this
     * should be null.
     *
     * @return the baseType value.
     */
    public String getBaseType() {
        return this.baseType;
    }

    /**
     * Set the baseType property: Name of item (B section) if any. If telemetry data is derived straight from this, this
     * should be null.
     *
     * @param baseType the baseType value to set.
     * @return the MonitorBase object itself.
     */
    public MonitorBase setBaseType(String baseType) {
        this.baseType = baseType;
        return this;
    }

    /**
     * Get the baseData property: The data payload for the telemetry request.
     *
     * @return the baseData value.
     */
    public MonitorDomain getBaseData() {
        return this.baseData;
    }

    /**
     * Set the baseData property: The data payload for the telemetry request.
     *
     * @param baseData the baseData value to set.
     * @return the MonitorBase object itself.
     */
    public MonitorBase setBaseData(MonitorDomain baseData) {
        this.baseData = baseData;
        return this;
    }
}
