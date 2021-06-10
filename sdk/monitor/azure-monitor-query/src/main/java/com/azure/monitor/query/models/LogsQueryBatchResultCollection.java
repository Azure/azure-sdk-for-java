// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * The collection wrapper to hold all results of a batch of logs queries.
 */
@Immutable
public final class LogsQueryBatchResultCollection {
    private final List<LogsQueryBatchResult> batchResults;

    /**
     * Creates an instance of {@link LogsQueryBatchResultCollection} to hold all results of a batch of logs queries.
     * @param batchResults The results of a batch of logs queries.
     */
    public LogsQueryBatchResultCollection(List<LogsQueryBatchResult> batchResults) {
        this.batchResults = batchResults;
    }

    /**
     * Returns the results of a batch of logs queries.
     * @return The results of a batch of logs queries.
     */
    public List<LogsQueryBatchResult> getBatchResults() {
        return batchResults;
    }
}
