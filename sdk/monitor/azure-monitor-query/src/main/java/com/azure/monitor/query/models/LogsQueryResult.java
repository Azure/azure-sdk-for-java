// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * The result of a logs query.
 */
@Immutable
public final class LogsQueryResult {
    private final List<LogsTable> logsTables;
    private final LogsQueryStatistics statistics;
    private final LogsQueryError error;
    private final LogsQueryVisualization visualization;

    /**
     * Creates an instance {@link LogsQueryResult} with a list of {@link LogsTable}.
     * @param logsTables The list of {@link LogsTable} returned as query result.
     * @param statistics The query execution statistics.
     * @param error The error details if there was an error executing the query.
     * @param visualization The visualization information for the logs query.
     */
    public LogsQueryResult(List<LogsTable> logsTables, LogsQueryStatistics statistics,
                           LogsQueryVisualization visualization, LogsQueryError error) {
        this.logsTables = logsTables;
        this.statistics = statistics;
        this.error = error;
        this.visualization = visualization;
    }

    /**
     * The list of {@link LogsTable} returned as query result.
     * @return The list of {@link LogsTable} returned as query result.
     */
    public List<LogsTable> getLogsTables() {
        return logsTables;
    }

    /**
     * Returns the query statistics.
     * @return the query statistics.
     */
    public LogsQueryStatistics getStatistics() {
        return statistics;
    }

    /**
     * Returns the error details if there was an error executing the query.
     * @return the error details if there was an error executing the query.
     */
    public LogsQueryError getError() {
        return error;
    }

    /**
     * Returns the visualization information for the logs query.
     * @return the visualization information for the logs query.
     */
    public LogsQueryVisualization getVisualization() {
        return visualization;
    }
}
