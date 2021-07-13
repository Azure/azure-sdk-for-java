// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.cloud.azure.cosmos.database}")
    private String dbName;

    @Value("${spring.cloud.azure.cosmos.uri}")
    private String uri;

    private final CosmosAsyncClient cosmosAsyncClient;

    public CosmosHealthIndicator(CosmosAsyncClient cosmosAsyncClient) {
        super("Cosmos health check failed");
        Assert.notNull(cosmosAsyncClient, "CosmosClient must not be null");
        this.cosmosAsyncClient = cosmosAsyncClient;
    }

    @Override
    protected void doHealthCheck(Builder builder) throws Exception {
        CosmosDatabaseResponse response = this.cosmosAsyncClient.getDatabase(dbName).read().block();

        if (response != null) {
            LOGGER.info("The health indicator cost {} RUs, cosmos uri: {}, dbName: {}",
                response.getRequestCharge(), uri, dbName);
        }
        if (response == null) {
            builder.down();
        } else {
            builder.up().withDetail("database", response.getProperties().getId());
        }

    }
}
