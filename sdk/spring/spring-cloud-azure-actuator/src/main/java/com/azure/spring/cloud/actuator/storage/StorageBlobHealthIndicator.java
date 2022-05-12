// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.storage;

import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.DownloadRetryOptions;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

import java.time.Duration;

import static com.azure.spring.cloud.actuator.implementation.util.ActuateConstants.DEFAULT_HEALTH_CHECK_TIMEOUT;
import static com.azure.spring.cloud.actuator.storage.StorageHealthConstants.NOT_CONFIGURED_STATUS;
import static com.azure.spring.cloud.actuator.storage.StorageHealthConstants.URL_FIELD;

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
    protected void doHealthCheck(Health.Builder builder) {
        if (blobServiceAsyncClient == null) {
            builder.status(NOT_CONFIGURED_STATUS);
        } else {
            try {
                String NOT_EXISTING_CONTAINER = "spring-cloud-azure-not-existing-container";
                String NOT_EXISTING_BLOB = "spring-cloud-azure-not-existing-blob";
                BlobAsyncClient blobAsyncClient =
                    blobServiceAsyncClient.getBlobContainerAsyncClient(NOT_EXISTING_CONTAINER)
                                          .getBlobAsyncClient(NOT_EXISTING_BLOB);

                builder.withDetail(URL_FIELD, blobServiceAsyncClient.getAccountUrl());
                BlobRange range = new BlobRange(0, (long) 2);
                DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(1);
                blobAsyncClient.downloadStreamWithResponse(range, options, null, false)
                               .block(timeout);

                builder.up();
            } catch (Exception e) {
                if (e instanceof BlobStorageException) {
                    builder.up();
                } else {
                    throw e;
                }
            }

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
