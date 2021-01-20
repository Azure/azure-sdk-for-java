// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.benchmark.Configuration;
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


public class ResourceManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceManager.class);
    private static final Duration RESOURCE_CRUD_WAIT_TIME = Duration.ofSeconds(30);

    private final Configuration _configuration;
    private final CosmosAsyncClient _client;

    public ResourceManager(final Configuration configuration, final CosmosAsyncClient client) {
        Preconditions.checkNotNull(configuration,
            "The Workload configuration defining the parameters can not be null");
        Preconditions.checkNotNull(client, "Need a non-null client for "
            + "setting up the Database and containers for the test");
        _configuration = configuration;
        _client = client;
    }

    /**
     * Initialize the CosmosDB database required for running this test, or if the database exists, delete all
     * legacy containers
     *
     * @throws CosmosException in the event of an error creating the underlying database, or deleting
     *                         containers from a previously created database of the same name
     */
    public void initializeDatabase() throws CosmosException {
        try {
            LOGGER.info("Creating database {} for the ctl workload", _configuration.getDatabaseId());
            _client.createDatabaseIfNotExists(_configuration.getDatabaseId())
                .block(RESOURCE_CRUD_WAIT_TIME);
        } catch (CosmosException e) {
            LOGGER.error("Exception while creating database {}", _configuration.getDatabaseId(), e);
            throw e;
        }

        deleteExistingContainers();
    }

    /**
     * Create desired container/collection for the test
     *
     * @throws CosmosException if the container could not be created
     */
    public void createContainer() throws CosmosException {
        final String containerName = _configuration.getCollectionId();
        final CosmosAsyncDatabase database = _client.getDatabase(_configuration.getDatabaseId());
        final ThroughputProperties containerThroughput = createManualThroughput(_configuration.getThroughput());
        try {
            LOGGER.info("Creating container {} in the database {}", containerName, database.getId());
            final CosmosContainerProperties containerProperties =
                new CosmosContainerProperties(containerName, PARTITION_KEY_PATH);
            database.createContainerIfNotExists(containerProperties, containerThroughput)
                .block(RESOURCE_CRUD_WAIT_TIME);
        } catch (CosmosException e) {
            LOGGER.error("Exception while creating container {}", containerName, e);
            throw e;
        }
    }

    /**
     * Delete all resources i.e. databases and containers created as part of this test
     */
    public void deleteResources() {
        // Delete all the containers in the database
        deleteExistingContainers();

        // Followed by the main database used for testing
        final CosmosAsyncDatabase database = _client.getDatabase(_configuration.getDatabaseId());
        try {
            LOGGER.info("Deleting the main database {} used in this test", _configuration.getDatabaseId());
            database.delete()
                .block(RESOURCE_CRUD_WAIT_TIME);
        } catch (CosmosException e) {
            LOGGER.error("Exception while deleting the database {}", _configuration.getDatabaseId(), e);
            throw e;
        }
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
