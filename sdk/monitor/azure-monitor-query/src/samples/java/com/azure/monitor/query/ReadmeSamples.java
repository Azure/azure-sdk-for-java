// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.experimental.models.TimeInterval;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.models.AggregationType;
import com.azure.monitor.query.models.LogsBatchQuery;
import com.azure.monitor.query.models.LogsBatchQueryResult;
import com.azure.monitor.query.models.LogsBatchQueryResults;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableCell;
import com.azure.monitor.query.models.LogsTableRow;
import com.azure.monitor.query.models.MetricResult;
import com.azure.monitor.query.models.MetricsQueryOptions;
import com.azure.monitor.query.models.MetricsQueryResult;

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
        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        LogsQueryAsyncClient logsQueryAsyncClient = new LogsQueryClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
    }

    /**
     * Sample for creating sync and async clients for querying metrics.
     */
    public void createMetricsClients() {
        MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        MetricsQueryAsyncClient metricsQueryAsyncClient = new MetricsQueryClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
    }

    /**
     * Sample to demonstrate executing a batch of Kusto queries for logs.
     */
    public void getLogsQueryBatch() {
        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        LogsBatchQuery logsBatchQuery = new LogsBatchQuery();
        String query1 = logsBatchQuery.addQuery("{workspace-id}", "{query-1}", new TimeInterval(Duration.ofDays(2)));
        String query2 = logsBatchQuery.addQuery("{workspace-id}", "{query-2}", new TimeInterval(Duration.ofDays(30)));

        LogsBatchQueryResults batchResultCollection = logsQueryClient
            .queryBatchWithResponse(logsBatchQuery, Context.NONE).getValue();

        List<LogsBatchQueryResult> responses = batchResultCollection.getBatchResults();

        for (LogsBatchQueryResult response : responses) {
            String queryId = response.getId();
            System.out.println("Response for query " + queryId);
            // Sample to iterate by row
            for (LogsTable table : response.getAllTables()) {
                for (LogsTableRow row : table.getRows()) {
                    System.out.println("Row index " + row.getRowIndex());
                    row.getRow()
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
        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // set request options: server timeout, rendering, statistics
        LogsQueryOptions options = new LogsQueryOptions()
            .setServerTimeout(Duration.ofMinutes(10));

        // make service call with these request options set as filter header
        Response<LogsQueryResult> response = logsQueryClient.queryWithResponse("{workspace-id}",
                "{query}", new TimeInterval(Duration.ofDays(2)), options, Context.NONE);
        LogsQueryResult logsQueryResult = response.getValue();

        // Sample to iterate by row
        for (LogsTable table : logsQueryResult.getAllTables()) {
            for (LogsTableRow row : table.getRows()) {
                System.out.println("Row index " + row.getRowIndex());
                row.getRow()
                    .forEach(cell -> System.out.println("Column = " + cell.getColumnName() + "; value = " + cell.getValueAsString()));
            }
        }
    }

    /**
     * Sample to demonstrate querying Azure Monitor for metrics.
     */
    public void getMetrics() {
        MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        Response<MetricsQueryResult> metricsResponse = metricsQueryClient
            .queryWithResponse(
                "{resource-id}",
                Arrays.asList("SuccessfulCalls"),
                new MetricsQueryOptions()
                    .setMetricNamespace("Microsoft.CognitiveServices/accounts")
                    .setTimeInterval(new TimeInterval(Duration.ofDays(30)))
                    .setGranularity(Duration.ofHours(1))
                    .setTop(100)
                    .setAggregations(Arrays.asList(AggregationType.AVERAGE, AggregationType.COUNT)),
                Context.NONE);

        MetricsQueryResult metricsQueryResult = metricsResponse.getValue();
        List<MetricResult> metrics = metricsQueryResult.getMetrics();
        metrics.stream()
            .forEach(metric -> {
                System.out.println(metric.getMetricName());
                System.out.println(metric.getId());
                System.out.println(metric.getResourceType());
                System.out.println(metric.getUnit());
                System.out.println(metric.getTimeSeries().size());
                System.out.println(metric.getTimeSeries().get(0).getValues().size());
                metric.getTimeSeries()
                    .stream()
                    .flatMap(ts -> ts.getValues().stream())
                    .forEach(mv -> System.out.println(mv.getTimeStamp().toString() + "; Count = " + mv.getCount()
                        + "; Average = " + mv.getAverage()));
            });
    }

    /**
     * Sample to demonstrate accessing query results.
     */
    public void getLogsQueryWithColumnNameAccess() {
        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        LogsQueryResult queryResults = logsQueryClient.query("{workspace-id}", "{kusto-query}",
            new TimeInterval(Duration.ofDays(2)));
        System.out.println("Number of tables = " + queryResults.getAllTables().size());

        // Sample to iterate over all cells in the table
        for (LogsTable table : queryResults.getAllTables()) {
            for (LogsTableCell tableCell : table.getAllTableCells()) {
                System.out.println("Column = " + tableCell.getColumnName() + "; value = " + tableCell.getValueAsString());
            }
        }

        // Sample to iterate over each row
        for (LogsTable table : queryResults.getAllTables()) {
            for (LogsTableRow tableRow : table.getRows()) {
                for (LogsTableCell tableCell : tableRow.getRow()) {
                    System.out.println("Column = " + tableCell.getColumnName()
                        + "; value = " + tableCell.getValueAsString());
                }
            }
        }

        // Sample to get a specific column by name
        for (LogsTable table : queryResults.getAllTables()) {
            for (LogsTableRow tableRow : table.getRows()) {
                Optional<LogsTableCell> tableCell = tableRow.getColumnValue("DurationMs");
                tableCell
                    .ifPresent(logsTableCell ->
                        System.out.println("Column = " + logsTableCell.getColumnName()
                            + "; value = " + logsTableCell.getValueAsString()));
            }
        }
    }

    /**
     * Sample to demonstrate reading the response as a strongly-typed object.
     */
    public void getLogsQueryResultAsModel() {
        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        List<CustomModel> results = logsQueryClient.query("{workspace-id}", "{kusto-query}",
                new TimeInterval(Duration.ofDays(2)), CustomModel.class);

        results.forEach(model -> {
            System.out.println("Time generated " + model.getTimeGenerated() + "; success = " + model.getSuccess()
                    + "; operation name = " + model.getOperationName());
        });
    }

    /**
     * Sample to demonstrate querying multiple workspaces.
     */
    public void getLogsQueryFromMultipleWorkspaces() {
        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        Response<LogsQueryResult> response = logsQueryClient.queryWithResponse("{workspace-id}", "{kusto-query}",
                new TimeInterval(Duration.ofDays(2)), new LogsQueryOptions()
                        .setAdditionalWorkspaces(Arrays.asList("{additional-workspace-identifiers}")),
                Context.NONE);
        LogsQueryResult result = response.getValue();

    }
}
