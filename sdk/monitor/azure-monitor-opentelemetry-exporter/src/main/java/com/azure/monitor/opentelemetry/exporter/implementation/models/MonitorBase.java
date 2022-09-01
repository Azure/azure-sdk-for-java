/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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
     * Get the baseType property: Name of item (B section) if any. If telemetry data is derived
     * straight from this, this should be null.
     *
     * @return the baseType value.
     */
    public String getBaseType() {
        return this.baseType;
    }

    /**
     * Set the baseType property: Name of item (B section) if any. If telemetry data is derived
     * straight from this, this should be null.
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
