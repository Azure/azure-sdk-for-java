// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosItemSerializer;
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

    @Override
    RequestOptions toRequestOptions() {
        RequestOptions requestOptions = super.toRequestOptions();
        requestOptions.setFilterPredicate(filterPredicate);
        return requestOptions;
    }
}
