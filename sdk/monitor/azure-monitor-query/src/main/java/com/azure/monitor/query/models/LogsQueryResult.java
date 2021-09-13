// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.experimental.models.HttpResponseError;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The result of a logs query.
 */
@Immutable
public class LogsQueryResult {
    private final List<LogsTable> logsTables;
    private final BinaryData statistics;
    private final HttpResponseError error;
    private final BinaryData visualization;
    private final ClientLogger logger = new ClientLogger(LogsQueryResult.class);

    /**
     * Creates an instance {@link LogsQueryResult} with a list of {@link LogsTable}.
     * @param logsTables The list of {@link LogsTable} returned as query result.
     * @param statistics The query execution statistics.
     * @param visualization The visualization information for the logs query.
     * @param error The error details if there was an error executing the query.
     */
    public LogsQueryResult(List<LogsTable> logsTables, BinaryData statistics,
                           BinaryData visualization, HttpResponseError error) {
        this.logsTables = logsTables;
        this.statistics = statistics;
        this.error = error;
        this.visualization = visualization;
    }

    /**
     * The list of {@link LogsTable} returned as query result.
     * @return The list of {@link LogsTable} returned as query result.
     */
    public List<LogsTable> getAllTables() {
        return logsTables;
    }

    /**
     * The primary {@link LogsTable} returned as query result.
     * @return The primary {@link LogsTable} returned as query result.
     * @throws IllegalStateException If this method is called when the query result contains more than one table.
     */
    public LogsTable getTable() {
        if (logsTables == null) {
            return null;
        }

        if (logsTables.size() > 1) {
            throw logger.logExceptionAsError(new IllegalStateException("The query result contains more than one table."
                    + " Use getAllTables() method instead."));
        }
        return logsTables.get(0);
    }

    /**
     * Returns the result of the logs query as a list of objects of type {@code T} where each row of the table is
     * mapped to this object type. This conversion of query result into an object model is supported only if the query
     * returns a single table in the response.
     * @param type The object type.
     * @param <T> The type into which each row of the table in the response is converted to.
     * @return A list of objects corresponding to the list of rows in the response table.
     * @throws IllegalStateException if the query response contains more than one table.
     */
    <T> List<T> toObject(Class<T> type) {
        if (this.logsTables.size() != 1) {
            throw logger.logExceptionAsError(
                    new IllegalStateException("Cannot map result to object if the response contains multiple tables."));
        }

        return logsTables.get(0)
                .getRows()
                .stream()
                .map(row -> row.toObject(type))
                .collect(Collectors.toList());
    }

    /**
     * Returns the query statistics.
     * @return the query statistics.
     */
    public BinaryData getStatistics() {
        return statistics;
    }

    /**
     * Returns the error details if there was an error executing the query.
     * @return the error details if there was an error executing the query.
     */
    public HttpResponseError getError() {
        return error;
    }

    /**
     * Returns the visualization information for the logs query.
     * @return the visualization information for the logs query.
     */
    public BinaryData getVisualization() {
        return visualization;
    }
}
