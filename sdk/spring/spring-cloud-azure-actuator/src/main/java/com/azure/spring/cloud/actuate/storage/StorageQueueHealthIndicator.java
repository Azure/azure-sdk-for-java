// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.storage;

import com.azure.core.http.rest.Response;
import com.azure.storage.queue.QueueServiceAsyncClient;
import com.azure.storage.queue.models.QueueServiceProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import static com.azure.spring.cloud.actuate.storage.StorageHealthConstants.POLL_TIMEOUT;
import static com.azure.spring.cloud.actuate.storage.StorageHealthConstants.URL_FIELD;

/**
 * Health indicator for file storage.
 */
public class StorageQueueHealthIndicator implements HealthIndicator {

    private final QueueServiceAsyncClient internalClient;

    public StorageQueueHealthIndicator(QueueServiceAsyncClient queueServiceClient) {
        internalClient = queueServiceClient;
    }

    @Override
    public Health health() {
        Health.Builder healthBuilder = new Health.Builder();

        try {
            healthBuilder.withDetail(URL_FIELD, internalClient.getQueueServiceUrl());
            Response<QueueServiceProperties> infoResponse;
            try {
                infoResponse = internalClient.getPropertiesWithResponse().block(POLL_TIMEOUT);
                if (infoResponse != null) {
                    healthBuilder.up();
                }
            } catch (Exception e) {
                healthBuilder.down(e);
            }
        } catch (Exception e) {
            healthBuilder.status("Could not complete health check.").down(e);
        }

        return healthBuilder.build();
    }
}
