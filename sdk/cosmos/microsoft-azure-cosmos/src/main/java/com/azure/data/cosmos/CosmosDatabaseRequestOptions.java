// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.RequestOptions;

/**
 * Encapsulates options that can be specified for a request issued to cosmos database.
 */
public class CosmosDatabaseRequestOptions{
    private Integer offerThroughput;
    private AccessCondition accessCondition;

    /**
     * Gets the conditions associated with the request.
     *
     * @return the access condition.
     */
    public AccessCondition accessCondition() {
        return accessCondition;
    }

    /**
     * Sets the conditions associated with the request.
     *
     * @param accessCondition the access condition.
     * @return the current request options
     */
    public CosmosDatabaseRequestOptions accessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }

    /**
     * Gets the throughput in the form of Request Units per second when creating a cosmos database.
     *
     * @return the throughput value.
     */
    Integer offerThroughput() {
        return offerThroughput;
    }

    /**
     * Sets the throughput in the form of Request Units per second when creating a cosmos database.
     *
     * @param offerThroughput the throughput value.
     * @return the current request options
     */
    CosmosDatabaseRequestOptions offerThroughput(Integer offerThroughput) {
        this.offerThroughput = offerThroughput;
        return this;
    }

    RequestOptions toRequestOptions() {
        RequestOptions options = new RequestOptions();
        options.setAccessCondition(accessCondition);
        options.setOfferThroughput(offerThroughput);
        return options;
    }
}