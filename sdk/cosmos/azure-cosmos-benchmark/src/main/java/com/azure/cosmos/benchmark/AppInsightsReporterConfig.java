// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

/**
 * Configuration for Application Insights metrics reporting destination.
 */
public class AppInsightsReporterConfig {
    private final String connectionString;
    private final int stepSeconds;
    private final String testCategory;

    public AppInsightsReporterConfig(String connectionString, int stepSeconds, String testCategory) {
        this.connectionString = connectionString;
        this.stepSeconds = stepSeconds > 0 ? stepSeconds : 10;
        this.testCategory = testCategory;
    }

    public String getConnectionString() { return connectionString; }
    public int getStepSeconds() { return stepSeconds; }
    public String getTestCategory() { return testCategory; }
}
