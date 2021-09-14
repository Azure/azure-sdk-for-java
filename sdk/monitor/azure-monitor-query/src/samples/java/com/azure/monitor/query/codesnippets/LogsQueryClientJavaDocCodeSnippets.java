// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.codesnippets;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.LogsQueryAsyncClient;
import com.azure.monitor.query.LogsQueryClient;
import com.azure.monitor.query.LogsQueryClientBuilder;
import com.azure.monitor.query.models.LogsBatchQuery;
import com.azure.monitor.query.models.LogsBatchQueryResult;
import com.azure.monitor.query.models.LogsBatchQueryResultCollection;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.LogsTableCell;
import com.azure.monitor.query.models.LogsTableRow;
import com.azure.monitor.query.models.MonitorQueryTimeInterval;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.stream.Collectors;

/**
 * Class containing javadoc code snippets for Logs Query client.
 */
public class LogsQueryClientJavaDocCodeSnippets {

    /**
     * Code snippet for creating a logs query client.
     */
    public void instantiation() {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        // BEGIN: com.azure.monitor.query.LogsQueryClient.instantiation
        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
                .credential(tokenCredential)
                .buildClient();
        // END: com.azure.monitor.query.LogsQueryClient.instantiation

        // BEGIN: com.azure.monitor.query.LogsQueryAsyncClient.instantiation
        LogsQueryAsyncClient logsQueryAsyncClient = new LogsQueryClientBuilder()
                .credential(tokenCredential)
                .buildAsyncClient();
        // END: com.azure.monitor.query.LogsQueryAsyncClient.instantiation
    }

    public void singleQueryAsync() {
        LogsQueryAsyncClient logsQueryAsyncClient = new LogsQueryClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildAsyncClient();
        // BEGIN: com.azure.monitor.query.LogsQueryAsyncClient.query#String-String-MonitorQueryTimeInterval
        Mono<LogsQueryResult> queryResult = logsQueryAsyncClient.query("{workspace-id}", "{kusto-query}",
                MonitorQueryTimeInterval.LAST_DAY);
        queryResult.subscribe(result -> {
            for (LogsTableRow row : result.getTable().getRows()) {
                System.out.println(row.getRow()
                        .stream()
                        .map(LogsTableCell::getValueAsString)
                        .collect(Collectors.joining(",")));
            }
        });
        // END: com.azure.monitor.query.LogsQueryAsyncClient.query#String-String-MonitorQueryTimeInterval
    }

    public void singleQuery() {
        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        // BEGIN: com.azure.monitor.query.LogsQueryClient.query#String-String-MonitorQueryTimeInterval
        LogsQueryResult queryResult = logsQueryClient.query("{workspace-id}", "{kusto-query}",
                MonitorQueryTimeInterval.LAST_DAY);
        for (LogsTableRow row : queryResult.getTable().getRows()) {
            System.out.println(row.getRow()
                    .stream()
                    .map(LogsTableCell::getValueAsString)
                    .collect(Collectors.joining(",")));
        }
        // END: com.azure.monitor.query.LogsQueryClient.query#String-String-MonitorQueryTimeInterval
    }

    public void singleQueryWithResponseAsync() {
        LogsQueryAsyncClient logsQueryAsyncClient = new LogsQueryClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildAsyncClient();
        // BEGIN: com.azure.monitor.query.LogsQueryAsyncClient.queryWithResponse#String-String-MonitorQueryTimeInterval-LogsQueryOptions
        Mono<Response<LogsQueryResult>> queryResult = logsQueryAsyncClient.queryWithResponse("{workspace-id}",
                "{kusto-query}",
                MonitorQueryTimeInterval.LAST_7_DAYS,
                new LogsQueryOptions().setServerTimeout(Duration.ofMinutes(2)));

        queryResult.subscribe(result -> {
            for (LogsTableRow row : result.getValue().getTable().getRows()) {
                System.out.println(row.getRow()
                        .stream()
                        .map(LogsTableCell::getValueAsString)
                        .collect(Collectors.joining(",")));
            }
        });
        // END: com.azure.monitor.query.LogsQueryAsyncClient.queryWithResponse#String-String-MonitorQueryTimeInterval-LogsQueryOptions
    }

    public void singleQueryWithResponse() {
        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
        
        // BEGIN: com.azure.monitor.query.LogsQueryClient.queryWithResponse#String-String-MonitorQueryTimeInterval-LogsQueryOptions-Context
        Response<LogsQueryResult> queryResult = logsQueryClient.queryWithResponse("{workspace-id}",
                "{kusto-query}",
                MonitorQueryTimeInterval.LAST_7_DAYS,
                new LogsQueryOptions().setServerTimeout(Duration.ofMinutes(2)),
                Context.NONE);

        for (LogsTableRow row : queryResult.getValue().getTable().getRows()) {
            System.out.println(row.getRow()
                    .stream()
                    .map(LogsTableCell::getValueAsString)
                    .collect(Collectors.joining(",")));
        }
        // END: com.azure.monitor.query.LogsQueryClient.queryWithResponse#String-String-MonitorQueryTimeInterval-LogsQueryOptions-Context
    }

    public void batchQueryAsync() {
        LogsQueryAsyncClient logsQueryAsyncClient = new LogsQueryClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildAsyncClient();

        // BEGIN: com.azure.monitor.query.LogsQueryAsyncClient.queryBatch#LogsBatchQuery
        LogsBatchQuery batchQuery = new LogsBatchQuery();
        String queryId1 = batchQuery.addQuery("{workspace-id-1}", "{kusto-query-1}", MonitorQueryTimeInterval.LAST_DAY);
        String queryId2 = batchQuery.addQuery("{workspace-id-2}", "{kusto-query-2}",
                MonitorQueryTimeInterval.LAST_7_DAYS, new LogsQueryOptions().setServerTimeout(Duration.ofMinutes(2)));
        
        Mono<LogsBatchQueryResultCollection> batchQueryResponse = logsQueryAsyncClient.queryBatch(batchQuery);
        
        batchQueryResponse.subscribe(result -> {
            for (LogsBatchQueryResult queryResult : result.getBatchResults()) {
                System.out.println("Logs query result for query id " + queryResult.getId());
                for (LogsTableRow row : queryResult.getTable().getRows()) {
                    System.out.println(row.getRow()
                            .stream()
                            .map(LogsTableCell::getValueAsString)
                            .collect(Collectors.joining(",")));
                }
            }
        });
        // END: com.azure.monitor.query.LogsQueryAsyncClient.queryBatch#LogsBatchQuery
    }

    public void batchQuery() {
        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        // BEGIN: com.azure.monitor.query.LogsQueryClient.queryBatch#LogsBatchQuery
        LogsBatchQuery batchQuery = new LogsBatchQuery();
        String queryId1 = batchQuery.addQuery("{workspace-id-1}", "{kusto-query-1}", MonitorQueryTimeInterval.LAST_DAY);
        String queryId2 = batchQuery.addQuery("{workspace-id-2}", "{kusto-query-2}",
                MonitorQueryTimeInterval.LAST_7_DAYS, new LogsQueryOptions().setServerTimeout(Duration.ofMinutes(2)));
        
        LogsBatchQueryResultCollection batchQueryResponse = logsQueryClient.queryBatch(batchQuery);

        for (LogsBatchQueryResult queryResult : batchQueryResponse.getBatchResults()) {
            System.out.println("Logs query result for query id " + queryResult.getId());
            for (LogsTableRow row : queryResult.getTable().getRows()) {
                System.out.println(row.getRow()
                        .stream()
                        .map(LogsTableCell::getValueAsString)
                        .collect(Collectors.joining(",")));
            }
        }
        // END: com.azure.monitor.query.LogsQueryClient.queryBatch#LogsBatchQuery
    }
}
