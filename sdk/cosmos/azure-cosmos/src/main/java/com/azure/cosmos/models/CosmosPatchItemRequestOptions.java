package com.azure.cosmos.models;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class CosmosPatchItemRequestOptions extends CosmosItemRequestOptions{
    private String filterPredicate;

    /**
     * copy constructor
     */
    CosmosPatchItemRequestOptions(CosmosPatchItemRequestOptions options) {
        consistencyLevel = options.consistencyLevel;
        indexingDirective = options.indexingDirective;
        preTriggerInclude = options.preTriggerInclude != null ? new ArrayList<>(options.preTriggerInclude) : null;
        postTriggerInclude = options.postTriggerInclude != null ? new ArrayList<>(options.postTriggerInclude) : null;
        sessionToken = options.sessionToken;
        partitionKey = options.partitionKey;
        ifMatchETag = options.ifMatchETag;
        ifNoneMatchETag = options.ifNoneMatchETag;
        contentResponseOnWriteEnabled = options.contentResponseOnWriteEnabled;
        filterPredicate = options.filterPredicate;
    }

    /**
     * Constructor
     */
    public CosmosPatchItemRequestOptions() {
        super();
    }

    /**
     * Constructor
     *
     * @param partitionKey the partition key
     */
    CosmosPatchItemRequestOptions(PartitionKey partitionKey) {
        super();
        setPartitionKey(partitionKey);
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
     * @return the current request options
     */
    public CosmosPatchItemRequestOptions setFilterPredicate(String filterPredicate) {
        this.filterPredicate = filterPredicate;
        return this;
    }

    /**
     * Sets the If-Match (ETag) associated with the request in the Azure Cosmos DB service.
     *
     * @param ifMatchETag the ifMatchETag associated with the request.
     * @return the current request options
     */
    public CosmosPatchItemRequestOptions setIfMatchETag(String ifMatchETag) {
        this.ifMatchETag = ifMatchETag;
        return this;
    }

    /**
     * Sets the If-None-Match (ETag) associated with the request in the Azure Cosmos DB service.
     *
     * @param ifNoneMatchETag the ifNoneMatchETag associated with the request.
     * @return the current request options
     */
    public CosmosPatchItemRequestOptions setIfNoneMatchETag(String ifNoneMatchETag) {
        this.ifNoneMatchETag = ifNoneMatchETag;
        return this;
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
     * @return the CosmosPatchItemRequestOptions.
     */
    public CosmosPatchItemRequestOptions setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
        return this;
    }

    /**
     * Sets the indexing directive (index, do not index etc).
     *
     * @param indexingDirective the indexing directive.
     * @return the CosmosPatchItemRequestOptions.
     */
    public CosmosPatchItemRequestOptions setIndexingDirective(IndexingDirective indexingDirective) {
        this.indexingDirective = indexingDirective;
        return this;
    }

    /**
     * Sets the triggers to be invoked before the operation.
     *
     * @param preTriggerInclude the triggers to be invoked before the operation.
     * @return the CosmosPatchItemRequestOptions.
     */
    public CosmosPatchItemRequestOptions setPreTriggerInclude(List<String> preTriggerInclude) {
        this.preTriggerInclude = preTriggerInclude;
        return this;
    }

    /**
     * Sets the triggers to be invoked after the operation.
     *
     * @param postTriggerInclude the triggers to be invoked after the operation.
     * @return the CosmosPatchItemRequestOptions.
     */
    public CosmosPatchItemRequestOptions setPostTriggerInclude(List<String> postTriggerInclude) {
        this.postTriggerInclude = postTriggerInclude;
        return this;
    }

    /**
     * Sets the token for use with session consistency.
     *
     * @param sessionToken the session token.
     * @return the CosmosPatchItemRequestOptions.
     */
    public CosmosPatchItemRequestOptions setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
        return this;
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
     * however if specified on {@link CosmosPatchItemRequestOptions},
     * it will override the value specified in {@link com.azure.cosmos.CosmosClientBuilder} for this request.
     *
     * @param contentResponseOnWriteEnabled a boolean indicating whether payload will be included
     * in the response or not for this request
     * @return the CosmosPatchItemRequestOptions.
     */
    public CosmosPatchItemRequestOptions setContentResponseOnWriteEnabled(Boolean contentResponseOnWriteEnabled) {
        this.contentResponseOnWriteEnabled = contentResponseOnWriteEnabled;
        return this;
    }

    /**
     * Sets the partition key
     *
     * @param partitionKey the partition key
     * @return the CosmosPatchItemRequestOptions.
     */
    CosmosPatchItemRequestOptions setPartitionKey(PartitionKey partitionKey) {
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
        requestOptions.setFilterPredicate(filterPredicate);
        return requestOptions;
    }
}
