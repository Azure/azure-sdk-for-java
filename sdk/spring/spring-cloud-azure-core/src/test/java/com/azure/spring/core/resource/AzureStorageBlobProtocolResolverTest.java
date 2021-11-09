// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.resource;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ProtocolResolver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AzureStorageBlobProtocolResolverTest extends AbstractAzureStorageProtocolResolverTest {


    private BlobServiceClient blobServiceClient;
    private BlobClient blobClient;
    private BlockBlobClient blockBlobClient;
    private BlobContainerClient blobContainerClient;


    @Override
    protected void initializeSDKClient() {
        blobServiceClient = mock(BlobServiceClient.class);

        // azure-blob://container/blob
        // mock sdk client to return an existing blob information
        blobContainerClient = mock(BlobContainerClient.class);
        blobClient = mock(BlobClient.class);
        blockBlobClient = mock(BlockBlobClient.class);

        when(blobServiceClient.getBlobContainerClient(eq(CONTAINER_NAME))).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(eq(EXISTING_ITEM_NAME))).thenReturn(blobClient);
        when(blobClient.getBlockBlobClient()).thenReturn(blockBlobClient);


        when(blobContainerClient.exists()).thenReturn(true);
        when(blockBlobClient.exists()).thenReturn(true);
        BlobProperties blobProperties = mock(BlobProperties.class);
        when(blockBlobClient.getProperties()).thenReturn(blobProperties);
        when(blobProperties.getBlobSize()).thenReturn(CONTENT_LENGTH);

        when(blobContainerClient.getBlobContainerName()).thenReturn(CONTAINER_NAME);
        when(blockBlobClient.getContainerName()).thenReturn(CONTAINER_NAME);
        when(blockBlobClient.getBlobName()).thenReturn(EXISTING_ITEM_NAME);
        when(blockBlobClient.openInputStream(any(BlobInputStreamOptions.class))).thenReturn(mock(BlobInputStream.class));
        when(blockBlobClient.getBlobOutputStream(any(BlockBlobOutputStreamOptions.class))).thenReturn(mock(BlobOutputStream.class));

        // azure-blob://non-existing/non-existing
        //mock sdk to return a non-existing blob (neither container nor blob exist)
        BlobContainerClient nonExistingBlobContainer = mock(BlobContainerClient.class);
        BlobClient nonExistingBlob = mock(BlobClient.class);
        BlockBlobClient nonExistingBlockBlob = mock(BlockBlobClient.class);

        when(blobServiceClient.getBlobContainerClient(eq(NON_EXISTING))).thenReturn(nonExistingBlobContainer);
        when(blobContainerClient.getBlobClient(eq(NON_EXISTING))).thenReturn(nonExistingBlob);
        when(nonExistingBlobContainer.getBlobClient(anyString())).thenReturn(nonExistingBlob);
        when(nonExistingBlob.getBlockBlobClient()).thenReturn(nonExistingBlockBlob);
        when(nonExistingBlobContainer.exists()).thenReturn(false);
        when(nonExistingBlockBlob.exists()).thenReturn(false);
        when(nonExistingBlockBlob.getBlobName()).thenReturn(NON_EXISTING);

    }

    @Override
    protected StorageType getStorageType() {
        return StorageType.BLOB;
    }

    @Override
    protected ProtocolResolver createInstance() {
        return new AzureStorageBlobProtocolResolver(blobServiceClient);
    }

    @Test
    void testGetResourceWithExistingResource() {
//        String resourceName = CONTAINER_NAME + "/" + BLOB_NAME;
//        Resource resource = getResource(resourceName);
//        assertNotNull(resource);
//        assertTrue(resource.exists());
        super.testGetResourceWithExistingResource();
        verify(blobContainerClient, times(1)).exists();
        verify(blockBlobClient, times(1)).exists();
    }


    @Test
    void testValidObject() throws Exception {
//        Resource resource = getResource("container/blob");
//        assertTrue(resource.exists());
//        assertEquals(CONTENT_LENGTH, resource.contentLength());
        super.testValidObject();
        verify(blockBlobClient, times(1)).getProperties();
    }


}
