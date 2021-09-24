// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.util.Assert;

/**
 * Simple implementation of a {@link HealthIndicator} returning status information for
 * Cosmos data stores.
 */
public class CosmosHealthIndicator extends AbstractHealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosHealthIndicator.class);

    private final CosmosAsyncClient cosmosAsyncClient;
    private final String database;
    private final String uri;

    public CosmosHealthIndicator(CosmosAsyncClient cosmosAsyncClient, String database, String uri) {
        super("Cosmos health check failed");
        Assert.notNull(cosmosAsyncClient, "CosmosClient must not be null");
        this.cosmosAsyncClient = cosmosAsyncClient;
        this.database = database;
        this.uri = uri;
    }

    @Override
    protected void doHealthCheck(Builder builder) {
        CosmosDatabaseResponse response = this.cosmosAsyncClient.getDatabase(database).read().block();

        if (response != null) {
            LOGGER.info("The health indicator cost {} RUs, cosmos uri: {}, dbName: {}",
                response.getRequestCharge(), uri, database);
        }
        if (response == null) {
            builder.down();
        } else {
            builder.up().withDetail("database", response.getProperties().getId());
        }

    }
}
