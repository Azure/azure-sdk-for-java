// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.RequestOptions;

/**
 * Encapsulates options that can be specified for an patch operation used in Bulk execution. It can be passed while
 * creating bulk patch request using {@link CosmosBulkOperations}.
 */
public final class CosmosBulkPatchItemRequestOptions {

    private String ifMatchETag;
    private String ifNoneMatchETag;
    private Boolean contentResponseOnWriteEnabled;
    private String filterPredicate;

    /**
     * Constructor
     */
    public CosmosBulkPatchItemRequestOptions() {
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
    public CosmosBulkPatchItemRequestOptions setFilterPredicate(String filterPredicate) {
        this.filterPredicate = filterPredicate;
        return this;
    }

    /**
     * Gets the boolean to only return the headers and status code in Cosmos DB response
     * in case of Create, Update and Delete operations in {@link CosmosItemOperation}.
     *
     * If set to false, service doesn't return payload in the response. It reduces networking
     * and CPU load by not sending the payload back over the network and serializing it on the client.
     *
     * This feature does not impact RU usage for read or write operations.
     *
     * By-default, this is null.
     *
     * @return a boolean indicating whether payload will be included in the response or not for this operation.
     */
    public Boolean isContentResponseOnWriteEnabled() {
        return this.contentResponseOnWriteEnabled;
    }

    /**
     * Sets the boolean to only return the headers and status code in Cosmos DB response
     * in case of Create, Update and Delete operations in {@link CosmosItemOperation}.
     *
     * If set to false, service doesn't return payload in the response. It reduces networking
     * and CPU load by not sending the payload back over the network and serializing it on the client.
     *
     * This feature does not impact RU usage for read or write operations.
     *
     * By-default, this is null.
     *
     * NOTE: This flag is also present on {@link CosmosClientBuilder}, however if specified
     * here, it will override the value specified in {@link CosmosClientBuilder} for this request.
     *
     * @param contentResponseOnWriteEnabled a boolean indicating whether payload will be included
     * in the response or not for this operation.
     *
     * @return the current request options.
     */
    public CosmosBulkPatchItemRequestOptions setContentResponseOnWriteEnabled(Boolean contentResponseOnWriteEnabled) {
        this.contentResponseOnWriteEnabled = contentResponseOnWriteEnabled;
        return this;
    }

    /**
     * Gets the If-None-Match (ETag) associated with the request in operation in {@link CosmosItemOperation}.
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
     * Sets the If-None-Match (ETag) associated with the request in operation in {@link CosmosItemOperation}.
     * Most commonly used to detect changes to the resource via read requests.
     * When Item Etag matches the specified ifNoneMatchETag then 304 status code will be returned, otherwise existing Item will be returned with 200.
     * To match any Etag use "*"
     * This will be ignored if specified for write requests (ex: Create, Replace, Delete).
     * For more details, refer to <a href="https://learn.microsoft.com/azure/cosmos-db/nosql/database-transactions-optimistic-concurrency#implementing-optimistic-concurrency-control-using-etag-and-http-headers">optimistic concurrency control documentation</a>
     *
     * @param ifNoneMatchEtag the ifNoneMatchETag associated with the request.
     * @return the current request options.
     */
    public CosmosBulkPatchItemRequestOptions setIfNoneMatchETag(final String ifNoneMatchEtag) {
        this.ifNoneMatchETag = ifNoneMatchEtag;
        return this;
    }

    /**
     * Gets the If-Match (ETag) associated with the operation in {@link CosmosItemOperation}.
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
     * Sets the If-Match (ETag) associated with the operation in {@link CosmosItemOperation}.
     * Most commonly used with replace, upsert and delete requests.
     * This will be ignored if specified for create requests or for upsert requests if the item doesn't exist.
     * For more details, refer to <a href="https://learn.microsoft.com/azure/cosmos-db/nosql/database-transactions-optimistic-concurrency#implementing-optimistic-concurrency-control-using-etag-and-http-headers">optimistic concurrency control documentation</a>
     *
     * @param ifMatchETag the ifMatchETag associated with the request.
     * @return the current request options
     */
    public CosmosBulkPatchItemRequestOptions setIfMatchETag(final String ifMatchETag) {
        this.ifMatchETag = ifMatchETag;
        return this;
    }

    RequestOptions toRequestOptions() {
        final RequestOptions requestOptions = new RequestOptions();
        requestOptions.setIfMatchETag(this.ifMatchETag);
        requestOptions.setIfNoneMatchETag(this.ifNoneMatchETag);
        requestOptions.setContentResponseOnWriteEnabled(this.contentResponseOnWriteEnabled);
        requestOptions.setFilterPredicate(this.filterPredicate);
        return requestOptions;
    }
}
