// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.resource;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

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

        Tuple3<BlobContainerClient, BlobClient, BlockBlobClient> clientTuple3 =
            mockClientsForExistingContainerAndBlob(this.blobServiceClient, CONTAINER_NAME, EXISTING_ITEM_NAME);

        this.blobContainerClient = clientTuple3.getT1();
        this.blobClient = clientTuple3.getT2();
        this.blockBlobClient = clientTuple3.getT3();

        mockClientsForNonExistingContainerAndBlob(this.blobServiceClient, NON_EXISTING_CONTAINER_NAME, NON_EXISTING_ITEM_NAME);
        mockClientsForNonExistingBlob(this.blobContainerClient, CONTAINER_NAME, NON_EXISTING_ITEM_NAME);
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

    private Tuple3<BlobContainerClient, BlobClient, BlockBlobClient> mockClientsForExistingContainerAndBlob(
        BlobServiceClient blobServiceClient, String containerName, String blobName) {
        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);
        BlobClient blobClient = mock(BlobClient.class);
        BlockBlobClient blockBlobClient = mock(BlockBlobClient.class);

        when(blobServiceClient.getBlobContainerClient(eq(containerName))).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(eq(blobName))).thenReturn(blobClient);
        when(blobClient.getBlockBlobClient()).thenReturn(blockBlobClient);

        when(blobContainerClient.exists()).thenReturn(true);
        when(blobContainerClient.getBlobContainerName()).thenReturn(containerName);

        when(blockBlobClient.getContainerName()).thenReturn(containerName);
        when(blockBlobClient.getBlobName()).thenReturn(blobName);
        when(blockBlobClient.openInputStream(any(BlobInputStreamOptions.class))).thenReturn(mock(BlobInputStream.class));
        when(blockBlobClient.getBlobOutputStream(any(BlockBlobOutputStreamOptions.class))).thenReturn(mock(BlobOutputStream.class));
        when(blockBlobClient.exists()).thenReturn(true);

        BlobProperties blobProperties = mock(BlobProperties.class);
        when(blobProperties.getBlobSize()).thenReturn(CONTENT_LENGTH);
        when(blockBlobClient.getProperties()).thenReturn(blobProperties);
        return Tuples.of(blobContainerClient, blobClient, blockBlobClient);
    }

    private void mockClientsForNonExistingContainerAndBlob(BlobServiceClient blobServiceClient,
                                                           String containerName,
                                                           String blobName) {
        BlobContainerClient nonExistingBlobContainer = mock(BlobContainerClient.class);
        BlobClient nonExistingBlob = mock(BlobClient.class);
        BlockBlobClient nonExistingBlockBlob = mock(BlockBlobClient.class);

        when(blobServiceClient.getBlobContainerClient(eq(containerName))).thenReturn(nonExistingBlobContainer);
        when(nonExistingBlobContainer.getBlobClient(eq(blobName))).thenReturn(nonExistingBlob);
        when(nonExistingBlob.getBlockBlobClient()).thenReturn(nonExistingBlockBlob);

        when(nonExistingBlobContainer.exists()).thenReturn(false);

        when(nonExistingBlockBlob.exists()).thenReturn(false);
        when(nonExistingBlockBlob.getContainerName()).thenReturn(containerName);
        when(nonExistingBlockBlob.getBlobName()).thenReturn(blobName);

        BlobStorageException blobStorageException = mock(BlobStorageException.class);
        when(blobStorageException.getErrorCode()).thenReturn(BlobErrorCode.CONTAINER_NOT_FOUND);
        when(nonExistingBlockBlob.openInputStream()).thenThrow(blobStorageException);
    }

    private void mockClientsForNonExistingBlob(BlobContainerClient blobContainerClient, String containerName, String blobName) {
        BlobClient nonExistingBlob = mock(BlobClient.class);
        BlockBlobClient nonExistingBlockBlob = mock(BlockBlobClient.class);

        when(blobContainerClient.getBlobClient(eq(blobName))).thenReturn(nonExistingBlob);
        when(nonExistingBlob.getBlockBlobClient()).thenReturn(nonExistingBlockBlob);

        when(nonExistingBlockBlob.exists()).thenReturn(false);
        when(nonExistingBlockBlob.getContainerName()).thenReturn(containerName);
        when(nonExistingBlockBlob.getBlobName()).thenReturn(blobName);

        BlobStorageException blobStorageException = mock(BlobStorageException.class);
        when(blobStorageException.getErrorCode()).thenReturn(BlobErrorCode.BLOB_NOT_FOUND);
        when(nonExistingBlockBlob.openInputStream()).thenThrow(blobStorageException);
    }
}
