// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

/**
 *
 */
@Immutable
public final class LogsQueryBatchResult {
    private final String id;
    private final int status;
    private final LogsQueryResult queryResult;
    private final LogsQueryErrorDetails error;

    /**
     * @param id
     * @param status
     * @param queryResult
     * @param error
     */
    public LogsQueryBatchResult(String id, int status, LogsQueryResult queryResult, LogsQueryErrorDetails error) {
        this.id = id;
        this.status = status;
        this.queryResult = queryResult;
        this.error = error;
    }

    /**
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * @return
     */
    public int getStatus() {
        return status;
    }

    /**
     * @return
     */
    public LogsQueryResult getQueryResult() {
        return queryResult;
    }

    /**
     * @return
     */
    public LogsQueryErrorDetails getError() {
        return error;
    }
}

