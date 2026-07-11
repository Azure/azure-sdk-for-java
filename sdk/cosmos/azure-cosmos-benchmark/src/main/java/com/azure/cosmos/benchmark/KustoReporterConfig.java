// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for the Azure Data Explorer (Kusto) metrics reporting destination.
 *
 * <p>Metrics are streamed into {@code <database>.<table>} via the Kusto streaming-ingest REST
 * endpoint using a bearer token obtained from {@code DefaultAzureCredential}. No additional Maven
 * dependency is required (the ingest call uses {@link java.net.HttpURLConnection}), which keeps the
 * benchmark module's strict dependency allowlist unchanged.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KustoReporterConfig {

    static final String DEFAULT_MAPPING_NAME = "BenchmarkMetrics_json";

    private final String clusterUrl;
    private final String database;
    private final String table;
    private final String mappingName;
    private final String testVariationName;
    private final String branchName;
    private final String commitId;

    @JsonCreator
    public KustoReporterConfig(
        @JsonProperty(value = "clusterUrl", required = true) String clusterUrl,
        @JsonProperty(value = "database", required = true) String database,
        @JsonProperty(value = "table", required = true) String table,
        @JsonProperty("mappingName") String mappingName,
        @JsonProperty("testVariationName") String testVariationName,
        @JsonProperty("branchName") String branchName,
        @JsonProperty("commitId") String commitId) {
        if (clusterUrl == null || clusterUrl.isEmpty()) {
            throw new IllegalArgumentException("clusterUrl must not be null or empty");
        }
        if (database == null || database.isEmpty()) {
            throw new IllegalArgumentException("database must not be null or empty");
        }
        if (table == null || table.isEmpty()) {
            throw new IllegalArgumentException("table must not be null or empty");
        }
        // Normalize: strip any trailing slash so endpoint composition is predictable.
        this.clusterUrl = clusterUrl.endsWith("/")
            ? clusterUrl.substring(0, clusterUrl.length() - 1)
            : clusterUrl;
        this.database = database;
        this.table = table;
        this.mappingName = (mappingName != null && !mappingName.isEmpty())
            ? mappingName : DEFAULT_MAPPING_NAME;
        this.testVariationName = testVariationName != null ? testVariationName : "";
        this.branchName = branchName != null ? branchName : "";
        this.commitId = commitId != null ? commitId : "";
    }

    public String getClusterUrl() { return clusterUrl; }
    public String getDatabase() { return database; }
    public String getTable() { return table; }
    public String getMappingName() { return mappingName; }
    public String getTestVariationName() { return testVariationName; }
    public String getBranchName() { return branchName; }
    public String getCommitId() { return commitId; }
}
