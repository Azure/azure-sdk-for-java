// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosPagedFlux;

/**
 * Specifies paging options for Cosmos Paged Flux implementation.
 * @see CosmosPagedFlux
 */
public class CosmosPagedFluxOptions {

    private String requestContinuation;
    private Integer maxItemCount;

    public CosmosPagedFluxOptions() {}

    /**
     * Gets the request continuation token.
     *
     * @return the request continuation.
     */
    public String getRequestContinuation() {
        return requestContinuation;
    }

    /**
     * Sets the request continuation token.
     *
     * @param requestContinuation the request continuation.
     * @return the {@link CosmosPagedFluxOptions}.
     */
    public CosmosPagedFluxOptions setRequestContinuation(String requestContinuation) {
        this.requestContinuation = requestContinuation;
        return this;
    }

    /**
     * Gets the maximum number of items to be returned in the enumeration
     * operation.
     *
     * @return the max number of items.
     */
    public Integer getMaxItemCount() {
        return this.maxItemCount;
    }

    /**
     * Sets the maximum number of items to be returned in the enumeration
     * operation.
     *
     * @param maxItemCount the max number of items.
     * @return the {@link CosmosPagedFluxOptions}.
     */
    public CosmosPagedFluxOptions setMaxItemCount(Integer maxItemCount) {
        this.maxItemCount = maxItemCount;
        return this;
    }
}
