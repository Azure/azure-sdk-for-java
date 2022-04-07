// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.resource;

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
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AzureStorageBlobProtocolResolverTest extends AbstractAzureStorageProtocolResolverTests {


    private BlobServiceClient blobServiceClient;
    private BlobClient blobClient;
    private BlockBlobClient blockBlobClient;
    private BlobContainerClient blobContainerClient;
    private ConfigurableListableBeanFactory beanFactory;


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
        beanFactory = mock(ConfigurableListableBeanFactory.class);
        when(beanFactory.getBean(BlobServiceClient.class)).thenReturn(blobServiceClient);
        AzureStorageBlobProtocolResolver protocolResolver = new AzureStorageBlobProtocolResolver();
        protocolResolver.postProcessBeanFactory(beanFactory);
        return protocolResolver;
    }

    @Test
    void testGetResourceWithExistingResource() {
        super.testGetResourceWithExistingResource();
        verify(blobContainerClient, times(1)).exists();
        verify(blockBlobClient, times(1)).exists();
    }


    @Test
    void testValidObject() throws Exception {
        super.testValidObject();
        verify(blockBlobClient, times(1)).getProperties();
    }

    @Test
    void protocolPatternMatched() {
        when(blobServiceClient.getBlobContainerClient(anyString())).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);

        String[] locations = new String[]{"azure-blob://test/test", "azure-blob://test/test2"};
        Resource[] resources = getResources(locations);
        assertEquals(locations.length, resources.length, "Correct number of resources found");
    }

    @Test
    void protocolPatternNotMatched() {
        when(blobServiceClient.getBlobContainerClient(anyString())).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);

        String[] locations = new String[]{"azureblob:test/test", "otherblob:test/test2"};
        Resource[] resources = getResources(locations);
        assertEquals(0, resources.length, "No resolved resources found");
    }
}
