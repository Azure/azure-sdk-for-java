// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The abstract common base of all domains. */
@Fluent
public class MonitorDomain {
    /*
     * Schema version
     */
    @JsonProperty(value = "ver", required = true)
    private int version;

    /**
     * Get the version property: Schema version.
     *
     * @return the version value.
     */
    public int getVersion() {
        return this.version;
    }

    /**
     * Set the version property: Schema version.
     *
     * @param version the version value to set.
     * @return the MonitorDomain object itself.
     */
    public MonitorDomain setVersion(int version) {
        this.version = version;
        return this;
    }
}
