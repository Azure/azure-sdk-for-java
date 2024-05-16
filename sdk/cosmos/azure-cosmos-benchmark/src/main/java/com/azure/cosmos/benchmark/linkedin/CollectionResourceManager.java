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
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.ThroughputProperties;
import com.google.common.base.Preconditions;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation for managing the Collection for this test. This class facilitates
 * container creation after the CTL environment has provisioned the database with the
 * required throughput.
 *
 * Since we need to operate in the daily and staging environment, it does NOT delete the
 * created container. In the daily environment, the database hosting the container is deleted,
 * while in the staging environment, the container is not deleted (it's long-lived).
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
            LOGGER.info("Checking for container {} in the database {}", containerName, database.getId());
            final CosmosAsyncContainer container = database.getContainer(containerName);
            final Optional<CosmosContainerResponse> response = getContainerProperties(container);
            if (response.isPresent()) {
                LOGGER.info("Container {} already exists in the database {}", containerName, database.getId());
                applyDesiredContainerPolicies(container, response.get().getProperties());
                return;
            }

            createContainer(database, containerName, collectionAttributes);
        } catch (CosmosException e) {
            LOGGER.error("Exception while configuring collection {}", containerName, e);
            throw e;
        }
    }

    @Override
    public void deleteResources() {
        LOGGER.info("The Collection {} will not be deleted.", _configuration.getCollectionId());
    }

    private Optional<CosmosContainerResponse> getContainerProperties(CosmosAsyncContainer container) {
        try {
            return Optional.ofNullable(container.read().
                block(RESOURCE_CRUD_WAIT_TIME));
        } catch (CosmosException e) {
            return Optional.empty();
        }
    }

    private void applyDesiredContainerPolicies(final CosmosAsyncContainer container,
        final CosmosContainerProperties properties) {
        LOGGER.info("Updating container {} properties to desired", container.getId());
        properties.setIndexingPolicy(_entityConfiguration.collectionAttributes().indexingPolicy());
        container.replace(properties)
            .block(RESOURCE_CRUD_WAIT_TIME);
    }

    private void createContainer(final CosmosAsyncDatabase database,
        final String containerName,
        final CollectionAttributes collectionAttributes) {
        LOGGER.info("Creating container {} in the database {} [throughput = {}]", containerName,
            database.getId(),
            _configuration.getThroughput());
        final ThroughputProperties throughputProperties =
            ThroughputProperties.createManualThroughput(_configuration.getThroughput());
        final CosmosContainerProperties containerProperties =
            new CosmosContainerProperties(containerName, Constants.PARTITION_KEY_PATH)
                .setIndexingPolicy(collectionAttributes.indexingPolicy());
        database.createContainerIfNotExists(containerProperties, throughputProperties)
            .block(RESOURCE_CRUD_WAIT_TIME);
    }
}
