// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.storage;

import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.StorageAccountInfo;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import static com.azure.spring.cloud.actuate.storage.Constants.NOT_CONFIGURED_STATUS;
import static com.azure.spring.cloud.actuate.storage.Constants.POLL_TIMEOUT;
import static com.azure.spring.cloud.actuate.storage.Constants.URL_FIELD;

/**
 * Health indicator for blob storage.
 */
public class StorageBlobHealthIndicator implements HealthIndicator {

    private final BlobServiceAsyncClient internalClient;

    public StorageBlobHealthIndicator(BlobServiceAsyncClient blobServiceAsyncClient) {
        internalClient = blobServiceAsyncClient;
    }

    @Override
    public Health health() {
        Health.Builder healthBuilder = new Health.Builder();

        try {
            if (internalClient == null) { // Not configured
                healthBuilder.status(NOT_CONFIGURED_STATUS);
            } else {
                healthBuilder.withDetail(URL_FIELD, internalClient.getAccountUrl());
                StorageAccountInfo info;
                try {
                    info = internalClient.getAccountInfo().block(POLL_TIMEOUT);
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
}
