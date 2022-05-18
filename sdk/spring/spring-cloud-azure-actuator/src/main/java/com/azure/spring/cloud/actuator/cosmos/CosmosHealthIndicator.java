// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.util.Assert;

import java.time.Duration;

import static com.azure.spring.cloud.actuator.implementation.util.ActuateConstants.DEFAULT_HEALTH_CHECK_TIMEOUT;

/**
 * Simple implementation of a {@link HealthIndicator} returning status information for Cosmos data stores.
 */
public class CosmosHealthIndicator extends AbstractHealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosHealthIndicator.class);

    private final CosmosAsyncClient cosmosAsyncClient;
    private final String database;
    private final String endpoint;
    private Duration timeout = DEFAULT_HEALTH_CHECK_TIMEOUT;

    /**
     * Creates a new instance of {@link CosmosHealthIndicator}.
     *
     * @param cosmosAsyncClient the cosmosAsyncClient
     * @param database database name
     * @param endpoint cosmos endpoint
     */
    public CosmosHealthIndicator(CosmosAsyncClient cosmosAsyncClient, String database, String endpoint) {
        super("Cosmos health check failed");
        Assert.notNull(cosmosAsyncClient, "CosmosClient must not be null");
        this.cosmosAsyncClient = cosmosAsyncClient;
        this.database = database;
        this.endpoint = endpoint;
    }

    @Override
    protected void doHealthCheck(Builder builder) {
        if (database == null) {
            builder.status(Status.UNKNOWN).withDetail("Database not configured",
                "The option of `spring.cloud.azure.cosmos.database` is not configured!");
            return;
        }

        CosmosDatabaseResponse response = this.cosmosAsyncClient.getDatabase(database)
                                                                .read()
                                                                .block(timeout);

        if (response != null) {
            LOGGER.info("The health indicator cost {} RUs, cosmos uri: {}, dbName: {}",
                response.getRequestCharge(), endpoint, database);
            builder.up()
                   .withDetail("RUs", response.getRequestCharge())
                   .withDetail("CosmosUri", endpoint)
                   .withDetail("Database", database);
        } else {
            builder.down();
        }
    }

    /**
     * Set health check request timeout.
     *
     * @param timeout the duration value.
     */
    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }
}
