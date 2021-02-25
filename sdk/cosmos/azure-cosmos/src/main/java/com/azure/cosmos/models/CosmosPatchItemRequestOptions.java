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
        super((CosmosItemRequestOptions) options);
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
     * Sets the FilterPredicate associated with the request in the Azure Cosmos DB service.
     *
     * @param filterPredicate the filterPredicate associated with the request.
     * @return the current request options
     */
    public CosmosPatchItemRequestOptions setFilterPredicate(String filterPredicate) {
        this.filterPredicate = filterPredicate;
        return this;
    }

    RequestOptions toRequestOptions() {
        RequestOptions requestOptions = super.toRequestOptions();
        requestOptions.setFilterPredicate(filterPredicate);
        return requestOptions;
    }
}
