// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.storage;

import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.BlobServiceProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.time.Duration;

import static com.azure.spring.cloud.actuate.storage.StorageHealthConstants.NOT_CONFIGURED_STATUS;
import static com.azure.spring.cloud.actuate.storage.StorageHealthConstants.URL_FIELD;
import static com.azure.spring.cloud.actuate.util.implementation.Constants.DEFAULT_HEALTH_CHECK_TIMEOUT;

/**
 * Health indicator for blob storage.
 */
public class StorageBlobHealthIndicator implements HealthIndicator {

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
    public Health health() {
        Health.Builder healthBuilder = new Health.Builder();
        try {
            if (blobServiceAsyncClient == null) { // Not configured
                healthBuilder.status(NOT_CONFIGURED_STATUS);
            } else {
                healthBuilder.withDetail(URL_FIELD, blobServiceAsyncClient.getAccountUrl());
                final Response<BlobServiceProperties> info;
                try {
                    info = blobServiceAsyncClient.getPropertiesWithResponse().block(timeout);
                    if (info != null) {
                        healthBuilder.up();
                    }
                } catch (Exception e) {
                    healthBuilder.down(e);
                }
            }
        } catch (Exception e) {
            healthBuilder.status("Could not complete health check.").down(e);
        }
        return healthBuilder.build();
    }

    /**
     * Set health check request timeout.
     * @param timeout the duration value.
     */
    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }
}
