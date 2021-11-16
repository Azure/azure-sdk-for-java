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
import static com.azure.spring.cloud.actuate.util.Constants.DEFAULT_TIMEOUT_SECONDS;

/**
 * Health indicator for blob storage.
 */
public class StorageBlobHealthIndicator implements HealthIndicator {

    private final BlobServiceAsyncClient blobServiceAsyncClient;
    private int timeout = DEFAULT_TIMEOUT_SECONDS;

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
                    info = blobServiceAsyncClient.getPropertiesWithResponse().block(Duration.ofSeconds(timeout));
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

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
