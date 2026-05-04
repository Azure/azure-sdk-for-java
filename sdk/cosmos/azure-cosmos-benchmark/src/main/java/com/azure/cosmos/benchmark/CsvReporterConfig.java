// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

/**
 * Configuration for CSV metrics reporting destination.
 */
public class CsvReporterConfig {
    private final String reportingDirectory;

    public CsvReporterConfig(String reportingDirectory) {
        if (reportingDirectory == null || reportingDirectory.isEmpty()) {
            throw new IllegalArgumentException("CSV reporter requires 'reportingDirectory' to be set");
        }
        this.reportingDirectory = reportingDirectory;
    }

    public String getReportingDirectory() { return reportingDirectory; }
}
