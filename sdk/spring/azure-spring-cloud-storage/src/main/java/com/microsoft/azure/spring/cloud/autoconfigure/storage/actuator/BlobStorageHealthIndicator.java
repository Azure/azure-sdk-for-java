// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.storage.actuator;

import static com.microsoft.azure.spring.cloud.autoconfigure.storage.actuator.AzureStorageActuatorConstants.POLL_TIMEOUT;
import static com.microsoft.azure.spring.cloud.autoconfigure.storage.actuator.AzureStorageActuatorConstants.URL_FIELD;
import static com.microsoft.azure.spring.cloud.autoconfigure.storage.actuator.AzureStorageActuatorConstants.NOT_CONFIGURED_STATUS;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.ApplicationContext;

import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.StorageAccountInfo;

public class BlobStorageHealthIndicator implements HealthIndicator {

    private BlobServiceAsyncClient internalClient;

    public BlobStorageHealthIndicator(ApplicationContext applicationContext) {
        BlobServiceClientBuilder blobStorageClientBuilder = applicationContext.getBean(BlobServiceClientBuilder.class);
        internalClient = blobStorageClientBuilder.buildAsyncClient();
    }

    @Override
    public Health health() {
        Health.Builder healthBuilder = new Health.Builder();

        try {
            if (internalClient == null) { // Not configured
                healthBuilder.status(NOT_CONFIGURED_STATUS);
            } else {
                healthBuilder.withDetail(URL_FIELD, internalClient.getAccountUrl());
                StorageAccountInfo info = null;
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
