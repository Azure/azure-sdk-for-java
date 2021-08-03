// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.monitor.query.models.LogsBatchQuery;
import com.azure.monitor.query.models.LogsBatchQueryResultCollection;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.QueryTimeSpan;

import java.util.List;

/**
 * The synchronous client for querying Azure Monitor logs.
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
     * @param workspaceId The workspaceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeSpan The time period for which the logs should be looked up.
     * @return The logs matching the query.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public LogsQueryResult queryLogs(String workspaceId, String query, QueryTimeSpan timeSpan) {
        return asyncClient.queryLogs(workspaceId, query, timeSpan).block();
    }

    /**
     * Returns all the Azure Monitor logs matching the given query in the specified workspaceId.
     * @param workspaceId The workspaceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeSpan The time period for which the logs should be looked up.
     * @param options The log query options to configure server timeout, set additional workspaces or enable
     * statistics and rendering information in response.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The logs matching the query including the HTTP response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LogsQueryResult> queryLogsWithResponse(String workspaceId, String query, QueryTimeSpan timeSpan,
                                                           LogsQueryOptions options, Context context) {
        return asyncClient.queryLogsWithResponse(workspaceId, query, timeSpan, options, context).block();
    }

    /**
     * Returns all the Azure Monitor logs matching the given batch of queries in the specified workspaceId.
     * @param workspaceId The workspaceId where the batch of queries should be executed.
     * @param queries A batch of Kusto queries.
     * @param timeSpan The time period for which the logs should be looked up.
     * @return A collection of query results corresponding to the input batch of queries.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public LogsBatchQueryResultCollection queryLogsBatch(String workspaceId, List<String> queries, QueryTimeSpan timeSpan) {
        return asyncClient.queryLogsBatch(workspaceId, queries, timeSpan).block();
    }

    /**
     * Returns all the Azure Monitor logs matching the given batch of queries.
     * @param logsBatchQuery {@link LogsBatchQuery} containing a batch of queries.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A collection of query results corresponding to the input batch of queries.@return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LogsBatchQueryResultCollection> queryLogsBatchWithResponse(LogsBatchQuery logsBatchQuery, Context context) {
        return asyncClient.queryLogsBatchWithResponse(logsBatchQuery, context).block();
    }

}
