// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.linkedin.data.EntityConfiguration;
import com.google.common.base.Preconditions;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * For local testing, the database creation needs to happen as part of the Test setup. This class
 * manages the database AND collection setup, and useful for ensuring database and other resources
 * and not left unused after local testing
 */
public class DatabaseResourceManager implements ResourceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseResourceManager.class);
    private static final Duration RESOURCE_CRUD_WAIT_TIME = Duration.ofSeconds(30);

    private final Configuration _configuration;
    private final CosmosAsyncClient _client;
    private final CollectionResourceManager _collectionResourceManager;

    public DatabaseResourceManager(final Configuration configuration,
        final EntityConfiguration entityConfiguration,
        final CosmosAsyncClient client) {
        Preconditions.checkNotNull(configuration,
            "The Workload configuration defining the parameters can not be null");
        Preconditions.checkNotNull(entityConfiguration,
            "The Test Entity specific configuration can not be null");
        Preconditions.checkNotNull(client, "Need a non-null client for "
            + "setting up the Database and collections for the test");
        _configuration = configuration;
        _client = client;
        _collectionResourceManager = new CollectionResourceManager(_configuration, entityConfiguration, _client);
    }

    @Override
    public void createResources() throws CosmosException {
        try {
            LOGGER.info("Creating database {} for the ctl workload if one doesn't exist", _configuration.getDatabaseId());
            _client.createDatabaseIfNotExists(_configuration.getDatabaseId())
                .block(RESOURCE_CRUD_WAIT_TIME);
        } catch (CosmosException e) {
            LOGGER.error("Exception while creating database {}", _configuration.getDatabaseId(), e);
            throw e;
        }

        // Delete any existing collections/containers in this database
        _collectionResourceManager.deleteResources();

        // And recreate the collections for this test
        _collectionResourceManager.createResources();
    }

    @Override
    public void deleteResources() {
        // Delete the database used for testing
        final CosmosAsyncDatabase database = _client.getDatabase(_configuration.getDatabaseId());
        try {
            LOGGER.info("Deleting the main database {} used in this test. Collection", _configuration.getDatabaseId());
            database.delete()
                .block(RESOURCE_CRUD_WAIT_TIME);
        } catch (CosmosException e) {
            LOGGER.error("Exception deleting the database {}", _configuration.getDatabaseId(), e);
            throw e;
        }

        LOGGER.info("Database resource cleanup completed");
    }
}
