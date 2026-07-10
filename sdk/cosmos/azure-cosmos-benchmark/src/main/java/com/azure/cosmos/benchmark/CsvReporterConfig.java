// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for CSV metrics reporting destination.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CsvReporterConfig {

    private final String reportingDirectory;

    @JsonCreator
    public CsvReporterConfig(
        @JsonProperty(value = "reportingDirectory", required = true) String reportingDirectory) {
        if (reportingDirectory == null || reportingDirectory.isEmpty()) {
            throw new IllegalArgumentException("reportingDirectory must not be null or empty");
        }
        this.reportingDirectory = reportingDirectory;
    }

    public String getReportingDirectory() { return reportingDirectory; }
}
