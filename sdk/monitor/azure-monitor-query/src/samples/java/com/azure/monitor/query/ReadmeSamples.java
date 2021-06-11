// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.monitor.query.models.AggregationType;
import com.azure.monitor.query.models.LogsBatchQuery;
import com.azure.monitor.query.models.LogsBatchQueryResult;
import com.azure.monitor.query.models.LogsBatchQueryResultCollection;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableCell;
import com.azure.monitor.query.models.LogsTableRow;
import com.azure.monitor.query.models.Metric;
import com.azure.monitor.query.models.MetricsQueryOptions;
import com.azure.monitor.query.models.MetricsQueryResult;
import com.azure.monitor.query.models.QueryTimeSpan;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        LogsQueryAsyncClient logsQueryAsyncClient = new LogsQueryClientBuilder()
            .credential(tokenCredential)
            .buildAsyncClient();
    }

    /**
     * Sample for creating sync and async clients for querying metrics.
     */
    public void createMetricsClients() {
        TokenCredential tokenCredential = null;

        MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        MetricsQueryAsyncClient metricsQueryAsyncClient = new MetricsQueryClientBuilder()
            .credential(tokenCredential)
            .buildAsyncClient();
    }

    /**
     * Sample to demonstrate executing a Kusto query for logs.
     */
    public void getLogsQuery() {
        TokenCredential tokenCredential = null;

        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        LogsQueryResult queryResults = logsQueryClient.queryLogs("{workspace-id}", "{kusto-query}",
            new QueryTimeSpan(Duration.ofDays(2)));
        System.out.println("Number of tables = " + queryResults.getLogsTables().size());

        // Sample to iterate over all cells in the table
        for (LogsTable table : queryResults.getLogsTables()) {
            for (LogsTableCell tableCell : table.getAllTableCells()) {
                System.out.println("Column = " + tableCell.getColumnName() + "; value = " + tableCell.getValueAsString());
            }
        }
    }

    /**
     * Sample to demonstrate executing a batch of Kusto queries for logs.
     */
    public void getLogsQueryBatch() {
        TokenCredential tokenCredential = null;

        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        LogsBatchQuery logsBatchQuery = new LogsBatchQuery()
            .addQuery("{workspace-id}", "{query-1}", new QueryTimeSpan(Duration.ofDays(2)))
            .addQuery("{workspace-id}", "{query-2}", new QueryTimeSpan(Duration.ofDays(30)));

        LogsBatchQueryResultCollection batchResultCollection = logsQueryClient
            .queryLogsBatchWithResponse(logsBatchQuery, Context.NONE).getValue();

        List<LogsBatchQueryResult> responses = batchResultCollection.getBatchResults();

        for (LogsBatchQueryResult response : responses) {
            LogsQueryResult queryResult = response.getQueryResult();

            // Sample to iterate by row
            for (LogsTable table : queryResult.getLogsTables()) {
                for (LogsTableRow row : table.getTableRows()) {
                    System.out.println("Row index " + row.getRowIndex());
                    row.getTableRow()
                        .forEach(cell -> System.out.println("Column = " + cell.getColumnName() + "; value = " + cell.getValueAsString()));
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

        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        // set request options: server timeout, rendering, statistics
        LogsQueryOptions options = new LogsQueryOptions("{workspace-id}",
            "{query}", new QueryTimeSpan(Duration.ofDays(2)))
            .setServerTimeout(Duration.ofMinutes(10));

        // make service call with these request options set as filter header
        Response<LogsQueryResult> response = logsQueryClient.queryLogsWithResponse(options, Context.NONE);
        LogsQueryResult logsQueryResult = response.getValue();

        // Sample to iterate by row
        for (LogsTable table : logsQueryResult.getLogsTables()) {
            for (LogsTableRow row : table.getTableRows()) {
                System.out.println("Row index " + row.getRowIndex());
                row.getTableRow()
                    .forEach(cell -> System.out.println("Column = " + cell.getColumnName() + "; value = " + cell.getValueAsString()));
            }
        }
    }

    /**
     * Sample to demonstrate querying Azure Monitor for metrics.
     */
    public void getMetrics() {
        TokenCredential tokenCredential = null;

        MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        Response<MetricsQueryResult> metricsResponse = metricsQueryClient
            .queryMetricsWithResponse(
                "{resource-id}",
                Arrays.asList("SuccessfulCalls"),
                new MetricsQueryOptions()
                    .setMetricsNamespace("Microsoft.CognitiveServices/accounts")
                    .setTimeSpan(new QueryTimeSpan(Duration.ofDays(30)))
                    .setInterval(Duration.ofHours(1))
                    .setTop(100)
                    .setAggregation(Arrays.asList(AggregationType.AVERAGE, AggregationType.COUNT)),
                Context.NONE);

        MetricsQueryResult metricsQueryResult = metricsResponse.getValue();
        List<Metric> metrics = metricsQueryResult.getMetrics();
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

    /**
     *
     */
    public void getLogsQueryWithColumnNameAccess() {
        TokenCredential tokenCredential = null;

        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        LogsQueryResult queryResults = logsQueryClient.queryLogs("{workspace-id}", "{kusto-query}",
            new QueryTimeSpan(Duration.ofDays(2)));
        System.out.println("Number of tables = " + queryResults.getLogsTables().size());

        // Sample to iterate over all cells in the table
        for (LogsTable table : queryResults.getLogsTables()) {
            for (LogsTableCell tableCell : table.getAllTableCells()) {
                System.out.println("Column = " + tableCell.getColumnName() + "; value = " + tableCell.getValueAsString());
            }
        }


        // Sample to iterate over each row
        for (LogsTable table : queryResults.getLogsTables()) {
            for (LogsTableRow tableRow : table.getTableRows()) {
                for (LogsTableCell tableCell : tableRow.getTableRow()) {
                    System.out.println("Column = " + tableCell.getColumnName()
                        + "; value = " + tableCell.getValueAsString());
                }
            }
        }

        // Sample to get a specific column by name
        for (LogsTable table : queryResults.getLogsTables()) {
            for (LogsTableRow tableRow : table.getTableRows()) {
                Optional<LogsTableCell> tableCell = tableRow.getColumnValue("DurationMs");
                tableCell
                    .ifPresent(logsTableCell ->
                        System.out.println("Column = " + logsTableCell.getColumnName()
                            + "; value = " + logsTableCell.getValueAsString()));
            }
        }
    }
}
