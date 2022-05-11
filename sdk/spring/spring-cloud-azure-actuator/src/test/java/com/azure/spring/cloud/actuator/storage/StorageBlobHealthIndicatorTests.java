// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.storage;

import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.BlobServiceProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.core.io.ResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StorageBlobHealthIndicatorTests {

    private static final String MOCK_URL = "https://test.blob.core.windows.net/";

    @Test
    void storageBlobIsUp() {
        @SuppressWarnings("unchecked") Response<BlobServiceProperties> mockResponse =
            (Response<BlobServiceProperties>) mock(Response.class);
        BlobServiceAsyncClient mockAsyncClient = getMockBlobServiceAsyncClient();
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        StorageBlobHealthIndicator indicator = new StorageBlobHealthIndicator(mockAsyncClient, resourceLoader);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void storageBlobIsDown() {
        BlobServiceAsyncClient mockAsyncClient = getMockBlobServiceAsyncClient();
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getResource(anyString())).thenThrow(new RuntimeException());
        StorageBlobHealthIndicator indicator = new StorageBlobHealthIndicator(mockAsyncClient, resourceLoader);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    private BlobServiceAsyncClient getMockBlobServiceAsyncClient() {
        BlobServiceAsyncClient mockAsyncClient = mock(BlobServiceAsyncClient.class);
        when(mockAsyncClient.getAccountUrl()).thenReturn(MOCK_URL);
        return mockAsyncClient;
    }
}
