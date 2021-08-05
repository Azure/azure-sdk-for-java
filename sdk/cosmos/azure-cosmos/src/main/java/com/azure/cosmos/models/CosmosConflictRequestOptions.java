// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.RequestOptions;

/**
 * The type Cosmos conflict request options.
 */
public final class CosmosConflictRequestOptions {
    private String ifMatchETag;
    private String ifNoneMatchETag;
    private PartitionKey partitionKey;

    public CosmosConflictRequestOptions() {}

    public CosmosConflictRequestOptions(PartitionKey partitionKey) {
        this.partitionKey = partitionKey;
    }

    /**
     * Gets the If-Match (ETag) associated with the request in the Azure Cosmos DB service.
     *
     * @return ifMatchETag the ifMatchETag associated with the request.
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
    public CosmosConflictRequestOptions setIfMatchETag(String ifMatchETag) {
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
     * @param ifNoneMatchEtag the ifNoneMatchETag associated with the request.
     * @return the current request options
     */
    public CosmosConflictRequestOptions setIfNoneMatchETag(String ifNoneMatchEtag) {
        this.ifNoneMatchETag = ifNoneMatchEtag;
        return this;
    }

    /**
     * Sets the partition key associated with the request in the Azure Cosmos DB service.
     *
     * @param partitionKey the partition key associated with the request.
     * @return the CosmosItemRequestOptions.
     */
    public CosmosConflictRequestOptions setPartitionKey(PartitionKey partitionKey) {
        this.partitionKey = partitionKey;
        return this;
    }

    /**
     * Gets the partition key associated with the request in the Azure Cosmos DB service.
     *
     * @return the partitionKey associated with the request.
     */
    public PartitionKey getPartitionKey() {
        return this.partitionKey;
    }

    RequestOptions toRequestOptions() {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setIfMatchETag(getIfMatchETag());
        requestOptions.setIfNoneMatchETag(getIfNoneMatchETag());
        requestOptions.setPartitionKey(getPartitionKey());
        return requestOptions;
    }
}
