package com.azure;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlockBlobClient;
import com.azure.storage.blob.ContainerClient;
import com.azure.storage.blob.models.BlobItem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

public class StorageBlob {
    private static ContainerClient containerClient;
    private static BlockBlobClient blobClient;

    private static final String STORAGE_CONNECTION_STRING = System.getenv("STORAGE_CONNECTION_STRING");

    private static final String CONTAINER_NAME = "mycontainer"; //This sample needs an existing container
    private static final String BLOB_NAME = "javaSmokeTestBlob-"+ UUID.randomUUID() +".txt";

    private static void uploadBlob() throws IOException {
        System.out.print("Uploading blob... ");
        String text = "This is a sample block blob created for SDK Smoke Test in Java!";
        ByteArrayInputStream data = new ByteArrayInputStream(text.getBytes());
        blobClient.upload(data, text.length());
        System.out.println("\tDONE.");

    }

    private static void listBlobsInContainer() {
        System.out.println("Listing all blobs in container...");
        Iterable<BlobItem> storageResponse = containerClient.listBlobsFlat();
        storageResponse.forEach(blobItem -> System.out.println("\t" + blobItem.name()));
        System.out.println("DONE.");
    }

    private static void deleteBlob() {
        System.out.print("Deleting blob... ");
        blobClient.delete();
        System.out.println("\tDONE.");
    }

    public static void main(String[] args) throws IOException {
        System.out.println("\n---------------------");
        System.out.println("STORAGE - BLOB");
        System.out.println("---------------------\n");

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
