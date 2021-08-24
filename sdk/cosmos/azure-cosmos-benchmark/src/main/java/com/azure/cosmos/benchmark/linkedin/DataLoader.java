// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosItemOperation;
import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.linkedin.data.EntityConfiguration;
import com.azure.cosmos.benchmark.linkedin.data.Key;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class DataLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataLoader.class);

    private static final int MAX_BATCH_SIZE = 10000;
    private static final int BULK_OPERATION_CONCURRENCY = 10;
    private static final Duration BATCH_DATA_LOAD_WAIT_DURATION = Duration.ofMinutes(5);
    private static final Duration VALIDATE_DATA_WAIT_DURATION = Duration.ofSeconds(120);
    private static final String COUNT_ALL_QUERY = "SELECT COUNT(1) FROM c";
    private static final String COUNT_ALL_QUERY_RESULT_FIELD = "$1";

    private final Configuration _configuration;
    private final EntityConfiguration _entityConfiguration;
    private final CosmosAsyncClient _client;

    public DataLoader(final Configuration configuration,
        final EntityConfiguration entityConfiguration,
        final CosmosAsyncClient client) {
        _configuration = Preconditions.checkNotNull(configuration,
            "The Workload configuration defining the parameters can not be null");
        _entityConfiguration = Preconditions.checkNotNull(entityConfiguration,
            "The test entity configuration can not be null");
        _client = Preconditions.checkNotNull(client,
            "The CosmosAsyncClient needed for data loading can not be null");
    }

    public void loadData() {
        final String containerName = _configuration.getCollectionId();
        final CosmosAsyncDatabase database = _client.getDatabase(_configuration.getDatabaseId());
        final CosmosAsyncContainer container = database.getContainer(containerName);

        LOGGER.info("Check container {} for existing data", containerName);
        final int documentCount = getDocumentCount(container);
        final int documentsToLoad = _configuration.getNumberOfPreCreatedDocuments() - documentCount;

        if (documentsToLoad <= 0) {
            LOGGER.info("Container {} already has the requisite number of documents: {} [desired: {}]",
                containerName, documentCount, _configuration.getNumberOfPreCreatedDocuments());
            return;
        }

        LOGGER.info("Starting batched data loading to load {} documents, with {} documents in each iteration",
            documentsToLoad,
            _configuration.getBulkloadBatchSize());
        final DataGenerationIterator dataGenerator =
            new DataGenerationIterator(_entityConfiguration.dataGenerator(),
                documentsToLoad,
                _configuration.getBulkloadBatchSize());

        while (dataGenerator.hasNext()) {
            final Map<Key, ObjectNode> newDocuments = dataGenerator.next();
            bulkCreateItems(database, container, newDocuments);
            newDocuments.clear();
        }

        validateDataCreation(_configuration.getNumberOfPreCreatedDocuments());
    }

    private void bulkCreateItems(final CosmosAsyncDatabase database,
        final CosmosAsyncContainer container,
        final Map<Key, ObjectNode> records) {
        final List<CosmosItemOperation> cosmosItemOperations = mapToCosmosItemOperation(records);
        LOGGER.info("Bulk loading {} documents in [{}:{}]", cosmosItemOperations.size(),
            database.getId(),
            container.getId());

        // We want to wait longer depending on the number of documents in each iteration
        final CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();
        cosmosBulkExecutionOptions.setMaxMicroBatchSize(MAX_BATCH_SIZE);
        cosmosBulkExecutionOptions.setMaxMicroBatchConcurrency(BULK_OPERATION_CONCURRENCY);
        container.processBulkOperations(Flux.fromIterable(cosmosItemOperations), cosmosBulkExecutionOptions)
            .blockLast(BATCH_DATA_LOAD_WAIT_DURATION);

        LOGGER.info("Completed loading {} documents into [{}:{}]", cosmosItemOperations.size(),
            database.getId(),
            container.getId());
    }

    private void validateDataCreation(int expectedSize) {
        final String containerName = _configuration.getCollectionId();
        final CosmosAsyncDatabase database = _client.getDatabase(_configuration.getDatabaseId());
        final CosmosAsyncContainer container = database.getContainer(containerName);
        LOGGER.info("Validating {} documents were loaded into [{}:{}]",
            expectedSize, _configuration.getDatabaseId(), containerName);

        final int resultCount = getDocumentCount(container);
        if (resultCount < (expectedSize * 0.90)) {
            throw new IllegalStateException(
                String.format("Number of documents %d in the container %s is less than the expected threshold %f ",
                    resultCount, containerName, (expectedSize * 0.90)));
        }

        LOGGER.info("Validated {} out of the {} expected documents were loaded into [{}:{}]",
            resultCount, expectedSize, _configuration.getDatabaseId(), containerName);
    }

    private int getDocumentCount(CosmosAsyncContainer container) {
        final List<FeedResponse<ObjectNode>> queryItemsResponseList = container
            .queryItems(COUNT_ALL_QUERY, ObjectNode.class)
            .byPage()
            .collectList()
            .block(VALIDATE_DATA_WAIT_DURATION);
        return Optional.ofNullable(queryItemsResponseList)
            .map(responseList -> responseList.get(0))
            .map(FeedResponse::getResults)
            .map(list -> list.get(0))
            .map(objectNode -> objectNode.get(COUNT_ALL_QUERY_RESULT_FIELD).intValue())
            .orElse(0);
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
                return CosmosBulkOperations.getCreateItemOperation(value, new PartitionKey(partitionKey));
            })
            .collect(Collectors.toList());
    }
}
