// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.resource;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StorageBlobResourceTests {
    private BlobServiceClient blobServiceClient;

    @BeforeEach
    void init() {
        this.blobServiceClient = mock(BlobServiceClient.class);
    }

    @Test
    void getInputStreamThrowFileNotFoundExceptionWhenContainerNotFound() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient blobClient = mock(BlobClient.class);
        BlockBlobClient blockBlobClient = mock(BlockBlobClient.class);
        when(this.blobServiceClient.getBlobContainerClient("some-container")).thenReturn(containerClient);
        when(containerClient.getBlobClient("some-blob.txt")).thenReturn(blobClient);
        when(blobClient.getBlockBlobClient()).thenReturn(blockBlobClient);

        StorageBlobResource storageBlobResource = new StorageBlobResource(this.blobServiceClient, "azure-blob://some-container/some-blob.txt");

        BlobStorageException blobStorageException = mock(BlobStorageException.class);
        when(blobStorageException.getErrorCode()).thenReturn(BlobErrorCode.CONTAINER_NOT_FOUND);
        when(blockBlobClient.openInputStream()).thenThrow(blobStorageException);

        Assertions.assertThrows(FileNotFoundException.class, () -> storageBlobResource.getInputStream(), "Blob or container not existed.");
    }

    @Test
    void getInputStreamThrowFileNotFoundExceptionWhenBlobNotFound() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient blobClient = mock(BlobClient.class);
        BlockBlobClient blockBlobClient = mock(BlockBlobClient.class);
        when(this.blobServiceClient.getBlobContainerClient("some-container")).thenReturn(containerClient);
        when(containerClient.getBlobClient("some-blob.txt")).thenReturn(blobClient);
        when(blobClient.getBlockBlobClient()).thenReturn(blockBlobClient);

        StorageBlobResource storageBlobResource = new StorageBlobResource(this.blobServiceClient, "azure-blob://some-container/some-blob.txt");

        BlobStorageException blobStorageException = mock(BlobStorageException.class);
        when(blobStorageException.getErrorCode()).thenReturn(BlobErrorCode.CONTAINER_NOT_FOUND);
        when(blockBlobClient.openInputStream()).thenThrow(blobStorageException);

        Assertions.assertThrows(FileNotFoundException.class, () -> storageBlobResource.getInputStream(), "Blob or container not existed.");
    }

    @Test
    void getInputStreamThrowIOExceptionOnOtherStorageException() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient blobClient = mock(BlobClient.class);
        BlockBlobClient blockBlobClient = mock(BlockBlobClient.class);
        when(this.blobServiceClient.getBlobContainerClient("some-container")).thenReturn(containerClient);
        when(containerClient.getBlobClient("some-blob.txt")).thenReturn(blobClient);
        when(blobClient.getBlockBlobClient()).thenReturn(blockBlobClient);

        StorageBlobResource storageBlobResource = new StorageBlobResource(this.blobServiceClient, "azure-blob://some-container/some-blob.txt");

        BlobStorageException blobStorageException = mock(BlobStorageException.class);
        when(blobStorageException.getErrorCode()).thenReturn(BlobErrorCode.INVALID_INPUT);
        when(blockBlobClient.openInputStream()).thenThrow(blobStorageException);

        Assertions.assertThrows(IOException.class, () -> storageBlobResource.getInputStream());
    }

    @Test
    void getOutputStreamWhenShareExistsWithoutAutoCreate() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient blobClient = mock(BlobClient.class);
        BlockBlobClient blockBlobClient = mock(BlockBlobClient.class);
        when(this.blobServiceClient.getBlobContainerClient("some-container")).thenReturn(containerClient);
        when(containerClient.getBlobClient("some-blob.txt")).thenReturn(blobClient);
        when(blobClient.getBlockBlobClient()).thenReturn(blockBlobClient);

        StorageBlobResource storageBlobResource = new StorageBlobResource(this.blobServiceClient, "azure-blob://some-container/some-blob.txt", false);

        BlobOutputStream blobOutputStream = mock(BlobOutputStream.class);
        when(blockBlobClient.getBlobOutputStream(any(BlockBlobOutputStreamOptions.class))).thenReturn(blobOutputStream);

        BlobStorageException blobStorageException = mock(BlobStorageException.class);
        when(blobStorageException.getErrorCode()).thenReturn(BlobErrorCode.CONTAINER_NOT_FOUND);
        doThrow(blobStorageException).when(blobOutputStream).write(any());

        Assertions.assertThrows(BlobStorageException.class, () -> storageBlobResource.getOutputStream().write("some-text".getBytes()));
    }

}
