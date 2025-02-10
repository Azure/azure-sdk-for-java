// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.implementation.storage;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StorageBlobHealthIndicatorTests {

    private static final String MOCK_URL = "https://test.blob.core.windows.net/";

    private BlobServiceAsyncClient mockBlobServiceAsyncClient;
    private BlobContainerAsyncClient containerAsyncClient;

    @BeforeEach
    void setup() {
        mockBlobServiceAsyncClient = getMockBlobServiceAsyncClient();
        containerAsyncClient = mock(BlobContainerAsyncClient.class);
        when(mockBlobServiceAsyncClient.getBlobContainerAsyncClient(anyString())).thenReturn(containerAsyncClient);

    }

    @Test
    void storageBlobIsUp() {
        when(containerAsyncClient.existsWithResponse()).thenReturn(Mono.just(new Response<Boolean>() {

            @Override
            public int getStatusCode() {
                return 404;
            }

            @Override
            public HttpHeaders getHeaders() {
                return null;
            }

            @Override
            public HttpRequest getRequest() {
                return null;
            }

            @Override
            public Boolean getValue() {
                return false;
            }
        }));

        StorageBlobHealthIndicator indicator = new StorageBlobHealthIndicator(mockBlobServiceAsyncClient);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void storageBlobIsDown() {
        when(containerAsyncClient.existsWithResponse())
            .thenReturn(Mono.error(new IllegalStateException("The gremlins have cut the cable.")));

        StorageBlobHealthIndicator indicator = new StorageBlobHealthIndicator(mockBlobServiceAsyncClient);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    private BlobServiceAsyncClient getMockBlobServiceAsyncClient() {
        BlobServiceAsyncClient mockAsyncClient = mock(BlobServiceAsyncClient.class);
        when(mockAsyncClient.getAccountUrl()).thenReturn(MOCK_URL);
        return mockAsyncClient;
    }
}
