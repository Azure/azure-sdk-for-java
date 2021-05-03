// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Fluent;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Fluent
public final class LogsQueryBatch {

    private final List<LogsQueryOptions> queries = new ArrayList<>();

    /**
     * @param workspaceId
     * @param query
     * @param timeSpan
     *
     * @return
     */
    public LogsQueryBatch addQuery(String workspaceId, String query, QueryTimeSpan timeSpan) {
        queries.add(new LogsQueryOptions(workspaceId, query, timeSpan));
        return this;
    }

    /**
     * @param logsQueryOptions
     *
     * @return
     */
    public LogsQueryBatch addQuery(LogsQueryOptions logsQueryOptions) {
        queries.add(logsQueryOptions);
        return this;
    }

    /**
     * @return
     */
    public List<LogsQueryOptions> getQueries() {
        return this.queries;
    }
}
