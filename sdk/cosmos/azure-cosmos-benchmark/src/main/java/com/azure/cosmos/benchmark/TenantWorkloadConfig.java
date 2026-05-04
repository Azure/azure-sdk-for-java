// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.core.credential.TokenCredential;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Fully-resolved configuration for a single tenant workload.
 * Extends {@link TenantDefaultConfig} (shared workload/connection settings) and adds
 * account-specific fields (serviceEndpoint, masterKey, etc.) and runtime fields
 * set by the orchestrator (suppressCleanup, cosmosMicrometerRegistry).
 *
 * <p>Jackson deserializes per-tenant JSON entries directly into this class.
 * Shared defaults from {@link TenantDefaultConfig} are applied via
 * {@link TenantDefaultConfig#applyTo(TenantWorkloadConfig)} after deserialization.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantWorkloadConfig extends TenantDefaultConfig {

    /**
     * Benchmark execution environment.
     */
    public enum Environment {
        Daily,   // CTL environment where we run the workload for a fixed number of hours
        Staging  // CTL environment where the workload runs as a long running job
    }

    private static final Logger logger = LoggerFactory.getLogger(TenantWorkloadConfig.class);

    public static final String DEFAULT_PARTITION_KEY_PATH = "/pk";

    private static final int INGESTION_RETRY_CONCURRENCY = 10;

    // ======== Account connection (per-tenant only) ========

    @JsonProperty("id")
    private String id;

    @JsonProperty("serviceEndpoint")
    private String serviceEndpoint;

    @JsonProperty("masterKey")
    private String masterKey;

    @JsonProperty("databaseId")
    private String databaseId;

    @JsonProperty("containerId")
    private String containerId;

    // ======== AAD auth (per-tenant only) ========

    @JsonProperty("aadLoginEndpoint")
    private String aadLoginEndpoint;

    @JsonProperty("aadTenantId")
    private String aadTenantId;

    @JsonProperty("aadManagedIdentityClientId")
    private String aadManagedIdentityClientId;

    // ======== Runtime fields (set by orchestrator, not from JSON) ========

    private boolean suppressCleanup = false;

    /** Cosmos SDK micrometer registry (set by orchestrator, not from JSON). */
    private transient MeterRegistry cosmosMicrometerRegistry;

    public TenantWorkloadConfig() {}

    // ======== Account-specific getters ========

    public String getId() { return id; }
    public String getServiceEndpoint() { return serviceEndpoint; }
    public String getMasterKey() { return masterKey; }
    public String getDatabaseId() { return databaseId; }
    public String getContainerId() { return containerId; }
    public String getAadLoginEndpoint() { return aadLoginEndpoint; }
    public String getAadTenantId() { return aadTenantId; }
    public String getAadManagedIdentityClientId() { return aadManagedIdentityClientId; }

    public int getIngestionRetryConcurrency() { return INGESTION_RETRY_CONCURRENCY; }

    public Environment getEnvironment() {
        if (environment == null) return Environment.Daily;
        return Environment.valueOf(environment);
    }

    public boolean isSuppressCleanup() { return suppressCleanup; }
    public MeterRegistry getCosmosMicrometerRegistry() { return cosmosMicrometerRegistry; }

    /**
     * Builds a TokenCredential for managed identity authentication.
     */
    public TokenCredential buildTokenCredential() {
        String loginEndpoint = aadLoginEndpoint != null ? aadLoginEndpoint : Configuration.getAadLoginUri();
        String tenantId = aadTenantId != null ? aadTenantId : Configuration.getAadTenantId();
        String clientId = aadManagedIdentityClientId != null ? aadManagedIdentityClientId : Configuration.getAadManagedIdentityId();

        return new DefaultAzureCredentialBuilder()
            .managedIdentityClientId(clientId)
            .authorityHost(loginEndpoint)
            .tenantId(tenantId)
            .build();
    }

    // ======== Setters ========

    public void setId(String id) { this.id = id; }
    public void setServiceEndpoint(String serviceEndpoint) { this.serviceEndpoint = serviceEndpoint; }
    public void setMasterKey(String masterKey) { this.masterKey = masterKey; }
    public void setDatabaseId(String databaseId) { this.databaseId = databaseId; }
    public void setContainerId(String containerId) { this.containerId = containerId; }
    public void setOperation(String operation) { this.operation = operation; }
    public void setConnectionMode(String connectionMode) { this.connectionMode = connectionMode; }
    public void setConsistencyLevel(String consistencyLevel) { this.consistencyLevel = consistencyLevel; }
    public void setMaxConnectionPoolSize(int maxConnectionPoolSize) { this.maxConnectionPoolSize = maxConnectionPoolSize; }
    public void setConnectionSharingAcrossClientsEnabled(boolean val) { this.connectionSharingAcrossClientsEnabled = val; }
    public void setNumberOfPreCreatedDocuments(int val) { this.numberOfPreCreatedDocuments = val; }
    public void setApplicationName(String applicationName) { this.applicationName = applicationName; }
    public void setSuppressCleanup(boolean suppressCleanup) { this.suppressCleanup = suppressCleanup; }
    public void setCosmosMicrometerRegistry(MeterRegistry registry) { this.cosmosMicrometerRegistry = registry; }

    @Override
    public String toString() {
        return "TenantWorkloadConfig{" +
            "id='" + id + '\'' +
            ", serviceEndpoint='" + serviceEndpoint + '\'' +
            ", databaseId='" + databaseId + '\'' +
            ", containerId='" + containerId + '\'' +
            ", operation=" + operation +
            ", connectionMode=" + connectionMode +
            ", connectionSharingAcrossClientsEnabled=" + isConnectionSharingAcrossClientsEnabled() +
            '}';
    }
}
