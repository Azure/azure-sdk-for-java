// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON model for the {@code tenantDefaults} section in the benchmark config.
 * Contains only shared workload settings that can be inherited by all tenants.
 *
 * <p>Account-specific fields (serviceEndpoint, masterKey, databaseId, containerId,
 * AAD auth fields) are intentionally excluded — they must be set per-tenant in
 * {@link TenantWorkloadConfig}.</p>
 *
 * <p>{@link TenantWorkloadConfig} extends this class, inheriting all shared fields
 * and adding account-specific + runtime fields. Jackson deserializes
 * {@code tenantDefaults} as this class (ignoring unknown/account fields), and
 * per-tenant entries as {@link TenantWorkloadConfig} (which sees all fields).</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class TenantDefaultConfig {

    // ======== Workload params ========

    @JsonProperty("operation")
    protected String operation;

    @JsonProperty("numberOfPreCreatedDocuments")
    protected Integer numberOfPreCreatedDocuments;

    @JsonProperty("throughput")
    protected Integer throughput;

    @JsonProperty("documentDataFieldSize")
    protected Integer documentDataFieldSize;

    @JsonProperty("documentDataFieldCount")
    protected Integer documentDataFieldCount;

    @JsonProperty("contentResponseOnWriteEnabled")
    protected Boolean contentResponseOnWriteEnabled;

    @JsonProperty("disablePassingPartitionKeyAsOptionOnWrite")
    protected Boolean disablePassingPartitionKeyAsOptionOnWrite;

    @JsonProperty("useNameLink")
    protected Boolean useNameLink;

    @JsonProperty("tupleSize")
    protected Integer tupleSize;

    @JsonProperty("pointOperationLatencyThresholdMs")
    protected Integer pointOperationLatencyThresholdMs;

    @JsonProperty("nonPointOperationLatencyThresholdMs")
    protected Integer nonPointOperationLatencyThresholdMs;

    @JsonProperty("isRegionScopedSessionContainerEnabled")
    protected Boolean isRegionScopedSessionContainerEnabled;

    @JsonProperty("isDefaultLog4jLoggerEnabled")
    protected Boolean isDefaultLog4jLoggerEnabled;

    @JsonProperty("diagnosticsThresholdDuration")
    protected String diagnosticsThresholdDuration;

    @JsonProperty("sparsityWaitTime")
    protected String sparsityWaitTime;

    @JsonProperty("isProactiveConnectionManagementEnabled")
    protected Boolean isProactiveConnectionManagementEnabled;

    @JsonProperty("isUseUnWarmedUpContainer")
    protected Boolean isUseUnWarmedUpContainer;

    @JsonProperty("numberOfCollectionForCtl")
    protected Integer numberOfCollectionForCtl;

    @JsonProperty("readWriteQueryReadManyPct")
    protected String readWriteQueryReadManyPct;

    @JsonProperty("encryptedStringFieldCount")
    protected Integer encryptedStringFieldCount;

    @JsonProperty("encryptedLongFieldCount")
    protected Integer encryptedLongFieldCount;

    @JsonProperty("encryptedDoubleFieldCount")
    protected Integer encryptedDoubleFieldCount;

    @JsonProperty("encryptionEnabled")
    protected Boolean encryptionEnabled;

    @JsonProperty("bulkloadBatchSize")
    protected Integer bulkloadBatchSize;

    @JsonProperty("testScenario")
    protected String testScenario;

    @JsonProperty("environment")
    protected String environment;

    @JsonProperty("useSync")
    protected Boolean useSync;

    @JsonProperty("proactiveConnectionRegionsCount")
    protected Integer proactiveConnectionRegionsCount;

    @JsonProperty("aggressiveWarmupDuration")
    protected String aggressiveWarmupDuration;

    // ======== Connection params ========

    @JsonProperty("connectionMode")
    protected String connectionMode;

    @JsonProperty("consistencyLevel")
    protected String consistencyLevel;

    @JsonProperty("maxConnectionPoolSize")
    protected Integer maxConnectionPoolSize;

    @JsonProperty("connectionSharingAcrossClientsEnabled")
    protected Boolean connectionSharingAcrossClientsEnabled;

    @JsonProperty("http2Enabled")
    protected Boolean http2Enabled;

    @JsonProperty("http2MaxConcurrentStreams")
    protected Integer http2MaxConcurrentStreams;

    @JsonProperty("preferredRegionsList")
    protected String preferredRegionsList;

    @JsonProperty("manageDatabase")
    protected Boolean manageDatabase;

    @JsonProperty("isManagedIdentityRequired")
    protected Boolean isManagedIdentityRequired;

    @JsonProperty("applicationName")
    protected String applicationName;

    // ======== Getters (with defaults for fields that may be null) ========

    public String getOperation() { return operation; }

    public Operation getOperationType() {
        if (operation == null) return Operation.WriteThroughput;
        Operation op = Operation.fromString(operation);
        return op != null ? op : Operation.WriteThroughput;
    }

    public int getNumberOfPreCreatedDocuments() { return numberOfPreCreatedDocuments != null ? numberOfPreCreatedDocuments : 1000; }
    public int getThroughput() { return throughput != null ? throughput : 100000; }
    public int getDocumentDataFieldSize() { return documentDataFieldSize != null ? documentDataFieldSize : 20; }
    public int getDocumentDataFieldCount() { return documentDataFieldCount != null ? documentDataFieldCount : 5; }
    public boolean isContentResponseOnWriteEnabled() { return contentResponseOnWriteEnabled != null ? contentResponseOnWriteEnabled : true; }
    public boolean isDisablePassingPartitionKeyAsOptionOnWrite() { return disablePassingPartitionKeyAsOptionOnWrite != null && disablePassingPartitionKeyAsOptionOnWrite; }
    public boolean isUseNameLink() { return useNameLink != null && useNameLink; }
    public int getTupleSize() { return tupleSize != null ? tupleSize : 1; }

    public Duration getPointOperationThreshold() {
        return pointOperationLatencyThresholdMs != null && pointOperationLatencyThresholdMs >= 0
            ? Duration.ofMillis(pointOperationLatencyThresholdMs) : Duration.ofDays(300);
    }

    public Duration getNonPointOperationThreshold() {
        return nonPointOperationLatencyThresholdMs != null && nonPointOperationLatencyThresholdMs >= 0
            ? Duration.ofMillis(nonPointOperationLatencyThresholdMs) : Duration.ofDays(300);
    }

    public boolean isRegionScopedSessionContainerEnabled() { return isRegionScopedSessionContainerEnabled != null && isRegionScopedSessionContainerEnabled; }
    public boolean isDefaultLog4jLoggerEnabled() { return isDefaultLog4jLoggerEnabled != null && isDefaultLog4jLoggerEnabled; }

    public Duration getDiagnosticsThresholdDuration() {
        return diagnosticsThresholdDuration == null ? Duration.ofSeconds(60) : Duration.parse(diagnosticsThresholdDuration);
    }

    public Duration getSparsityWaitTime() {
        return sparsityWaitTime == null ? null : Duration.parse(sparsityWaitTime);
    }

    public boolean isProactiveConnectionManagementEnabled() { return isProactiveConnectionManagementEnabled != null && isProactiveConnectionManagementEnabled; }
    public boolean isUseUnWarmedUpContainer() { return isUseUnWarmedUpContainer != null && isUseUnWarmedUpContainer; }
    public int getProactiveConnectionRegionsCount() { return proactiveConnectionRegionsCount != null ? proactiveConnectionRegionsCount : 1; }

    public Duration getAggressiveWarmupDuration() {
        return aggressiveWarmupDuration == null ? Duration.ZERO : Duration.parse(aggressiveWarmupDuration);
    }

    public int getNumberOfCollectionForCtl() { return numberOfCollectionForCtl != null ? numberOfCollectionForCtl : 4; }
    public String getReadWriteQueryReadManyPct() { return readWriteQueryReadManyPct != null ? readWriteQueryReadManyPct : "90,8,1,1"; }
    public int getEncryptedStringFieldCount() { return encryptedStringFieldCount != null ? encryptedStringFieldCount : 1; }
    public int getEncryptedLongFieldCount() { return encryptedLongFieldCount != null ? encryptedLongFieldCount : 0; }
    public int getEncryptedDoubleFieldCount() { return encryptedDoubleFieldCount != null ? encryptedDoubleFieldCount : 0; }
    public boolean isEncryptionEnabled() { return encryptionEnabled != null && encryptionEnabled; }
    public int getBulkloadBatchSize() { return bulkloadBatchSize != null ? bulkloadBatchSize : 200000; }
    public String getTestScenario() { return testScenario != null ? testScenario : "GET"; }
    public boolean isSync() { return useSync != null && useSync; }

    public ConnectionMode getConnectionMode() {
        return connectionMode == null ? ConnectionMode.DIRECT : ConnectionMode.valueOf(connectionMode.toUpperCase());
    }

    public ConsistencyLevel getConsistencyLevel() {
        if (consistencyLevel == null) return ConsistencyLevel.SESSION;
        for (ConsistencyLevel level : ConsistencyLevel.values()) {
            if (level.toString().equalsIgnoreCase(consistencyLevel) || level.name().equalsIgnoreCase(consistencyLevel)) {
                return level;
            }
        }
        return ConsistencyLevel.valueOf(consistencyLevel.toUpperCase());
    }

    public int getMaxConnectionPoolSize() { return maxConnectionPoolSize != null ? maxConnectionPoolSize : 1000; }
    public boolean isConnectionSharingAcrossClientsEnabled() { return connectionSharingAcrossClientsEnabled != null && connectionSharingAcrossClientsEnabled; }
    public boolean isHttp2Enabled() { return http2Enabled != null && http2Enabled; }
    public Integer getHttp2MaxConcurrentStreams() { return http2MaxConcurrentStreams; }

    public List<String> getPreferredRegionsList() {
        if (preferredRegionsList == null || preferredRegionsList.isEmpty()) return null;
        List<String> regions = new ArrayList<>();
        for (String r : preferredRegionsList.split(",")) { regions.add(r.trim()); }
        return regions;
    }

    public boolean shouldManageDatabase() { return manageDatabase != null ? manageDatabase : false; }
    public String getApplicationName() { return applicationName != null ? applicationName : ""; }
    public boolean isManagedIdentityRequired() { return isManagedIdentityRequired != null && isManagedIdentityRequired; }

    /**
     * Apply these defaults to a tenant config, filling in only fields that are null on the tenant.
     * Works via direct protected field access since TenantWorkloadConfig extends this class.
     */
    void applyTo(TenantWorkloadConfig tenant) {
        if (operation != null && tenant.operation == null) tenant.operation = operation;
        if (numberOfPreCreatedDocuments != null && tenant.numberOfPreCreatedDocuments == null) tenant.numberOfPreCreatedDocuments = numberOfPreCreatedDocuments;
        if (throughput != null && tenant.throughput == null) tenant.throughput = throughput;
        if (documentDataFieldSize != null && tenant.documentDataFieldSize == null) tenant.documentDataFieldSize = documentDataFieldSize;
        if (documentDataFieldCount != null && tenant.documentDataFieldCount == null) tenant.documentDataFieldCount = documentDataFieldCount;
        if (contentResponseOnWriteEnabled != null && tenant.contentResponseOnWriteEnabled == null) tenant.contentResponseOnWriteEnabled = contentResponseOnWriteEnabled;
        if (disablePassingPartitionKeyAsOptionOnWrite != null && tenant.disablePassingPartitionKeyAsOptionOnWrite == null) tenant.disablePassingPartitionKeyAsOptionOnWrite = disablePassingPartitionKeyAsOptionOnWrite;
        if (useNameLink != null && tenant.useNameLink == null) tenant.useNameLink = useNameLink;
        if (tupleSize != null && tenant.tupleSize == null) tenant.tupleSize = tupleSize;
        if (pointOperationLatencyThresholdMs != null && tenant.pointOperationLatencyThresholdMs == null) tenant.pointOperationLatencyThresholdMs = pointOperationLatencyThresholdMs;
        if (nonPointOperationLatencyThresholdMs != null && tenant.nonPointOperationLatencyThresholdMs == null) tenant.nonPointOperationLatencyThresholdMs = nonPointOperationLatencyThresholdMs;
        if (isRegionScopedSessionContainerEnabled != null && tenant.isRegionScopedSessionContainerEnabled == null) tenant.isRegionScopedSessionContainerEnabled = isRegionScopedSessionContainerEnabled;
        if (isDefaultLog4jLoggerEnabled != null && tenant.isDefaultLog4jLoggerEnabled == null) tenant.isDefaultLog4jLoggerEnabled = isDefaultLog4jLoggerEnabled;
        if (diagnosticsThresholdDuration != null && tenant.diagnosticsThresholdDuration == null) tenant.diagnosticsThresholdDuration = diagnosticsThresholdDuration;
        if (sparsityWaitTime != null && tenant.sparsityWaitTime == null) tenant.sparsityWaitTime = sparsityWaitTime;
        if (isProactiveConnectionManagementEnabled != null && tenant.isProactiveConnectionManagementEnabled == null) tenant.isProactiveConnectionManagementEnabled = isProactiveConnectionManagementEnabled;
        if (isUseUnWarmedUpContainer != null && tenant.isUseUnWarmedUpContainer == null) tenant.isUseUnWarmedUpContainer = isUseUnWarmedUpContainer;
        if (numberOfCollectionForCtl != null && tenant.numberOfCollectionForCtl == null) tenant.numberOfCollectionForCtl = numberOfCollectionForCtl;
        if (readWriteQueryReadManyPct != null && tenant.readWriteQueryReadManyPct == null) tenant.readWriteQueryReadManyPct = readWriteQueryReadManyPct;
        if (encryptedStringFieldCount != null && tenant.encryptedStringFieldCount == null) tenant.encryptedStringFieldCount = encryptedStringFieldCount;
        if (encryptedLongFieldCount != null && tenant.encryptedLongFieldCount == null) tenant.encryptedLongFieldCount = encryptedLongFieldCount;
        if (encryptedDoubleFieldCount != null && tenant.encryptedDoubleFieldCount == null) tenant.encryptedDoubleFieldCount = encryptedDoubleFieldCount;
        if (encryptionEnabled != null && tenant.encryptionEnabled == null) tenant.encryptionEnabled = encryptionEnabled;
        if (bulkloadBatchSize != null && tenant.bulkloadBatchSize == null) tenant.bulkloadBatchSize = bulkloadBatchSize;
        if (testScenario != null && tenant.testScenario == null) tenant.testScenario = testScenario;
        if (environment != null && tenant.environment == null) tenant.environment = environment;
        if (useSync != null && tenant.useSync == null) tenant.useSync = useSync;
        if (proactiveConnectionRegionsCount != null && tenant.proactiveConnectionRegionsCount == null) tenant.proactiveConnectionRegionsCount = proactiveConnectionRegionsCount;
        if (aggressiveWarmupDuration != null && tenant.aggressiveWarmupDuration == null) tenant.aggressiveWarmupDuration = aggressiveWarmupDuration;
        if (connectionMode != null && tenant.connectionMode == null) tenant.connectionMode = connectionMode;
        if (consistencyLevel != null && tenant.consistencyLevel == null) tenant.consistencyLevel = consistencyLevel;
        if (maxConnectionPoolSize != null && tenant.maxConnectionPoolSize == null) tenant.maxConnectionPoolSize = maxConnectionPoolSize;
        if (connectionSharingAcrossClientsEnabled != null && tenant.connectionSharingAcrossClientsEnabled == null) tenant.connectionSharingAcrossClientsEnabled = connectionSharingAcrossClientsEnabled;
        if (http2Enabled != null && tenant.http2Enabled == null) tenant.http2Enabled = http2Enabled;
        if (http2MaxConcurrentStreams != null && tenant.http2MaxConcurrentStreams == null) tenant.http2MaxConcurrentStreams = http2MaxConcurrentStreams;
        if (preferredRegionsList != null && tenant.preferredRegionsList == null) tenant.preferredRegionsList = preferredRegionsList;
        if (manageDatabase != null && tenant.manageDatabase == null) tenant.manageDatabase = manageDatabase;
        if (isManagedIdentityRequired != null && tenant.isManagedIdentityRequired == null) tenant.isManagedIdentityRequired = isManagedIdentityRequired;
        if (applicationName != null && tenant.applicationName == null) tenant.applicationName = applicationName;
    }
}
