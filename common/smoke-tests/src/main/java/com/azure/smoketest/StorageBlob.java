// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.smoketest;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

public class StorageBlob {
    private static BlobContainerClient containerClient;
    private static BlockBlobClient blockBlobClient;

    private static final String STORAGE_CONNECTION_STRING = System.getenv("AZURE_STORAGE_CONNECTION_STRING");

    private static final String CONTAINER_NAME = "mycontainer"; //This sample needs an existing container
    private static final String BLOB_NAME = "javaSmokeTestBlob-"+ UUID.randomUUID() +".txt";

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageBlob.class);

    private static void uploadBlob() throws IOException {
        LOGGER.info("Uploading blob... ");
        String text = "This is a sample block blob created for SDK Smoke Test in Java!";
        ByteArrayInputStream data = new ByteArrayInputStream(text.getBytes());
        blockBlobClient.upload(data, text.length());
        LOGGER.info("\tDONE.");
    }

    private static void listBlobsInContainer() {
        LOGGER.info("Listing all blobs in container...");
        Iterable<BlobItem> storageResponse = containerClient.listBlobs();
        storageResponse.forEach(blobItem -> LOGGER.info("\t{}",blobItem.getName()));
        LOGGER.info("DONE.");
    }

    private static void deleteBlob() {
        LOGGER.info("Deleting blob... ");
        blockBlobClient.delete();
        LOGGER.info("\tDONE.");
    }

    public static void main(String[] args) throws IOException {
        LOGGER.info("---------------------");
        LOGGER.info("STORAGE - BLOB");
        LOGGER.info("---------------------");

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
            .connectionString(STORAGE_CONNECTION_STRING)
            .buildClient();
        containerClient = serviceClient.getBlobContainerClient(CONTAINER_NAME);
        BlobClient blobClient = containerClient.getBlobClient(BLOB_NAME);
        blockBlobClient = blobClient.getBlockBlobClient();

        try{
            uploadBlob();
            listBlobsInContainer();
        }
        finally {
            deleteBlob();
        }
    }
}
