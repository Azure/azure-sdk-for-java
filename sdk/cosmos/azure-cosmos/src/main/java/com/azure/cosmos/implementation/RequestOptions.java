// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.CosmosRequestOptions;
import com.azure.cosmos.models.DedicatedGatewayRequestOptions;
import com.azure.cosmos.models.IndexingDirective;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.ThroughputProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Encapsulates options that can be specified for a request issued to the Azure Cosmos DB database service.
 */
public class RequestOptions implements OverridableRequestOptions {
    private Map<String, String> customOptions;
    private List<String> preTriggerInclude;
    private List<String> postTriggerInclude;
    private IndexingDirective indexingDirective;
    private ConsistencyLevel consistencyLevel;
    private String sessionToken;
    private Integer resourceTokenExpirySeconds;
    private String offerType;
    private String ifMatchETag;
    private String ifNoneMatchETag;
    private Integer offerThroughput;
    private PartitionKey partitionkey;
    private boolean scriptLoggingEnabled;
    private boolean quotaInfoEnabled;
    private Map<String, Object> properties;
    private ThroughputProperties throughputProperties;
    private Boolean contentResponseOnWriteEnabled;
    private String filterPredicate;
    private String throughputControlGroupName;
    private OperationContextAndListenerTuple operationContextAndListenerTuple;
    private DedicatedGatewayRequestOptions dedicatedGatewayRequestOptions;
    private CosmosDiagnosticsThresholds thresholds;
    private boolean useTrackingIds;
    private String trackingId;
    private Boolean nonIdempotentWriteRetriesEnabled;
    private CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyConfig;
    private List<String> excludeRegions;

    private Supplier<CosmosDiagnosticsContext> diagnosticsCtxSupplier;
    private CosmosItemSerializer effectiveItemSerializer;

    private final AtomicReference<Runnable> markE2ETimeoutInRequestContextCallbackHook;
    private Set<String> keywordIdentifiers;

    private PartitionKeyDefinition partitionKeyDefinition;

    public RequestOptions() {

        this.markE2ETimeoutInRequestContextCallbackHook = new AtomicReference<>(null);
        this.effectiveItemSerializer = CosmosItemSerializer.DEFAULT_SERIALIZER;
    }

    public RequestOptions(RequestOptions toBeCloned) {
        this.indexingDirective = toBeCloned.indexingDirective;
        this.consistencyLevel = toBeCloned.consistencyLevel;
        this.sessionToken = toBeCloned.sessionToken;
        this.resourceTokenExpirySeconds = toBeCloned.resourceTokenExpirySeconds;
        this.offerType = toBeCloned.offerType;
        this.ifMatchETag = toBeCloned.ifMatchETag;
        this.ifNoneMatchETag = toBeCloned.ifNoneMatchETag;
        this.offerThroughput = toBeCloned.offerThroughput;
        this.partitionkey = toBeCloned.partitionkey;
        this.scriptLoggingEnabled = toBeCloned.scriptLoggingEnabled;
        this.quotaInfoEnabled = toBeCloned.quotaInfoEnabled;
        this.throughputProperties = toBeCloned.throughputProperties;
        this.contentResponseOnWriteEnabled = toBeCloned.contentResponseOnWriteEnabled;
        this.filterPredicate = toBeCloned.filterPredicate;
        this.throughputControlGroupName = toBeCloned.throughputControlGroupName;
        this.operationContextAndListenerTuple = toBeCloned.operationContextAndListenerTuple;
        this.dedicatedGatewayRequestOptions = toBeCloned.dedicatedGatewayRequestOptions;
        this.thresholds = toBeCloned.thresholds;
        this.trackingId = toBeCloned.trackingId;
        this.nonIdempotentWriteRetriesEnabled = toBeCloned.nonIdempotentWriteRetriesEnabled;
        this.endToEndOperationLatencyConfig = toBeCloned.endToEndOperationLatencyConfig;
        this.diagnosticsCtxSupplier = toBeCloned.diagnosticsCtxSupplier;
        this.markE2ETimeoutInRequestContextCallbackHook = new AtomicReference<>(null);
        this.effectiveItemSerializer= toBeCloned.effectiveItemSerializer;
        this.partitionKeyDefinition = toBeCloned.partitionKeyDefinition;

        if (toBeCloned.customOptions != null) {
            this.customOptions = new HashMap<>(toBeCloned.customOptions);
        }

        if (toBeCloned.properties != null) {
            this.properties = new HashMap<>(toBeCloned.properties);
        }

        if (toBeCloned.preTriggerInclude != null) {
            this.preTriggerInclude = new ArrayList<>(toBeCloned.preTriggerInclude);
        }

        if (toBeCloned.postTriggerInclude != null) {
            this.postTriggerInclude = new ArrayList<>(toBeCloned.postTriggerInclude);
        }

        if (toBeCloned.excludeRegions != null) {
            this.excludeRegions = new ArrayList<>(toBeCloned.excludeRegions);
        }

        if (toBeCloned.keywordIdentifiers != null) {
            this.keywordIdentifiers = new HashSet<>(toBeCloned.keywordIdentifiers);
        }
    }

    /**
     * Gets the triggers to be invoked before the operation.
     *
     * @return the triggers to be invoked before the operation.
     */
    public List<String> getPreTriggerInclude() {
        return this.preTriggerInclude;
    }

    OperationContextAndListenerTuple getOperationContextAndListenerTuple() {
        return operationContextAndListenerTuple;
    }

    public void setOperationContextAndListenerTuple (
        Object operationContextAndListenerTupleAsObject) {

        this.operationContextAndListenerTuple =
            (OperationContextAndListenerTuple)operationContextAndListenerTupleAsObject;
    }

    /**
     * Sets the triggers to be invoked before the operation.
     *
     * @param preTriggerInclude the triggers to be invoked before the operation.
     */
    public void setPreTriggerInclude(List<String> preTriggerInclude) {
        this.preTriggerInclude = preTriggerInclude;
    }

    public RequestOptions setNonIdempotentWriteRetriesEnabled(boolean enabled) {
        this.nonIdempotentWriteRetriesEnabled = enabled;

        return this;
    }

    @Override
    public Boolean getNonIdempotentWriteRetriesEnabled() {
        return this.nonIdempotentWriteRetriesEnabled;
    }

    /**
     * Gets the triggers to be invoked after the operation.
     *
     * @return the triggers to be invoked after the operation.
     */
    public List<String> getPostTriggerInclude() {
        return this.postTriggerInclude;
    }

    /**
     * Sets the triggers to be invoked after the operation.
     *
     * @param postTriggerInclude the triggers to be invoked after the operation.
     */
    public void setPostTriggerInclude(List<String> postTriggerInclude) {
        this.postTriggerInclude = postTriggerInclude;
    }

    /**
     * Gets the If-Match (ETag) associated with the request in the Azure Cosmos DB service.
     *
     * @return tthe ifMatchETag associated with the request.
     */
    public String getIfMatchETag() {
        return this.ifMatchETag;
    }

    /**
     * Sets the If-Match (ETag) associated with the request in the Azure Cosmos DB service.
     *
     * @param ifMatchETag the ifMatchETag associated with the request.
     */
    public void setIfMatchETag(String ifMatchETag) {
        this.ifMatchETag = ifMatchETag;
    }

    /**
     * Gets the If-None-Match (ETag) associated with the request in the Azure Cosmos DB service.
     *
     * @return the ifNoneMatchETag associated with the request.
     */
    public String getIfNoneMatchETag() {
        return this.ifNoneMatchETag;
    }

    /**
     * Sets the If-None-Match (ETag) associated with the request in the Azure Cosmos DB service.
     *
     * @param ifNoneMatchETag the ifNoneMatchETag associated with the request.
     */
    public void setIfNoneMatchETag(String ifNoneMatchETag) {
        this.ifNoneMatchETag = ifNoneMatchETag;
    }

    /**
     * Gets the FilterPredicate associated with the request in the Azure Cosmos DB service.
     *
     * @return the FilterPredicate associated with the request.
     */
    public String getFilterPredicate() {
        return this.filterPredicate;
    }

    /**
     * Sets the FilterPredicate associated with the request in the Azure Cosmos DB service.
     *
     * @param filterPredicate the filterPredicate associated with the request.
     */
    public void setFilterPredicate(String filterPredicate) {
        this.filterPredicate = filterPredicate;
    }

    /**
     * Gets the indexing directive (index, do not index etc).
     *
     * @return the indexing directive.
     */
    public IndexingDirective getIndexingDirective() {
        return this.indexingDirective;
    }

    /**
     * Sets the indexing directive (index, do not index etc).
     *
     * @param indexingDirective the indexing directive.
     */
    public void setIndexingDirective(IndexingDirective indexingDirective) {
        this.indexingDirective = indexingDirective;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getTrackingId() {
        return this.trackingId;
    }

    /**
     * Gets the consistency level required for the request.
     *
     * @return the consistency level.
     */
    @Override
    public ConsistencyLevel getConsistencyLevel() {
        return this.consistencyLevel;
    }

    /**
     * Sets the consistency level required for the request.
     *
     * @param consistencyLevel the consistency level.
     */
    public void setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }

    /**
     * Gets the token for use with session consistency.
     *
     * @return the session token.
     */
    public String getSessionToken() {
        return this.sessionToken;
    }

    /**
     * Sets the token for use with session consistency.
     *
     * @param sessionToken the session token.
     */
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    /**
     * Gets the expiry time for resource token. Used when creating, updating, reading permission.
     *
     * @return the resource token expiry seconds.
     */
    public Integer getResourceTokenExpirySeconds() {
        return this.resourceTokenExpirySeconds;
    }

    /**
     * Sets the expiry time for resource token. Used when creating, updating, reading permission.
     *
     * @param resourceTokenExpirySeconds the resource token expiry seconds.
     */
    public void setResourceTokenExpirySeconds(Integer resourceTokenExpirySeconds) {
        this.resourceTokenExpirySeconds = resourceTokenExpirySeconds;
    }

    /**
     * Gets the offer type when creating a container.
     *
     * @return the offer type.
     */
    public String getOfferType() {
        return this.offerType;
    }

    /**
     * Sets the offer type when creating a container.
     *
     * @param offerType the offer type.
     */
    public void setOfferType(String offerType) {
        this.offerType = offerType;
    }

    /**
     * Gets the throughput in the form of Request Units per second when creating a container.
     *
     * @return the throughput value.
     */
    public Integer getOfferThroughput() {
        return this.offerThroughput;
    }

    /**
     * Sets the throughput in the form of Request Units per second when creating a container.
     *
     * @param offerThroughput the throughput value.
     */
    public void setOfferThroughput(Integer offerThroughput) {
        this.offerThroughput = offerThroughput;
    }

    public void setThroughputProperties(ThroughputProperties throughputProperties) {
        this.throughputProperties = throughputProperties;
    }

    public ThroughputProperties getThroughputProperties() {
        return this.throughputProperties;
    }

    /**
     * Gets the partition key used to identify the current request's target partition.
     *
     * @return the partition key value.
     */
    public PartitionKey getPartitionKey() {
        return this.partitionkey;
    }

    /**
     * Sets the partition key used to identify the current request's target partition.
     *
     * @param partitionkey the partition key value.
     */
    public void setPartitionKey(PartitionKey partitionkey) {
        this.partitionkey = partitionkey;
    }

    /**
     * Gets whether Javascript stored procedure logging is enabled for the current request in the Azure Cosmos DB database
     * service or not.
     *
     * @return true if Javascript stored procedure logging is enabled
     */
    public boolean isScriptLoggingEnabled() {
        return scriptLoggingEnabled;
    }

    /**
     * Sets whether Javascript stored procedure logging is enabled for the current request in the Azure Cosmos DB database
     * service or not.
     *
     * @param scriptLoggingEnabled true if stored procedure Javascript logging is enabled
     */
    public void setScriptLoggingEnabled(boolean scriptLoggingEnabled) {
        this.scriptLoggingEnabled = scriptLoggingEnabled;
    }

    /**
     * Gets the quotaInfoEnabled setting for container read requests in the Azure Cosmos DB database service.
     * quotaInfoEnabled is used to enable/disable getting container quota related stats for item
     * container read requests.
     *
     * @return true if quotaInfoEnabled is enabled
     */
    public boolean isQuotaInfoEnabled() {
        return quotaInfoEnabled;
    }

    /**
     * Sets the quotaInfoEnabled setting for container read requests in the Azure Cosmos DB database service.
     * quotaInfoEnabled is used to enable/disable getting container quota related stats for item
     * container read requests.
     *
     * @param quotaInfoEnabled a boolean value indicating whether quotaInfoEnabled is enabled or not
     */
    public void setQuotaInfoEnabled(boolean quotaInfoEnabled) {
        this.quotaInfoEnabled = quotaInfoEnabled;
    }

    /**
     * Sets the custom request option value by key
     *
     * @param name  a string representing the custom option's name
     * @param value a STRING representing the custom option's value
     */
    public void setHeader(String name, String value) {
        if (this.customOptions == null) {
            this.customOptions = new HashMap<>();
        }
        this.customOptions.put(name, value);
    }

    /**
     * Gets the custom request options
     *
     * @return Map of custom request options
     */
    public Map<String, String> getHeaders() {
        return this.customOptions;
    }
    /**
     * Gets the properties
     *
     * @return Map of request options properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Sets the properties used to identify the request token.
     *
     * @param properties the properties.
     */
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * Gets the boolean to only return the headers and status code in Cosmos DB response
     * in case of Create, Update and Delete operations on CosmosItem.
     * <p>
     * If set to false, service doesn't return payload in the response. It reduces networking
     * and CPU load by not sending the payload back over the network and serializing it on the client.
     * <p>
     * This feature does not impact RU usage for read or write operations.
     * <p>
     * By-default, this is null.
     *
     * @return a boolean indicating whether payload will be included in the response or not for this request.
     */
    @Override
    public Boolean isContentResponseOnWriteEnabled() {
        return contentResponseOnWriteEnabled;
    }

    /**
     * Sets the boolean to only return the headers and status code in Cosmos DB response
     * in case of Create, Update and Delete operations on CosmosItem.
     * <p>
     * If set to false, service doesn't return payload in the response. It reduces networking
     * and CPU load by not sending the payload back over the network and serializing it on the client.
     * <p>
     * This feature does not impact RU usage for read or write operations.
     * <p>
     * By-default, this is null.
     * <p>
     * NOTE: This flag is also present on {@link com.azure.cosmos.CosmosClientBuilder},
     * however if specified on {@link com.azure.cosmos.models.CosmosItemRequestOptions},
     * it will override the value specified in {@link com.azure.cosmos.CosmosClientBuilder} for this request.
     *
     * @param contentResponseOnWriteEnabled a boolean indicating whether payload will be included
     * in the response or not for this request
     */
    public void setContentResponseOnWriteEnabled(Boolean contentResponseOnWriteEnabled) {
        this.contentResponseOnWriteEnabled = contentResponseOnWriteEnabled;
    }

    @Override
    public String getThroughputControlGroupName() {
        return this.throughputControlGroupName;
    }

    public void setThroughputControlGroupName(String throughputControlGroupName) {
        this.throughputControlGroupName = throughputControlGroupName;
    }

    @Override
    public DedicatedGatewayRequestOptions getDedicatedGatewayRequestOptions() {
        return dedicatedGatewayRequestOptions;
    }

    public void setDedicatedGatewayRequestOptions(DedicatedGatewayRequestOptions dedicatedGatewayRequestOptions) {
        this.dedicatedGatewayRequestOptions = dedicatedGatewayRequestOptions;
    }

    @Override
    public CosmosDiagnosticsThresholds getDiagnosticsThresholds() {
        return this.thresholds;
    }

    @Override
    public Boolean isScanInQueryEnabled() {
        return null;
    }

    @Override
    public Integer getMaxDegreeOfParallelism() {
        return null;
    }

    @Override
    public Integer getMaxBufferedItemCount() {
        return null;
    }

    @Override
    public Integer getResponseContinuationTokenLimitInKb() {
        return null;
    }

    @Override
    public Integer getMaxItemCount() {
        return null;
    }

    @Override
    public Boolean isQueryMetricsEnabled() {
        return null;
    }

    @Override
    public Boolean isIndexMetricsEnabled() {
        return null;
    }

    @Override
    public Integer getMaxPrefetchPageCount() {
        return null;
    }

    @Override
    public String getQueryNameOrDefault(String defaultQueryName) {
        return null;
    }

    public void setDiagnosticsThresholds(CosmosDiagnosticsThresholds thresholds) {
        this.thresholds = thresholds;
    }

    public void setDiagnosticsContextSupplier(Supplier<CosmosDiagnosticsContext> ctxSupplier) {
        this.diagnosticsCtxSupplier = ctxSupplier;
    }

    public CosmosDiagnosticsContext getDiagnosticsContextSnapshot() {
        Supplier<CosmosDiagnosticsContext> ctxSupplierSnapshot = this.diagnosticsCtxSupplier;
        if (ctxSupplierSnapshot == null) {
            return null;
        }

        return ctxSupplierSnapshot.get();
    }

    public void setCosmosEndToEndLatencyPolicyConfig(CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig) {
        this.endToEndOperationLatencyConfig = endToEndOperationLatencyPolicyConfig;
    }

    @Override
    public CosmosEndToEndOperationLatencyPolicyConfig getCosmosEndToEndLatencyPolicyConfig(){
        return this.endToEndOperationLatencyConfig;
    }

    @Override
    public List<String> getExcludedRegions() {
        return this.excludeRegions;
    }

    public void setExcludedRegions(List<String> excludeRegions) {
        this.excludeRegions = excludeRegions;
    }

    public AtomicReference<Runnable> getMarkE2ETimeoutInRequestContextCallbackHook() {
        return this.markE2ETimeoutInRequestContextCallbackHook;
    }

    public void setKeywordIdentifiers(Set<String> keywordIdentifiers) {
        this.keywordIdentifiers = keywordIdentifiers;
    }

    @Override
    public Set<String> getKeywordIdentifiers() {
        return keywordIdentifiers;
    }

    @Override
    public void override(CosmosRequestOptions cosmosCommonRequestOptions) {
        this.consistencyLevel = overrideOption(cosmosCommonRequestOptions.getConsistencyLevel(), this.consistencyLevel);
        this.contentResponseOnWriteEnabled = overrideOption(cosmosCommonRequestOptions.isContentResponseOnWriteEnabled(), this.contentResponseOnWriteEnabled);
        this.nonIdempotentWriteRetriesEnabled = overrideOption(cosmosCommonRequestOptions.getNonIdempotentWriteRetriesEnabled(), this.nonIdempotentWriteRetriesEnabled);
        this.dedicatedGatewayRequestOptions = overrideOption(cosmosCommonRequestOptions.getDedicatedGatewayRequestOptions(), this.dedicatedGatewayRequestOptions);
        this.excludeRegions = overrideOption(cosmosCommonRequestOptions.getExcludedRegions(), this.excludeRegions);
        this.throughputControlGroupName = overrideOption(cosmosCommonRequestOptions.getThroughputControlGroupName(), this.throughputControlGroupName);
        this.thresholds = overrideOption(cosmosCommonRequestOptions.getDiagnosticsThresholds(), this.thresholds);
        this.endToEndOperationLatencyConfig = overrideOption(cosmosCommonRequestOptions.getCosmosEndToEndLatencyPolicyConfig(), this.endToEndOperationLatencyConfig);
        this.keywordIdentifiers = overrideOption(cosmosCommonRequestOptions.getKeywordIdentifiers(), this.keywordIdentifiers);
    }

    public CosmosItemSerializer getEffectiveItemSerializer() {
        return this.effectiveItemSerializer;
    }

    public void setEffectiveItemSerializer(CosmosItemSerializer serializer) {
        this.effectiveItemSerializer = serializer;
    }

    public void setUseTrackingIds(boolean useTrackingIds) {
        this.useTrackingIds = useTrackingIds;
    }

    public boolean getUseTrackingIds() {
        return this.useTrackingIds;
    }

    public WriteRetryPolicy calculateAndGetEffectiveNonIdempotentRetriesEnabled(
        WriteRetryPolicy clientDefault,
        boolean operationDefault) {

        if (this.nonIdempotentWriteRetriesEnabled != null) {
            return new WriteRetryPolicy(
                this.nonIdempotentWriteRetriesEnabled,
                this.useTrackingIds);
        }

        if (!operationDefault) {
            this.setNonIdempotentWriteRetriesEnabled(false);
            this.setUseTrackingIds(false);
            return WriteRetryPolicy.DISABLED;
        }

        if (clientDefault != null) {
            if (clientDefault.isEnabled()) {
                this.setNonIdempotentWriteRetriesEnabled(true);
                this.setUseTrackingIds(clientDefault.useTrackingIdProperty());
            } else {
                this.setNonIdempotentWriteRetriesEnabled(false);
                this.setUseTrackingIds(false);
            }

            return clientDefault;
        }

        this.setNonIdempotentWriteRetriesEnabled(false);
        this.setUseTrackingIds(false);
        return WriteRetryPolicy.DISABLED;
    }

    public void setPartitionKeyDefinition(PartitionKeyDefinition partitionKeyDefinition) {
        this.partitionKeyDefinition = partitionKeyDefinition;
    }

    public PartitionKeyDefinition getPartitionKeyDefinition() {
        return this.partitionKeyDefinition;
    }
}
