// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.implementation.storage;

import com.azure.core.http.rest.Response;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.models.ShareServiceProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StorageFileShareHealthIndicatorTests {

    private static final String MOCK_URL = "https://test.file.core.windows.net/";

    @Test
    void storageFileIsUp() {
        ShareServiceAsyncClient mockAsyncClient = getMockShareServiceAsyncClient();
        @SuppressWarnings("unchecked") Response<ShareServiceProperties> mockResponse =
            (Response<ShareServiceProperties>) Mockito.mock(Response.class);
        when(mockAsyncClient.getPropertiesWithResponse()).thenReturn(Mono.just(mockResponse));
        StorageFileShareHealthIndicator indicator = new StorageFileShareHealthIndicator(mockAsyncClient);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void storageFileIsDown() {
        ShareServiceAsyncClient mockAsyncClient = getMockShareServiceAsyncClient();
        when(mockAsyncClient.getPropertiesWithResponse())
            .thenReturn(Mono.error(new IllegalStateException("The gremlins have cut the cable.")));
        StorageFileShareHealthIndicator indicator = new StorageFileShareHealthIndicator(mockAsyncClient);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    private ShareServiceAsyncClient getMockShareServiceAsyncClient() {
        ShareServiceAsyncClient mockAsyncClient = mock(ShareServiceAsyncClient.class);
        when(mockAsyncClient.getFileServiceUrl()).thenReturn(MOCK_URL);
        return mockAsyncClient;
    }
}
