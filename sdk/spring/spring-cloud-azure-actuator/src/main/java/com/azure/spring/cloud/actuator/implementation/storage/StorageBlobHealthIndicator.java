// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.implementation.storage;

import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

import java.time.Duration;

import static com.azure.spring.cloud.actuator.implementation.util.ActuateConstants.DEFAULT_HEALTH_CHECK_TIMEOUT;
import static com.azure.spring.cloud.actuator.implementation.storage.StorageHealthConstants.NOT_CONFIGURED_STATUS;
import static com.azure.spring.cloud.actuator.implementation.storage.StorageHealthConstants.NOT_EXISTING_CONTAINER;
import static com.azure.spring.cloud.actuator.implementation.storage.StorageHealthConstants.URL_FIELD;

/**
 * Health indicator for blob storage.
 */
public class StorageBlobHealthIndicator extends AbstractHealthIndicator {

    private final BlobServiceAsyncClient blobServiceAsyncClient;
    private Duration timeout = DEFAULT_HEALTH_CHECK_TIMEOUT;

    /**
     * Creates a new instance of {@link StorageBlobHealthIndicator}.
     * @param blobServiceAsyncClient the blob service client
     */
    public StorageBlobHealthIndicator(BlobServiceAsyncClient blobServiceAsyncClient) {
        this.blobServiceAsyncClient = blobServiceAsyncClient;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        if (blobServiceAsyncClient == null) {
            builder.status(NOT_CONFIGURED_STATUS);
            return;
        }

        BlobContainerAsyncClient containerAsyncClient = blobServiceAsyncClient.getBlobContainerAsyncClient(
            NOT_EXISTING_CONTAINER);
        Response<Boolean> exists = containerAsyncClient.existsWithResponse().block(timeout);
        if (exists == null) {
            throw new RuntimeException("Error occurred checking the container existence!");
        }

        builder.up()
               .withDetail(URL_FIELD, blobServiceAsyncClient.getAccountUrl());
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
