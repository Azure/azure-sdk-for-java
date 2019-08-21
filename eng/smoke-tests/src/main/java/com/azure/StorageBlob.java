// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlockBlobClient;
import com.azure.storage.blob.ContainerClient;
import com.azure.storage.blob.models.BlobItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

public class StorageBlob {
    private static ContainerClient containerClient;
    private static BlockBlobClient blobClient;

    private static final String STORAGE_CONNECTION_STRING = System.getenv("AZURE_STORAGE_CONNECTION_STRING");

    private static final String CONTAINER_NAME = "mycontainer"; //This sample needs an existing container
    private static final String BLOB_NAME = "javaSmokeTestBlob-"+ UUID.randomUUID() +".txt";

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosDB.class);
    
    private static void uploadBlob() throws IOException {
        LOGGER.info("Uploading blob... ");
        String text = "This is a sample block blob created for SDK Smoke Test in Java!";
        ByteArrayInputStream data = new ByteArrayInputStream(text.getBytes());
        blobClient.upload(data, text.length());
        LOGGER.info("\tDONE.");

    }

    private static void listBlobsInContainer() {
        LOGGER.info("Listing all blobs in container...");
        Iterable<BlobItem> storageResponse = containerClient.listBlobsFlat();
        storageResponse.forEach(blobItem -> LOGGER.info("\t{}",blobItem.name()));
        LOGGER.info("DONE.");
    }

    private static void deleteBlob() {
        LOGGER.info("Deleting blob... ");
        blobClient.delete();
        LOGGER.info("\tDONE.");
    }

    public static void main(String[] args) throws IOException {
        LOGGER.info("---------------------");
        LOGGER.info("STORAGE - BLOB");
        LOGGER.info("---------------------");

        BlobServiceClient serviceClient = new BlobServiceClientBuilder().connectionString(STORAGE_CONNECTION_STRING).buildClient();
        containerClient = serviceClient.getContainerClient(CONTAINER_NAME);
        blobClient = containerClient.getBlockBlobClient(BLOB_NAME);

        try{
            uploadBlob();
            listBlobsInContainer();
        }
        finally {
            deleteBlob();
        }
    }
}
