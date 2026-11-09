// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.RequestOptions;

/**
 * Encapsulates options that can be specified for an operation within a {@link CosmosBatch}.
 */
public final class CosmosBatchPatchItemRequestOptions {

    private String ifMatchETag;
    private String ifNoneMatchETag;
    private String filterPredicate;

    /**
     * Constructor
     */
    public CosmosBatchPatchItemRequestOptions() {
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
     * Sets the FilterPredicate associated with the request in the Azure Cosmos DB service. for example: {@code setFilterPredicate("from c where c.taskNum = 3")}.
     *
     * @param filterPredicate the filterPredicate associated with the request.
     * @return the current request options
     */
    public CosmosBatchPatchItemRequestOptions setFilterPredicate(String filterPredicate) {
        this.filterPredicate = filterPredicate;
        return this;
    }

    /**
     * Gets the If-Match (ETag) associated with the operation in CosmosBatch.
     * Most commonly used with replace, upsert and delete requests.
     * This will be ignored if specified for create requests or for upsert requests if the item doesn't exist.
     * For more details, refer to <a href="https://learn.microsoft.com/azure/cosmos-db/nosql/database-transactions-optimistic-concurrency#implementing-optimistic-concurrency-control-using-etag-and-http-headers">optimistic concurrency control documentation</a>
     *
     * @return ifMatchETag the ifMatchETag associated with the request.
     */
    public String getIfMatchETag() {
        return this.ifMatchETag;
    }

    /**
     * Sets the If-Match (ETag) associated with the operation in CosmosBatch.
     * Most commonly used with replace, upsert and delete requests.
     * This will be ignored if specified for create requests or for upsert requests if the item doesn't exist.
     * For more details, refer to <a href="https://learn.microsoft.com/azure/cosmos-db/nosql/database-transactions-optimistic-concurrency#implementing-optimistic-concurrency-control-using-etag-and-http-headers">optimistic concurrency control documentation</a>
     *
     * @param ifMatchETag the ifMatchETag associated with the request.
     * @return the current request options
     */
    public CosmosBatchPatchItemRequestOptions setIfMatchETag(final String ifMatchETag) {
        this.ifMatchETag = ifMatchETag;
        return this;
    }

    /**
     * Gets the If-None-Match (ETag) associated with the request in operation in CosmosBatch.
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
     * Sets the If-None-Match (ETag) associated with the request in operation in CosmosBatch.
     * Most commonly used to detect changes to the resource via read requests.
     * When Item Etag matches the specified ifNoneMatchETag then 304 status code will be returned, otherwise existing Item will be returned with 200.
     * To match any Etag use "*"
     * This will be ignored if specified for write requests (ex: Create, Replace, Delete).
     * For more details, refer to <a href="https://learn.microsoft.com/azure/cosmos-db/nosql/database-transactions-optimistic-concurrency#implementing-optimistic-concurrency-control-using-etag-and-http-headers">optimistic concurrency control documentation</a>
     *
     * @param ifNoneMatchEtag the ifNoneMatchETag associated with the request.
     * @return the current request options
     */
    public CosmosBatchPatchItemRequestOptions setIfNoneMatchETag(final String ifNoneMatchEtag) {
        this.ifNoneMatchETag = ifNoneMatchEtag;
        return this;
    }

    RequestOptions toRequestOptions() {
        final RequestOptions requestOptions = new RequestOptions();
        requestOptions.setIfMatchETag(this.ifMatchETag);
        requestOptions.setIfNoneMatchETag(this.ifNoneMatchETag);
        requestOptions.setFilterPredicate(this.filterPredicate);
        return requestOptions;
    }
}
