// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.monitor.query.base.LogsBaseClient;
import com.azure.monitor.query.models.LogsQueryBatch;
import com.azure.monitor.query.models.LogsQueryBatchResultCollection;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.QueryTimeSpan;

import java.util.List;

/**
 *
 */
@ServiceClient(builder = LogsClientBuilder.class)
public final class LogsClient {

    private final LogsAsyncClient asyncClient;

    LogsClient(LogsAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * @param workspaceId
     * @param query
     * @param timeSpan
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public LogsQueryResult queryLogs(String workspaceId, String query, QueryTimeSpan timeSpan) {
        return asyncClient.queryLogs(workspaceId, query, timeSpan).block();
    }

    /**
     * @param options
     * @param context
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LogsQueryResult> queryLogsWithResponse(LogsQueryOptions options, Context context) {
        return asyncClient.queryLogsWithResponse(options, context).block();
    }

    /**
     * @param workspaceId
     * @param queries
     * @param timeSpan
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public LogsQueryBatchResultCollection queryLogsBatch(String workspaceId, List<String> queries, QueryTimeSpan timeSpan) {
        return asyncClient.queryLogsBatch(workspaceId, queries, timeSpan).block();
    }

    /**
     * @param logsQueryBatch
     * @param context
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LogsQueryBatchResultCollection> queryLogsBatchWithResponse(LogsQueryBatch logsQueryBatch, Context context) {
        return asyncClient.queryLogsBatchWithResponse(logsQueryBatch, context).block();
    }

}
