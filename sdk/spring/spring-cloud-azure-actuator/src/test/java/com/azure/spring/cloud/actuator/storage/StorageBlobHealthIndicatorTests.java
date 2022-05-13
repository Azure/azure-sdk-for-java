// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.storage;

import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.DownloadRetryOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StorageBlobHealthIndicatorTests {

    private static final String MOCK_URL = "https://test.blob.core.windows.net/";

    private BlobAsyncClient mockBlobAsyncClient;
    private BlobServiceAsyncClient mockBlobServiceAsyncClient;

    @BeforeEach
    void setup() {
        mockBlobServiceAsyncClient = getMockBlobServiceAsyncClient();
        BlobContainerAsyncClient containerAsyncClient = mock(BlobContainerAsyncClient.class);
        mockBlobAsyncClient = mock(BlobAsyncClient.class);
        when(mockBlobServiceAsyncClient.getBlobContainerAsyncClient(anyString())).thenReturn(containerAsyncClient);
        when(containerAsyncClient.getBlobAsyncClient(anyString())).thenReturn(mockBlobAsyncClient);
    }

    @Test
    void storageBlobIsUp() {
        BlobDownloadAsyncResponse blobDownloadAsyncResponse = mock(BlobDownloadAsyncResponse.class);

        when(mockBlobAsyncClient.downloadStreamWithResponse(
            any(BlobRange.class),
            any(DownloadRetryOptions.class),
            isNull(),
            eq(false)
        )).thenReturn(Mono.just(blobDownloadAsyncResponse));

        StorageBlobHealthIndicator indicator = new StorageBlobHealthIndicator(mockBlobServiceAsyncClient);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void testWithBlobStorageExceptionForUp() {
        BlobStorageException exceptionMock = mock(BlobStorageException.class);
        when(mockBlobAsyncClient.downloadStreamWithResponse(
            any(BlobRange.class),
            any(DownloadRetryOptions.class),
            isNull(),
            eq(false)
        )).thenReturn(Mono.error(exceptionMock));

        StorageBlobHealthIndicator indicator = new StorageBlobHealthIndicator(mockBlobServiceAsyncClient);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }


    @Test
    void storageBlobIsDown() {
        when(mockBlobAsyncClient.downloadStreamWithResponse(
            any(BlobRange.class),
            any(DownloadRetryOptions.class),
            isNull(),
            eq(false)
        )).thenReturn(Mono.error(new IllegalStateException("The gremlins have cut the cable.")));

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
