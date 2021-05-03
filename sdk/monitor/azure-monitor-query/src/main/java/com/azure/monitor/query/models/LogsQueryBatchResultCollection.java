// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 *
 */
@Immutable
public final class LogsQueryBatchResultCollection {
    private final List<LogsQueryBatchResult> batchResults;

    /**
     * @param batchResults
     */
    public LogsQueryBatchResultCollection(List<LogsQueryBatchResult> batchResults) {
        this.batchResults = batchResults;
    }

    /**
     * @return
     */
    public List<LogsQueryBatchResult> getBatchResults() {
        return batchResults;
    }
}
