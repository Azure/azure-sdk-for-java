// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Fully-resolved configuration for a single tenant workload.
 * Contains account connection info, AAD auth, workload params, and connection
 * settings. Each instance is the effective config after merging
 * tenantDefaults with per-tenant overrides at parse time.
 *
 * <p>This is the single config object passed to {@link AsyncBenchmark} --
 * no intermediate Configuration conversion needed.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantWorkloadConfig {

    /**
     * Benchmark execution environment.
     */
    public enum Environment {
        Daily,   // CTL environment where we run the workload for a fixed number of hours
        Staging  // CTL environment where the workload runs as a long running job
    }

    private static final Logger logger = LoggerFactory.getLogger(TenantWorkloadConfig.class);

    // ======== Meter name constants (used by AsyncBenchmark + CosmosTotalResultReporter) ========


    public static final String DEFAULT_PARTITION_KEY_PATH = "/pk";

    // ======== Account connection ========

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

    // ======== AAD auth ========

    @JsonProperty("aadLoginEndpoint")
    private String aadLoginEndpoint;

    @JsonProperty("aadTenantId")
    private String aadTenantId;

    @JsonProperty("aadManagedIdentityClientId")
    private String aadManagedIdentityClientId;

    @JsonProperty("isManagedIdentityRequired")
    private Boolean isManagedIdentityRequired;

    // ======== Workload params ========

    @JsonProperty("operation")
    private String operation;

    private static final int INGESTION_RETRY_CONCURRENCY = 10;

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

    /**
     * Per-client flag: controls region-scoped session capturing on the CosmosClientBuilder.
     * Unlike JVM-global system properties (circuit breaker, PPAF, minConnectionPoolSize),
     * this is set per-client via {@code CosmosClientBuilderAccessor.setRegionScopedSessionCapturingEnabled}
     * and can genuinely differ per tenant.
     */
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

    @JsonProperty("applicationName")
    private String applicationName;

    // ======== Lifecycle (set by orchestrator, not from JSON) ========

    private boolean suppressCleanup = false;

    /** Cosmos SDK micrometer registry (set by orchestrator, not from JSON). */
    private transient MeterRegistry cosmosMicrometerRegistry;

    public TenantWorkloadConfig() {}

    /**
     * Apply defaults from a TenantDefaultConfig. Only fills in fields that are null on this tenant.
     */
    void applyDefaults(TenantDefaultConfig defaults) {
        if (defaults == null) return;
        if (operation == null) operation = defaults.getOperation();
        if (numberOfPreCreatedDocuments == null) numberOfPreCreatedDocuments = defaults.getNumberOfPreCreatedDocuments();
        if (throughput == null) throughput = defaults.getThroughput();
        if (documentDataFieldSize == null) documentDataFieldSize = defaults.getDocumentDataFieldSize();
        if (documentDataFieldCount == null) documentDataFieldCount = defaults.getDocumentDataFieldCount();
        if (contentResponseOnWriteEnabled == null) contentResponseOnWriteEnabled = defaults.getContentResponseOnWriteEnabled();
        if (disablePassingPartitionKeyAsOptionOnWrite == null) disablePassingPartitionKeyAsOptionOnWrite = defaults.getDisablePassingPartitionKeyAsOptionOnWrite();
        if (useNameLink == null) useNameLink = defaults.getUseNameLink();
        if (tupleSize == null) tupleSize = defaults.getTupleSize();
        if (pointOperationLatencyThresholdMs == null) pointOperationLatencyThresholdMs = defaults.getPointOperationLatencyThresholdMs();
        if (nonPointOperationLatencyThresholdMs == null) nonPointOperationLatencyThresholdMs = defaults.getNonPointOperationLatencyThresholdMs();
        if (isRegionScopedSessionContainerEnabled == null) isRegionScopedSessionContainerEnabled = defaults.getIsRegionScopedSessionContainerEnabled();
        if (isDefaultLog4jLoggerEnabled == null) isDefaultLog4jLoggerEnabled = defaults.getIsDefaultLog4jLoggerEnabled();
        if (diagnosticsThresholdDuration == null) diagnosticsThresholdDuration = defaults.getDiagnosticsThresholdDuration();
        if (sparsityWaitTime == null) sparsityWaitTime = defaults.getSparsityWaitTime();
        if (isProactiveConnectionManagementEnabled == null) isProactiveConnectionManagementEnabled = defaults.getIsProactiveConnectionManagementEnabled();
        if (isUseUnWarmedUpContainer == null) isUseUnWarmedUpContainer = defaults.getIsUseUnWarmedUpContainer();
        if (numberOfCollectionForCtl == null) numberOfCollectionForCtl = defaults.getNumberOfCollectionForCtl();
        if (readWriteQueryReadManyPct == null) readWriteQueryReadManyPct = defaults.getReadWriteQueryReadManyPct();
        if (encryptedStringFieldCount == null) encryptedStringFieldCount = defaults.getEncryptedStringFieldCount();
        if (encryptedLongFieldCount == null) encryptedLongFieldCount = defaults.getEncryptedLongFieldCount();
        if (encryptedDoubleFieldCount == null) encryptedDoubleFieldCount = defaults.getEncryptedDoubleFieldCount();
        if (encryptionEnabled == null) encryptionEnabled = defaults.getEncryptionEnabled();
        if (bulkloadBatchSize == null) bulkloadBatchSize = defaults.getBulkloadBatchSize();
        if (testScenario == null) testScenario = defaults.getTestScenario();
        if (environment == null) environment = defaults.getEnvironment();
        if (useSync == null) useSync = defaults.getUseSync();
        if (proactiveConnectionRegionsCount == null) proactiveConnectionRegionsCount = defaults.getProactiveConnectionRegionsCount();
        if (aggressiveWarmupDuration == null) aggressiveWarmupDuration = defaults.getAggressiveWarmupDuration();
        if (connectionMode == null) connectionMode = defaults.getConnectionMode();
        if (consistencyLevel == null) consistencyLevel = defaults.getConsistencyLevel();
        if (maxConnectionPoolSize == null) maxConnectionPoolSize = defaults.getMaxConnectionPoolSize();
        if (connectionSharingAcrossClientsEnabled == null) connectionSharingAcrossClientsEnabled = defaults.getConnectionSharingAcrossClientsEnabled();
        if (http2Enabled == null) http2Enabled = defaults.getHttp2Enabled();
        if (http2MaxConcurrentStreams == null) http2MaxConcurrentStreams = defaults.getHttp2MaxConcurrentStreams();
        if (preferredRegionsList == null) preferredRegionsList = defaults.getPreferredRegionsList();
        if (manageDatabase == null) manageDatabase = defaults.getManageDatabase();
        if (isManagedIdentityRequired == null) isManagedIdentityRequired = defaults.getIsManagedIdentityRequired();
    }



    // ======== Getters (with defaults for fields that may be null) ========

    public String getId() { return id; }
    public String getServiceEndpoint() { return serviceEndpoint; }
    public String getMasterKey() { return masterKey; }
    public String getDatabaseId() { return databaseId; }
    public String getContainerId() { return containerId; }
    public String getAadLoginEndpoint() { return aadLoginEndpoint; }
    public String getAadTenantId() { return aadTenantId; }
    public String getAadManagedIdentityClientId() { return aadManagedIdentityClientId; }

    public boolean isManagedIdentityRequired() {
        return isManagedIdentityRequired != null && isManagedIdentityRequired;
    }

    public String getOperation() { return operation; }

    public Operation getOperationType() {
        if (operation == null) return Operation.WriteThroughput;
        Operation op = Operation.fromString(operation);
        return op != null ? op : Operation.WriteThroughput;
    }

    public int getIngestionRetryConcurrency() { return INGESTION_RETRY_CONCURRENCY; }
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
            ? Duration.ofMillis(pointOperationLatencyThresholdMs)
            : Duration.ofDays(300);
    }

    public Duration getNonPointOperationThreshold() {
        return nonPointOperationLatencyThresholdMs != null && nonPointOperationLatencyThresholdMs >= 0
            ? Duration.ofMillis(nonPointOperationLatencyThresholdMs)
            : Duration.ofDays(300);
    }

    public boolean isRegionScopedSessionContainerEnabled() {
        return isRegionScopedSessionContainerEnabled != null && isRegionScopedSessionContainerEnabled;
    }

    public boolean isDefaultLog4jLoggerEnabled() {
        return isDefaultLog4jLoggerEnabled != null && isDefaultLog4jLoggerEnabled;
    }

    public Duration getDiagnosticsThresholdDuration() {
        if (diagnosticsThresholdDuration == null) return Duration.ofSeconds(60);
        return Duration.parse(diagnosticsThresholdDuration);
    }

    public Duration getSparsityWaitTime() {
        if (sparsityWaitTime == null) return null;
        return Duration.parse(sparsityWaitTime);
    }

    public boolean isProactiveConnectionManagementEnabled() {
        return isProactiveConnectionManagementEnabled != null && isProactiveConnectionManagementEnabled;
    }

    public boolean isUseUnWarmedUpContainer() {
        return isUseUnWarmedUpContainer != null && isUseUnWarmedUpContainer;
    }

    public int getProactiveConnectionRegionsCount() {
        return proactiveConnectionRegionsCount != null ? proactiveConnectionRegionsCount : 1;
    }

    public Duration getAggressiveWarmupDuration() {
        if (aggressiveWarmupDuration == null) return Duration.ZERO;
        return Duration.parse(aggressiveWarmupDuration);
    }

    public int getNumberOfCollectionForCtl() { return numberOfCollectionForCtl != null ? numberOfCollectionForCtl : 4; }
    public String getReadWriteQueryReadManyPct() { return readWriteQueryReadManyPct != null ? readWriteQueryReadManyPct : "90,8,1,1"; }
    public int getEncryptedStringFieldCount() { return encryptedStringFieldCount != null ? encryptedStringFieldCount : 1; }
    public int getEncryptedLongFieldCount() { return encryptedLongFieldCount != null ? encryptedLongFieldCount : 0; }
    public int getEncryptedDoubleFieldCount() { return encryptedDoubleFieldCount != null ? encryptedDoubleFieldCount : 0; }
    public boolean isEncryptionEnabled() { return encryptionEnabled != null && encryptionEnabled; }
    public int getBulkloadBatchSize() { return bulkloadBatchSize != null ? bulkloadBatchSize : 200000; }
    public String getTestScenario() { return testScenario != null ? testScenario : "GET"; }
    public Environment getEnvironment() {
        if (environment == null) return Environment.Daily;
        return Environment.valueOf(environment);
    }
    public boolean isSync() { return useSync != null && useSync; }

    public ConnectionMode getConnectionMode() {
        if (connectionMode == null) return ConnectionMode.DIRECT;
        return ConnectionMode.valueOf(connectionMode.toUpperCase());
    }

    public ConsistencyLevel getConsistencyLevel() {
        if (consistencyLevel == null) return ConsistencyLevel.SESSION;
        for (ConsistencyLevel level : ConsistencyLevel.values()) {
            if (level.toString().equalsIgnoreCase(consistencyLevel)
                || level.name().equalsIgnoreCase(consistencyLevel)) {
                return level;
            }
        }
        return ConsistencyLevel.valueOf(consistencyLevel.toUpperCase());
    }

    public int getMaxConnectionPoolSize() { return maxConnectionPoolSize != null ? maxConnectionPoolSize : 1000; }

    public boolean isConnectionSharingAcrossClientsEnabled() {
        return connectionSharingAcrossClientsEnabled != null && connectionSharingAcrossClientsEnabled;
    }

    public boolean isHttp2Enabled() {
        return http2Enabled != null && http2Enabled;
    }

    public Integer getHttp2MaxConcurrentStreams() {
        return http2MaxConcurrentStreams;
    }

    public List<String> getPreferredRegionsList() {
        if (preferredRegionsList == null || preferredRegionsList.isEmpty()) return null;
        List<String> regions = new ArrayList<>();
        for (String r : preferredRegionsList.split(",")) {
            regions.add(r.trim());
        }
        return regions;
    }

    public boolean shouldManageDatabase() { return manageDatabase != null ? manageDatabase : false; }
    public String getApplicationName() { return applicationName != null ? applicationName : ""; }
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
    public void setConnectionSharingAcrossClientsEnabled(boolean connectionSharingAcrossClientsEnabled) { this.connectionSharingAcrossClientsEnabled = connectionSharingAcrossClientsEnabled; }
    public void setNumberOfPreCreatedDocuments(int numberOfPreCreatedDocuments) { this.numberOfPreCreatedDocuments = numberOfPreCreatedDocuments; }
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
