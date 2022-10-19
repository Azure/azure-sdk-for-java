// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.storage;

import com.azure.core.http.rest.Response;
import com.azure.storage.queue.QueueServiceAsyncClient;
import com.azure.storage.queue.models.QueueServiceProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StorageQueueHealthIndicatorTests {

    private static final String MOCK_URL = "https://test.queue.core.windows.net/";

    @Test
    void storageQueueIsUp() {
        QueueServiceAsyncClient mockAsyncClient = getMockQueueServiceAsyncClient();
        @SuppressWarnings("unchecked") Response<QueueServiceProperties> mockResponse =
            (Response<QueueServiceProperties>) Mockito.mock(Response.class);
        when(mockAsyncClient.getPropertiesWithResponse()).thenReturn(Mono.just(mockResponse));
        StorageQueueHealthIndicator indicator = new StorageQueueHealthIndicator(mockAsyncClient);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void storageQueueIsDown() {
        QueueServiceAsyncClient mockAsyncClient = getMockQueueServiceAsyncClient();
        when(mockAsyncClient.getPropertiesWithResponse())
            .thenReturn(Mono.error(new IllegalStateException("The gremlins have cut the cable.")));
        StorageQueueHealthIndicator indicator = new StorageQueueHealthIndicator(mockAsyncClient);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    private QueueServiceAsyncClient getMockQueueServiceAsyncClient() {
        QueueServiceAsyncClient mockAsyncClient = mock(QueueServiceAsyncClient.class);
        when(mockAsyncClient.getQueueServiceUrl()).thenReturn(MOCK_URL);
        return mockAsyncClient;
    }
}
