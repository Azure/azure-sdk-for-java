// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.util.Beta;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates options that can be specified for a request issued to cosmos Item.
 */
public class CosmosItemRequestOptions {
    private ConsistencyLevel consistencyLevel;
    private IndexingDirective indexingDirective;
    private List<String> preTriggerInclude;
    private List<String> postTriggerInclude;
    private String sessionToken;
    private PartitionKey partitionKey;
    private String ifMatchETag;
    private String ifNoneMatchETag;
    private Boolean contentResponseOnWriteEnabled;
    private String throughputControlGroupName;
    private DedicatedGatewayRequestOptions dedicatedGatewayRequestOptions;

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
     * can only be reduce for read/query requests. So when the Account's default consistency level
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
     * If set to false, service doesn't returns payload in the response. It reduces networking
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
    @Beta(value = Beta.SinceVersion.V4_15_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public DedicatedGatewayRequestOptions getDedicatedGatewayRequestOptions() {
        return this.dedicatedGatewayRequestOptions;
    }

    /**
     * Sets the Dedicated Gateway Request Options
     * @param dedicatedGatewayRequestOptions Dedicated Gateway Request Options
     * @return the CosmosItemRequestOptions
     */
    @Beta(value = Beta.SinceVersion.V4_15_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosItemRequestOptions setDedicatedGatewayRequestOptions(DedicatedGatewayRequestOptions dedicatedGatewayRequestOptions) {
        this.dedicatedGatewayRequestOptions = dedicatedGatewayRequestOptions;
        return this;
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
        //TODO: Should we set any default values instead of nulls?
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
        requestOptions.setDedicatedGatewayRequestOptions(dedicatedGatewayRequestOptions);
        return requestOptions;
    }

    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getThroughputControlGroupName() {
        return this.throughputControlGroupName;
    }

    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public void setThroughputControlGroupName(String throughputControlGroupName) {
        this.throughputControlGroupName = throughputControlGroupName;
    }
}
