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

import com.azure.core.http.rest.Response;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import com.azure.storage.file.share.models.ShareServiceProperties;

public class FileStorageHealthIndicator implements HealthIndicator {

    private ShareServiceAsyncClient internalClient;

    public FileStorageHealthIndicator(ApplicationContext applicationContext) {
        ShareServiceClientBuilder shareStorageClientBuilder = applicationContext
            .getBean(ShareServiceClientBuilder.class);
        internalClient = shareStorageClientBuilder.buildAsyncClient();
    }

    @Override
    public Health health() {
        Health.Builder healthBuilder = new Health.Builder();

        try {
            healthBuilder.withDetail(URL_FIELD, internalClient.getFileServiceUrl());
            Response<ShareServiceProperties> infoResponse = null;
            try {
                infoResponse = internalClient.getPropertiesWithResponse().block(POLL_TIMEOUT);
                if (infoResponse != null) {
                    healthBuilder.up();
                }
            } catch (Exception e) {
                healthBuilder.down(e);
            }
        } catch (NoSuchBeanDefinitionException nsbe) {
            healthBuilder.status(NOT_CONFIGURED_STATUS);
        }

        return healthBuilder.build();
    }
}
