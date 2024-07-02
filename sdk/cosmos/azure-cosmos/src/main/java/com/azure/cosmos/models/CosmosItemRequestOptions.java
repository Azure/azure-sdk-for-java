// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.WriteRetryPolicy;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates options that can be specified for a request issued to cosmos Item.
 */
public class CosmosItemRequestOptions {
    private final static ImplementationBridgeHelpers.CosmosDiagnosticsThresholdsHelper.CosmosDiagnosticsThresholdsAccessor thresholdsAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsThresholdsHelper.getCosmosAsyncClientAccessor();

    private ConsistencyLevel consistencyLevel;
    private IndexingDirective indexingDirective;
    private OperationContextAndListenerTuple operationContextAndListenerTuple;
    private List<String> preTriggerInclude;
    private List<String> postTriggerInclude;
    private String sessionToken;
    private PartitionKey partitionKey;
    private String ifMatchETag;
    private String ifNoneMatchETag;
    private Boolean contentResponseOnWriteEnabled;
    private String throughputControlGroupName;
    private DedicatedGatewayRequestOptions dedicatedGatewayRequestOptions;
    private Map<String, String> customOptions;
    private CosmosDiagnosticsThresholds thresholds;
    private Boolean nonIdempotentWriteRetriesEnabled;
    private boolean useTrackingIds;
    private CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig;
    private List<String> excludeRegions;
    private CosmosItemSerializer customSerializer;

    /**
     * copy constructor
     */
    CosmosItemRequestOptions(CosmosItemRequestOptions options) {
        consistencyLevel = options.consistencyLevel;
        indexingDirective = options.indexingDirective;
        preTriggerInclude = options.preTriggerInclude != null ? new ArrayList<>(options.preTriggerInclude) : null;
        postTriggerInclude = options.postTriggerInclude != null ? new ArrayList<>(options.postTriggerInclude) : null;
        sessionToken = options.sessionToken;
        partitionKey = options.partitionKey;
        ifMatchETag = options.ifMatchETag;
        ifNoneMatchETag = options.ifNoneMatchETag;
        contentResponseOnWriteEnabled = options.contentResponseOnWriteEnabled;
        throughputControlGroupName = options.throughputControlGroupName;
        dedicatedGatewayRequestOptions = options.dedicatedGatewayRequestOptions;
        thresholds = options.thresholds;
        operationContextAndListenerTuple = options.operationContextAndListenerTuple;
        nonIdempotentWriteRetriesEnabled = options.nonIdempotentWriteRetriesEnabled;
        useTrackingIds = options.useTrackingIds;
        endToEndOperationLatencyPolicyConfig = options.endToEndOperationLatencyPolicyConfig;
        excludeRegions = options.excludeRegions;
        customSerializer = options.customSerializer;
        if (options.customOptions != null) {
            this.customOptions = new HashMap<>(options.customOptions);
        }
    }


    /**
     * Constructor
     */
    public CosmosItemRequestOptions() {
        super();
    }

    /**
     * Constructor
     *
     * @param partitionKey the partition key
     */
    CosmosItemRequestOptions(PartitionKey partitionKey) {
        super();

        setPartitionKey(partitionKey);
        this.thresholds = new CosmosDiagnosticsThresholds();
    }

    /**
     * Gets the If-Match (ETag) associated with the request in the Azure Cosmos DB service.
     *
     * @return the ifMatchETag associated with the request.
     */
    public String getIfMatchETag() {
        return this.ifMatchETag;
    }

    /**
     * Sets the If-Match (ETag) associated with the request in the Azure Cosmos DB service.
     *
     * @param ifMatchETag the ifMatchETag associated with the request.
     * @return the current request options
     */
    public CosmosItemRequestOptions setIfMatchETag(String ifMatchETag) {
        this.ifMatchETag = ifMatchETag;
        return this;
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
     * @return the current request options
     */
    public CosmosItemRequestOptions setIfNoneMatchETag(String ifNoneMatchETag) {
        this.ifNoneMatchETag = ifNoneMatchETag;
        return this;
    }

    /**
     * Gets the consistency level required for the request.
     *
     * @return the consistency level.
     */
    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    /**
     * Sets the consistency level required for the request. The effective consistency level
     * can only be reduced for read/query requests. So when the Account's default consistency level
     * is for example Session you can specify on a request-by-request level for individual requests
     * that Eventual consistency is sufficient - which could reduce the latency and RU charges for this
     * request but will not guarantee session consistency (read-your-own-write) anymore
     * NOTE: If the consistency-level set on a request level here is SESSION and the default consistency
     * level specified when constructing the CosmosClient instance via CosmosClientBuilder.consistencyLevel
     * is not SESSION then session token capturing also needs to be enabled by calling
     * CosmosClientBuilder:sessionCapturingOverrideEnabled(true) explicitly.
     *
     * @param consistencyLevel the consistency level.
     * @return the CosmosItemRequestOptions.
     */
    public CosmosItemRequestOptions setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
        return this;
    }

    /**
     * Gets the indexing directive (index, do not index etc).
     *
     * @return the indexing directive.
     */
    public IndexingDirective getIndexingDirective() {
        return indexingDirective;
    }

    /**
     * Sets the indexing directive (index, do not index etc).
     *
     * @param indexingDirective the indexing directive.
     * @return the CosmosItemRequestOptions.
     */
    public CosmosItemRequestOptions setIndexingDirective(IndexingDirective indexingDirective) {
        this.indexingDirective = indexingDirective;
        return this;
    }

    /**
     * Gets the triggers to be invoked before the operation.
     *
     * @return the triggers to be invoked before the operation.
     */
    public List<String> getPreTriggerInclude() {
        return preTriggerInclude;
    }

    /**
     * Sets the triggers to be invoked before the operation.
     *
     * @param preTriggerInclude the triggers to be invoked before the operation.
     * @return the CosmosItemRequestOptions.
     */
    public CosmosItemRequestOptions setPreTriggerInclude(List<String> preTriggerInclude) {
        this.preTriggerInclude = preTriggerInclude;
        return this;
    }

    /**
     * Gets the triggers to be invoked after the operation.
     *
     * @return the triggers to be invoked after the operation.
     */
    public List<String> getPostTriggerInclude() {
        return postTriggerInclude;
    }

    /**
     * Sets the triggers to be invoked after the operation.
     *
     * @param postTriggerInclude the triggers to be invoked after the operation.
     * @return the CosmosItemRequestOptions.
     */
    public CosmosItemRequestOptions setPostTriggerInclude(List<String> postTriggerInclude) {
        this.postTriggerInclude = postTriggerInclude;
        return this;
    }

    /**
     * Gets the token for use with session consistency.
     *
     * @return the session token.
     */
    public String getSessionToken() {
        return sessionToken;
    }

    /**
     * Sets the token for use with session consistency.
     *
     * @param sessionToken the session token.
     * @return the CosmosItemRequestOptions.
     */
    public CosmosItemRequestOptions setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
        return this;
    }

    /**
     * Gets the boolean to only return the headers and status code in Cosmos DB response
     * in case of Create, Update and Delete operations on CosmosItem.
     *
     * If set to false, service doesn't return a payload in the response. It reduces networking
     * and CPU load by not sending the payload back over the network and serializing it on the client.
     *
     * This feature does not impact RU usage for read or write operations.
     *
     * NOTE: This flag is also present on {@link com.azure.cosmos.CosmosClientBuilder},
     * however if specified on {@link CosmosItemRequestOptions},
     * it will override the value specified in {@link com.azure.cosmos.CosmosClientBuilder} for this request.
     *
     * By-default, this is null.
     *
     * @return a boolean indicating whether payload will be included in the response or not for this request.
     */
    public Boolean isContentResponseOnWriteEnabled() {
        return contentResponseOnWriteEnabled;
    }

    /**
     * Sets the boolean to only return the headers and status code in Cosmos DB response
     * in case of Create, Update and Delete operations on CosmosItem.
     *
     * If set to false, service doesn't return payload in the response. It reduces networking
     * and CPU load by not sending the payload back over the network and serializing it on the client.
     *
     * This feature does not impact RU usage for read or write operations.
     *
     * By-default, this is null.
     *
     * NOTE: This flag is also present on {@link com.azure.cosmos.CosmosClientBuilder},
     * however if specified on {@link CosmosItemRequestOptions},
     * it will override the value specified in {@link com.azure.cosmos.CosmosClientBuilder} for this request.
     *
     * @param contentResponseOnWriteEnabled a boolean indicating whether payload will be included
     * in the response or not for this request
     * @return the CosmosItemRequestOptions.
     */
    public CosmosItemRequestOptions setContentResponseOnWriteEnabled(Boolean contentResponseOnWriteEnabled) {
        this.contentResponseOnWriteEnabled = contentResponseOnWriteEnabled;
        return this;
    }

    /**
     * Gets the Dedicated Gateway Request Options
     * @return the Dedicated Gateway Request Options
     */
    public DedicatedGatewayRequestOptions getDedicatedGatewayRequestOptions() {
        return this.dedicatedGatewayRequestOptions;
    }

    /**
     * Gets the {@link CosmosEndToEndOperationLatencyPolicyConfig} defined
     *
     * @return the {@link CosmosEndToEndOperationLatencyPolicyConfig}
     */
    CosmosEndToEndOperationLatencyPolicyConfig getCosmosEndToEndOperationLatencyPolicyConfig() {
        return endToEndOperationLatencyPolicyConfig;
    }

    /**
     * Enables automatic retries for write operations even when the SDK can't
     * guarantee that they are idempotent. This is an override of the
     * {@link CosmosClientBuilder#nonIdempotentWriteRetryOptions(com.azure.cosmos.NonIdempotentWriteRetryOptions)} behavior for a specific request/operation.
     * <br/>
     * NOTE: the setting on the CosmosClientBuilder will determine the default behavior for Create, Replace,
     * Upsert and Delete operations. It can be overridden on per-request base in the request options. For patch
     * operations by default (unless overridden in the request options) retries are always disabled by default.
     * <br/>
     * - Create: retries can result in surfacing (more) 409-Conflict requests to the application when a retry tries
     * to create a document that the initial attempt successfully created. When enabling
     * useTrackingIdPropertyForCreateAndReplace this can be avoided for 409-Conflict caused by retries.
     * <br/>
     * - Replace: retries can result in surfacing (more) 412-Precondition failure requests to the application when a
     * replace operations are using a pre-condition check (etag) and a retry tries to update a document that the
     * initial attempt successfully updated (causing the etag to change). When enabling
     * useTrackingIdPropertyForCreateAndReplace this can be avoided for 412-Precondition failures caused by retries.
     * <br/>
     * - Delete: retries can result in surfacing (more) 404-NotFound requests when a delete operation is retried and the
     * initial attempt succeeded. Ideally, write retries should only be enabled when applications can gracefully
     * handle 404 - Not Found.
     * <br/>
     * - Upsert: retries can result in surfacing a 200 - looking like the document was updated when actually the
     * document has been created by the initial attempt - so logically within the same operation. This will only
     * impact applications who have special casing for 201 vs. 200 for upsert operations.
     * <br/>
     * Patch: retries for patch can but will not always be idempotent - it completely depends on the patch operations
     * being executed and the precondition filters being used. Before enabling write retries for patch this needs
     * to be carefully reviewed and tests - which is wht retries for patch can only be enabled on request options
     * - any CosmosClient wide configuration will be ignored.
     * <br/>
     * Bulk/Delete by PK/Transactional Batch/Stroed Procedure execution: No automatic retries are supported.
     * @param nonIdempotentWriteRetriesEnabled  a flag indicating whether the SDK should enable automatic retries for
     * an operation when idempotency can't be guaranteed because for the previous attempt a request has been sent
     * on the network.
     * @param useTrackingIdPropertyForCreateAndReplace a flag indicating whether write operations can use the
     * trackingId system property '/_trackingId' to allow identification of conflicts and pre-condition failures due
     * to retries. If enabled, each document being created or replaced will have an additional '/_trackingId' property
     * for which the value will be updated by the SDK. If it is not desired to add this new json property (for example
     * due to the RU-increase based on the payload size or because it causes documents to exceed the max payload size
     * upper limit), the usage of this system property can be disabled by setting this parameter to false. This means
     * there could be a higher level of 409/312 due to retries - and applications would need to handle them gracefully
     * on their own.
     * @return the CosmosItemRequestOptions
     */
    public CosmosItemRequestOptions setNonIdempotentWriteRetryPolicy(
        boolean nonIdempotentWriteRetriesEnabled,
        boolean useTrackingIdPropertyForCreateAndReplace) {

        this.nonIdempotentWriteRetriesEnabled = nonIdempotentWriteRetriesEnabled;
        this.useTrackingIds = useTrackingIdPropertyForCreateAndReplace;

        return this;
    }

    /**
     * Sets the Dedicated Gateway Request Options
     * @param dedicatedGatewayRequestOptions Dedicated Gateway Request Options
     * @return the CosmosItemRequestOptions
     */
    public CosmosItemRequestOptions setDedicatedGatewayRequestOptions(DedicatedGatewayRequestOptions dedicatedGatewayRequestOptions) {
        this.dedicatedGatewayRequestOptions = dedicatedGatewayRequestOptions;
        return this;
    }

    /**
     * Sets the {@link CosmosEndToEndOperationLatencyPolicyConfig} to be used for the request. If the config is already set
     * on the client, then this will override the client level config for this request
     *
     * @param endToEndOperationLatencyPolicyConfig the {@link CosmosEndToEndOperationLatencyPolicyConfig}
     * @return {@link CosmosItemRequestOptions}
     */
    public CosmosItemRequestOptions setCosmosEndToEndOperationLatencyPolicyConfig(CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig) {
        this.endToEndOperationLatencyPolicyConfig = endToEndOperationLatencyPolicyConfig;
        return this;
    }

    /**
     * List of regions to exclude for the request/retries. Example "East US" or "East US, West US"
     * These regions will be excluded from the preferred regions list
     *
     * @param excludeRegions list of regions
     * @return the {@link CosmosItemRequestOptions}
     */
    public CosmosItemRequestOptions setExcludedRegions(List<String> excludeRegions) {
        this.excludeRegions = excludeRegions;
        return this;
    }

    /**
     * Gets the list of regions to be excluded for the request/retries. These regions are excluded
     * from the preferred region list.
     *
     * @return a list of excluded regions
     * */
    public List<String> getExcludedRegions() {
        if (this.excludeRegions == null) {
            return null;
        }
        return UnmodifiableList.unmodifiableList(this.excludeRegions);
    }

    /**
     * Gets the partition key
     *
     * @return the partition key
     */
    PartitionKey getPartitionKey() {
        return partitionKey;
    }

    /**
     * Sets the partition key
     *
     * @param partitionKey the partition key
     * @return the CosmosItemRequestOptions.
     */
    CosmosItemRequestOptions setPartitionKey(PartitionKey partitionKey) {
        this.partitionKey = partitionKey;
        return this;
    }

    RequestOptions toRequestOptions() {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setIfMatchETag(getIfMatchETag());
        requestOptions.setIfNoneMatchETag(getIfNoneMatchETag());
        requestOptions.setConsistencyLevel(getConsistencyLevel());
        requestOptions.setIndexingDirective(indexingDirective);
        requestOptions.setPreTriggerInclude(preTriggerInclude);
        requestOptions.setPostTriggerInclude(postTriggerInclude);
        requestOptions.setSessionToken(sessionToken);
        requestOptions.setPartitionKey(partitionKey);
        requestOptions.setContentResponseOnWriteEnabled(contentResponseOnWriteEnabled);
        requestOptions.setThroughputControlGroupName(throughputControlGroupName);
        requestOptions.setOperationContextAndListenerTuple(operationContextAndListenerTuple);
        requestOptions.setDedicatedGatewayRequestOptions(dedicatedGatewayRequestOptions);
        requestOptions.setDiagnosticsThresholds(thresholds);
        if (this.nonIdempotentWriteRetriesEnabled != null) {
            requestOptions.setNonIdempotentWriteRetriesEnabled(this.nonIdempotentWriteRetriesEnabled);
        }
        requestOptions.setCosmosEndToEndLatencyPolicyConfig(endToEndOperationLatencyPolicyConfig);
        requestOptions.setExcludedRegions(excludeRegions);
        if(this.customOptions != null) {
            for(Map.Entry<String, String> entry : this.customOptions.entrySet()) {
                requestOptions.setHeader(entry.getKey(), entry.getValue());
            }
        }
        requestOptions.setEffectiveItemSerializer(this.customSerializer);
        requestOptions.setUseTrackingIds(this.useTrackingIds);
        return requestOptions;
    }

    /**
     * Gets the throughput control group name.
     *
     * @return the throughput control group name.
     */
    public String getThroughputControlGroupName() {
        return this.throughputControlGroupName;
    }

    /**
     * Sets the throughput control group name.
     *
     * @param throughputControlGroupName the throughput control group name.
     */
    public void setThroughputControlGroupName(String throughputControlGroupName) {
        this.throughputControlGroupName = throughputControlGroupName;
    }

    /**
     * Gets the thresholdForDiagnosticsOnTracer, if latency on CRUD operation is greater than this
     * diagnostics will be sent to open telemetry exporter as events in tracer span of end to end CRUD api.
     *
     * Default is 100 ms.
     *
     * @return  thresholdForDiagnosticsOnTracerInMS the latency threshold for diagnostics on tracer.
     */
    public Duration getThresholdForDiagnosticsOnTracer() {

        return thresholdsAccessor.getPointReadLatencyThreshold(this.thresholds);
    }

    /**
     * Sets the thresholdForDiagnosticsOnTracer, if latency on CRUD operation is greater than this
     * diagnostics will be sent to open telemetry exporter as events in tracer span of end to end CRUD api.
     *
     * Default is 100 ms.
     *
     * @param thresholdForDiagnosticsOnTracer the latency threshold for diagnostics on tracer.
     * @return the CosmosItemRequestOptions
     */
    public CosmosItemRequestOptions setThresholdForDiagnosticsOnTracer(Duration thresholdForDiagnosticsOnTracer) {
        this.thresholds.setPointOperationLatencyThreshold(thresholdForDiagnosticsOnTracer);

        return this;
    }

    /**
     * Sets the custom item request option value by key
     *
     * @param name  a string representing the custom option's name
     * @param value a string representing the custom option's value
     *
     * @return the CosmosItemRequestOptions.
     */
    CosmosItemRequestOptions setHeader(String name, String value) {
        if (this.customOptions == null) {
            this.customOptions = new HashMap<>();
        }
        this.customOptions.put(name, value);
        return this;
    }

    /**
     * Allows overriding the diagnostic thresholds for a specific operation.
     * @param operationSpecificThresholds the diagnostic threshold override for this operation
     * @return the CosmosItemRequestOptions.
     */
    public CosmosItemRequestOptions setDiagnosticsThresholds(
        CosmosDiagnosticsThresholds operationSpecificThresholds) {

        this.thresholds = operationSpecificThresholds;
        return this;
    }

    /**
     * Gets the diagnostic thresholds used as an override for a specific operation. If no operation specific
     * diagnostic threshold has been specified, this method will return null, although at runtime the default
     * thresholds specified at the client-level will be used.
     * @return the diagnostic thresholds used as an override for a specific operation.
     */
    public CosmosDiagnosticsThresholds getDiagnosticsThresholds() {
        return this.thresholds;
    }

    /**
     * Gets the custom item serializer defined for this instance of request options
     * @return the custom item serializer
     */
    public CosmosItemSerializer getCustomItemSerializer() {
        return this.customSerializer;
    }

    /**
     * Allows specifying a custom item serializer to be used for this operation. If the serializer
     * on the request options is null, the serializer on CosmosClientBuilder is used. If both serializers
     * are null (the default), an internal Jackson ObjectMapper is ued for serialization/deserialization.
     * @param customItemSerializer the custom item serializer for this operation
     * @return  the CosmosItemRequestOptions.
     */
    public CosmosItemRequestOptions setCustomItemSerializer(CosmosItemSerializer customItemSerializer) {
        this.customSerializer = customItemSerializer;

        return this;
    }


    /**
     * Gets the custom item request options
     *
     * @return Map of custom request options
     */
    Map<String, String> getHeaders() {
        return this.customOptions;
    }

    void setOperationContextAndListenerTuple(OperationContextAndListenerTuple operationContextAndListenerTuple) {
        this.operationContextAndListenerTuple = operationContextAndListenerTuple;
    }

    OperationContextAndListenerTuple getOperationContextAndListenerTuple() {
        return this.operationContextAndListenerTuple;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosItemRequestOptionsHelper.setCosmosItemRequestOptionsAccessor(
            new ImplementationBridgeHelpers.CosmosItemRequestOptionsHelper.CosmosItemRequestOptionsAccessor() {

                @Override
                public RequestOptions toRequestOptions(CosmosItemRequestOptions itemRequestOptions) {
                    return itemRequestOptions.toRequestOptions();
                }

                @Override
                public void setOperationContext(CosmosItemRequestOptions itemRequestOptions,
                                                OperationContextAndListenerTuple operationContextAndListenerTuple) {
                    itemRequestOptions.setOperationContextAndListenerTuple(operationContextAndListenerTuple);
                }

                @Override
                public OperationContextAndListenerTuple getOperationContext(CosmosItemRequestOptions itemRequestOptions) {
                    return itemRequestOptions.getOperationContextAndListenerTuple();
                }

                @Override
                public CosmosItemRequestOptions clone(CosmosItemRequestOptions options) {
                    return new CosmosItemRequestOptions(options);
                }

                @Override
                public CosmosItemRequestOptions setHeader(CosmosItemRequestOptions cosmosItemRequestOptions,
                                                          String name, String value) {
                    return cosmosItemRequestOptions.setHeader(name, value);
                }

                @Override
                public Map<String, String> getHeader(CosmosItemRequestOptions cosmosItemRequestOptions) {
                    return cosmosItemRequestOptions.getHeaders();
                }

                @Override
                public CosmosDiagnosticsThresholds getDiagnosticsThresholds(CosmosItemRequestOptions cosmosItemRequestOptions) {
                    return cosmosItemRequestOptions.thresholds;
                }

                @Override
                public CosmosEndToEndOperationLatencyPolicyConfig getEndToEndOperationLatencyPolicyConfig(
                    CosmosItemRequestOptions options) {

                    if (options == null) {
                        return null;
                    }

                    return options.getCosmosEndToEndOperationLatencyPolicyConfig();
                }

                @Override
                public CosmosPatchItemRequestOptions clonePatchItemRequestOptions(CosmosPatchItemRequestOptions options) {
                    return new CosmosPatchItemRequestOptions(options);
                }
            }
        );
    }

    static { initialize(); }
}
