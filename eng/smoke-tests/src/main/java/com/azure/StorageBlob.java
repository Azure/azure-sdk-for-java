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

    private static final Logger logger = LoggerFactory.getLogger(CosmosDB.class);


    private static void uploadBlob() throws IOException {
        logger.info("Uploading blob... ");
        String text = "This is a sample block blob created for SDK Smoke Test in Java!";
        ByteArrayInputStream data = new ByteArrayInputStream(text.getBytes());
        blobClient.upload(data, text.length());
        logger.info("\tDONE.");

    }

    private static void listBlobsInContainer() {
        logger.info("Listing all blobs in container...");
        Iterable<BlobItem> storageResponse = containerClient.listBlobsFlat();
        storageResponse.forEach(blobItem -> logger.info("\t{}",blobItem.name()));
        logger.info("DONE.");
    }

    private static void deleteBlob() {
        logger.info("Deleting blob... ");
        blobClient.delete();
        logger.info("\tDONE.");
    }

    public static void main(String[] args) throws IOException {
        logger.info("---------------------");
        logger.info("STORAGE - BLOB");
        logger.info("---------------------");

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
