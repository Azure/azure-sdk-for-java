// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Fluent;

import java.util.ArrayList;
import java.util.List;

/**
 * A fluent class to create a batch of logs queries.
 */
@Fluent
public final class LogsBatchQuery {

    private final List<LogsQueryOptions> queries = new ArrayList<>();

    /**
     * Adds a new logs query to the batch.
     * @param workspaceId The workspaceId on which the query is executed.
     * @param query The Kusto query.
     * @param timeSpan The time period for which the logs should be queried.
     * @return The updated {@link LogsBatchQuery}.
     */
    public LogsBatchQuery addQuery(String workspaceId, String query, QueryTimeSpan timeSpan) {
        queries.add(new LogsQueryOptions(workspaceId, query, timeSpan));
        return this;
    }

    /**
     * Adds a new logs query to the batch.
     * @param logsQueryOptions The logs query options
     * @return The updated {@link LogsBatchQuery}
     */
    public LogsBatchQuery addQuery(LogsQueryOptions logsQueryOptions) {
        queries.add(logsQueryOptions);
        return this;
    }

    /**
     * Returns all queries added to this batch.
     * @return A list of queries in this batch.
     */
    public List<LogsQueryOptions> getQueries() {
        return this.queries;
    }
}
