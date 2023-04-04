// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.RequestOptions;

/**
 * Encapsulates options for {@link CosmosPatchOperations}
 */
public class CosmosPatchItemRequestOptions extends CosmosItemRequestOptions {
    private String filterPredicate;

    /**
     * copy constructor
     */
    CosmosPatchItemRequestOptions(CosmosPatchItemRequestOptions options) {
        super(options);
        filterPredicate = options.filterPredicate;
    }

    /**
     * Constructor
     */
    public CosmosPatchItemRequestOptions() {
        super();
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
    public CosmosPatchItemRequestOptions setFilterPredicate(String filterPredicate) {
        this.filterPredicate = filterPredicate;
        return this;
    }

    /**
     * Enables automatic retries for Replace operations even when the SDK can't
     * guarantee that they are idempotent. This is an override of the
     * {@link CosmosClientBuilder} behavior for a specific create operation.
     * Whether retries are guaranteed to be idempotent, when executing patch operations depends on the type of
     * patch operations being applied and the precondition filters being used. For patch operations write retries
     * can only be enabled in request options because it is an operation-by-operation decisions that needs to take
     * the set of patch operations and precondition filters under consideration.
     * <p>
     * NOTE: the setting on the CosmosClientBuilder will determine the default behavior for Create, Replace,
     * Upsert and Delete operations. It can be overridden on per-request base in the request options. For patch
     * operations by default (unless overridden in the request options) retries are always disabled by default
     * when the retry can't be guaranteed to be idempotent. The exception for patch is used because whether
     * a retry is "safe" for a patch operation really depends on the set of patch instructions. The documentation
     * for the patch operation has more details.
     * @return the CosmosPatchItemRequestOptions.
     */
    public CosmosPatchItemRequestOptions enableNonIdempotentWriteRetries() {

        this.setNonIdempotentWriteRetryPolicy(true, false);
        return this;
    }

    /**
     * Disables automatic retries for write operations when the SDK can't
     * guarantee that they are idempotent. This is an override of the
     * {@link CosmosClientBuilder} behavior for a specific operation.
     *
     * @return the CosmosPatchItemRequestOptions.
     */
    public CosmosPatchItemRequestOptions disableNonIdempotentWriteRetries() {
        this.setNonIdempotentWriteRetryPolicy(false, false);

        return this;
    }

    RequestOptions toRequestOptions() {
        RequestOptions requestOptions = super.toRequestOptions();
        requestOptions.setFilterPredicate(filterPredicate);
        return requestOptions;
    }
}
