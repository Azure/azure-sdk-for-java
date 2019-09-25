// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.RequestOptions;

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
    private AccessCondition accessCondition;

    /**
     * Constructor
     */
    public CosmosItemRequestOptions(){
        super();
    }

    /**
     * Constructor
     * @param partitionKey the partition key 
     */
    public CosmosItemRequestOptions(Object partitionKey){
        super();
        if (partitionKey instanceof PartitionKey) {
            setPartitionKey((PartitionKey) partitionKey);
        } else {
            setPartitionKey(new PartitionKey(partitionKey));
        }
    }

    /**
     * Gets the conditions associated with the request.
     *
     * @return the access condition.
     */
    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    /**
     * Sets the conditions associated with the request.
     *
     * @param accessCondition the access condition.
     * @return the current request options
     */
    public CosmosItemRequestOptions setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
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
     * Sets the consistency level required for the request.
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
     * Sets the partition key
     * @param partitionKey the partition key
     * @return the CosmosItemRequestOptions.
     */
    public CosmosItemRequestOptions setPartitionKey(PartitionKey partitionKey) {
        this.partitionKey = partitionKey;
        return this;
    }

    /**
     * Gets the partition key
     * @return the partition key
     */
    public PartitionKey getPartitionKey() {
        return partitionKey;
    }

    RequestOptions toRequestOptions() {
        //TODO: Should we set any default values instead of nulls?
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setAccessCondition(accessCondition);
        requestOptions.setAccessCondition(getAccessCondition());
        requestOptions.setConsistencyLevel(getConsistencyLevel());
        requestOptions.setIndexingDirective(indexingDirective);
        requestOptions.setPreTriggerInclude(preTriggerInclude);
        requestOptions.setPostTriggerInclude(postTriggerInclude);
        requestOptions.setSessionToken(sessionToken);
        requestOptions.setPartitionKey(partitionKey);
        return requestOptions;
    }
}
