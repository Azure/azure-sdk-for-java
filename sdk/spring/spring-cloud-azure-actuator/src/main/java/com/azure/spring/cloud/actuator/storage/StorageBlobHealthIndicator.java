// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.storage;

import com.azure.storage.blob.BlobServiceAsyncClient;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.core.io.ResourceLoader;

import static com.azure.spring.cloud.actuator.storage.StorageHealthConstants.NOT_CONFIGURED_STATUS;
import static com.azure.spring.cloud.actuator.storage.StorageHealthConstants.URL_FIELD;

/**
 * Health indicator for blob storage.
 */
public class StorageBlobHealthIndicator extends AbstractHealthIndicator {

    private final BlobServiceAsyncClient blobServiceAsyncClient;

    private final ResourceLoader resourceLoader;

    /**
     * Creates a new instance of {@link StorageBlobHealthIndicator}.
     *
     * @param blobServiceAsyncClient the blob service client
     * @param resourceLoader the resource loader
     */
    public StorageBlobHealthIndicator(BlobServiceAsyncClient blobServiceAsyncClient, ResourceLoader resourceLoader) {
        this.blobServiceAsyncClient = blobServiceAsyncClient;
        this.resourceLoader = resourceLoader;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        if (blobServiceAsyncClient == null) {
            builder.status(NOT_CONFIGURED_STATUS);
        } else {
            builder.withDetail(URL_FIELD, blobServiceAsyncClient.getAccountUrl());
            try {
                resourceLoader.getResource("azure-blob://spring-cloud-azure-not-existing-container/not-existing-blob");
                builder.up();
            } catch (Exception e) {
                builder.down();
            }
        }
    }

}
