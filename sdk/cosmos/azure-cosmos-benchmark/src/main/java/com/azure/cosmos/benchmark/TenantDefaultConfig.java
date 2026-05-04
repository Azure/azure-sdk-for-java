// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON model for the {@code tenantDefaults} section in the benchmark config.
 * Contains only shared workload settings that can be inherited by all tenants.
 *
 * <p>Account-specific fields (serviceEndpoint, masterKey, databaseId, containerId,
 * AAD fields) are intentionally excluded — they must be set per-tenant.</p>
 *
 * <p>After deserialization, defaults are applied to each {@link TenantWorkloadConfig}
 * via {@link #applyTo(TenantWorkloadConfig)}.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class TenantDefaultConfig {

    // ======== Workload params ========

    @JsonProperty("operation")
    private String operation;

    @JsonProperty("numberOfPreCreatedDocuments")
    private Integer numberOfPreCreatedDocuments;

    @JsonProperty("throughput")
    private Integer throughput;

    @JsonProperty("documentDataFieldSize")
    private Integer documentDataFieldSize;

    @JsonProperty("documentDataFieldCount")
    private Integer documentDataFieldCount;

    @JsonProperty("contentResponseOnWriteEnabled")
    private Boolean contentResponseOnWriteEnabled;

    @JsonProperty("disablePassingPartitionKeyAsOptionOnWrite")
    private Boolean disablePassingPartitionKeyAsOptionOnWrite;

    @JsonProperty("useNameLink")
    private Boolean useNameLink;

    @JsonProperty("tupleSize")
    private Integer tupleSize;

    @JsonProperty("pointOperationLatencyThresholdMs")
    private Integer pointOperationLatencyThresholdMs;

    @JsonProperty("nonPointOperationLatencyThresholdMs")
    private Integer nonPointOperationLatencyThresholdMs;

    @JsonProperty("isRegionScopedSessionContainerEnabled")
    private Boolean isRegionScopedSessionContainerEnabled;

    @JsonProperty("isDefaultLog4jLoggerEnabled")
    private Boolean isDefaultLog4jLoggerEnabled;

    @JsonProperty("diagnosticsThresholdDuration")
    private String diagnosticsThresholdDuration;

    @JsonProperty("sparsityWaitTime")
    private String sparsityWaitTime;

    @JsonProperty("isProactiveConnectionManagementEnabled")
    private Boolean isProactiveConnectionManagementEnabled;

    @JsonProperty("isUseUnWarmedUpContainer")
    private Boolean isUseUnWarmedUpContainer;

    @JsonProperty("numberOfCollectionForCtl")
    private Integer numberOfCollectionForCtl;

    @JsonProperty("readWriteQueryReadManyPct")
    private String readWriteQueryReadManyPct;

    @JsonProperty("encryptedStringFieldCount")
    private Integer encryptedStringFieldCount;

    @JsonProperty("encryptedLongFieldCount")
    private Integer encryptedLongFieldCount;

    @JsonProperty("encryptedDoubleFieldCount")
    private Integer encryptedDoubleFieldCount;

    @JsonProperty("encryptionEnabled")
    private Boolean encryptionEnabled;

    @JsonProperty("bulkloadBatchSize")
    private Integer bulkloadBatchSize;

    @JsonProperty("testScenario")
    private String testScenario;

    @JsonProperty("environment")
    private String environment;

    @JsonProperty("useSync")
    private Boolean useSync;

    @JsonProperty("proactiveConnectionRegionsCount")
    private Integer proactiveConnectionRegionsCount;

    @JsonProperty("aggressiveWarmupDuration")
    private String aggressiveWarmupDuration;

    // ======== Connection params ========

    @JsonProperty("connectionMode")
    private String connectionMode;

    @JsonProperty("consistencyLevel")
    private String consistencyLevel;

    @JsonProperty("maxConnectionPoolSize")
    private Integer maxConnectionPoolSize;

    @JsonProperty("connectionSharingAcrossClientsEnabled")
    private Boolean connectionSharingAcrossClientsEnabled;

    @JsonProperty("http2Enabled")
    private Boolean http2Enabled;

    @JsonProperty("http2MaxConcurrentStreams")
    private Integer http2MaxConcurrentStreams;

    @JsonProperty("preferredRegionsList")
    private String preferredRegionsList;

    @JsonProperty("manageDatabase")
    private Boolean manageDatabase;

    @JsonProperty("isManagedIdentityRequired")
    private Boolean isManagedIdentityRequired;

    /**
     * Apply these defaults to a tenant config, filling in only fields that are null on the tenant.
     */
    void applyTo(TenantWorkloadConfig tenant) {
        tenant.applyDefaults(this);
    }

    // Package-private getters for applyDefaults
    String getOperation() { return operation; }
    Integer getNumberOfPreCreatedDocuments() { return numberOfPreCreatedDocuments; }
    Integer getThroughput() { return throughput; }
    Integer getDocumentDataFieldSize() { return documentDataFieldSize; }
    Integer getDocumentDataFieldCount() { return documentDataFieldCount; }
    Boolean getContentResponseOnWriteEnabled() { return contentResponseOnWriteEnabled; }
    Boolean getDisablePassingPartitionKeyAsOptionOnWrite() { return disablePassingPartitionKeyAsOptionOnWrite; }
    Boolean getUseNameLink() { return useNameLink; }
    Integer getTupleSize() { return tupleSize; }
    Integer getPointOperationLatencyThresholdMs() { return pointOperationLatencyThresholdMs; }
    Integer getNonPointOperationLatencyThresholdMs() { return nonPointOperationLatencyThresholdMs; }
    Boolean getIsRegionScopedSessionContainerEnabled() { return isRegionScopedSessionContainerEnabled; }
    Boolean getIsDefaultLog4jLoggerEnabled() { return isDefaultLog4jLoggerEnabled; }
    String getDiagnosticsThresholdDuration() { return diagnosticsThresholdDuration; }
    String getSparsityWaitTime() { return sparsityWaitTime; }
    Boolean getIsProactiveConnectionManagementEnabled() { return isProactiveConnectionManagementEnabled; }
    Boolean getIsUseUnWarmedUpContainer() { return isUseUnWarmedUpContainer; }
    Integer getNumberOfCollectionForCtl() { return numberOfCollectionForCtl; }
    String getReadWriteQueryReadManyPct() { return readWriteQueryReadManyPct; }
    Integer getEncryptedStringFieldCount() { return encryptedStringFieldCount; }
    Integer getEncryptedLongFieldCount() { return encryptedLongFieldCount; }
    Integer getEncryptedDoubleFieldCount() { return encryptedDoubleFieldCount; }
    Boolean getEncryptionEnabled() { return encryptionEnabled; }
    Integer getBulkloadBatchSize() { return bulkloadBatchSize; }
    String getTestScenario() { return testScenario; }
    String getEnvironment() { return environment; }
    Boolean getUseSync() { return useSync; }
    Integer getProactiveConnectionRegionsCount() { return proactiveConnectionRegionsCount; }
    String getAggressiveWarmupDuration() { return aggressiveWarmupDuration; }
    String getConnectionMode() { return connectionMode; }
    String getConsistencyLevel() { return consistencyLevel; }
    Integer getMaxConnectionPoolSize() { return maxConnectionPoolSize; }
    Boolean getConnectionSharingAcrossClientsEnabled() { return connectionSharingAcrossClientsEnabled; }
    Boolean getHttp2Enabled() { return http2Enabled; }
    Integer getHttp2MaxConcurrentStreams() { return http2MaxConcurrentStreams; }
    String getPreferredRegionsList() { return preferredRegionsList; }
    Boolean getManageDatabase() { return manageDatabase; }
    Boolean getIsManagedIdentityRequired() { return isManagedIdentityRequired; }
}
