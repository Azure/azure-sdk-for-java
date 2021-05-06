// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.monitor.query.models.AggregationType;
import com.azure.monitor.query.models.LogsQueryBatch;
import com.azure.monitor.query.models.LogsQueryBatchResult;
import com.azure.monitor.query.models.LogsQueryBatchResultCollection;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableCell;
import com.azure.monitor.query.models.LogsTableRow;
import com.azure.monitor.query.models.Metrics;
import com.azure.monitor.query.models.MetricsQueryOptions;
import com.azure.monitor.query.models.MetricsQueryResult;
import com.azure.monitor.query.models.QueryTimeSpan;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 * <p>
 * Code samples for the README.md
 */
public class ReadmeSamples {

    /**
     * Sample for creating sync and async clients for querying logs.
     */
    public void createLogsClients() {
        TokenCredential tokenCredential = null;

        LogsClient logsClient = new LogsClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        LogsAsyncClient logsAsyncClient = new LogsClientBuilder()
            .credential(tokenCredential)
            .buildAsyncClient();
    }

    /**
     * Sample for creating sync and async clients for querying metrics.
     */
    public void createMetricsClients() {
        TokenCredential tokenCredential = null;

        MetricsClient metricsClient = new MetricsClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        MetricsAsyncClient metricsAsyncClient = new MetricsClientBuilder()
            .credential(tokenCredential)
            .buildAsyncClient();
    }

    /**
     * Sample to demonstrate executing a Kusto query for logs.
     */
    public void getLogsQuery() {
        TokenCredential tokenCredential = null;

        LogsClient logsClient = new LogsClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        LogsQueryResult queryResults = logsClient.queryLogs("{workspace-id}", "{kusto-query}",
            new QueryTimeSpan(Duration.ofDays(2)));
        System.out.println("Number of tables = " + queryResults.getLogsTables().size());

        // Sample to iterate over all cells in the table
        for (LogsTable table : queryResults.getLogsTables()) {
            for (LogsTableCell tableCell : table.getAllTableCells()) {
                System.out.println("Column = " + tableCell.getColumnName() + "; value = " + tableCell.getRowValue());
            }
        }
    }

    /**
     * Sample to demonstrate executing a batch of Kusto queries for logs.
     */
    public void getLogsQueryBatch() {
        TokenCredential tokenCredential = null;

        LogsClient logsClient = new LogsClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        LogsQueryBatch logsQueryBatch = new LogsQueryBatch()
            .addQuery("{workspace-id}", "{query-1}", new QueryTimeSpan(Duration.ofDays(2)))
            .addQuery("{workspace-id}", "{query-2}", new QueryTimeSpan(Duration.ofDays(30)));

        LogsQueryBatchResultCollection batchResultCollection = logsClient
            .queryLogsBatchWithResponse(logsQueryBatch, Context.NONE).getValue();

        List<LogsQueryBatchResult> responses = batchResultCollection.getBatchResults();

        for (LogsQueryBatchResult response : responses) {
            LogsQueryResult queryResult = response.getQueryResult();

            // Sample to iterate by row
            for (LogsTable table : queryResult.getLogsTables()) {
                for (LogsTableRow row : table.getTableRows()) {
                    System.out.println("Row index " + row.getRowIndex());
                    row.getTableRow()
                        .forEach(cell -> System.out.println("Column = " + cell.getColumnName() + "; value = " + cell.getRowValue()));
                }
            }
        }
    }


    /**
     * Sample to demonstrate executing a complex Kusto query for logs that requires a long time to complete and
     * requires extending server timeout.
     */
    public void getLogsWithServerTimeout() {
        TokenCredential tokenCredential = null;

        LogsClient logsClient = new LogsClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        // set request options: server timeout, rendering, statistics
        LogsQueryOptions options = new LogsQueryOptions("{workspace-id}",
            "{query}", new QueryTimeSpan(Duration.ofDays(2)))
            .setServerTimeout(Duration.ofMinutes(10));

        // make service call with these request options set as filter header
        Response<LogsQueryResult> response = logsClient.queryLogsWithResponse(options, Context.NONE);
        LogsQueryResult logsQueryResult = response.getValue();

        // Sample to iterate by row
        for (LogsTable table : logsQueryResult.getLogsTables()) {
            for (LogsTableRow row : table.getTableRows()) {
                System.out.println("Row index " + row.getRowIndex());
                row.getTableRow()
                    .forEach(cell -> System.out.println("Column = " + cell.getColumnName() + "; value = " + cell.getRowValue()));
            }
        }
    }

    /**
     * Sample to demonstrate querying Azure Monitor for metrics.
     */
    public void getMetrics() {
        TokenCredential tokenCredential = null;

        MetricsClient metricsClient = new MetricsClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        Response<MetricsQueryResult> metricsResponse = metricsClient
            .queryMetricsWithResponse(
                "{resource-id}",
                Arrays.asList("SuccessfulCalls"),
                new MetricsQueryOptions()
                    .setMetricsNamespace("Microsoft.CognitiveServices/accounts")
                    .setTimespan(Duration.ofDays(30).toString())
                    .setInterval(Duration.ofHours(1))
                    .setTop(100)
                    .setAggregation(Arrays.asList(AggregationType.AVERAGE, AggregationType.COUNT)),
                Context.NONE);

        MetricsQueryResult metricsQueryResult = metricsResponse.getValue();
        List<Metrics> metrics = metricsQueryResult.getMetrics();
        metrics.stream()
            .forEach(metric -> {
                System.out.println(metric.getMetricsName());
                System.out.println(metric.getId());
                System.out.println(metric.getType());
                System.out.println(metric.getUnit());
                System.out.println(metric.getTimeSeries().size());
                System.out.println(metric.getTimeSeries().get(0).getData().size());
                metric.getTimeSeries()
                    .stream()
                    .flatMap(ts -> ts.getData().stream())
                    .forEach(mv -> System.out.println(mv.getTimeStamp().toString() + "; Count = " + mv.getCount()
                        + "; Average = " + mv.getAverage()));
            });
    }

}
