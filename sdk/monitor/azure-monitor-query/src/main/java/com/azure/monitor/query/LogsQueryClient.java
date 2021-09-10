// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.experimental.models.TimeInterval;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.monitor.query.implementation.logs.models.LogsQueryHelper;
import com.azure.monitor.query.models.LogsBatchQuery;
import com.azure.monitor.query.models.LogsBatchQueryResultCollection;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsQueryResult;

import java.util.List;

/**
 * The synchronous client for querying Azure Monitor logs.
 *
 * <p><strong>Instantiating a synchronous Logs query Client</strong></p>
 *
 * {@codesnippet com.azure.monitor.query.LogsQueryClient.instantiation}
 */
@ServiceClient(builder = LogsQueryClientBuilder.class)
public final class LogsQueryClient {

    private final LogsQueryAsyncClient asyncClient;

    /**
     * Constructor that has the async client to make sync over async service calls.
     * @param asyncClient The asynchronous client.
     */
    LogsQueryClient(LogsQueryAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Returns all the Azure Monitor logs matching the given query in the specified workspaceId.
     *
     * <p><strong>Query logs from the last 24 hours</strong></p>
     *
     * {@codesnippet com.azure.monitor.query.LogsQueryClient.query#String-String-TimeInterval}
     * @param workspaceId The workspaceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeInterval The time period for which the logs should be looked up.
     * @return The logs matching the query.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public LogsQueryResult query(String workspaceId, String query, TimeInterval timeInterval) {
        return asyncClient.query(workspaceId, query, timeInterval).block();
    }

    /**
     * Returns all the Azure Monitor logs matching the given query in the specified workspaceId.
     * @param workspaceId The workspaceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeInterval The time period for which the logs should be looked up.
     * @param type The type the result of this query should be mapped to.
     * @param <T> The type the result of this query should be mapped to.
     * @return The logs matching the query as a list of objects of type T.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> List<T> query(String workspaceId, String query, TimeInterval timeInterval, Class<T> type) {
        LogsQueryResult logsQueryResult = asyncClient.query(workspaceId, query, timeInterval).block();
        if (logsQueryResult != null) {
            return LogsQueryHelper.toObject(logsQueryResult.getTable(), type);
        }
        return null;
    }

    /**
     * Returns all the Azure Monitor logs matching the given query in the specified workspaceId.
     * @param workspaceId The workspaceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeInterval The time period for which the logs should be looked up.
     * @param type The type the result of this query should be mapped to.
     * @param options The log query options to configure server timeout, set additional workspaces or enable
     * statistics and rendering information in response.
     * @param <T> The type the result of this query should be mapped to.
     * @return The logs matching the query as a list of objects of type T.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> List<T> query(String workspaceId, String query, TimeInterval timeInterval,
                             Class<T> type, LogsQueryOptions options) {
        LogsQueryResult logsQueryResult = queryWithResponse(workspaceId, query, timeInterval, options, Context.NONE)
                .getValue();
        if (logsQueryResult != null) {
            return LogsQueryHelper.toObject(logsQueryResult.getTable(), type);
        }
        return null;
    }

    /**
     * Returns all the Azure Monitor logs matching the given query in the specified workspaceId.
     *
     * <p><strong>Query logs from the last 7 days and set the service timeout to 2 minutes</strong></p>
     *
     * {@codesnippet com.azure.monitor.query.LogsQueryClient.queryWithResponse#String-String-TimeInterval-LogsQueryOptions-Context}
     * @param workspaceId The workspaceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeInterval The time period for which the logs should be looked up.
     * @param options The log query options to configure server timeout, set additional workspaces or enable
     * statistics and rendering information in response.
     * @param context Additional context that is passed through the Http pipeline during the service call. If no
     * additional context is required, pass {@link Context#NONE} instead.
     * @return The logs matching the query including the HTTP response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LogsQueryResult> queryWithResponse(String workspaceId, String query, TimeInterval timeInterval,
                                                       LogsQueryOptions options, Context context) {
        return asyncClient.queryWithResponse(workspaceId, query, timeInterval, options, context).block();
    }

    /**
     * Returns all the Azure Monitor logs matching the given query in the specified workspaceId.
     *
     * @param workspaceId The workspaceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeInterval The time period for which the logs should be looked up.
     * @param type The type the result of this query should be mapped to.
     * @param <T> The type the result of this query should be mapped to.
     * @param options The log query options to configure server timeout, set additional workspaces or enable
     * statistics and rendering information in response.
     * @param context Additional context that is passed through the Http pipeline during the service call. If no
     * additional context is required, pass {@link Context#NONE} instead.
     * @return The logs matching the query including the HTTP response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Response<List<T>> queryWithResponse(String workspaceId, String query, TimeInterval timeInterval,
                                                       Class<T> type, LogsQueryOptions options, Context context) {
        return asyncClient.queryWithResponse(workspaceId, query, timeInterval, options, context)
                .map(response -> new SimpleResponse<>(response.getRequest(),
                        response.getStatusCode(), response.getHeaders(),
                        LogsQueryHelper.toObject(response.getValue().getTable(), type)))
                .block();
    }

    /**
     * Returns all the Azure Monitor logs matching the given batch of queries in the specified workspaceId.
     * @param workspaceId The workspaceId where the batch of queries should be executed.
     * @param queries A batch of Kusto queries.
     * @param timeInterval The time period for which the logs should be looked up.
     * @return A collection of query results corresponding to the input batch of queries.
     */
    LogsBatchQueryResultCollection queryBatch(String workspaceId, List<String> queries, TimeInterval timeInterval) {
        return asyncClient.queryBatch(workspaceId, queries, timeInterval).block();
    }

    /**
     * Returns all the Azure Monitor logs matching the given batch of queries.
     *
     * <p><strong>Execute a batch of logs queries</strong></p>
     *
     * {@codesnippet com.azure.monitor.query.LogsQueryClient.queryBatch#LogsBatchQuery}
     * @param logsBatchQuery {@link LogsBatchQuery} containing a batch of queries.
     * @return A collection of query results corresponding to the input batch of queries.@return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public LogsBatchQueryResultCollection queryBatch(LogsBatchQuery logsBatchQuery) {
        return asyncClient.queryBatch(logsBatchQuery).block();
    }


    /**
     * Returns all the Azure Monitor logs matching the given batch of queries.
     *
     * @param logsBatchQuery {@link LogsBatchQuery} containing a batch of queries.
     * @param context Additional context that is passed through the Http pipeline during the service call. If no
     * additional context is required, pass {@link Context#NONE} instead.
     * @return A collection of query results corresponding to the input batch of queries.@return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LogsBatchQueryResultCollection> queryBatchWithResponse(LogsBatchQuery logsBatchQuery, Context context) {
        return asyncClient.queryBatchWithResponse(logsBatchQuery, context).block();
    }

}
