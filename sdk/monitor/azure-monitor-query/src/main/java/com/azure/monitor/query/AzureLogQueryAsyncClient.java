// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.monitor.query.log.implementation.AzureLogAnalyticsImpl;
import com.azure.monitor.query.log.implementation.models.BatchRequest;
import com.azure.monitor.query.log.implementation.models.BatchResponse;
import com.azure.monitor.query.log.implementation.models.LogQueryRequest;
import com.azure.monitor.query.log.implementation.models.QueryBody;
import com.azure.monitor.query.log.implementation.models.QueryResults;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public final class AzureLogQueryAsyncClient {
    private final AzureLogAnalyticsImpl innerClient;

    public AzureLogQueryAsyncClient(AzureLogAnalyticsImpl innerClient) {
        this.innerClient = innerClient;
    }

    public Mono<QueryResults> queryLogs(String workspaceId, String query) {
        return innerClient.getQueries().executeAsync(workspaceId, new QueryBody(query), null);
    }

    public Mono<Response<QueryResults>> queryLogsWithResponse(String workspaceId, String query,
                                                              QueryLogsRequestOptions options) {

        StringBuilder filter = new StringBuilder();
        if (options.isIncludeRendering()) {
            filter.append("include-render=true");
        }

        if (options.isIncludeStatistics()) {
            if (filter.length() > 0) {
                filter.append(";");
            }
            filter.append("include-statistics=true");
        }

        if (options.getServerTimeout() != null) {
            if (filter.length() > 0) {
                filter.append(";");
            }
            filter.append("wait=");
            filter.append(options.getServerTimeout().getSeconds());
        }
        return innerClient.getQueries().executeWithResponseAsync(workspaceId, new QueryBody(query), filter.toString(),
            Context.NONE);
    }

    public BatchResponse queryLogsBatch(List<String> queries) {
        BatchRequest batchRequest = new BatchRequest();
        List<LogQueryRequest> requests = queries.stream()
            .map(query -> new LogQueryRequest().setId("1").setBody(new QueryBody(query)))
            .collect(Collectors.toList());
        batchRequest.setRequests(requests);
        return innerClient.getQueries().batch(batchRequest);
    }

}
