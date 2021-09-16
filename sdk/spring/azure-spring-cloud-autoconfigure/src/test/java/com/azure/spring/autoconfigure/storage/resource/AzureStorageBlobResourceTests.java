// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.storage.resource;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;

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
public class AzureStorageBlobResourceTests {

    private static final String CONTAINER_NAME = "container";
    private static final String NON_EXISTING = "non-existing";
    private static final String BLOB_NAME = "blob";
    private static final long CONTENT_LENGTH = 4096L;

    @Value("azure-blob://container/blob")
    private Resource remoteResource;

    @Autowired
    private BlobServiceClient blobServiceClient;

    @Test
    void testEmptyPath() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new StorageBlobResource(this.blobServiceClient,
            "azure-blob://"));
    }

    @Test
    void testSlashPath() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new StorageBlobResource(this.blobServiceClient,
            "azure-blob:///"));
    }

    @Test
    void testValidObject() throws Exception {
        Assertions.assertTrue(this.remoteResource.exists());
        Assertions.assertEquals(CONTENT_LENGTH, this.remoteResource.contentLength());
    }

    @Test
    void testWritable() throws Exception {
        Assertions.assertTrue(this.remoteResource instanceof WritableResource);
        WritableResource writableResource = (WritableResource) this.remoteResource;
        Assertions.assertTrue(writableResource.isWritable());
        writableResource.getOutputStream();
    }

    @Test
    void testWritableOutputStream() throws Exception {
        String location = "azure-blob://container/blob";

        StorageBlobResource resource = new StorageBlobResource(blobServiceClient, location);
        OutputStream os = resource.getOutputStream();
        Assertions.assertNotNull(os);
    }

    @Test
    void testWritableOutputStreamNoAutoCreateOnNullBlob() {
        String location = "azure-blob://container/non-existing";

        StorageBlobResource resource = new StorageBlobResource(this.blobServiceClient, location);
        Assertions.assertThrows(FileNotFoundException.class, () -> resource.getOutputStream());
    }

    @Test
    void testGetInputStreamOnNullBlob() {
        String location = "azure-blob://container/non-existing";

        StorageBlobResource resource = new StorageBlobResource(blobServiceClient, location);
        Assertions.assertThrows(FileNotFoundException.class, () -> resource.getInputStream());
    }

    @Test
    void testGetFilenameOnNonExistingBlob() {
        String location = "azure-blob://container/non-existing";
        StorageBlobResource resource = new StorageBlobResource(blobServiceClient, location);
        Assertions.assertEquals(NON_EXISTING, resource.getFilename());
    }

    @Test
    void testContainerDoesNotExist() {
        StorageBlobResource resource = new StorageBlobResource(this.blobServiceClient,
            "azure-blob://non-existing/blob");
        Assertions.assertFalse(resource.exists());
    }

    @Test
    void testContainerExistsButResourceDoesNot() {
        StorageBlobResource resource = new StorageBlobResource(this.blobServiceClient,
            "azure-blob://container/non-existing");
        Assertions.assertFalse(resource.exists());
    }

    @Configuration
    @Import({ StorageBlobClientConfiguration.class })
    static class StorageApplication {

    }

    static class StorageBlobClientConfiguration {

        @Bean
        public BlobServiceClient blobServiceClient() {
            return mockBlobServiceClientBuilder().buildClient();
        }

        public BlobServiceClientBuilder mockBlobServiceClientBuilder() {
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
            when(blobServiceClient.getBlobContainerClient(eq(CONTAINER_NAME))).thenReturn(blobContainer);
            when(blobServiceClient.getBlobContainerClient(eq(NON_EXISTING))).thenReturn(nonExistingBlobContainer);
            when(blobContainer.getBlobClient(eq(BLOB_NAME))).thenReturn(blob);
            when(blobContainer.getBlobClient(eq(NON_EXISTING))).thenReturn(nonExistingBlob);
            when(nonExistingBlobContainer.getBlobClient(anyString())).thenReturn(nonExistingBlob);
            when(blob.getBlockBlobClient()).thenReturn(blockBlob);
            when(nonExistingBlob.getBlockBlobClient()).thenReturn(nonExistingBlockBlob);

            when(blobContainer.exists()).thenReturn(true);
            when(nonExistingBlobContainer.exists()).thenReturn(false);
            when(blockBlob.exists()).thenReturn(true);
            when(nonExistingBlockBlob.exists()).thenReturn(false);

            when(blobContainer.getBlobContainerName()).thenReturn(CONTAINER_NAME);
            when(blockBlob.getContainerName()).thenReturn(CONTAINER_NAME);
            when(blockBlob.getBlobName()).thenReturn(BLOB_NAME);
            when(nonExistingBlockBlob.getBlobName()).thenReturn(NON_EXISTING);

            when(blockBlob.openInputStream()).thenReturn(mock(BlobInputStream.class));
            when(blockBlob.getBlobOutputStream(true)).thenReturn(mock(BlobOutputStream.class));

            when(blockBlob.getProperties()).thenReturn(blobProperties);
            when(blobProperties.getBlobSize()).thenReturn(CONTENT_LENGTH);

            return serviceClientBuilder;
        }
    }

}
