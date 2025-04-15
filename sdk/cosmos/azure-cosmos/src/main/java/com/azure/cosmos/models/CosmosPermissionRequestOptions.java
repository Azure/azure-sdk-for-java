// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.RequestOptions;

/**
 * Contains the request options of CosmosAsyncPermission
 */
public final class CosmosPermissionRequestOptions {
    //TODO: Need to add respective options
    private String ifMatchETag;
    private String ifNoneMatchETag;

    /**
     * Gets the If-Match (ETag) associated with the request in the Azure Cosmos DB service.
     * Most commonly used with replace and delete requests.
     * This will be ignored if specified for create requests.
     * For more details, refer to <a href="https://learn.microsoft.com/azure/cosmos-db/nosql/database-transactions-optimistic-concurrency#implementing-optimistic-concurrency-control-using-etag-and-http-headers">optimistic concurrency control documentation</a>
     *
     * @return the ifMatchETag associated with the request.
     */
    public String getIfMatchETag() {
        return this.ifMatchETag;
    }

    /**
     * Sets the If-Match (ETag) associated with the request in the Azure Cosmos DB service.
     * Most commonly used with replace and delete requests.
     * This will be ignored if specified for create requests.
     * For more details, refer to <a href="https://learn.microsoft.com/azure/cosmos-db/nosql/database-transactions-optimistic-concurrency#implementing-optimistic-concurrency-control-using-etag-and-http-headers">optimistic concurrency control documentation</a>
     *
     * @param ifMatchETag the ifMatchETag associated with the request.
     * @return the current request options
     */
    public CosmosPermissionRequestOptions setIfMatchETag(String ifMatchETag) {
        this.ifMatchETag = ifMatchETag;
        return this;
    }

    /**
     * Gets the If-None-Match (ETag) associated with the request in the Azure Cosmos DB service.
     * Most commonly used to detect changes to the resource via read requests.
     * When Item Etag matches the specified ifNoneMatchETag then 304 status code will be returned, otherwise existing Item will be returned with 200.
     * To match any Etag use "*"
     * This will be ignored if specified for write requests (ex: Create, Replace, Delete).
     * For more details, refer to <a href="https://learn.microsoft.com/azure/cosmos-db/nosql/database-transactions-optimistic-concurrency#implementing-optimistic-concurrency-control-using-etag-and-http-headers">optimistic concurrency control documentation</a>
     *
     * @return the ifNoneMatchETag associated with the request.
     */
    public String getIfNoneMatchETag() {
        return this.ifNoneMatchETag;
    }

    /**
     * Sets the If-None-Match (ETag) associated with the request in the Azure Cosmos DB service.
     * Most commonly used to detect changes to the resource via read requests.
     * When Item Etag matches the specified ifNoneMatchETag then 304 status code will be returned, otherwise existing Item will be returned with 200.
     * To match any Etag use "*"
     * This will be ignored if specified for write requests (ex: Create, Replace, Delete).
     * For more details, refer to <a href="https://learn.microsoft.com/azure/cosmos-db/nosql/database-transactions-optimistic-concurrency#implementing-optimistic-concurrency-control-using-etag-and-http-headers">optimistic concurrency control documentation</a>
     *
     * @param ifNoneMatchETag the ifNoneMatchETag associated with the request.
     * @return the current request options
     */
    public CosmosPermissionRequestOptions setIfNoneMatchETag(String ifNoneMatchETag) {
        this.ifNoneMatchETag = ifNoneMatchETag;
        return this;
    }

    RequestOptions toRequestOptions() {
        //TODO: Should we set any default values instead of nulls?
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setIfMatchETag(getIfMatchETag());
        requestOptions.setIfNoneMatchETag(getIfNoneMatchETag());
        return requestOptions;
    }
}
