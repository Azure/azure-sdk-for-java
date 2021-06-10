// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.monitor.query.log.implementation.AzureLogAnalyticsImpl;
import com.azure.monitor.query.log.implementation.models.BatchRequest;
import com.azure.monitor.query.log.implementation.models.BatchResponse;
import com.azure.monitor.query.log.implementation.models.ErrorInfo;
import com.azure.monitor.query.log.implementation.models.ErrorResponseException;
import com.azure.monitor.query.log.implementation.models.LogQueryRequest;
import com.azure.monitor.query.log.implementation.models.LogQueryResponse;
import com.azure.monitor.query.log.implementation.models.LogQueryResult;
import com.azure.monitor.query.log.implementation.models.QueryBody;
import com.azure.monitor.query.log.implementation.models.QueryResults;
import com.azure.monitor.query.log.implementation.models.Table;
import com.azure.monitor.query.models.LogsQueryBatch;
import com.azure.monitor.query.models.LogsQueryBatchResult;
import com.azure.monitor.query.models.LogsQueryBatchResultCollection;
import com.azure.monitor.query.models.LogsQueryError;
import com.azure.monitor.query.models.LogsQueryErrorDetails;
import com.azure.monitor.query.models.LogsQueryException;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableCell;
import com.azure.monitor.query.models.LogsTableColumn;
import com.azure.monitor.query.models.LogsTableRow;
import com.azure.monitor.query.models.QueryTimeSpan;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * The asynchronous client for querying Azure Monitor logs.
 */
@ServiceClient(builder = LogsClientBuilder.class, isAsync = true)
public final class LogsAsyncClient {

    private final AzureLogAnalyticsImpl innerClient;

    /**
     * Constructor that has the inner generated client to make the service call.
     * @param innerClient The inner generated client.
     */
    LogsAsyncClient(AzureLogAnalyticsImpl innerClient) {
        this.innerClient = innerClient;
    }

    /**
     * Returns all the Azure Monitor logs matching the given query in the specified workspaceId.
     * @param workspaceId The workspaceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeSpan The time period for which the logs should be looked up.
     * @return The logs matching the query.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<LogsQueryResult> queryLogs(String workspaceId, String query, QueryTimeSpan timeSpan) {
        return queryLogsWithResponse(new LogsQueryOptions(workspaceId, query, timeSpan))
                .map(Response::getValue);
    }

    /**
     * Returns all the Azure Monitor logs matching the given query in the specified workspaceId.
     * @param options The query options.
     * @return The logs matching the query.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<LogsQueryResult>> queryLogsWithResponse(LogsQueryOptions options) {
        return withContext(context -> queryLogsWithResponse(options, context));
    }

    /**
     * Returns all the Azure Monitor logs matching the given batch of queries in the specified workspaceId.
     * @param workspaceId The workspaceId where the batch of queries should be executed.
     * @param queries A batch of Kusto queries.
     * @param timeSpan The time period for which the logs should be looked up.
     * @return A collection of query results corresponding to the input batch of queries.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<LogsQueryBatchResultCollection> queryLogsBatch(String workspaceId, List<String> queries,
                                                               QueryTimeSpan timeSpan) {
        LogsQueryBatch logsQueryBatch = new LogsQueryBatch();
        queries.forEach(query -> logsQueryBatch.addQuery(workspaceId, query, timeSpan));
        return queryLogsBatchWithResponse(logsQueryBatch).map(Response::getValue);
    }

    /**
     * Returns all the Azure Monitor logs matching the given batch of queries.
     * @param logsQueryBatch {@link LogsQueryBatch} containing a batch of queries.
     * @return A collection of query results corresponding to the input batch of queries.@return
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

        return innerClient.getQueries().batchWithResponseAsync(batchRequest, context)
                .onErrorMap(ex -> {
                    if (ex instanceof ErrorResponseException) {
                        ErrorResponseException error = (ErrorResponseException) ex;
                        ErrorInfo errorInfo = error.getValue().getError();
                        return new LogsQueryException(error.getResponse(), mapLogsQueryError(errorInfo));
                    }
                    return ex;
                })
                .map(this::convertToLogQueryBatchResult);
    }

    private Response<LogsQueryBatchResultCollection> convertToLogQueryBatchResult(Response<BatchResponse> response) {
        List<LogsQueryBatchResult> batchResults = new ArrayList<>();
        LogsQueryBatchResultCollection logsQueryBatchResultCollection = new LogsQueryBatchResultCollection(batchResults);

        BatchResponse batchResponse = response.getValue();
        for (LogQueryResponse singleQueryResponse : batchResponse.getResponses()) {
            LogsQueryBatchResult logsQueryBatchResult = new LogsQueryBatchResult(singleQueryResponse.getId(),
                    singleQueryResponse.getStatus(), getLogsQueryResult(singleQueryResponse.getBody()),
                    mapLogsQueryError(singleQueryResponse.getBody().getError()));
            batchResults.add(logsQueryBatchResult);
        }
        batchResults.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getId())));
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), logsQueryBatchResultCollection);
    }

    private LogsQueryErrorDetails mapLogsQueryError(ErrorInfo errors) {
        if (errors != null) {
            List<LogsQueryError> errorDetails = Collections.emptyList();
            if (errors.getDetails() != null) {
                errorDetails = errors.getDetails()
                        .stream()
                        .map(errorDetail -> new LogsQueryError(errorDetail.getCode(),
                                errorDetail.getMessage(),
                                errorDetail.getTarget(),
                                errorDetail.getValue(),
                                errorDetail.getResources(),
                                errorDetail.getAdditionalProperties()))
                        .collect(Collectors.toList());
            }

            ErrorInfo innerError = errors.getInnererror();
            ErrorInfo currentError = errors.getInnererror();
            while (currentError != null) {
                innerError = errors.getInnererror();
                currentError = errors.getInnererror();
            }
            String code = errors.getCode();
            if (!errors.getCode().equals(innerError.getCode())) {
                code = innerError.getCode();
            }
            return new LogsQueryErrorDetails(errors.getMessage(), code, errorDetails);
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
                sb.append(";");
            }
            sb.append("wait=");
            sb.append(options.getServerTimeout().getSeconds());
        }

        String preferHeader = sb.toString().isEmpty() ? null : sb.toString();
        QueryBody queryBody = new QueryBody(options.getQuery());
        if (options.getTimeSpan() != null) {
            queryBody.setTimespan(options.getTimeSpan().toString());
        }
        return innerClient
                .getQueries()
                .executeWithResponseAsync(options.getWorkspaceId(),
                        queryBody,
                        preferHeader,
                        context)
                .onErrorMap(ex -> {
                    if (ex instanceof ErrorResponseException) {
                        ErrorResponseException error = (ErrorResponseException) ex;
                        ErrorInfo errorInfo = error.getValue().getError();
                        return new LogsQueryException(error.getResponse(), mapLogsQueryError(errorInfo));
                    }
                    return ex;
                })
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

        if (queryResults.getTables() == null) {
            return null;
        }

        for (Table table : queryResults.getTables()) {
            List<LogsTableCell> tableCells = new ArrayList<>();
            List<LogsTableRow> tableRows = new ArrayList<>();
            List<LogsTableColumn> tableColumns = new ArrayList<>();
            LogsTable logsTable = new LogsTable(tableCells, tableRows, tableColumns);
            tables.add(logsTable);
            List<List<String>> rows = table.getRows();

            for (int i = 0; i < rows.size(); i++) {
                List<String> row = rows.get(i);
                LogsTableRow tableRow = new LogsTableRow(i, new ArrayList<>());
                tableRows.add(tableRow);
                for (int j = 0; j < row.size(); j++) {
                    LogsTableCell cell = new LogsTableCell(table.getColumns().get(j).getName(),
                            table.getColumns().get(j).getType(), j, i, row.get(j));
                    tableCells.add(cell);
                    tableRow.getTableRow().add(cell);
                }
            }
        }
        return logsQueryResult;
    }

    private LogsQueryResult getLogsQueryResult(LogQueryResult queryResults) {
        List<LogsTable> tables = new ArrayList<>();
        LogsQueryResult logsQueryResult = new LogsQueryResult(tables);

        if (queryResults.getTables() == null) {
            return null;
        }

        for (Table table : queryResults.getTables()) {
            List<LogsTableCell> tableCells = new ArrayList<>();
            List<LogsTableRow> tableRows = new ArrayList<>();
            List<LogsTableColumn> tableColumns = new ArrayList<>();
            LogsTable logsTable = new LogsTable(tableCells, tableRows, tableColumns);
            tables.add(logsTable);
            List<List<String>> rows = table.getRows();

            for (int i = 0; i < rows.size(); i++) {
                List<String> row = rows.get(i);
                LogsTableRow tableRow = new LogsTableRow(i, new ArrayList<>());
                tableRows.add(tableRow);
                for (int j = 0; j < row.size(); j++) {
                    LogsTableCell cell = new LogsTableCell(table.getColumns().get(j).getName(),
                            table.getColumns().get(j).getType(), j, i, row.get(j));
                    tableCells.add(cell);
                    tableRow.getTableRow().add(cell);
                }
            }
        }
        return logsQueryResult;
    }
}
