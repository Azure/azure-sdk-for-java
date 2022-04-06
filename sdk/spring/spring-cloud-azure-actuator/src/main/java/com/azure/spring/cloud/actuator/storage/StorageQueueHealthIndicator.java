// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.storage;

import com.azure.core.http.rest.Response;
import com.azure.storage.queue.QueueServiceAsyncClient;
import com.azure.storage.queue.models.QueueServiceProperties;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

import java.time.Duration;

import static com.azure.spring.cloud.actuator.implementation.util.ActuateConstants.DEFAULT_HEALTH_CHECK_TIMEOUT;
import static com.azure.spring.cloud.actuator.storage.StorageHealthConstants.URL_FIELD;

/**
 * Health indicator for file storage.
 */
public class StorageQueueHealthIndicator extends AbstractHealthIndicator {

    private final QueueServiceAsyncClient internalClient;
    private Duration timeout = DEFAULT_HEALTH_CHECK_TIMEOUT;

    /**
     * Creates a new instance of {@link StorageQueueHealthIndicator}.
     * @param queueServiceClient the queue service client
     */
    public StorageQueueHealthIndicator(QueueServiceAsyncClient queueServiceClient) {
        internalClient = queueServiceClient;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        builder.withDetail(URL_FIELD, internalClient.getQueueServiceUrl());
        Response<QueueServiceProperties> infoResponse = internalClient.getPropertiesWithResponse()
                                                                      .block(timeout);
        if (infoResponse != null) {
            builder.up();
        }
    }

    /**
     * Set health check request timeout.
     * @param timeout the duration value.
     */
    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }
}
