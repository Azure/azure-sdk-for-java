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
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.ThroughputProperties;
import com.google.common.base.Preconditions;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.azure.cosmos.benchmark.linkedin.impl.Constants.PARTITION_KEY_PATH;
import static com.azure.cosmos.models.ThroughputProperties.createManualThroughput;


public class ResourceManagerImpl implements ResourceManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceManagerImpl.class);
    private static final Duration RESOURCE_CRUD_WAIT_TIME = Duration.ofSeconds(30);

    private final Configuration _configuration;
    private final EntityConfiguration _entityConfiguration;
    private final CosmosAsyncClient _client;

    public ResourceManagerImpl(final Configuration configuration,
        final EntityConfiguration entityConfiguration,
        final CosmosAsyncClient client) {
        Preconditions.checkNotNull(configuration,
            "The Workload configuration defining the parameters can not be null");
        Preconditions.checkNotNull(entityConfiguration,
            "The Test Entity specific configuration can not be null");
        Preconditions.checkNotNull(client, "Need a non-null client for "
            + "setting up the Database and containers for the test");
        _configuration = configuration;
        _entityConfiguration = entityConfiguration;
        _client = client;
    }

    @Override
    public void createDatabase() throws CosmosException {
        try {
            LOGGER.info("Creating database {} for the ctl workload if one doesn't exist", _configuration.getDatabaseId());
            final ThroughputProperties throughputProperties = createManualThroughput(_configuration.getThroughput());
            _client.createDatabaseIfNotExists(_configuration.getDatabaseId(), throughputProperties)
                .block(RESOURCE_CRUD_WAIT_TIME);
        } catch (CosmosException e) {
            LOGGER.error("Exception while creating database {}", _configuration.getDatabaseId(), e);
            throw e;
        }

        deleteExistingContainers();
    }

    @Override
    public void createContainer() throws CosmosException {
        final String containerName = _configuration.getCollectionId();
        final CosmosAsyncDatabase database = _client.getDatabase(_configuration.getDatabaseId());
        final CollectionAttributes collectionAttributes = _entityConfiguration.collectionAttributes();
        try {
            LOGGER.info("Creating container {} in the database {}", containerName, database.getId());
            final CosmosContainerProperties containerProperties =
                new CosmosContainerProperties(containerName, PARTITION_KEY_PATH)
                    .setIndexingPolicy(collectionAttributes.indexingPolicy());
            database.createContainerIfNotExists(containerProperties)
                .block(RESOURCE_CRUD_WAIT_TIME);
        } catch (CosmosException e) {
            LOGGER.error("Exception while creating container {}", containerName, e);
            throw e;
        }
    }

    @Override
    public void deleteResources() {
        // Delete all the containers in the database
        deleteExistingContainers();

        LOGGER.info("Resource cleanup completed");
    }

    private void deleteExistingContainers() {
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
            LOGGER.info("Deleting container {} in the Database {}", cosmosAsyncContainer.getId(), _configuration.getDatabaseId());
            try {
                cosmosAsyncContainer.delete()
                    .block(RESOURCE_CRUD_WAIT_TIME);
            } catch (CosmosException e) {
                LOGGER.error("Error deleting container {} in the Database {}",
                    cosmosAsyncContainer.getId(), _configuration.getDatabaseId(), e);
            }
        }
    }
}
