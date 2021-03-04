// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.util.Beta;

/**
 * Encapsulates options that can be specified for an patch operation used in Bulk execution. It can be passed while
 * creating bulk patch request using {@link BulkOperations}.
 */
@Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class BulkPatchItemRequestOptions
        extends BulkItemRequestOptionsBase {
    private String filterPredicate;

    /**
     * Gets the FilterPredicate associated with the request in the Azure Cosmos DB service.
     *
     * @return the FilterPredicate associated with the request.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getFilterPredicate() {
        return this.filterPredicate;
    }

    /**
     * Sets the FilterPredicate associated with the request in the Azure Cosmos DB service. for example: {@code setFilterPredicate("from c where c.taskNum = 3")}.
     *
     * @param filterPredicate the filterPredicate associated with the request.
     * @return the current request options
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public BulkPatchItemRequestOptions setFilterPredicate(String filterPredicate) {
        this.filterPredicate = filterPredicate;
        return this;
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
     * NOTE: This flag is also present on {@link com.azure.cosmos.CosmosClientBuilder}, however if specified
     * here, it will override the value specified in {@link com.azure.cosmos.CosmosClientBuilder} for this request.
     *
     * @param contentResponseOnWriteEnabled a boolean indicating whether payload will be included
     * in the response or not for this operation.
     *
     * @return the current request options.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public BulkPatchItemRequestOptions setContentResponseOnWriteEnabled(Boolean contentResponseOnWriteEnabled) {
        super.setContentResponseOnWriteEnabledCore(contentResponseOnWriteEnabled);
        return this;
    }

    /**
     * Sets the If-None-Match (ETag) associated with the request in operation in {@link CosmosItemOperation}.
     *
     * @param ifNoneMatchEtag the ifNoneMatchETag associated with the request.
     * @return the current request options.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public BulkPatchItemRequestOptions setIfNoneMatchETag(final String ifNoneMatchEtag) {
        super.setIfNoneMatchETagCore(ifNoneMatchEtag);
        return this;
    }

    /**
     * Sets the If-Match (ETag) associated with the operation in {@link CosmosItemOperation}.
     *
     * @param ifMatchETag the ifMatchETag associated with the request.
     * @return the current request options
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public BulkPatchItemRequestOptions setIfMatchETag(final String ifMatchETag) {
        super.setIfMatchETagCore(ifMatchETag);
        return this;
    }

    RequestOptions toRequestOptions() {
        final RequestOptions requestOptions = super.toRequestOptions();
        requestOptions.setFilterPredicate(getFilterPredicate());
        return requestOptions;
    }
}
