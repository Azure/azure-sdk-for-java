// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.monitor.query.implementation.logs.AzureLogAnalyticsImpl;
import com.azure.monitor.query.implementation.logs.models.BatchQueryRequest;
import com.azure.monitor.query.implementation.logs.models.BatchRequest;
import com.azure.monitor.query.implementation.logs.models.BatchResponse;
import com.azure.monitor.query.implementation.logs.models.LogsQueryHelper;
import com.azure.monitor.query.implementation.logs.models.QueryBody;
import com.azure.monitor.query.implementation.logs.models.QueryResults;
import com.azure.monitor.query.models.LogsBatchQuery;
import com.azure.monitor.query.models.LogsBatchQueryResultCollection;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.QueryTimeInterval;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static com.azure.monitor.query.implementation.logs.models.LogsQueryHelper.AZURE_RESPONSE_TIMEOUT;
import static com.azure.monitor.query.implementation.logs.models.LogsQueryHelper.CLIENT_TIMEOUT_BUFFER;
import static com.azure.monitor.query.implementation.logs.models.LogsQueryHelper.buildPreferHeaderString;
import static com.azure.monitor.query.implementation.logs.models.LogsQueryHelper.convertToLogQueryBatchResult;
import static com.azure.monitor.query.implementation.logs.models.LogsQueryHelper.convertToLogQueryResult;
import static com.azure.monitor.query.implementation.logs.models.LogsQueryHelper.getAllWorkspaces;
import static com.azure.monitor.query.implementation.logs.models.LogsQueryHelper.updateContext;

/**
 * <p>Provides a synchronous service client for querying logs in the Azure Monitor Service.</p>
 *
 * <p>The LogsQueryClient is a synchronous client that provides methods to execute Kusto queries against
 * Azure Monitor logs. It provides methods to query logs in a specific workspace, execute a batch of queries, and
 * query logs for a specific Azure resource.</p>
 *
 * <h2>Getting Started</h2>
 *
 * <p>Authenticating and building instances of this client are handled by {@link LogsQueryClientBuilder}.
 * his sample shows how to authenticate and build a LogsQueryClient instance using LogQueryClientBuilder.</p>
 *
 * <!-- src_embed com.azure.monitor.query.LogsQueryClient.instantiation -->
 * <pre>
 * LogsQueryClient logsQueryClient = new LogsQueryClientBuilder&#40;&#41;
 *         .credential&#40;tokenCredential&#41;
 *         .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.monitor.query.LogsQueryClient.instantiation -->
 *
 * <p>For more information on building and authenticating, see the {@link LogsQueryClientBuilder} documentation.</p>
 *
 * <h3>Client Usage</h3>
 *
 * <p>
 *     For more information on how to use this client, see the following method documentation:
 * </p>
 *
 * <ul>
 *     <li>
 *         {@link LogsQueryClient#queryWorkspace(String, String, QueryTimeInterval) queryWorkspace(String, String, QueryTimeInterval)} - Query logs from a workspace.
 *     </li>
 *     <li>
 *         {@link LogsQueryClient#queryWorkspaceWithResponse(String, String, QueryTimeInterval, LogsQueryOptions, Context) queryWorkspaceWithResponse(String, String, QueryTimeInterval, LogsQueryOptions, Context)} - Query logs from a workspace using query options and context with service response returned.
 *     </li>
 *     <li>
 *         {@link LogsQueryClient#queryBatch(LogsBatchQuery) queryBatch(LogsBatchQuery)} - Execute a batch of logs queries.
 *     </li>
 *     <li>
 *         {@link LogsQueryClient#queryBatchWithResponse(LogsBatchQuery, Context) queryBatchWithResponse(LogsBatchQuery, Context)} - Execute a batch of logs queries with context and service response returned.
 *     </li>
 *     <li>
 *         {@link LogsQueryClient#queryResource(String, String, QueryTimeInterval) queryResource(String, String, QueryTimeInterval)} - Query logs for an Azure resource.
 *     </li>
 *     <li>
 *         {@link LogsQueryClient#queryResourceWithResponse(String, String, QueryTimeInterval, LogsQueryOptions, Context) queryResourceWithResponse(String, String, QueryTimeInterval, LogsQueryOptions, Context)} - Query logs for an Azure resource with query options and context with service response returned.
 *     </li>
 * </ul>
 *
 * @see com.azure.monitor.query
 * @see LogsQueryClientBuilder
 */
@ServiceClient(builder = LogsQueryClientBuilder.class)
public final class LogsQueryClient {

    private final AzureLogAnalyticsImpl serviceClient;

    /**
     * Constructor that takes the inner service client.
     * @param serviceClient The service client.
     */
    LogsQueryClient(AzureLogAnalyticsImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Returns all the Azure Monitor logs matching the given query in the specified workspaceId.
     *
     * <p><strong>Query logs from the last 24 hours</strong></p>
     *
     * <!-- src_embed com.azure.monitor.query.LogsQueryClient.query#String-String-QueryTimeInterval -->
     * <pre>
     * LogsQueryResult queryResult = logsQueryClient.queryWorkspace&#40;&quot;&#123;workspace-id&#125;&quot;, &quot;&#123;kusto-query&#125;&quot;,
     *         QueryTimeInterval.LAST_DAY&#41;;
     * for &#40;LogsTableRow row : queryResult.getTable&#40;&#41;.getRows&#40;&#41;&#41; &#123;
     *     System.out.println&#40;row.getRow&#40;&#41;
     *             .stream&#40;&#41;
     *             .map&#40;LogsTableCell::getValueAsString&#41;
     *             .collect&#40;Collectors.joining&#40;&quot;,&quot;&#41;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.monitor.query.LogsQueryClient.query#String-String-QueryTimeInterval -->
     * @param workspaceId The workspaceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeInterval The time period for which the logs should be looked up.
     * @return The logs matching the query.
     * @throws NullPointerException if {@code workspaceId} or {@code query} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public LogsQueryResult queryWorkspace(String workspaceId, String query, QueryTimeInterval timeInterval) {
        return queryWorkspaceWithResponse(workspaceId, query, timeInterval, new LogsQueryOptions(), Context.NONE)
            .getValue();
    }

    /**
     * Returns all the Azure Monitor logs matching the given query in the specified workspaceId.
     * @param workspaceId The workspaceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeInterval The time period for which the logs should be looked up.
     * @param type The type the result of this query should be mapped to.
     * @param <T> The type the result of this query should be mapped to.
     * @return The logs matching the query as a list of objects of type T.
     * @throws NullPointerException if {@code workspaceId} or {@code query} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> List<T> queryWorkspace(String workspaceId, String query, QueryTimeInterval timeInterval, Class<T> type) {
        LogsQueryResult logsQueryResult
            = queryWorkspaceWithResponse(workspaceId, query, timeInterval, new LogsQueryOptions(), Context.NONE)
                .getValue();
        if (logsQueryResult != null) {
            return LogsQueryHelper.toObject(logsQueryResult.getTable(), type);
        }
        return null;
    }

    /**
     * Returns all the Azure Monitor logs matching the given query in the specified workspaceId.
     * @param workspaceId The workspaceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeInterval The time period for which the logs should be looked up.
     * @param type The type the result of this query should be mapped to.
     * @param options The log query options to configure server timeout, set additional workspaces or enable
     * statistics and rendering information in response.
     * @param <T> The type the result of this query should be mapped to.
     * @return The logs matching the query as a list of objects of type T.
     * @throws NullPointerException if {@code workspaceId} or {@code query} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> List<T> queryWorkspace(String workspaceId, String query, QueryTimeInterval timeInterval, Class<T> type,
        LogsQueryOptions options) {
        LogsQueryResult logsQueryResult
            = queryWorkspaceWithResponse(workspaceId, query, timeInterval, options, Context.NONE).getValue();
        if (logsQueryResult != null) {
            return LogsQueryHelper.toObject(logsQueryResult.getTable(), type);
        }
        return null;
    }

    /**
     * Returns all the Azure Monitor logs matching the given query in the specified workspaceId.
     *
     * <p><strong>Query logs from the last 7 days and set the service timeout to 2 minutes</strong></p>
     *
     * <!-- src_embed com.azure.monitor.query.LogsQueryClient.queryWithResponse#String-String-QueryTimeInterval-LogsQueryOptions-Context -->
     * <pre>
     * Response&lt;LogsQueryResult&gt; queryResult = logsQueryClient.queryWorkspaceWithResponse&#40;&quot;&#123;workspace-id&#125;&quot;,
     *         &quot;&#123;kusto-query&#125;&quot;,
     *         QueryTimeInterval.LAST_7_DAYS,
     *         new LogsQueryOptions&#40;&#41;.setServerTimeout&#40;Duration.ofMinutes&#40;2&#41;&#41;,
     *         Context.NONE&#41;;
     *
     * for &#40;LogsTableRow row : queryResult.getValue&#40;&#41;.getTable&#40;&#41;.getRows&#40;&#41;&#41; &#123;
     *     System.out.println&#40;row.getRow&#40;&#41;
     *             .stream&#40;&#41;
     *             .map&#40;LogsTableCell::getValueAsString&#41;
     *             .collect&#40;Collectors.joining&#40;&quot;,&quot;&#41;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.monitor.query.LogsQueryClient.queryWithResponse#String-String-QueryTimeInterval-LogsQueryOptions-Context -->
     * @param workspaceId The workspaceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeInterval The time period for which the logs should be looked up.
     * @param options The log query options to configure server timeout, set additional workspaces or enable
     * statistics and rendering information in response.
     * @param context Additional context that is passed through the Http pipeline during the service call. If no
     * additional context is required, pass {@link Context#NONE} instead.
     * @return The logs matching the query including the HTTP response.
     * @throws NullPointerException if {@code workspaceId} or {@code query} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LogsQueryResult> queryWorkspaceWithResponse(String workspaceId, String query,
        QueryTimeInterval timeInterval, LogsQueryOptions options, Context context) {
        return queryWorkspaceWithResponseInternal(workspaceId, query, timeInterval, options, context);
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
     * @param context Additional context that is passed through the Http pipeline during the service call. If no
     * additional context is required, pass {@link Context#NONE} instead.
     * @return The logs matching the query including the HTTP response.
     * @throws NullPointerException if {@code workspaceId} or {@code query} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Response<List<T>> queryWorkspaceWithResponse(String workspaceId, String query,
        QueryTimeInterval timeInterval, Class<T> type, LogsQueryOptions options, Context context) {
        Response<LogsQueryResult> response
            = queryWorkspaceWithResponseInternal(workspaceId, query, timeInterval, options, context);
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            LogsQueryHelper.toObject(response.getValue().getTable(), type));
    }

    Response<LogsQueryResult> queryWorkspaceWithResponseInternal(String workspaceId, String query,
        QueryTimeInterval timeInterval, LogsQueryOptions options, Context context) {

        Objects.requireNonNull(workspaceId, "'workspaceId' cannot be null.");
        Objects.requireNonNull(query, "'query' cannot be null.");
        String preferHeader = buildPreferHeaderString(options);
        context = updateContext(options.getServerTimeout(), context);

        QueryBody queryBody = new QueryBody(query);
        if (timeInterval != null) {
            queryBody.setTimespan(LogsQueryHelper.toIso8601Format(timeInterval));
        }
        queryBody.setWorkspaces(getAllWorkspaces(options));
        Response<QueryResults> queryResultsResponse
            = serviceClient.getQueries().executeWithResponse(workspaceId, queryBody, preferHeader, context);
        return convertToLogQueryResult(queryResultsResponse);
    }

    /**
     * Returns all the Azure Monitor logs matching the given batch of queries.
     *
     * <p><strong>Execute a batch of logs queries</strong></p>
     *
     * <!-- src_embed com.azure.monitor.query.LogsQueryClient.queryBatch#LogsBatchQuery -->
     * <pre>
     * LogsBatchQuery batchQuery = new LogsBatchQuery&#40;&#41;;
     * String queryId1 = batchQuery.addWorkspaceQuery&#40;&quot;&#123;workspace-id-1&#125;&quot;, &quot;&#123;kusto-query-1&#125;&quot;, QueryTimeInterval.LAST_DAY&#41;;
     * String queryId2 = batchQuery.addWorkspaceQuery&#40;&quot;&#123;workspace-id-2&#125;&quot;, &quot;&#123;kusto-query-2&#125;&quot;,
     *         QueryTimeInterval.LAST_7_DAYS, new LogsQueryOptions&#40;&#41;.setServerTimeout&#40;Duration.ofMinutes&#40;2&#41;&#41;&#41;;
     *
     * LogsBatchQueryResultCollection batchQueryResponse = logsQueryClient.queryBatch&#40;batchQuery&#41;;
     *
     * for &#40;LogsBatchQueryResult queryResult : batchQueryResponse.getBatchResults&#40;&#41;&#41; &#123;
     *     System.out.println&#40;&quot;Logs query result for query id &quot; + queryResult.getId&#40;&#41;&#41;;
     *     for &#40;LogsTableRow row : queryResult.getTable&#40;&#41;.getRows&#40;&#41;&#41; &#123;
     *         System.out.println&#40;row.getRow&#40;&#41;
     *                 .stream&#40;&#41;
     *                 .map&#40;LogsTableCell::getValueAsString&#41;
     *                 .collect&#40;Collectors.joining&#40;&quot;,&quot;&#41;&#41;&#41;;
     *     &#125;
     * &#125;
     * </pre>
     * <!-- end com.azure.monitor.query.LogsQueryClient.queryBatch#LogsBatchQuery -->
     * @param logsBatchQuery {@link LogsBatchQuery} containing a batch of queries.
     * @return A collection of query results corresponding to the input batch of queries.@return
     * @throws NullPointerException if {@code logsBatchQuery} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public LogsBatchQueryResultCollection queryBatch(LogsBatchQuery logsBatchQuery) {
        return queryBatchWithResponse(logsBatchQuery, Context.NONE).getValue();
    }

    /**
     * Returns all the Azure Monitor logs matching the given batch of queries.
     *
     * @param logsBatchQuery {@link LogsBatchQuery} containing a batch of queries.
     * @param context Additional context that is passed through the Http pipeline during the service call. If no
     * additional context is required, pass {@link Context#NONE} instead.
     * @return A collection of query results corresponding to the input batch of queries.@return
     * @throws NullPointerException if {@code logsBatchQuery} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LogsBatchQueryResultCollection> queryBatchWithResponse(LogsBatchQuery logsBatchQuery,
        Context context) {
        return queryBatchWithResponseInternal(logsBatchQuery, context);
    }

    private Response<LogsBatchQueryResultCollection> queryBatchWithResponseInternal(LogsBatchQuery logsBatchQuery,
        Context context) {
        Objects.requireNonNull(logsBatchQuery, "'logsBatchQuery' cannot be null.");
        List<BatchQueryRequest> requests = LogsQueryHelper.getBatchQueries(logsBatchQuery);
        Duration maxServerTimeout = LogsQueryHelper.getMaxServerTimeout(logsBatchQuery);
        if (maxServerTimeout != null) {
            context = context.addData(AZURE_RESPONSE_TIMEOUT, maxServerTimeout.plusSeconds(CLIENT_TIMEOUT_BUFFER));
        }

        BatchRequest batchRequest = new BatchRequest(requests);

        Response<BatchResponse> batchResponseResponse
            = serviceClient.getQueries().batchWithResponse(batchRequest, context);
        return convertToLogQueryBatchResult(batchResponseResponse);
    }

    /**
     * Returns all the Azure Monitor logs matching the given query for an Azure resource.
     *
     * <p><strong>Query logs from the last 24 hours</strong></p>
     *
     * <!-- src_embed com.azure.monitor.query.LogsQueryClient.queryResource#String-String-QueryTimeInterval -->
     * <pre>
     * LogsQueryResult queryResult = logsQueryClient.queryResource&#40;&quot;&#123;resource-id&#125;&quot;, &quot;&#123;kusto-query&#125;&quot;,
     *     QueryTimeInterval.LAST_DAY&#41;;
     * for &#40;LogsTableRow row : queryResult.getTable&#40;&#41;.getRows&#40;&#41;&#41; &#123;
     *     System.out.println&#40;row.getRow&#40;&#41;
     *         .stream&#40;&#41;
     *         .map&#40;LogsTableCell::getValueAsString&#41;
     *         .collect&#40;Collectors.joining&#40;&quot;,&quot;&#41;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.monitor.query.LogsQueryClient.queryResource#String-String-QueryTimeInterval -->
     * @param resourceId The resourceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeInterval The time period for which the logs should be looked up.
     * @return The logs matching the query.
     * @throws NullPointerException if {@code resourceId} or {@code query} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public LogsQueryResult queryResource(String resourceId, String query, QueryTimeInterval timeInterval) {
        return queryResourceWithResponse(resourceId, query, timeInterval, new LogsQueryOptions(), Context.NONE)
            .getValue();
    }

    /**
     * Returns all the Azure Monitor logs matching the given query for an Azure resource.
     * @param resourceId The resourceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeInterval The time period for which the logs should be looked up.
     * @param type The type the result of this query should be mapped to.
     * @param <T> The type the result of this query should be mapped to.
     * @return The logs matching the query as a list of objects of type T.
     * @throws NullPointerException if {@code resourceId} or {@code query} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> List<T> queryResource(String resourceId, String query, QueryTimeInterval timeInterval, Class<T> type) {
        LogsQueryResult logsQueryResult = queryResource(resourceId, query, timeInterval);
        if (logsQueryResult != null) {
            return LogsQueryHelper.toObject(logsQueryResult.getTable(), type);
        }
        return null;
    }

    /**
     * Returns all the Azure Monitor logs matching the given query for an Azure resource.
     * @param resourceId The resourceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeInterval The time period for which the logs should be looked up.
     * @param type The type the result of this query should be mapped to.
     * @param options The log query options to configure server timeout, set additional workspaces or enable
     * statistics and rendering information in response.
     * @param <T> The type the result of this query should be mapped to.
     * @return The logs matching the query as a list of objects of type T.
     * @throws NullPointerException if {@code resourceId} or {@code query} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> List<T> queryResource(String resourceId, String query, QueryTimeInterval timeInterval, Class<T> type,
        LogsQueryOptions options) {
        LogsQueryResult logsQueryResult
            = queryResourceWithResponse(resourceId, query, timeInterval, options, Context.NONE).getValue();
        if (logsQueryResult != null) {
            return LogsQueryHelper.toObject(logsQueryResult.getTable(), type);
        }
        return null;
    }

    /**
     * Returns all the Azure Monitor logs matching the given query for an Azure resource.
     *
     * <p><strong>Query logs from the last 7 days and set the service timeout to 2 minutes</strong></p>
     *
     * <!-- src_embed com.azure.monitor.query.LogsQueryClient.queryResourceWithResponse#String-String-QueryTimeInterval-LogsQueryOptions-Context -->
     * <pre>
     * Response&lt;LogsQueryResult&gt; queryResult = logsQueryClient.queryResourceWithResponse&#40;&quot;&#123;resource-id&#125;&quot;,
     *     &quot;&#123;kusto-query&#125;&quot;,
     *     QueryTimeInterval.LAST_7_DAYS,
     *     new LogsQueryOptions&#40;&#41;.setServerTimeout&#40;Duration.ofMinutes&#40;2&#41;&#41;,
     *     Context.NONE&#41;;
     *
     * for &#40;LogsTableRow row : queryResult.getValue&#40;&#41;.getTable&#40;&#41;.getRows&#40;&#41;&#41; &#123;
     *     System.out.println&#40;row.getRow&#40;&#41;
     *         .stream&#40;&#41;
     *         .map&#40;LogsTableCell::getValueAsString&#41;
     *         .collect&#40;Collectors.joining&#40;&quot;,&quot;&#41;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.monitor.query.LogsQueryClient.queryResourceWithResponse#String-String-QueryTimeInterval-LogsQueryOptions-Context -->
     * @param resourceId The resourceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeInterval The time period for which the logs should be looked up.
     * @param options The log query options to configure server timeout, set additional workspaces or enable
     * statistics and rendering information in response.
     * @param context Additional context that is passed through the Http pipeline during the service call. If no
     * additional context is required, pass {@link Context#NONE} instead.
     * @return The logs matching the query including the HTTP response.
     * @throws NullPointerException if {@code resourceId} or {@code query} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LogsQueryResult> queryResourceWithResponse(String resourceId, String query,
        QueryTimeInterval timeInterval, LogsQueryOptions options, Context context) {
        return queryResourceWithResponseInternal(resourceId, query, timeInterval, options, context);
    }

    /**
     * Returns all the Azure Monitor logs matching the given query for an Azure resource.
     *
     * @param resourceId The resourceId where the query should be executed.
     * @param query The Kusto query to fetch the logs.
     * @param timeInterval The time period for which the logs should be looked up.
     * @param type The type the result of this query should be mapped to.
     * @param <T> The type the result of this query should be mapped to.
     * @param options The log query options to configure server timeout, set additional workspaces or enable
     * statistics and rendering information in response.
     * @param context Additional context that is passed through the Http pipeline during the service call. If no
     * additional context is required, pass {@link Context#NONE} instead.
     * @return The logs matching the query including the HTTP response.
     * @throws NullPointerException if {@code resourceId} or {@code query} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Response<List<T>> queryResourceWithResponse(String resourceId, String query,
        QueryTimeInterval timeInterval, Class<T> type, LogsQueryOptions options, Context context) {
        Response<LogsQueryResult> response
            = queryResourceWithResponseInternal(resourceId, query, timeInterval, options, context);
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            LogsQueryHelper.toObject(response.getValue().getTable(), type));
    }

    private Response<LogsQueryResult> queryResourceWithResponseInternal(String resourceId, String query,
        QueryTimeInterval timeInterval, LogsQueryOptions options, Context context) {
        Objects.requireNonNull(resourceId, "'resourceId' cannot be null.");
        Objects.requireNonNull(query, "'query' cannot be null.");

        if (resourceId.startsWith("/")) {
            resourceId = resourceId.substring(1);
        }

        String preferHeader = buildPreferHeaderString(options);
        context = updateContext(options.getServerTimeout(), context);

        QueryBody queryBody = new QueryBody(query);
        if (timeInterval != null) {
            queryBody.setTimespan(LogsQueryHelper.toIso8601Format(timeInterval));
        }
        queryBody.setWorkspaces(getAllWorkspaces(options));
        Response<QueryResults> queryResultsResponse
            = serviceClient.getQueries().resourceExecuteWithResponse(resourceId, queryBody, preferHeader, context);
        return convertToLogQueryResult(queryResultsResponse);
    }

}
