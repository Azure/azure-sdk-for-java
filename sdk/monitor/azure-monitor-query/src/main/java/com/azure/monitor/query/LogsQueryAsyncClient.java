// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ServiceResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.models.ResponseError;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.monitor.query.implementation.logs.AzureLogAnalyticsImpl;
import com.azure.monitor.query.implementation.logs.models.BatchQueryRequest;
import com.azure.monitor.query.implementation.logs.models.BatchQueryResponse;
import com.azure.monitor.query.implementation.logs.models.BatchQueryResults;
import com.azure.monitor.query.implementation.logs.models.BatchRequest;
import com.azure.monitor.query.implementation.logs.models.BatchResponse;
import com.azure.monitor.query.implementation.logs.models.ErrorInfo;
import com.azure.monitor.query.implementation.logs.models.ErrorResponseException;
import com.azure.monitor.query.implementation.logs.models.LogsQueryHelper;
import com.azure.monitor.query.implementation.logs.models.QueryBody;
import com.azure.monitor.query.implementation.logs.models.QueryResults;
import com.azure.monitor.query.implementation.logs.models.Table;
import com.azure.monitor.query.models.LogsBatchQuery;
import com.azure.monitor.query.models.LogsBatchQueryResult;
import com.azure.monitor.query.models.LogsBatchQueryResultCollection;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.LogsQueryResultStatus;
import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableCell;
import com.azure.monitor.query.models.LogsTableColumn;
import com.azure.monitor.query.models.LogsTableRow;
import com.azure.monitor.query.models.QueryTimeInterval;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * The asynchronous client for querying Azure Monitor logs.
 * <p><strong>Instantiating an asynchronous Logs query Client</strong></p>
 *
 * {@codesnippet com.azure.monitor.query.LogsQueryAsyncClient.instantiation}
 */
@ServiceClient(builder = LogsQueryClientBuilder.class, isAsync = true)
public final class LogsQueryAsyncClient {

    private static final String AZURE_RESPONSE_TIMEOUT = "azure-response-timeout";
    private static final int CLIENT_TIMEOUT_BUFFER = 5;
    private final AzureLogAnalyticsImpl innerClient;

    /**
     * Constructor that has the inner generated client to make the service call.
     * @param innerClient The inner generated client.
     */
    LogsQueryAsyncClient(AzureLogAnalyticsImpl innerClient) {
        this.innerClient = innerClient;
    }

    /**
     * Returns all the Azure Monitor logs matching the given query in the specified workspaceId.
     *
     * <p><strong>Query logs from the last 24 hours</strong></p>
     * {@codesnippet com.azure.monitor.query.LogsQueryAsyncClient.query#String-String-QueryTimeInterval}
     *
     * @param workspaceId The workspaceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeInterval The time period for which the logs should be looked up.
     * @return The logs matching the query.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<LogsQueryResult> queryWorkspace(String workspaceId, String query, QueryTimeInterval timeInterval) {
        return queryWorkspaceWithResponse(workspaceId, query, timeInterval, new LogsQueryOptions())
                .map(Response::getValue);
    }

    /**
     * Returns all the Azure Monitor logs matching the given query in the specified workspaceId.
     * @param workspaceId The workspaceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeInterval The time period for which the logs should be looked up.
     * @param type The type the result of this query should be mapped to.
     * @param <T> The type the result of this query should be mapped to.
     * @return The logs matching the query as a list of objects of type T.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<List<T>> queryWorkspace(String workspaceId, String query, QueryTimeInterval timeInterval, Class<T> type) {
        return queryWorkspace(workspaceId, query, timeInterval)
                .map(result -> LogsQueryHelper.toObject(result.getTable(), type));
    }

    /**
     * @param workspaceId The workspaceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeInterval The time period for which the logs should be looked up.
     * @param type The type the result of this query should be mapped to.
     * @param <T> The type the result of this query should be mapped to.
     * @param options The log query options to configure server timeout, set additional workspaces or enable
     * statistics and rendering information in response.
     * @return The logs matching the query as a list of objects of type T.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<List<T>> queryWorkspace(String workspaceId, String query, QueryTimeInterval timeInterval,
                                            Class<T> type, LogsQueryOptions options) {
        return queryWorkspaceWithResponse(workspaceId, query, timeInterval, options, Context.NONE)
                .map(response -> LogsQueryHelper.toObject(response.getValue().getTable(), type));
    }

    /**
     * Returns all the Azure Monitor logs matching the given query in the specified workspaceId.
     *
     * <p><strong>Query logs from the last 7 days and set the service timeout to 2 minutes</strong></p>
     *
     * {@codesnippet com.azure.monitor.query.LogsQueryAsyncClient.queryWithResponse#String-String-QueryTimeInterval-LogsQueryOptions}
     *
     * @param workspaceId The workspaceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeInterval The time period for which the logs should be looked up.
     * @param options The log query options to configure server timeout, set additional workspaces or enable
     * statistics and rendering information in response.
     * @return The logs matching the query.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<LogsQueryResult>> queryWorkspaceWithResponse(String workspaceId, String query,
                                                                      QueryTimeInterval timeInterval, LogsQueryOptions options) {
        return withContext(context -> queryWorkspaceWithResponse(workspaceId, query, timeInterval, options, context));
    }

    /**
     * Returns all the Azure Monitor logs matching the given query in the specified workspaceId.
     *
     * @param workspaceId The workspaceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeInterval The time period for which the logs should be looked up.
     * @param type The type the result of this query should be mapped to.
     * @param <T> The type the result of this query should be mapped to.
     * @param options The log query options to configure server timeout, set additional workspaces or enable
     * statistics and rendering information in response.
     * @return The logs matching the query including the HTTP response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<Response<List<T>>> queryWorkspaceWithResponse(String workspaceId, String query, QueryTimeInterval timeInterval,
                                                                  Class<T> type, LogsQueryOptions options) {
        return queryWorkspaceWithResponse(workspaceId, query, timeInterval, options)
                .map(response -> new SimpleResponse<>(response.getRequest(),
                        response.getStatusCode(), response.getHeaders(),
                        LogsQueryHelper.toObject(response.getValue().getTable(), type)));
    }

    /**
     * Returns all the Azure Monitor logs matching the given batch of queries in the specified workspaceId.
     *
     * @param workspaceId The workspaceId where the batch of queries should be executed.
     * @param queries A batch of Kusto queries.
     * @param timeInterval The time period for which the logs should be looked up.
     * @return A collection of query results corresponding to the input batch of queries.
     */
    Mono<LogsBatchQueryResultCollection> queryBatch(String workspaceId, List<String> queries,
                                                    QueryTimeInterval timeInterval) {
        LogsBatchQuery logsBatchQuery = new LogsBatchQuery();
        queries.forEach(query -> logsBatchQuery.addWorkspaceQuery(workspaceId, query, timeInterval));
        return queryBatchWithResponse(logsBatchQuery).map(Response::getValue);
    }

    /**
     * Returns all the Azure Monitor logs matching the given batch of queries.
     *
     * <p><strong>Execute a batch of logs queries</strong></p>
     *
     * {@codesnippet com.azure.monitor.query.LogsQueryAsyncClient.queryBatch#LogsBatchQuery}
     *
     * @param logsBatchQuery {@link LogsBatchQuery} containing a batch of queries.
     * @return A collection of query results corresponding to the input batch of queries.@return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<LogsBatchQueryResultCollection> queryBatch(LogsBatchQuery logsBatchQuery) {
        return queryBatchWithResponse(logsBatchQuery)
                .map(Response::getValue);
    }

    /**
     * Returns all the Azure Monitor logs matching the given batch of queries.
     * @param logsBatchQuery {@link LogsBatchQuery} containing a batch of queries.
     * @return A collection of query results corresponding to the input batch of queries.@return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<LogsBatchQueryResultCollection>> queryBatchWithResponse(LogsBatchQuery logsBatchQuery) {
        return queryBatchWithResponse(logsBatchQuery, Context.NONE);
    }

    Mono<Response<LogsBatchQueryResultCollection>> queryBatchWithResponse(LogsBatchQuery logsBatchQuery, Context context) {
        List<BatchQueryRequest> requests = LogsQueryHelper.getBatchQueries(logsBatchQuery);
        Duration maxServerTimeout = LogsQueryHelper.getMaxServerTimeout(logsBatchQuery);
        if (maxServerTimeout != null) {
            context = context.addData(AZURE_RESPONSE_TIMEOUT, maxServerTimeout.plusSeconds(CLIENT_TIMEOUT_BUFFER));
        }

        BatchRequest batchRequest = new BatchRequest(requests);

        return innerClient.getQueries().batchWithResponseAsync(batchRequest, context)
                .onErrorMap(ex -> {
                    if (ex instanceof ErrorResponseException) {
                        ErrorResponseException error = (ErrorResponseException) ex;
                        ErrorInfo errorInfo = error.getValue().getError();
                        return new HttpResponseException(error.getMessage(), error.getResponse(),
                                mapLogsQueryError(errorInfo));
                    }
                    return ex;
                })
                .map(this::convertToLogQueryBatchResult);
    }

    private Context updateContext(Duration serverTimeout, Context context) {
        if (serverTimeout != null) {
            return context.addData(AZURE_RESPONSE_TIMEOUT, serverTimeout.plusSeconds(CLIENT_TIMEOUT_BUFFER));
        }
        return context;
    }

    private Response<LogsBatchQueryResultCollection> convertToLogQueryBatchResult(Response<BatchResponse> response) {
        List<LogsBatchQueryResult> batchResults = new ArrayList<>();
        LogsBatchQueryResultCollection logsBatchQueryResultCollection = new LogsBatchQueryResultCollection(batchResults);

        BatchResponse batchResponse = response.getValue();

        for (BatchQueryResponse singleQueryResponse : batchResponse.getResponses()) {

            BatchQueryResults queryResults = singleQueryResponse.getBody();
            LogsQueryResult logsQueryResult = getLogsQueryResult(queryResults.getTables(),
                    queryResults.getStatistics(), queryResults.getRender(), queryResults.getError());
            LogsBatchQueryResult logsBatchQueryResult = new LogsBatchQueryResult(singleQueryResponse.getId(),
                    singleQueryResponse.getStatus(), logsQueryResult.getAllTables(), logsQueryResult.getStatistics(),
                    logsQueryResult.getVisualization(), logsQueryResult.getError());
            batchResults.add(logsBatchQueryResult);
        }
        batchResults.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getId())));
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), logsBatchQueryResultCollection);
    }

    private ResponseError mapLogsQueryError(ErrorInfo errors) {
        if (errors != null) {
            ErrorInfo innerError = errors.getInnererror();
            ErrorInfo currentError = errors.getInnererror();
            while (currentError != null) {
                innerError = currentError.getInnererror();
                currentError = currentError.getInnererror();
            }
            String code = errors.getCode();
            if (errors.getCode() != null && innerError != null && errors.getCode().equals(innerError.getCode())) {
                code = innerError.getCode();
            }
            return new ResponseError(code, errors.getMessage());
        }

        return null;
    }

    Mono<Response<LogsQueryResult>> queryWorkspaceWithResponse(String workspaceId, String query, QueryTimeInterval timeInterval,
                                                               LogsQueryOptions options, Context context) {
        String preferHeader = LogsQueryHelper.buildPreferHeaderString(options);
        context = updateContext(options.getServerTimeout(), context);

        QueryBody queryBody = new QueryBody(query);
        if (timeInterval != null) {
            queryBody.setTimespan(LogsQueryHelper.toIso8601Format(timeInterval));
        }
        queryBody.setWorkspaces(getAllWorkspaces(options));
        return innerClient
                .getQueries()
                .executeWithResponseAsync(workspaceId,
                        queryBody,
                        preferHeader,
                        context)
                .onErrorMap(ex -> {
                    if (ex instanceof ErrorResponseException) {
                        ErrorResponseException error = (ErrorResponseException) ex;
                        ErrorInfo errorInfo = error.getValue().getError();
                        return new HttpResponseException(error.getMessage(), error.getResponse(),
                                mapLogsQueryError(errorInfo));
                    }
                    return ex;
                })
                .map(this::convertToLogQueryResult)
                .handle((Response<LogsQueryResult> response, SynchronousSink<Response<LogsQueryResult>> sink) -> {
                    if (response.getValue().getQueryResultStatus() == LogsQueryResultStatus.PARTIAL_FAILURE) {
                        sink.error(new ServiceResponseException("Query execution returned partial errors. To "
                                + "disable exceptions on partial errors, set disableExceptionOnPartialErrors in "
                                + "LogsQueryOptions to true."));
                    } else {
                        sink.next(response);
                    }
                });
    }

    private Response<LogsQueryResult> convertToLogQueryResult(Response<QueryResults> response) {
        QueryResults queryResults = response.getValue();
        LogsQueryResult logsQueryResult = getLogsQueryResult(queryResults.getTables(), queryResults.getStatistics(),
                queryResults.getRender(), queryResults.getError());
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), logsQueryResult);
    }

    private LogsQueryResult getLogsQueryResult(List<Table> innerTables, Object innerStats,
                                               Object innerVisualization, ErrorInfo innerError) {
        List<LogsTable> tables = null;

        if (innerTables != null) {
            tables = new ArrayList<>();
            for (Table table : innerTables) {
                List<LogsTableCell> tableCells = new ArrayList<>();
                List<LogsTableRow> tableRows = new ArrayList<>();
                List<LogsTableColumn> tableColumns = new ArrayList<>();
                LogsTable logsTable = new LogsTable(tableCells, tableRows, tableColumns);
                tables.add(logsTable);
                List<List<Object>> rows = table.getRows();

                for (int i = 0; i < rows.size(); i++) {
                    List<Object> row = rows.get(i);
                    LogsTableRow tableRow = new LogsTableRow(i, new ArrayList<>());
                    tableRows.add(tableRow);
                    for (int j = 0; j < row.size(); j++) {
                        LogsTableCell cell = new LogsTableCell(table.getColumns().get(j).getName(),
                                table.getColumns().get(j).getType(), j, i, row.get(j));
                        tableCells.add(cell);
                        tableRow.getRow().add(cell);
                    }
                }
            }
        }

        BinaryData queryStatistics = null;

        if (innerStats != null) {
            queryStatistics = BinaryData.fromObject(innerStats);
        }

        BinaryData queryVisualization = null;
        if (innerVisualization != null) {
            queryVisualization = BinaryData.fromObject(innerVisualization);
        }

        LogsQueryResult logsQueryResult = new LogsQueryResult(tables, queryStatistics, queryVisualization,
                mapLogsQueryError(innerError));
        return logsQueryResult;
    }

    private List<String> getAllWorkspaces(LogsQueryOptions body) {
        List<String> allWorkspaces = new ArrayList<>();
        if (!CoreUtils.isNullOrEmpty(body.getAdditionalWorkspaces())) {
            allWorkspaces.addAll(body.getAdditionalWorkspaces());
        }
        return allWorkspaces;
    }
}
