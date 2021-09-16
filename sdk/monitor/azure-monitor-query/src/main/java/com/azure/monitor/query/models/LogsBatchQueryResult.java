// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.experimental.models.HttpResponseError;
import com.azure.core.util.BinaryData;

import java.util.List;

/**
 * Class containing the result of a single logs query in a batch.
 */
@Immutable
public final class LogsBatchQueryResult extends LogsQueryResult {
    private final String id;
    private final int status;

    /**
     * Creates an instance of {@link LogsBatchQueryResult} containing the result of a single logs query in a batch.
     * @param id The query id.
     * @param status The response status of the query.
     * @param logsTables The list of {@link LogsTable} returned as query result.
     * @param statistics The query execution statistics.
     * @param visualization The visualization information for the logs query.
     * @param error The error details if there was an error executing the query.
     */
    public LogsBatchQueryResult(String id, int status, List<LogsTable> logsTables, BinaryData statistics,
                                BinaryData visualization, HttpResponseError error) {
        super(logsTables, statistics, visualization, error);
        this.id = id;
        this.status = status;
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
    int getStatus() {
        return status;
    }
}

