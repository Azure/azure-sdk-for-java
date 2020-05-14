// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.RequestOptions;

/**
 * Encapsulates options that can be specified for a request issued to cosmos database.
 */
public final class CosmosDatabaseRequestOptions {
    private Integer offerThroughput;
    private AccessCondition accessCondition;
    private ThroughputProperties throughputProperties;

    /**
     * Gets the conditions associated with the request.
     *
     * @return the access condition.
     */
    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    /**
     * Sets the conditions associated with the request.
     *
     * @param accessCondition the access condition.
     * @return the current request options
     */
    public CosmosDatabaseRequestOptions setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
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
        options.setAccessCondition(accessCondition);
        options.setOfferThroughput(offerThroughput);
        options.setThroughputProperties(this.throughputProperties);
        return options;
    }
}
