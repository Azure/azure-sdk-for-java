// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.benchmark.linkedin.impl.exceptions.CosmosDBDataAccessorException;
import com.azure.cosmos.benchmark.linkedin.impl.keyextractor.KeyExtractor;
import com.azure.cosmos.benchmark.linkedin.impl.metrics.MetricsFactory;
import com.azure.cosmos.benchmark.linkedin.impl.models.CollectionKey;
import com.azure.cosmos.benchmark.linkedin.impl.models.GetRequestOptions;
import com.azure.cosmos.benchmark.linkedin.impl.models.Result;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.microsoft.applicationinsights.core.dependencies.http.HttpStatus;
import java.time.Clock;
import java.util.Objects;
import javax.annotation.concurrent.ThreadSafe;


/**
 * Class to encapsulate the CosmosDB GET operation for Azure Async SDK.
 *
 * @param <K> The key for the entity stored in the data store
 * @param <V> The entity stored in the data store
 */
@ThreadSafe
class GetExecutor<K, V> {

    private static final String ERROR_MESSAGE_FORMAT = "Exception reading the document: %s from: %s";

    private final DataLocator _dataLocator;
    private final KeyExtractor<K> _keyExtractor;
    private final ResponseHandler<K, V> _responseHandler;
    private final Metrics _metrics;
    private final Clock _clock;
    private final OperationsLogger _logger;

    /**
     * This is initialized during the CosmosDBDataAccessor construction time, and
     * the constructor must not invoke any operations on the DataAccessor
     */
    GetExecutor(final DataLocator dataLocator,
        final KeyExtractor<K> keyExtractor,
        final ResponseHandler<K, V> responseHandler,
        final MetricsFactory metricsFactory,
        final Clock clock,
        final OperationsLogger logger) {
        Preconditions.checkNotNull(metricsFactory, "The MetricsFactory is null!");
        _dataLocator = Preconditions.checkNotNull(dataLocator, "The DataLocator for this entity is null!");
        _keyExtractor = Preconditions.checkNotNull(keyExtractor, "The CosmosDBKeyExtractorV3 is null!");
        _responseHandler = Preconditions.checkNotNull(responseHandler, "The CosmosDBResponseHandler is null!");
        _clock = Preconditions.checkNotNull(clock, "The Clock is null!");
        _logger = Preconditions.checkNotNull(logger, "The Logger is null!");
        // Initialize the metrics prior to the first operation
        final CollectionKey activeCollection = _dataLocator.getCollection();
        _metrics = metricsFactory.getMetrics(activeCollection, Constants.METHOD_GET);
    }

    Result<K, V> get(final K key, final GetRequestOptions requestOptions) throws CosmosDBDataAccessorException {
        final String id = _keyExtractor.getId(key);
        final PartitionKey partitioningKey = new PartitionKey(_keyExtractor.getPartitioningKey(key));
        final CollectionKey activeCollection = _dataLocator.getCollection();
        _metrics.logCounterMetric(Metrics.Type.CALL_COUNT);
        final long startTime = _clock.millis();

        try {
            final CosmosItemResponse<ObjectNode> response = _dataLocator.getAsyncContainer(activeCollection)
                .readItem(id, partitioningKey, ObjectNode.class)
                .block();

            // Usually we get an Exception when the document does not exist. But in the new SDK,
            // with the use of Flux/Mono, adding conditions to handle null responses.
            if (Objects.nonNull(response)) {
                _logger.logDebugInfo(Constants.METHOD_GET, key, activeCollection, _clock.millis() - startTime,
                    response.getActivityId(), response.getDiagnostics().toString());
            }

            return _responseHandler.convertResponse(key, response, requestOptions.shouldFetchTombstone());
        } catch (CosmosException ex) {
            if (ex.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                _metrics.logCounterMetric(Metrics.Type.NOT_FOUND);
                return _responseHandler.convertException(key, ex);
            }

            _metrics.error(startTime);
            final String errorMessage = String.format(ERROR_MESSAGE_FORMAT, id, activeCollection.getCollectionName());
            throw _responseHandler.createException(errorMessage, ex);
        } catch (Exception ex) {
            _metrics.error(startTime);
            final String errorMessage = String.format(ERROR_MESSAGE_FORMAT, id, activeCollection.getCollectionName());
            throw new CosmosDBDataAccessorException.Builder()
                .setMessage(errorMessage)
                .setCause(ex.getCause())
                .build();
        } finally {
            _metrics.completed(startTime);
        }
    }
}
