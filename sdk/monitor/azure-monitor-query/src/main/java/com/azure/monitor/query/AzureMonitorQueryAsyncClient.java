// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.monitor.query.log.implementation.AzureLogAnalyticsImpl;
import com.azure.monitor.query.log.implementation.models.BatchRequest;
import com.azure.monitor.query.log.implementation.models.BatchResponse;
import com.azure.monitor.query.log.implementation.models.ErrorDetails;
import com.azure.monitor.query.log.implementation.models.LogQueryRequest;
import com.azure.monitor.query.log.implementation.models.LogQueryResponse;
import com.azure.monitor.query.log.implementation.models.QueryBody;
import com.azure.monitor.query.log.implementation.models.QueryResults;
import com.azure.monitor.query.log.implementation.models.Table;
import com.azure.monitor.query.metrics.implementation.MonitorManagementClientImpl;
import com.azure.monitor.query.metrics.implementation.models.Metric;
import com.azure.monitor.query.metrics.implementation.models.MetricValue;
import com.azure.monitor.query.metrics.implementation.models.MetricsResponse;
import com.azure.monitor.query.metrics.implementation.models.ResultType;
import com.azure.monitor.query.metrics.implementation.models.TimeSeriesElement;
import com.azure.monitor.query.metricsdefinitions.implementation.MetricsDefinitionsClientImpl;
import com.azure.monitor.query.models.MetricDefinition;
import com.azure.monitor.query.metricsnamespaces.implementation.MetricsNamespacesClientImpl;
import com.azure.monitor.query.models.MetricNamespace;
import com.azure.monitor.query.models.LogsQueryErrorDetails;
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
import com.azure.monitor.query.models.MetricsTimeSeriesElement;
import com.azure.monitor.query.models.MetricsValue;
import com.azure.monitor.query.models.QueryTimeSpan;
import com.azure.monitor.query.rest.AzureMonitorQueryRestClient;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;

/**
 *
 */
@ServiceClient(builder = AzureMonitorQueryClientBuilder.class, isAsync = true)
public final class AzureMonitorQueryAsyncClient {
    private final MonitorManagementClientImpl metricsClient;
    private final MetricsNamespacesClientImpl metricsNamespacesClient;
    private final MetricsDefinitionsClientImpl metricsDefinitionsClient;
    private final AzureLogAnalyticsImpl logClient;

    AzureMonitorQueryAsyncClient(AzureLogAnalyticsImpl loglogClient, MonitorManagementClientImpl metricsClient,
                                 MetricsNamespacesClientImpl metricsNamespacesClient,
                                 MetricsDefinitionsClientImpl metricsDefinitionsClient) {
        this.logClient = loglogClient;
        this.metricsClient = metricsClient;
        this.metricsNamespacesClient = metricsNamespacesClient;
        this.metricsDefinitionsClient = metricsDefinitionsClient;
    }

    /**
     * @return
     */
    public AzureMonitorQueryRestClient getRestClient() {
        return new AzureMonitorQueryRestClient();
    }

    /**
     * @param workspaceId
     * @param query
     * @param timeSpan
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<LogsQueryResult> queryLogs(String workspaceId, String query, QueryTimeSpan timeSpan) {
        return queryLogsWithResponse(new LogsQueryOptions(workspaceId, query, timeSpan))
            .map(Response::getValue);
    }

    /**
     * @param options
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<LogsQueryResult>> queryLogsWithResponse(LogsQueryOptions options) {
        return withContext(context -> queryLogsWithResponse(options, context));
    }

    /**
     * @param workspaceId
     * @param queries
     * @param timeSpan
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<LogsQueryBatchResultCollection> queryLogsBatch(String workspaceId, List<String> queries,
                                                      QueryTimeSpan timeSpan) {
        LogsQueryBatch logsQueryBatch = new LogsQueryBatch();
        queries.forEach(query -> logsQueryBatch.addQuery(workspaceId, query, timeSpan));
        return queryLogsBatchWithResponse(logsQueryBatch).map(Response::getValue);
    }

    /**
     * @param logsQueryBatch
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<LogsQueryBatchResultCollection>> queryLogsBatchWithResponse(LogsQueryBatch logsQueryBatch) {
        return queryLogsBatchWithResponse(logsQueryBatch, Context.NONE);
    }

    Mono<Response<LogsQueryBatchResultCollection>> queryLogsBatchWithResponse(LogsQueryBatch logsQueryBatch, Context context) {
        BatchRequest batchRequest = new BatchRequest();
        AtomicInteger id = new AtomicInteger();

        List<LogQueryRequest> requests = logsQueryBatch.getQueries()
            .stream()
            .map(query -> {
                QueryBody body = new QueryBody(query.getQuery())
                    .setWorkspaces(query.getWorkspaceNames())
                    .setAzureResourceIds(query.getAzureResourceIds())
                    .setQualifiedNames(query.getQualifiedWorkspaceNames())
                    .setWorkspaceIds(query.getWorkspaceIds());

                return new LogQueryRequest()
                    .setId(String.valueOf(id.incrementAndGet()))
                    .setBody(body)
                    .setWorkspace(query.getWorkspaceId())
                    .setPath("/query")
                    .setMethod("POST");
            })
            .collect(Collectors.toList());
        batchRequest.setRequests(requests);

        return logClient.getQueries().batchWithResponseAsync(batchRequest, context)
            .map(response -> convertToLogQueryBatchResult(response));
    }

    private Response<LogsQueryBatchResultCollection> convertToLogQueryBatchResult(Response<BatchResponse> response) {
        List<LogsQueryBatchResult> batchResults = new ArrayList<>();
        LogsQueryBatchResultCollection logsQueryBatchResultCollection = new LogsQueryBatchResultCollection(batchResults);

        BatchResponse batchResponse = response.getValue();
        for (LogQueryResponse singleQueryResponse : batchResponse.getResponses()) {
            LogsQueryBatchResult logsQueryBatchResult = new LogsQueryBatchResult(singleQueryResponse.getId(),
                singleQueryResponse.getStatus(), getLogsQueryResult(singleQueryResponse.getBody()),
                mapLogsQueryBatchError(singleQueryResponse.getBody().getErrors()));
            batchResults.add(logsQueryBatchResult);
        }
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), logsQueryBatchResultCollection);
    }

    private LogsQueryErrorDetails mapLogsQueryBatchError(ErrorDetails errors) {
        if (errors != null) {
            return new LogsQueryErrorDetails(errors.getMessage(), errors.getCode(), errors.getTarget());
        }
        return null;
    }

    Mono<Response<LogsQueryResult>> queryLogsWithResponse(LogsQueryOptions options, Context context) {
        StringBuilder sb = new StringBuilder();
        if (options.isIncludeRendering()) {
            sb.append("include-render=true");
        }

        if (options.isIncludeStatistics()) {
            if (sb.length() > 0) {
                sb.append(";");
            }
            sb.append("include-statistics=true");
        }

        if (options.getServerTimeout() != null) {
            if (sb.length() > 0) {
                sb.append(";");            }
            sb.append("wait=");
            sb.append(options.getServerTimeout().getSeconds());
        }

        String preferHeader = sb.toString().isEmpty() ? null : sb.toString();
        QueryBody queryBody = new QueryBody(options.getQuery());
        if (options.getTimeSpan() != null) {
            queryBody.setTimespan(options.getTimeSpan().toString());
        }
        return logClient
            .getQueries()
            .executeWithResponseAsync(options.getWorkspaceId(),
                queryBody,
                preferHeader,
                context)
            .map(this::convertToLogQueryResult);
    }

    private Response<LogsQueryResult> convertToLogQueryResult(Response<QueryResults> response) {
        QueryResults queryResults = response.getValue();
        LogsQueryResult logsQueryResult = getLogsQueryResult(queryResults);
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), logsQueryResult);
    }

    private LogsQueryResult getLogsQueryResult(QueryResults queryResults) {
        List<LogsTable> tables = new ArrayList<>();
        LogsQueryResult logsQueryResult = new LogsQueryResult(tables);

        for (Table table : queryResults.getTables()) {
            List<LogsTableCell> tableCells = new ArrayList<>();
            List<LogsTableRow> tableRows = new ArrayList<>();
            LogsTable logsTable = new LogsTable(tableCells, tableRows);
            tables.add(logsTable);
            List<List<String>> rows = table.getRows();

            for (int i = 0; i < rows.size(); i++) {
                List<String> row = rows.get(i);
                LogsTableRow tableRow = new LogsTableRow(i, new ArrayList<>());
                tableRows.add(tableRow);
                List<LogsTableCell> rowCells = new ArrayList<>();
                for (int j = 0; j < row.size(); j++) {
                    LogsTableCell cell = new LogsTableCell(table.getColumns().get(j).getName(),
                        table.getColumns().get(j).getType(), j, i, row.get(j));
                    rowCells.add(cell);
                    tableCells.add(cell);
                    tableRow.getTableRow().add(cell);
                }
            }
        }
        return logsQueryResult;
    }

    /*
    METRICS
     */


    /**
     * @param resourceUri
     * @param metricsNames
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<MetricsQueryResult> queryMetrics(String resourceUri, List<String> metricsNames) {
        return queryMetricsWithResponse(resourceUri, metricsNames, null).map(Response::getValue);
    }

    /**
     * @param resourceUri
     * @param metricsNames
     * @param options
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<MetricsQueryResult>> queryMetricsWithResponse(String resourceUri, List<String> metricsNames,
                                                              MetricsQueryOptions options) {
        return withContext(context -> queryMetricsWithResponse(resourceUri, metricsNames, options, context));
    }

    /**
     * @param resourceUri
     * @param startTime
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<MetricNamespace> listMetricsNamespace(String resourceUri, OffsetDateTime startTime) {
        return metricsNamespacesClient
            .getMetricNamespaces()
            .listAsync(resourceUri, startTime.toString());
    }

    /**
     * @param resourceUri
     * @param metricsNamespace
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<MetricDefinition> listMetricsDefinition(String resourceUri, String metricsNamespace) {
        return metricsDefinitionsClient
            .getMetricDefinitions()
            .listAsync(resourceUri, metricsNamespace);
    }

    PagedFlux<MetricNamespace> listMetricsNamespace(String resourceUri, OffsetDateTime startTime, Context context) {
        return metricsNamespacesClient
            .getMetricNamespaces()
            .listAsync(resourceUri, startTime.toString(), context);
    }

    PagedFlux<MetricDefinition> listMetricsDefinition(String resourceUri, String metricsNamespace, Context context) {
        return metricsDefinitionsClient.getMetricDefinitions()
            .listAsync(resourceUri, metricsNamespace, context);
    }

    Mono<Response<MetricsQueryResult>> queryMetricsWithResponse(String resourceUri, List<String> metricsNames,
                                                                MetricsQueryOptions options, Context context) {
        return metricsClient
            .getMetrics()
            .listWithResponseAsync(resourceUri, options.getTimespan(), options.getInterval(),
                String.join(",", metricsNames), options.getAggregation(), options.getTop(),options.getOrderby(),
                options.getFilter(), ResultType.DATA, options.getMetricsNamespace(), context)
            .map(response -> convertToMetricsQueryResult(response));
    }

    private Response<MetricsQueryResult> convertToMetricsQueryResult(Response<MetricsResponse> response) {
        MetricsResponse metricsResponse = response.getValue();
        MetricsQueryResult metricsQueryResult = new MetricsQueryResult(
            metricsResponse.getCost(), metricsResponse.getTimespan(), metricsResponse.getInterval(),
            metricsResponse.getNamespace(), metricsResponse.getResourceregion(), mapMetrics(metricsResponse.getValue()));

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), metricsQueryResult);
    }

    private List<Metrics> mapMetrics(List<Metric> value) {
        return value.stream()
            .map(metric -> new Metrics(metric.getId(), metric.getType(), metric.getUnit(), metric.getName().getValue(),
                mapTimeSeries(metric.getTimeseries())))
            .collect(Collectors.toList());
    }

    private List<MetricsTimeSeriesElement> mapTimeSeries(List<TimeSeriesElement> timeseries) {
        return timeseries.stream()
            .map(timeSeriesElement -> new MetricsTimeSeriesElement(mapMetricsData(timeSeriesElement.getData())))
            .collect(Collectors.toList());
    }

    private List<MetricsValue> mapMetricsData(List<MetricValue> data) {
        return data.stream()
            .map(metricValue -> new MetricsValue(metricValue.getTimeStamp(),
                metricValue.getAverage(), metricValue.getMinimum(), metricValue.getMaximum(), metricValue.getTotal(),
                metricValue.getCount()))
            .collect(Collectors.toList());
    }
}
