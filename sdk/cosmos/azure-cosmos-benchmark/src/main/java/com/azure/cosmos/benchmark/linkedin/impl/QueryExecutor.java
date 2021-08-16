// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl;

import com.azure.cosmos.benchmark.linkedin.impl.exceptions.CosmosDBDataAccessorException;
import com.azure.cosmos.benchmark.linkedin.impl.metrics.MetricsFactory;
import com.azure.cosmos.benchmark.linkedin.impl.models.BatchGetResult;
import com.azure.cosmos.benchmark.linkedin.impl.models.CollectionKey;
import com.azure.cosmos.benchmark.linkedin.impl.models.QueryOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.concurrent.ThreadSafe;


/**
 * Class to encapsulate the CosmosDB SQL QUERY operation for Azure Async SDK.
 *
 * @param <K> The key for the entity stored in the data store
 * @param <V> The entity stored in the data store
 */
@ThreadSafe
class QueryExecutor<K, V> {

    private static final String ERROR_MESSAGE = "Exception when performing SQL query";

    private final DataLocator _dataLocator;
    private final ResponseHandler<K, V> _responseHandler;
    private final Metrics _metrics;
    private final Clock _clock;
    private final OperationsLogger _logger;

    /**
     * This is initialized during the CosmosDBDataAccessor construction time, and
     * the constructor must not invoke any operations on the DataAccessor
     */
    QueryExecutor(final DataLocator dataLocator,
        final ResponseHandler<K, V> responseHandler,
        final MetricsFactory metricsFactory,
        final Clock clock,
        final OperationsLogger logger) {
        Preconditions.checkNotNull(metricsFactory, "The MetricsFactory is null!");
        _dataLocator = Preconditions.checkNotNull(dataLocator, "DataLocator for this entity can not be null");
        _responseHandler = Preconditions.checkNotNull(responseHandler, "The CosmosDBResponseHandler can not be null");
        _clock = Preconditions.checkNotNull(clock, "clock cannot be null");
        _logger = Preconditions.checkNotNull(logger, "The Logger can not be null");

        // Initialize the metrics prior to the first operation
        final CollectionKey activeCollection = _dataLocator.getCollection();
        _metrics = metricsFactory.getMetrics(activeCollection, Constants.METHOD_SQL_QUERY);
    }

    /**
     * Retrieve documents by their keys using SQL expression
     * @param queryOptions: queryOptions specific to GET requests.
     * @return BatchGetResult with key after fetching data using SQL expression from cosmos DB
     */
    BatchGetResult<K, V> query(QueryOptions queryOptions) throws CosmosDBDataAccessorException {
        Preconditions.checkNotNull(queryOptions, "queryOptions is null!");
        Preconditions.checkNotNull(queryOptions.getDocumentDBQuery(), "SQL query is null!");

        /*
         * For maxBufferedItemCount and maxDegreeOfParallelism: If they are set to less than 0,
         * the system automatically decides the number of items to buffer (the number of concurrent operations to run).
         */
        final CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions()
            .setMaxDegreeOfParallelism(-1)
            .setMaxBufferedItemCount(-1);

        // Explicitly set the PartitioningKey in the CosmosQueryRequestOptions if we are querying by the partitioningKey
        // This provides a hint to the SDK, and optimizes the query execution
        if (queryOptions.getPartitioningKey().isPresent()) {
            final PartitionKey partitioningKey = new PartitionKey(queryOptions.getPartitioningKey().get());
            cosmosQueryRequestOptions.setPartitionKey(partitioningKey);
        }

        final CollectionKey activeCollection = _dataLocator.getCollection();
        _metrics.logCounterMetric(Metrics.Type.CALL_COUNT);
        final String query = queryOptions.getDocumentDBQuery();
        long startTime = _clock.millis();

        try {
            final SqlQuerySpec sqlQuerySpec = new SqlQuerySpec(query,
                queryOptions.getSqlParameterList().orElse(Collections.emptyList()));

            final List<FeedResponse<ObjectNode>> responseList = _dataLocator.getAsyncContainer(activeCollection)
                .queryItems(sqlQuerySpec, cosmosQueryRequestOptions, ObjectNode.class)
                .byPage()
                .collectList()
                .block();

            if (Objects.nonNull(responseList)) {
                String activityId = "";
                if (responseList.size() > 0) {
                    activityId = responseList.get(0).getActivityId();
                }
                _logger.logDebugInfo(Constants.METHOD_SQL_QUERY, query, activeCollection, _clock.millis() - startTime,
                    activityId, null);
            }

            // Map the response as K-V values
            final BatchGetResult<K, V> result = _responseHandler.convertFeedResponse(responseList);
            if (result.getResults().size() == 0) {
                _metrics.logCounterMetric(Metrics.Type.NOT_FOUND);
            }

            return  result;
        } catch (Exception ex) {
            _metrics.error(startTime);
            throw new CosmosDBDataAccessorException.Builder()
                .setMessage(ERROR_MESSAGE)
                .setCause(ex.getCause())
                .build();
        } finally {
            _metrics.completed(startTime);
        }
    }
}
