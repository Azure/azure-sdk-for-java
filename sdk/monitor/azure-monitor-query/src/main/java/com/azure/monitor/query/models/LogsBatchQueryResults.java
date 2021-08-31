// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.query.implementation.logs.models.LogsQueryHelper;

import java.util.List;

/**
 * The collection wrapper to hold all results of a batch of logs queries.
 */
@Immutable
public final class LogsBatchQueryResults {
    private final ClientLogger logger = new ClientLogger(LogsBatchQueryResults.class);
    private final List<LogsBatchQueryResult> batchResults;

    /**
     * Creates an instance of {@link LogsBatchQueryResults} to hold all results of a batch of logs queries.
     * @param batchResults The results of a batch of logs queries.
     */
    public LogsBatchQueryResults(List<LogsBatchQueryResult> batchResults) {
        this.batchResults = batchResults;
    }

    /**
     * Returns the results of a batch of logs queries.
     * @return The results of a batch of logs queries.
     */
    public List<LogsBatchQueryResult> getBatchResults() {
        return batchResults;
    }

    /**
     * Returns the batch query result of a specific query identified by the queryId.
     * @param queryId The query id of a query in the batch request.
     * @param type The model type to which the result will be deserialized to.
     * @param <T> The type parameter.
     * @return A list of objects of type T that contain the query result of the given query id.
     * @throws IllegalArgumentException if the result does not contain the query id.
     */
    public <T> List<T> getResult(String queryId, Class<T> type) {
        return batchResults.stream()
                .filter(result -> result.getId().equals(queryId))
                .map(queryResult -> LogsQueryHelper.toObject(queryResult.getTable(), type))
                .findFirst()
                .orElseThrow(() -> logger.logExceptionAsError(new IllegalArgumentException(queryId + " not found in the batch result")));
    }

    /**
     * Returns the batch query result of a specific query identified by the queryId.
     * @param queryId The query id of a query in the batch request.
     * @return the result of the given query id.
     * @throws IllegalArgumentException if the result does not contain the query id.
     */
    public LogsBatchQueryResult getResult(String queryId) {
        return batchResults.stream()
                .filter(result -> result.getId().equals(queryId))
                .findFirst()
                .orElseThrow(() -> logger.logExceptionAsError(new IllegalArgumentException(queryId + " not found in the batch result")));
    }
}
