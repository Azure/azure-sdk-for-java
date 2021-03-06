// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.linkedin.data.CollectionAttributes;
import com.azure.cosmos.benchmark.linkedin.data.EntityConfiguration;
import com.azure.cosmos.benchmark.linkedin.impl.Constants;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.google.common.base.Preconditions;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation for managing only the Collections for this test. This class facilitates
 * container creation after the CTL environment has provisioned the database with the
 * required throughput
 */
public class CollectionResourceManager implements ResourceManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionResourceManager.class);
    private static final Duration RESOURCE_CRUD_WAIT_TIME = Duration.ofSeconds(30);

    private final Configuration _configuration;
    private final EntityConfiguration _entityConfiguration;
    private final CosmosAsyncClient _client;

    public CollectionResourceManager(final Configuration configuration,
        final EntityConfiguration entityConfiguration,
        final CosmosAsyncClient client) {
        Preconditions.checkNotNull(configuration,
            "The Workload configuration defining the parameters can not be null");
        Preconditions.checkNotNull(entityConfiguration,
            "The Test Entity specific configuration can not be null");
        Preconditions.checkNotNull(client, "Need a non-null client for "
            + "setting up the Database and collections for the test");
        _configuration = configuration;
        _entityConfiguration = entityConfiguration;
        _client = client;
    }

    @Override
    public void createResources() throws CosmosException {
        final String containerName = _configuration.getCollectionId();
        final CosmosAsyncDatabase database = _client.getDatabase(_configuration.getDatabaseId());
        final CollectionAttributes collectionAttributes = _entityConfiguration.collectionAttributes();
        try {
            LOGGER.info("Creating container {} in the database {}", containerName, database.getId());
            final CosmosContainerProperties containerProperties =
                new CosmosContainerProperties(containerName, Constants.PARTITION_KEY_PATH)
                    .setIndexingPolicy(collectionAttributes.indexingPolicy());
            database.createContainerIfNotExists(containerProperties)
                .block(RESOURCE_CRUD_WAIT_TIME);
        } catch (CosmosException e) {
            LOGGER.error("Exception while creating collection {}", containerName, e);
            throw e;
        }
    }

    @Override
    public void deleteResources() {
        deleteExistingCollections();

        LOGGER.info("Collection resource cleanup completed");
    }

    private void deleteExistingCollections() {
        final CosmosAsyncDatabase database = _client.getDatabase(_configuration.getDatabaseId());
        final List<CosmosAsyncContainer> cosmosAsyncContainers = database.readAllContainers()
            .byPage()
            .toStream()
            .flatMap(cosmosContainerPropertiesFeedResponse ->
                cosmosContainerPropertiesFeedResponse.getResults().stream())
            .map(cosmosContainerProperties -> database.getContainer(cosmosContainerProperties.getId()))
            .collect(Collectors.toList());

        // Run a best effort attempt to delete all existing containers and data there-in
        for (CosmosAsyncContainer cosmosAsyncContainer : cosmosAsyncContainers) {
            LOGGER.info("Deleting collection {} in the Database {}", cosmosAsyncContainer.getId(), _configuration.getDatabaseId());
            try {
                cosmosAsyncContainer.delete()
                    .block(RESOURCE_CRUD_WAIT_TIME);
            } catch (CosmosException e) {
                LOGGER.error("Error deleting collection {} in the Database {}",
                    cosmosAsyncContainer.getId(), _configuration.getDatabaseId(), e);
            }
        }
    }
}
