package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.ThroughputProperties;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.azure.cosmos.models.ThroughputProperties.*;


public class ResourceManager {
    private final static Logger LOGGER = LoggerFactory.getLogger(ResourceManager.class);

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
            _client.createDatabaseIfNotExists(_configuration.getDatabaseId()).block();
        } catch (CosmosException e) {
            LOGGER.error("Exception while creating database {}", _configuration.getDatabaseId(), e);
            throw e;
        }

        deleteExistingContainers();
    }

    /**
     * Create desired containers for the test
     *
     * @throws CosmosException if any container could not be created
     */
    public void createContainers() throws CosmosException {

        final String collectionId = _configuration.getCollectionId();
        int numberOfCollection = _configuration.getNumberOfCollectionForCtl();
        if (numberOfCollection < 1) {
            numberOfCollection = 1;
        }

        final CosmosAsyncDatabase database = _client.getDatabase(_configuration.getDatabaseId());
        final ThroughputProperties containerThroughput = createManualThroughput(_configuration.getThroughput());
        for (int i = 1; i <= numberOfCollection; i++) {
            final String containerName = collectionId + "_" + i;
            try {
                final CosmosContainerProperties containerProperties =
                    new CosmosContainerProperties(containerName, Constants.PARTITION_KEY_PATH);
                database.createContainerIfNotExists(containerProperties, containerThroughput);

            } catch (CosmosException e) {
                LOGGER.error("Exception while creating container {}", containerName, e);
                throw e;
            }
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
            database.delete().block();
        } catch (CosmosException e) {
            LOGGER.error("Exception while deleting the database {}", _configuration.getDatabaseId(), e);
            throw e;
        }
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
                cosmosAsyncContainer.delete().block();
            } catch (CosmosException e) {
                LOGGER.error("Error deleting container {} in the Database {}",
                    cosmosAsyncContainer.getId(), _configuration.getDatabaseId(), e);
            }
        }
    }
}
