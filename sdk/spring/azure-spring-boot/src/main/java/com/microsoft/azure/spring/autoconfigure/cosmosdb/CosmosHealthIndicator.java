// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.cosmosdb;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosDatabaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Value("${azure.cosmosdb.database}")
    private String dbName;
    @Value("${azure.cosmosdb.uri}")
    private String uri;

    @Autowired
    private CosmosClient cosmosClient;

    public CosmosHealthIndicator(CosmosClient cosmosClient) {
        super("CosmosDB health check failed");
        Assert.notNull(cosmosClient, "CosmosClient must not be null");
        this.cosmosClient = cosmosClient;
    }

    @Override
    protected void doHealthCheck(Builder builder) throws Exception {
        CosmosDatabaseResponse response = this.cosmosClient.getDatabase(dbName).read().block();

        if (response != null) {
            LOGGER.info("The health indicator cost {} RUs, cosmos uri: {}, dbName: {}",
                response.requestCharge(), uri, dbName);
        }
        if (response == null) {
            builder.down();
        } else {
            builder.up().withDetail("database", response.database().id());
        }

    }
}
