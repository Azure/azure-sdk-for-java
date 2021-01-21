// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.BulkOperations;
import com.azure.cosmos.BulkProcessingOptions;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosItemOperation;
import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.linkedin.data.Key;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;


public class DataLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataLoader.class);

    private static final int MAX_BATCH_SIZE = 10000;
    private static final int BULK_OPERATION_CONCURRENCY = 5;
    private static final Duration BULK_LOAD_WAIT_DURATION = Duration.ofSeconds(60);
    private static final String COUNT_ALL_QUERY = "SELECT COUNT(1) FROM c";
    private static final String COUNT_ALL_QUERY_RESULT_FIELD = "$1";

    private final Configuration _configuration;
    private final CosmosAsyncClient _client;

    public DataLoader(final Configuration configuration, final CosmosAsyncClient client) {
        _configuration = Preconditions.checkNotNull(configuration,
            "The Workload configuration defining the parameters can not be null");
        _client = Preconditions.checkNotNull(client,
            "The CosmosAsyncClient needed for data loading can not be null");
    }

    public void loadData(final Map<Key, ObjectNode> records) {
        bulkCreateItems(records);
        validateDataCreation(records.size());
    }

    private void bulkCreateItems(final Map<Key, ObjectNode> records) {
        final List<CosmosItemOperation> cosmosItemOperations = mapToCosmosItemOperation(records);
        final String containerName = _configuration.getCollectionId();
        final CosmosAsyncDatabase database = _client.getDatabase(_configuration.getDatabaseId());
        final CosmosAsyncContainer container = database.getContainer(containerName);
        LOGGER.info("Bulk loading {} documents in [{}:{}]", cosmosItemOperations.size(),
            database.getId(),
            containerName);

        final BulkProcessingOptions<Object> bulkProcessingOptions = new BulkProcessingOptions<>(Object.class);
        bulkProcessingOptions.setMaxMicroBatchSize(MAX_BATCH_SIZE)
            .setMaxMicroBatchConcurrency(BULK_OPERATION_CONCURRENCY);
        container.processBulkOperations(Flux.fromIterable(cosmosItemOperations), bulkProcessingOptions)
            .blockLast(BULK_LOAD_WAIT_DURATION);

        LOGGER.info("Completed document loading into [{}:{}]", database.getId(), containerName);
    }

    private void validateDataCreation(int expectedSize) {
        final String containerName = _configuration.getCollectionId();
        final CosmosAsyncDatabase database = _client.getDatabase(_configuration.getDatabaseId());
        final CosmosAsyncContainer container = database.getContainer(containerName);
        LOGGER.info("Validating {} documents were loaded into [{}:{}]",
            expectedSize, _configuration.getDatabaseId(), containerName);

        final List<FeedResponse<ObjectNode>> queryItemsResponseList = container
            .queryItems(COUNT_ALL_QUERY, ObjectNode.class)
            .byPage()
            .collectList()
            .block(BULK_LOAD_WAIT_DURATION);
        final int resultCount = Optional.ofNullable(queryItemsResponseList)
            .map(responseList -> responseList.get(0))
            .map(FeedResponse::getResults)
            .map(list -> list.get(0))
            .map(objectNode -> objectNode.get(COUNT_ALL_QUERY_RESULT_FIELD).intValue())
            .orElse(0);

        if (resultCount != expectedSize) {
            throw new IllegalStateException("Expected number of records " + expectedSize
                + " not found in the container " + containerName
                + ". Actual count: " + resultCount);
        }
    }

    /**
     * Map the generated data to createItem requests in the underlying container
     *
     * @param records Data we want to load into the container
     * @return List of CosmosItemOperation, each mapping to a createItem for that record
     */
    private List<CosmosItemOperation> mapToCosmosItemOperation(final Map<Key, ObjectNode> records) {
        return records.entrySet()
            .stream()
            .map(record -> {
                final String partitionKey = record.getKey().getPartitioningKey();
                final ObjectNode value = record.getValue();
                return BulkOperations.getCreateItemOperation(value, new PartitionKey(partitionKey));
            })
            .collect(Collectors.toList());
    }
}
