// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.http.rest.Response;
import com.azure.monitor.query.log.implementation.models.BatchResponse;
import com.azure.monitor.query.log.implementation.models.QueryResults;

import java.util.List;

public final class AzureLogQueryClient {

    private final AzureLogQueryAsyncClient asyncClient;

    public AzureLogQueryClient(AzureLogQueryAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    public QueryResults queryLogs(String workspaceId, String query){
        return asyncClient.queryLogs(workspaceId, query).block();
    }

    public Response<QueryResults> queryLogs(String workspaceId, String query, QueryLogsRequestOptions options) {
        return null;
    }

    public Response<QueryResults> queryLogsWithResponse(String workspaceId, String query,
                                                        QueryLogsRequestOptions options){
        return asyncClient.queryLogsWithResponse(workspaceId, query, options).block();
    }

    public BatchResponse queryLogsBatch(List<String> queries) {
        return asyncClient.queryLogsBatch(queries);
    }

}
