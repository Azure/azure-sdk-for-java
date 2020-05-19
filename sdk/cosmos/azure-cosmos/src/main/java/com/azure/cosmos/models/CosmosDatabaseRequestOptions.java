// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.RequestOptions;

/**
 * Encapsulates options that can be specified for a request issued to cosmos database.
 */
public final class CosmosDatabaseRequestOptions {
    private Integer offerThroughput;
    private String ifMatchEtag;
    private String ifNoneMatchEtag;
    private ThroughputProperties throughputProperties;

    /**
     * Gets the If-Match (ETag) associated with the request in the Azure Cosmos DB service.
     *
     * @return the ifMatchEtag associated with the request.
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
    public CosmosDatabaseRequestOptions setIfMatchEtag(String ifMatchEtag) {
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
    public CosmosDatabaseRequestOptions setIfNoneMatchEtag(String ifNoneMatchEtag) {
        this.ifNoneMatchEtag = ifNoneMatchEtag;
        return this;
    }

    /**
     * Gets the throughput in the form of Request Units per second when creating a cosmos database.
     *
     * @return the throughput value.
     */
    Integer getOfferThroughput() {
        return offerThroughput;
    }

    /**
     * Sets the throughput in the form of Request Units per second when creating a cosmos database.
     *
     * @param offerThroughput the throughput value.
     * @return the current request options
     */
    CosmosDatabaseRequestOptions setOfferThroughput(Integer offerThroughput) {
        this.offerThroughput = offerThroughput;
        return this;
    }

    CosmosDatabaseRequestOptions setThroughputProperties(ThroughputProperties throughputProperties) {
        this.throughputProperties = throughputProperties;
        return this;
    }

    RequestOptions toRequestOptions() {
        RequestOptions options = new RequestOptions();
        options.setIfMatchEtag(getIfMatchEtag());
        options.setIfNoneMatchEtag(getIfNoneMatchEtag());
        options.setOfferThroughput(offerThroughput);
        options.setThroughputProperties(this.throughputProperties);
        return options;
    }
}
