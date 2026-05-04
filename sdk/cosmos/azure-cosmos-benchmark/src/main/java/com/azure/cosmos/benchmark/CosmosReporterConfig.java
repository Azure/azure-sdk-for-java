// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for Cosmos DB metrics reporting destination.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CosmosReporterConfig {

    private final String serviceEndpoint;
    private final String masterKey;
    private final String database;
    private final String container;
    private final String testVariationName;
    private final String branchName;
    private final String commitId;

    @JsonCreator
    public CosmosReporterConfig(
        @JsonProperty(value = "serviceEndpoint", required = true) String serviceEndpoint,
        @JsonProperty(value = "masterKey", required = true) String masterKey,
        @JsonProperty(value = "database", required = true) String database,
        @JsonProperty(value = "container", required = true) String container,
        @JsonProperty("testVariationName") String testVariationName,
        @JsonProperty("branchName") String branchName,
        @JsonProperty("commitId") String commitId) {
        if (serviceEndpoint == null || serviceEndpoint.isEmpty()) {
            throw new IllegalArgumentException("serviceEndpoint must not be null or empty");
        }
        if (masterKey == null || masterKey.isEmpty()) {
            throw new IllegalArgumentException("masterKey must not be null or empty");
        }
        if (database == null || database.isEmpty()) {
            throw new IllegalArgumentException("database must not be null or empty");
        }
        if (container == null || container.isEmpty()) {
            throw new IllegalArgumentException("container must not be null or empty");
        }
        this.serviceEndpoint = serviceEndpoint;
        this.masterKey = masterKey;
        this.database = database;
        this.container = container;
        this.testVariationName = testVariationName != null ? testVariationName : "";
        this.branchName = branchName != null ? branchName : "";
        this.commitId = commitId != null ? commitId : "";
    }

    public String getServiceEndpoint() { return serviceEndpoint; }
    public String getMasterKey() { return masterKey; }
    public String getDatabase() { return database; }
    public String getContainer() { return container; }
    public String getTestVariationName() { return testVariationName; }
    public String getBranchName() { return branchName; }
    public String getCommitId() { return commitId; }
}
