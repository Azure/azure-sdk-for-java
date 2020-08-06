/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileNotFoundException;
import java.io.OutputStream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Warren Zhu
 */
@SpringBootTest(properties = "spring.main.banner-mode=off")
@RunWith(SpringRunner.class)
public class AzureBlobStorageTests {

    private static final String containerName = "container";
    private static final String nonExisting = "non-existing";
    private static final String blobName = "blob";
    private static final long contentLength = 4096L;

    @Value("azure-blob://container/blob")
    private Resource remoteResource;

    @Autowired
    private BlobServiceClient blobServiceClient;

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyPath() {
        new BlobStorageResource(this.blobServiceClient, "azure-blob://");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSlashPath() {
        new BlobStorageResource(this.blobServiceClient, "azure-blob:///");
    }

    @Test
    public void testValidObject() throws Exception {
        Assert.assertTrue(this.remoteResource.exists());
        Assert.assertEquals(contentLength, this.remoteResource.contentLength());
    }

    @Test
    public void testWritable() throws Exception {
        Assert.assertTrue(this.remoteResource instanceof WritableResource);
        WritableResource writableResource = (WritableResource) this.remoteResource;
        Assert.assertTrue(writableResource.isWritable());
        writableResource.getOutputStream();
    }

    @Test
    public void testWritableOutputStream() throws Exception {
        String location = "azure-blob://container/blob";

        BlobStorageResource resource = new BlobStorageResource(blobServiceClient, location);
        OutputStream os = resource.getOutputStream();
        Assert.assertNotNull(os);
    }

    @Test(expected = FileNotFoundException.class)
    public void testWritableOutputStreamNoAutoCreateOnNullBlob() throws Exception {
        String location = "azure-blob://container/non-existing";

        BlobStorageResource resource = new BlobStorageResource(this.blobServiceClient, location);
        resource.getOutputStream();
    }

    @Test(expected = FileNotFoundException.class)
    public void testGetInputStreamOnNullBlob() throws Exception {
        String location = "azure-blob://container/non-existing";

        BlobStorageResource resource = new BlobStorageResource(blobServiceClient, location);
        resource.getInputStream();
    }

    @Test
    public void testGetFilenameOnNonExistingBlob() {
        String location = "azure-blob://container/non-existing";
        BlobStorageResource resource = new BlobStorageResource(blobServiceClient, location);
        Assert.assertEquals(nonExisting, resource.getFilename());
    }

    @Test
    public void testContainerDoesNotExist() {
        BlobStorageResource resource = new BlobStorageResource(this.blobServiceClient,
                "azure-blob://non-existing/blob");
        Assert.assertFalse(resource.exists());
    }

    @Test
    public void testContainerExistsButResourceDoesNot() {
        BlobStorageResource resource = new BlobStorageResource(this.blobServiceClient,
                "azure-blob://container/non-existing");
        Assert.assertFalse(resource.exists());
    }

    @Configuration
    @Import(AzureStorageProtocolResolver.class)
    static class StorageApplication {

        @Bean
        public static BlobServiceClient mockBlobServiceClient() {
            return mockBlobServiceClientBuilder().buildClient();
        }

        @Bean
        public static BlobServiceClientBuilder mockBlobServiceClientBuilder() {
            BlobServiceClientBuilder serviceClientBuilder = mock(BlobServiceClientBuilder.class);

            BlobServiceClient blobServiceClient = mock(BlobServiceClient.class);
            BlobContainerClient blobContainer = mock(BlobContainerClient.class);
            BlobContainerClient nonExistingBlobContainer = mock(BlobContainerClient.class);
            BlobClient blob = mock(BlobClient.class);
            BlobClient nonExistingBlob = mock(BlobClient.class);
            BlockBlobClient blockBlob = mock(BlockBlobClient.class);
            BlockBlobClient nonExistingBlockBlob = mock(BlockBlobClient.class);
            BlobProperties blobProperties = mock(BlobProperties.class);

            when(serviceClientBuilder.buildClient()).thenReturn(blobServiceClient);
            when(blobServiceClient.getBlobContainerClient(eq(containerName))).thenReturn(blobContainer);
            when(blobServiceClient.getBlobContainerClient(eq(nonExisting))).thenReturn(nonExistingBlobContainer);
            when(blobContainer.getBlobClient(eq(blobName))).thenReturn(blob);
            when(blobContainer.getBlobClient(eq(nonExisting))).thenReturn(nonExistingBlob);
            when(nonExistingBlobContainer.getBlobClient(anyString())).thenReturn(nonExistingBlob);
            when(blob.getBlockBlobClient()).thenReturn(blockBlob);
            when(nonExistingBlob.getBlockBlobClient()).thenReturn(nonExistingBlockBlob);

            when(blobContainer.exists()).thenReturn(true);
            when(nonExistingBlobContainer.exists()).thenReturn(false);
            when(blockBlob.exists()).thenReturn(true);
            when(nonExistingBlockBlob.exists()).thenReturn(false);

            when(blobContainer.getBlobContainerName()).thenReturn(containerName);
            when(blockBlob.getContainerName()).thenReturn(containerName);
            when(blockBlob.getBlobName()).thenReturn(blobName);
            when(nonExistingBlockBlob.getBlobName()).thenReturn(nonExisting);

            when(blockBlob.openInputStream()).thenReturn(mock(BlobInputStream.class));
            when(blockBlob.getBlobOutputStream()).thenReturn(mock(BlobOutputStream.class));

            when(blockBlob.getProperties()).thenReturn(blobProperties);
            when(blobProperties.getBlobSize()).thenReturn(contentLength);

            return serviceClientBuilder;
        }

    }

}
