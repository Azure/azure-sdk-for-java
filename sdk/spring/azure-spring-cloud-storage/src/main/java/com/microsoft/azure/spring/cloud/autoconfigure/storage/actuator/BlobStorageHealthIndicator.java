// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.storage.actuator;

import static com.microsoft.azure.spring.cloud.autoconfigure.storage.actuator.AzureStorageActuatorConstants.POLL_TIMEOUT;
import static com.microsoft.azure.spring.cloud.autoconfigure.storage.actuator.AzureStorageActuatorConstants.URL_FIELD;
import static com.microsoft.azure.spring.cloud.autoconfigure.storage.actuator.AzureStorageActuatorConstants.NOT_CONFIGURED_STATUS;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.ApplicationContext;

import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.StorageAccountInfo;

public class BlobStorageHealthIndicator implements HealthIndicator {

    private ApplicationContext applicationContext;

    public BlobStorageHealthIndicator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Health health() {
        Health.Builder healthBuilder = new Health.Builder();

        try {
            BlobServiceClientBuilder blobStorageClientBuilder = applicationContext
                    .getBean(BlobServiceClientBuilder.class);
            BlobServiceAsyncClient client = blobStorageClientBuilder.buildAsyncClient();
            if (client == null) { // Not configured
                healthBuilder.status(NOT_CONFIGURED_STATUS);
            } else {
                healthBuilder.withDetail(URL_FIELD, client.getAccountUrl());
                StorageAccountInfo info = null;
                try {
                    info = client.getAccountInfo().block(POLL_TIMEOUT);
                    if (info != null) {
                        healthBuilder.up();
                    }
                } catch (Exception e) {
                    healthBuilder.down(e);
                }

            }

        } catch (NoSuchBeanDefinitionException nsbe) {
            healthBuilder.status(NOT_CONFIGURED_STATUS);
        } catch (Exception e) {
            healthBuilder.status("Could not complete health check.").down(e);
        }
        return healthBuilder.build();
    }
}
