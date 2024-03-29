// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.appcontainers.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration of Application Insights.
 */
@Fluent
public final class AppInsightsConfiguration {
    /*
     * Application Insights connection string
     */
    @JsonProperty(value = "connectionString")
    private String connectionString;

    /**
     * Creates an instance of AppInsightsConfiguration class.
     */
    public AppInsightsConfiguration() {
    }

    /**
     * Get the connectionString property: Application Insights connection string.
     * 
     * @return the connectionString value.
     */
    public String connectionString() {
        return this.connectionString;
    }

    /**
     * Set the connectionString property: Application Insights connection string.
     * 
     * @param connectionString the connectionString value to set.
     * @return the AppInsightsConfiguration object itself.
     */
    public AppInsightsConfiguration withConnectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }
}
