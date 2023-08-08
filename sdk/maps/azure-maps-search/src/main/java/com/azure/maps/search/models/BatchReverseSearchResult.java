// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.models;

import java.util.List;

/** This object is returned from a successful Search Address Batch service call. */
public final class BatchReverseSearchResult {
    private final BatchResultSummary batchSummary;
    private final List<ReverseSearchAddressBatchItem> batchItems;
    private String batchId;

    /**
     * Creates a new {@link BatchReverseSearchResult} with default properties.
     */
    public BatchReverseSearchResult() {
        this.batchSummary = null;
        this.batchItems = null;
    }

    /**
     * Creates a new {@link BatchReverseSearchResult} with a summary and batch items.
     *
     * @param batchSummary the summary of this batch's search results.
     * @param batchItems the items returned in this search.
     */
    public BatchReverseSearchResult(BatchResultSummary batchSummary,
            List<ReverseSearchAddressBatchItem> batchItems) {
        this.batchSummary = batchSummary;
        this.batchItems = batchItems;
    }

    /**
     * Get the batchSummary property: Summary of the results for the batch request.
     *
     * @return the batchSummary value.
     */
    public BatchResultSummary getBatchSummary() {
        return this.batchSummary;
    }

    /**
     * Get the batchItems property: Array containing the batch results.
     *
     * @return the batchItems value.
     */
    public List<ReverseSearchAddressBatchItem> getBatchItems() {
        return this.batchItems;
    }

    /**
     * Return this id for this batch. Only available when the batch is cached.
     * @return the batch id
     */
    public String getBatchId() {
        return batchId;
    }

    /**
     * Sets the if of this batch.
     *
     * @param batchId the id of this batch, retrieved from the asynchronous API.
     */
    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }
}
