// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for Application Insights metrics reporting destination.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppInsightsReporterConfig {

    @JsonProperty("connectionString")
    private String connectionString;

    @JsonProperty("stepSeconds")
    private int stepSeconds = 10;

    @JsonProperty("testCategory")
    private String testCategory;

    /** Jackson deserialization constructor. */
    public AppInsightsReporterConfig() {}

    public AppInsightsReporterConfig(String connectionString, int stepSeconds, String testCategory) {
        this.connectionString = connectionString;
        this.stepSeconds = stepSeconds > 0 ? stepSeconds : 10;
        this.testCategory = testCategory;
    }

    public String getConnectionString() { return connectionString; }
    public int getStepSeconds() { return stepSeconds; }
    public String getTestCategory() { return testCategory; }
}
