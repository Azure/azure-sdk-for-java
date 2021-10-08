// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.models.AggregationType;
import com.azure.monitor.query.models.LogsBatchQuery;
import com.azure.monitor.query.models.LogsBatchQueryResult;
import com.azure.monitor.query.models.LogsBatchQueryResultCollection;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.LogsQueryResultStatus;
import com.azure.monitor.query.models.LogsTableRow;
import com.azure.monitor.query.models.MetricResult;
import com.azure.monitor.query.models.MetricValue;
import com.azure.monitor.query.models.MetricsQueryOptions;
import com.azure.monitor.query.models.MetricsQueryResult;
import com.azure.monitor.query.models.QueryTimeInterval;
import com.azure.monitor.query.models.TimeSeriesElement;

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
     * Sample to query logs using a single Kusto query.
     */
    public void queryLogs() {
        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        LogsQueryResult queryResults = logsQueryClient.queryWorkspace("{workspace-id}", "{kusto-query}",
                new QueryTimeInterval(Duration.ofDays(2)));

        for (LogsTableRow row : queryResults.getTable().getRows()) {
            System.out.println(row.getColumnValue("OperationName") + " " + row.getColumnValue("ResourceGroup"));
        }
    }

    /**
     * A custom model type to map logs query result to an object.
     */
    public class CustomLogModel {
        private String resourceGroup;
        private String operationName;

        public String getResourceGroup() {
            return resourceGroup;
        }

        public String getOperationName() {
            return operationName;
        }
    }

    /**
     * Sample to query logs and convert the response to a strongly-typed list of objects.
     */
    public void queryLogsAsModel() {
        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        List<CustomLogModel> customLogModels = logsQueryClient.queryWorkspace("{workspace-id}", "{kusto-query}",
                new QueryTimeInterval(Duration.ofDays(2)), CustomLogModel.class);

        for (CustomLogModel customLogModel : customLogModels) {
            System.out.println(customLogModel.getOperationName() + " " + customLogModel.getResourceGroup());
        }
    }

    /**
     * Sample to execute a batch of logs queries.
     */
    public void queryBatch() {
        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        LogsBatchQuery logsBatchQuery = new LogsBatchQuery();
        String query1 = logsBatchQuery.addWorkspaceQuery("{workspace-id}", "{query-1}", new QueryTimeInterval(Duration.ofDays(2)));
        String query2 = logsBatchQuery.addWorkspaceQuery("{workspace-id}", "{query-2}", new QueryTimeInterval(Duration.ofDays(30)));
        String query3 = logsBatchQuery.addWorkspaceQuery("{workspace-id}", "{query-3}", new QueryTimeInterval(Duration.ofDays(10)));

        LogsBatchQueryResultCollection batchResults = logsQueryClient
                .queryBatchWithResponse(logsBatchQuery, Context.NONE).getValue();

        LogsBatchQueryResult query1Result = batchResults.getResult(query1);
        for (LogsTableRow row : query1Result.getTable().getRows()) {
            System.out.println(row.getColumnValue("OperationName") + " " + row.getColumnValue("ResourceGroup"));
        }

        List<CustomLogModel> customLogModels = batchResults.getResult(query2, CustomLogModel.class);
        for (CustomLogModel customLogModel : customLogModels) {
            System.out.println(customLogModel.getOperationName() + " " + customLogModel.getResourceGroup());
        }

        LogsBatchQueryResult query3Result = batchResults.getResult(query3);
        if (query3Result.getQueryResultStatus() == LogsQueryResultStatus.FAILURE) {
            System.out.println(query3Result.getError().getMessage());
        }
    }

    /**
     * Sample to demonstrate executing an advanced Kusto query for logs that requires a long time to complete and
     * requires extending server timeout.
     */
    public void getLogsWithServerTimeout() {
        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // set request options: server timeout
        LogsQueryOptions options = new LogsQueryOptions()
            .setServerTimeout(Duration.ofMinutes(10));

        Response<LogsQueryResult> response = logsQueryClient.queryWorkspaceWithResponse("{workspace-id}",
                "{kusto-query}", new QueryTimeInterval(Duration.ofDays(2)), options, Context.NONE);
    }

    /**
     * Sample to demonstrate querying multiple workspaces.
     */
    public void getLogsQueryFromMultipleWorkspaces() {
        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        Response<LogsQueryResult> response = logsQueryClient.queryWorkspaceWithResponse("{workspace-id}", "{kusto-query}",
                new QueryTimeInterval(Duration.ofDays(2)), new LogsQueryOptions()
                        .setAdditionalWorkspaces(Arrays.asList("{additional-workspace-identifiers}")),
                Context.NONE);
        LogsQueryResult result = response.getValue();
    }


    /**
     * Sample to demonstrate querying Azure Monitor for metrics.
     */
    public void getMetrics() {
        MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        MetricsQueryResult metricsQueryResult = metricsQueryClient.queryResource("{resource-uri}",
                Arrays.asList("SuccessfulCalls", "TotalCalls"));

        for (MetricResult metric : metricsQueryResult.getMetrics()) {
            System.out.println("Metric name " + metric.getMetricName());
            for (TimeSeriesElement timeSeriesElement : metric.getTimeSeries()) {
                System.out.println("Dimensions " + timeSeriesElement.getMetadata());
                for (MetricValue metricValue : timeSeriesElement.getValues()) {
                    System.out.println(metricValue.getTimeStamp() + " " + metricValue.getTotal());
                }
            }
        }
    }

    /**
     * Sample to demonstrate querying Azure Monitor for metrics with advanced options.
     */
    public void getMetricsWithOptions() {
        MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        Response<MetricsQueryResult> metricsResponse = metricsQueryClient
            .queryResourceWithResponse("{resource-id}", Arrays.asList("SuccessfulCalls", "TotalCalls"),
                new MetricsQueryOptions()
                    .setGranularity(Duration.ofHours(1))
                    .setAggregations(Arrays.asList(AggregationType.AVERAGE, AggregationType.COUNT)),
                Context.NONE);

        MetricsQueryResult metricsQueryResult = metricsResponse.getValue();

        for (MetricResult metric : metricsQueryResult.getMetrics()) {
            System.out.println("Metric name " + metric.getMetricName());
            for (TimeSeriesElement timeSeriesElement : metric.getTimeSeries()) {
                System.out.println("Dimensions " + timeSeriesElement.getMetadata());
                for (MetricValue metricValue : timeSeriesElement.getValues()) {
                    System.out.println(metricValue.getTimeStamp() + " " + metricValue.getTotal());
                }
            }
        }
    }
}
