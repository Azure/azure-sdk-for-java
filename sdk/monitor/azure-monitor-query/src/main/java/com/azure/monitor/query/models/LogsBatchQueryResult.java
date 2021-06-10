// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

/**
 * Class containing the result of a single logs query in a batch.
 */
@Immutable
public final class LogsBatchQueryResult {
    private final String id;
    private final int status;
    private final LogsQueryResult queryResult;

    /**
     * Creates an instance of {@link LogsBatchQueryResult} containing the result of a single logs query in a batch.
     * @param id The query id.
     * @param status The response status of the query.
     * @param queryResult The result of the query.
     */
    public LogsBatchQueryResult(String id, int status, LogsQueryResult queryResult) {
        this.id = id;
        this.status = status;
        this.queryResult = queryResult;
    }

    /**
     * Returns the query id.
     * @return The query id.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the response status of the query.
     * @return The response status of the query.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Returns the logs query result.
     * @return The logs query result.
     */
    public LogsQueryResult getQueryResult() {
        return queryResult;
    }
}

