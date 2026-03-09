// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

/**
 * Configuration for Cosmos DB metrics reporting destination.
 */
public class CosmosReporterConfig {
    private final String serviceEndpoint;
    private final String masterKey;
    private final String database;
    private final String container;
    private final String testVariationName;
    private final String branchName;
    private final String commitId;

    public CosmosReporterConfig(String serviceEndpoint, String masterKey,
                                String database, String container,
                                String testVariationName, String branchName, String commitId) {
        if (serviceEndpoint == null || serviceEndpoint.isEmpty()) {
            throw new IllegalArgumentException("Cosmos reporter requires 'serviceEndpoint' to be set");
        }
        if (masterKey == null || masterKey.isEmpty()) {
            throw new IllegalArgumentException("Cosmos reporter requires 'masterKey' to be set");
        }
        if (database == null || database.isEmpty()) {
            throw new IllegalArgumentException("Cosmos reporter requires 'database' to be set");
        }
        if (container == null || container.isEmpty()) {
            throw new IllegalArgumentException("Cosmos reporter requires 'container' to be set");
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
