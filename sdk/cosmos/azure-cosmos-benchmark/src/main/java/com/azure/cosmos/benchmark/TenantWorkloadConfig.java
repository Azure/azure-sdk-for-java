// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Fully-resolved configuration for a single tenant workload.
 * Contains account connection info, AAD auth, workload params, and connection
 * settings. Each instance is the effective config after merging
 * globalDefaults with per-tenant overrides at parse time.
 *
 * <p>This is the single config object passed to {@link AsyncBenchmark} --
 * no intermediate Configuration conversion needed.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantWorkloadConfig {

    private static final Logger logger = LoggerFactory.getLogger(TenantWorkloadConfig.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // ======== Meter name constants (used by AsyncBenchmark + CosmosTotalResultReporter) ========

    public static final String SUCCESS_COUNTER_METER_NAME = "#Successful Operations";
    public static final String FAILURE_COUNTER_METER_NAME = "#Unsuccessful Operations";
    public static final String LATENCY_METER_NAME = "Latency";
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

    @JsonProperty("concurrency")
    private Integer concurrency;

    @JsonProperty("numberOfOperations")
    private Integer numberOfOperations;

    @JsonProperty("numberOfPreCreatedDocuments")
    private Integer numberOfPreCreatedDocuments;

    @JsonProperty("throughput")
    private Integer throughput;

    @JsonProperty("skipWarmUpOperations")
    private Integer skipWarmUpOperations;

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

    @JsonProperty("maxRunningTimeDuration")
    private String maxRunningTimeDuration;

    @JsonProperty("sparsityWaitTime")
    private String sparsityWaitTime;

    @JsonProperty("isProactiveConnectionManagementEnabled")
    private Boolean isProactiveConnectionManagementEnabled;

    @JsonProperty("isUseUnWarmedUpContainer")
    private Boolean isUseUnWarmedUpContainer;

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

    public int getConcurrency() { return concurrency != null ? concurrency : 1000; }
    public int getNumberOfOperations() { return numberOfOperations != null ? numberOfOperations : 100000; }
    public int getNumberOfPreCreatedDocuments() { return numberOfPreCreatedDocuments != null ? numberOfPreCreatedDocuments : 1000; }
    public int getThroughput() { return throughput != null ? throughput : 100000; }
    public int getSkipWarmUpOperations() { return skipWarmUpOperations != null ? skipWarmUpOperations : 0; }
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

    public Duration getMaxRunningTimeDuration() {
        if (maxRunningTimeDuration == null) return null;
        return Duration.parse(maxRunningTimeDuration);
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

    public ConnectionMode getConnectionMode() {
        if (connectionMode == null) return ConnectionMode.DIRECT;
        return ConnectionMode.valueOf(connectionMode.toUpperCase());
    }

    public ConsistencyLevel getConsistencyLevel() {
        if (consistencyLevel == null) return ConsistencyLevel.SESSION;
        return ConsistencyLevel.valueOf(consistencyLevel.toUpperCase());
    }

    public int getMaxConnectionPoolSize() { return maxConnectionPoolSize != null ? maxConnectionPoolSize : 1000; }

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
    public void setConcurrency(int concurrency) { this.concurrency = concurrency; }
    public void setNumberOfOperations(int numberOfOperations) { this.numberOfOperations = numberOfOperations; }
    public void setConnectionMode(String connectionMode) { this.connectionMode = connectionMode; }
    public void setConsistencyLevel(String consistencyLevel) { this.consistencyLevel = consistencyLevel; }
    public void setMaxConnectionPoolSize(int maxConnectionPoolSize) { this.maxConnectionPoolSize = maxConnectionPoolSize; }
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
            ", concurrency=" + concurrency +
            ", connectionMode=" + connectionMode +
            '}';
    }

    // ======== Apply defaults from a map ========

    private void applyMap(Map<String, String> map, boolean overwrite) {
        if (map == null) return;
        for (Map.Entry<String, String> e : map.entrySet()) {
            applyField(e.getKey(), e.getValue(), overwrite);
        }
    }

    private void applyField(String key, String value, boolean overwrite) {
        if (value == null) return;
        try {
            switch (key) {
                case "serviceEndpoint":
                    if (overwrite || serviceEndpoint == null) serviceEndpoint = value; break;
                case "masterKey":
                    if (overwrite || masterKey == null) masterKey = value; break;
                case "databaseId":
                    if (overwrite || databaseId == null) databaseId = value; break;
                case "collectionId":
                case "containerId":
                    if (overwrite || containerId == null) containerId = value; break;
                case "applicationName":
                    if (overwrite || applicationName == null) applicationName = value; break;
                case "aadLoginEndpoint":
                    if (overwrite || aadLoginEndpoint == null) aadLoginEndpoint = value; break;
                case "aadTenantId":
                    if (overwrite || aadTenantId == null) aadTenantId = value; break;
                case "aadManagedIdentityClientId":
                    if (overwrite || aadManagedIdentityClientId == null) aadManagedIdentityClientId = value; break;
                case "isManagedIdentityRequired":
                    if (overwrite || isManagedIdentityRequired == null) isManagedIdentityRequired = Boolean.parseBoolean(value); break;
                case "operation":
                    if (overwrite || operation == null) operation = value; break;
                case "concurrency":
                    if (overwrite || concurrency == null) concurrency = Integer.parseInt(value); break;
                case "numberOfOperations":
                    if (overwrite || numberOfOperations == null) numberOfOperations = Integer.parseInt(value); break;
                case "numberOfPreCreatedDocuments":
                    if (overwrite || numberOfPreCreatedDocuments == null) numberOfPreCreatedDocuments = Integer.parseInt(value); break;
                case "skipWarmUpOperations":
                    if (overwrite || skipWarmUpOperations == null) skipWarmUpOperations = Integer.parseInt(value); break;
                case "throughput":
                    if (overwrite || throughput == null) throughput = Integer.parseInt(value); break;
                case "documentDataFieldSize":
                    if (overwrite || documentDataFieldSize == null) documentDataFieldSize = Integer.parseInt(value); break;
                case "documentDataFieldCount":
                    if (overwrite || documentDataFieldCount == null) documentDataFieldCount = Integer.parseInt(value); break;
                case "contentResponseOnWriteEnabled":
                    if (overwrite || contentResponseOnWriteEnabled == null) contentResponseOnWriteEnabled = Boolean.parseBoolean(value); break;
                case "disablePassingPartitionKeyAsOptionOnWrite":
                    if (overwrite || disablePassingPartitionKeyAsOptionOnWrite == null) disablePassingPartitionKeyAsOptionOnWrite = Boolean.parseBoolean(value); break;
                case "useNameLink":
                    if (overwrite || useNameLink == null) useNameLink = Boolean.parseBoolean(value); break;
                case "tupleSize":
                    if (overwrite || tupleSize == null) tupleSize = Integer.parseInt(value); break;
                case "pointOperationLatencyThresholdMs":
                    if (overwrite || pointOperationLatencyThresholdMs == null) pointOperationLatencyThresholdMs = Integer.parseInt(value); break;
                case "nonPointOperationLatencyThresholdMs":
                    if (overwrite || nonPointOperationLatencyThresholdMs == null) nonPointOperationLatencyThresholdMs = Integer.parseInt(value); break;
                case "isRegionScopedSessionContainerEnabled":
                    if (overwrite || isRegionScopedSessionContainerEnabled == null) isRegionScopedSessionContainerEnabled = Boolean.parseBoolean(value); break;
                case "isDefaultLog4jLoggerEnabled":
                    if (overwrite || isDefaultLog4jLoggerEnabled == null) isDefaultLog4jLoggerEnabled = Boolean.parseBoolean(value); break;
                case "maxRunningTimeDuration":
                    if (overwrite || maxRunningTimeDuration == null) maxRunningTimeDuration = value; break;
                case "sparsityWaitTime":
                    if (overwrite || sparsityWaitTime == null) sparsityWaitTime = value; break;
                case "isProactiveConnectionManagementEnabled":
                    if (overwrite || isProactiveConnectionManagementEnabled == null) isProactiveConnectionManagementEnabled = Boolean.parseBoolean(value); break;
                case "isUseUnWarmedUpContainer":
                    if (overwrite || isUseUnWarmedUpContainer == null) isUseUnWarmedUpContainer = Boolean.parseBoolean(value); break;
                case "proactiveConnectionRegionsCount":
                    if (overwrite || proactiveConnectionRegionsCount == null) proactiveConnectionRegionsCount = Integer.parseInt(value); break;
                case "aggressiveWarmupDuration":
                    if (overwrite || aggressiveWarmupDuration == null) aggressiveWarmupDuration = value; break;
                case "connectionMode":
                    if (overwrite || connectionMode == null) connectionMode = value; break;
                case "consistencyLevel":
                    if (overwrite || consistencyLevel == null) consistencyLevel = value; break;
                case "maxConnectionPoolSize":
                    if (overwrite || maxConnectionPoolSize == null) maxConnectionPoolSize = Integer.parseInt(value); break;
                case "preferredRegionsList":
                    if (overwrite || preferredRegionsList == null) preferredRegionsList = value; break;
                case "manageDatabase":
                    if (overwrite || manageDatabase == null) manageDatabase = Boolean.parseBoolean(value); break;
                // JVM-global properties (minConnectionPoolSizePerEndpoint, isPartitionLevelCircuitBreakerEnabled,
                // isPerPartitionAutomaticFailoverRequired) are handled in BenchmarkConfig, not per-tenant.
                case "minConnectionPoolSizePerEndpoint":
                case "isPartitionLevelCircuitBreakerEnabled":
                case "isPerPartitionAutomaticFailoverRequired":
                    break;
                default:
                    logger.debug("Unknown config key '{}' (value: {})", key, value);
                    break;
            }
        } catch (Exception ex) {
            logger.warn("Failed to apply '{}' = '{}': {}", key, value, ex.getMessage());
        }
    }

    // ======== Factory from Configuration (for tests and legacy paths) ========

    /**
     * Build a TenantWorkloadConfig from a legacy Configuration object.
     * Used by tests and CLI paths that still parse via JCommander.
     */
    public static TenantWorkloadConfig fromConfiguration(Configuration cfg) {
        TenantWorkloadConfig t = new TenantWorkloadConfig();
        t.id = "cli-tenant";
        t.serviceEndpoint = cfg.getServiceEndpoint();
        t.masterKey = cfg.getMasterKey();
        t.databaseId = cfg.getDatabaseId();
        t.containerId = cfg.getCollectionId();
        t.operation = cfg.getOperationType().name();
        t.concurrency = cfg.getConcurrency();
        t.numberOfOperations = cfg.getNumberOfOperations();
        t.numberOfPreCreatedDocuments = cfg.getNumberOfPreCreatedDocuments();
        t.throughput = cfg.getThroughput();
        t.skipWarmUpOperations = cfg.getSkipWarmUpOperations();
        t.documentDataFieldSize = cfg.getDocumentDataFieldSize();
        t.documentDataFieldCount = cfg.getDocumentDataFieldCount();
        t.contentResponseOnWriteEnabled = cfg.isContentResponseOnWriteEnabled();
        t.disablePassingPartitionKeyAsOptionOnWrite = cfg.isDisablePassingPartitionKeyAsOptionOnWrite();
        t.useNameLink = cfg.isUseNameLink();
        t.connectionMode = cfg.getConnectionMode().name();
        t.consistencyLevel = cfg.getConsistencyLevel().name();
        t.maxConnectionPoolSize = cfg.getMaxConnectionPoolSize();
        t.manageDatabase = cfg.shouldManageDatabase();
        t.applicationName = cfg.getApplicationName();
        t.isManagedIdentityRequired = cfg.isManagedIdentityRequired();

        // AAD auth
        t.aadLoginEndpoint = cfg.getInstanceAadLoginEndpoint();
        t.aadTenantId = cfg.getInstanceAadTenantId();
        t.aadManagedIdentityClientId = cfg.getInstanceAadManagedIdentityClientId();

        // Workload details
        t.tupleSize = cfg.getTupleSize();
        if (cfg.getMaxRunningTimeDuration() != null) {
            t.maxRunningTimeDuration = cfg.getMaxRunningTimeDuration().toString();
        }
        if (cfg.getSparsityWaitTime() != null) {
            t.sparsityWaitTime = cfg.getSparsityWaitTime().toString();
        }

        // Diagnostics thresholds
        t.pointOperationLatencyThresholdMs = cfg.getPointOperationThreshold().toMillis() < Duration.ofDays(100).toMillis()
            ? (int) cfg.getPointOperationThreshold().toMillis() : null;
        t.nonPointOperationLatencyThresholdMs = cfg.getNonPointOperationThreshold().toMillis() < Duration.ofDays(100).toMillis()
            ? (int) cfg.getNonPointOperationThreshold().toMillis() : null;

        // Feature flags
        t.isRegionScopedSessionContainerEnabled = cfg.isRegionScopedSessionContainerEnabled();
        t.isDefaultLog4jLoggerEnabled = cfg.isDefaultLog4jLoggerEnabled();

        // Proactive connection management
        t.isProactiveConnectionManagementEnabled = cfg.isProactiveConnectionManagementEnabled();
        t.isUseUnWarmedUpContainer = cfg.isUseUnWarmedUpContainer();
        t.proactiveConnectionRegionsCount = cfg.getProactiveConnectionRegionsCount();
        if (cfg.getAggressiveWarmupDuration() != null) {
            t.aggressiveWarmupDuration = cfg.getAggressiveWarmupDuration().toString();
        }

        // Connection
        t.preferredRegionsList = cfg.getPreferredRegionsList() != null
            ? String.join(",", cfg.getPreferredRegionsList()) : null;

        // Note: JVM-global system properties (isPartitionLevelCircuitBreakerEnabled,
        // isPerPartitionAutomaticFailoverRequired, minConnectionPoolSizePerEndpoint)
        // are handled in BenchmarkConfig, not per-tenant.

        return t;
    }

    // ======== Static parsing ========

    public static List<TenantWorkloadConfig> parseTenantsFile(File tenantsFile) throws IOException {
        JsonNode root = OBJECT_MAPPER.readTree(tenantsFile);

        Map<String, String> globalDefaults = new HashMap<>();
        JsonNode defaultsNode = root.get("globalDefaults");
        if (defaultsNode != null && defaultsNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = defaultsNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                globalDefaults.put(entry.getKey(), entry.getValue().asText());
            }
        }

        if (!globalDefaults.isEmpty()) {
            logger.info("tenants.json globalDefaults applied to all tenants (per-tenant values take priority): {}",
                globalDefaults.keySet());
        }

        List<TenantWorkloadConfig> tenants = new ArrayList<>();

        JsonNode tenantsNode = root.get("tenants");
        if (tenantsNode != null && tenantsNode.isArray()) {
            for (JsonNode tenantNode : tenantsNode) {
                TenantWorkloadConfig tenant = OBJECT_MAPPER.treeToValue(tenantNode, TenantWorkloadConfig.class);
                tenant.applyMap(globalDefaults, false);
                validateTenantConfig(tenant);
                tenants.add(tenant);
            }
        }

        logger.info("Parsed {} tenants from {}", tenants.size(), tenantsFile.getName());
        return tenants;
    }

    private static void validateTenantConfig(TenantWorkloadConfig tenant) {
        List<String> missing = new ArrayList<>();
        if (isNullOrEmpty(tenant.getServiceEndpoint())) {
            missing.add("serviceEndpoint");
        }
        if (isNullOrEmpty(tenant.getDatabaseId())) {
            missing.add("databaseId");
        }
        if (isNullOrEmpty(tenant.getContainerId())) {
            missing.add("containerId");
        }
        if (!tenant.isManagedIdentityRequired()
            && isNullOrEmpty(tenant.getMasterKey())) {
            missing.add("masterKey (required when isManagedIdentityRequired is not true)");
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(
                "Tenant '" + tenant.getId() + "' is missing required configuration: " + missing);
        }
    }

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }
}
