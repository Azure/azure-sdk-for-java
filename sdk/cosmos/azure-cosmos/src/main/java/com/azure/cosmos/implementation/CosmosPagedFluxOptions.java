// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.util.CosmosPagedFlux;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Specifies paging options for Cosmos Paged Flux implementation.
 * @see CosmosPagedFlux
 */
public class CosmosPagedFluxOptions {

    private FeedOperationState operationState;
    private String requestContinuation;
    private Integer maxItemCount;
    private String queryText;

    public CosmosPagedFluxOptions() {}

    public FeedOperationState getFeedOperationState() {
        return this.operationState;
    }

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
     * Gets the targeted number of items to be returned in the enumeration
     * operation per page.
     * <p>
     * For query operations this is a hard upper limit.
     * For ChangeFeed operations the number of items returned in a single
     * page can exceed the targeted number if the targeted number is smaller
     * than the number of change feed events within an atomic transaction. In this case
     * all items within that atomic transaction are returned even when this results in
     * page size > targeted maxItemSize.
     * </p>
     *
     * @return the targeted number of items.
     */
    public Integer getMaxItemCount() {
        return this.maxItemCount;
    }

    /**
     * Sets the targeted number of items to be returned in the enumeration
     * operation per page.
     * <p>
     * For query operations this is a hard upper limit.
     * For ChangeFeed operations the number of items returned in a single
     * page can exceed the targeted number if the targeted number is smaller
     * than the number of change feed events within an atomic transaction. In this case
     * all items within that atomic transaction are returned even when this results in
     * page size > targeted maxItemSize.
     * </p>
     *
     * @param maxItemCount the max number of items.
     * @return the {@link CosmosPagedFluxOptions}.
     */
    public CosmosPagedFluxOptions setMaxItemCount(Integer maxItemCount) {
        this.maxItemCount = maxItemCount;
        if (this.operationState != null) {
            this.operationState.setMaxItemCount(maxItemCount);
        }
        return this;
    }

    public void setFeedOperationState(FeedOperationState state) {
        this.operationState = checkNotNull(state, "Argument 'state' must not be NULL.");
    }

    public double getSamplingRateSnapshot() {
        FeedOperationState stateSnapshot = this.operationState;
        if (stateSnapshot == null) {
            return 0;
        }

        Double samplingRateSnapshot = stateSnapshot.getSamplingRateSnapshot();
        if (samplingRateSnapshot == null) {
            return 0;
        }

        return samplingRateSnapshot;
    }

    public void setSamplingRateSnapshot(double samplingRateSnapshot, boolean isSampledOut) {
        if (this.operationState != null) {
            this.operationState.setSamplingRateSnapshot(samplingRateSnapshot, isSampledOut);
        }
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

}
