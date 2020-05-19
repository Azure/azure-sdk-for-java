// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.RequestOptions;

/**
 * The type Cosmos conflict request options.
 */
public final class CosmosConflictRequestOptions {
    private String ifMatchEtag;
    private String ifNoneMatchEtag;

    /**
     * Gets the If-Match (ETag) associated with the request in the Azure Cosmos DB service.
     *
     * @return ifMatchEtag the ifMatchEtag associated with the request.
     */
    public String getIfMatchEtag() {
        return this.ifMatchEtag;
    }

    /**
     * Sets the the If-Match (ETag) associated with the request in the Azure Cosmos DB service.
     *
     * @param ifMatchEtag the ifMatchEtag associated with the request.
     * @return the current request options
     */
    public CosmosConflictRequestOptions setIfMatchEtag(String ifMatchEtag) {
        this.ifMatchEtag = ifMatchEtag;
        return this;
    }

    /**
     * Gets the If-None-Match (ETag) associated with the request in the Azure Cosmos DB service.
     *
     * @return the ifNoneMatchEtag associated with the request.
     */
    public String getIfNoneMatchEtag() {
        return this.ifNoneMatchEtag;
    }

    /**
     * Sets the the If-None-Match (ETag) associated with the request in the Azure Cosmos DB service.
     *
     * @param ifNoneMatchEtag the ifNoneMatchEtag associated with the request.
     * @return the current request options
     */
    public CosmosConflictRequestOptions setIfNoneMatchEtag(String ifNoneMatchEtag) {
        this.ifNoneMatchEtag = ifNoneMatchEtag;
        return this;
    }

    RequestOptions toRequestOptions() {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setIfMatchEtag(getIfMatchEtag());
        requestOptions.setIfNoneMatchEtag(getIfNoneMatchEtag());
        return requestOptions;
    }
}
