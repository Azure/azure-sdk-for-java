// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for Cosmos DB metrics reporting destination.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CosmosReporterConfig {

    @JsonProperty("serviceEndpoint")
    private String serviceEndpoint;

    @JsonProperty("masterKey")
    private String masterKey;

    @JsonProperty("database")
    private String database;

    @JsonProperty("container")
    private String container;

    @JsonProperty("testVariationName")
    private String testVariationName = "";

    @JsonProperty("branchName")
    private String branchName = "";

    @JsonProperty("commitId")
    private String commitId = "";

    /** Jackson deserialization constructor. */
    public CosmosReporterConfig() {}

    public CosmosReporterConfig(String serviceEndpoint, String masterKey,
                                String database, String container,
                                String testVariationName, String branchName, String commitId) {
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
