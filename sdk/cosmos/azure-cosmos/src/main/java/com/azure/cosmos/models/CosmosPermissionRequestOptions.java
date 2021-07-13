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
    private Integer resourceTokenExpirySeconds;

    /**
     * Gets the resource token expiry in seconds associated with the request in the Azure Cosmos DB service.
     *
     * @return the resourceTokenExpirySeconds associated with the request.
     */
    public Integer getResourceTokenExpirySeconds() {
        return this.resourceTokenExpirySeconds;
    }

    /**
     * Sets the resource token expiry in seconds associated with the request in the Azure Cosmos DB service.
     *
     * @param resourceTokenExpirySeconds the resourceTokenExpirySeconds associated with the request.
     * @return the current request options
     */
    public CosmosPermissionRequestOptions setResourceTokenExpirySeconds(Integer resourceTokenExpirySeconds) {
        this.resourceTokenExpirySeconds = resourceTokenExpirySeconds;
        return this;
    }

    /**
     * Gets the If-Match (ETag) associated with the request in the Azure Cosmos DB service.
     *
     * @return the ifMatchETag associated with the request.
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
    public CosmosPermissionRequestOptions setIfMatchETag(String ifMatchETag) {
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
        requestOptions.setResourceTokenExpirySeconds(getResourceTokenExpirySeconds());
        return requestOptions;
    }
}
