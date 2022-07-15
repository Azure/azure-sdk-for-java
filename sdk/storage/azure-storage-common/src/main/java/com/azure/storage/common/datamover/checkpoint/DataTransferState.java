// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.datamover.checkpoint;

import com.azure.core.http.HttpRange;

import java.util.List;

/**
 * Represents state of transfers.
 */
public class DataTransferState {
    private String identifier;
    private boolean completed;
    // TODO (kasobol-msft) This shouldn't really be HttpRange. Just for prototype.
    private List<HttpRange> completedRanges;

    /**
     * TODO (kasobol-msft) add docs.
     * @return id.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * TODO (kasobol-msft) add docs.
     * @param identifier id
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * TODO (kasobol-msft) add docs.
     * @return completed.
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * TODO (kasobol-msft) add docs.
     * @param completed completed
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * TODO (kasobol-msft) add docs.
     * @return ranges
     */
    public List<HttpRange> getCompletedRanges() {
        return completedRanges;
    }

    /**
     * TODO (kasobol-msft) add docs.
     * @param completedRanges ranges.
     */
    public void setCompletedRanges(List<HttpRange> completedRanges) {
        this.completedRanges = completedRanges;
    }
}
