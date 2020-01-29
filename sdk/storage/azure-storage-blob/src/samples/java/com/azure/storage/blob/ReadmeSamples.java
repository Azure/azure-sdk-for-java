// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.specialized.BlockBlobClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
public class ReadmeSamples {

    private BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().buildClient();
    private BlobContainerClient blobContainerClient = new BlobContainerClientBuilder().buildClient();
    private BlobClient blobClient = new BlobClientBuilder().buildClient();

    public void getBlobServiceClient1() {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .endpoint("<your-storage-account-url>")
            .sasToken("<your-sasToken>")
            .buildClient();
    }

    public void getBlobServiceClient2() {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .endpoint("<your-storage-account-url>" + "?" + "<your-sasToken>")
            .buildClient();
    }

    public void getBlobContainerClient1() {
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient("mycontainer");
    }

    public void getBlobContainerClient2() {
        BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
            .endpoint("<your-storage-account-url>")
            .sasToken("<your-sasToken>")
            .containerName("mycontainer")
            .buildClient();
    }

    public void getBlobContainerClient3() {
        BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
            .endpoint("<your-storage-account-url>" + "/" + "mycontainer" + "?" + "<your-sasToken>")
            .buildClient();
    }

    public void getBlobClient1() {
        BlobClient blobClient = blobContainerClient.getBlobClient("myblob");
    }

    public void getBlobClient2() {
        BlobClient blobClient = new BlobClientBuilder()
            .endpoint("<your-storage-account-url>")
            .sasToken("<your-sasToken>")
            .containerName("mycontainer")
            .blobName("myblob")
            .buildClient();
    }

    public void getBlobClient3() {
        BlobClient blobClient = new BlobClientBuilder()
            .endpoint("<your-storage-account-url>" + "/" + "mycontainer" + "/" + "myblob" + "?" + "<your-sasToken>")
            .buildClient();
    }

    public void createBlobContainerClient1() {
        blobServiceClient.createBlobContainer("mycontainer");
    }

    public void createBlobContainerClient2() {
        blobContainerClient.create();
    }

    public void uploadBlobFromStream() {
        BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient("myblockblob").getBlockBlobClient();
        String dataSample = "samples";
        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(dataSample.getBytes())) {
            blockBlobClient.upload(dataStream, dataSample.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadBlobFromFile() {
        BlobClient blobClient = blobContainerClient.getBlobClient("myblockblob");
        blobClient.uploadFromFile("local-file.jpg");
    }

    public void downloadBlobToStream() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            blobClient.download(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadBlobToFile() {
        blobClient.downloadToFile("downloaded-file.jpg");
    }

    public void enumerateBlobs() {
        for (BlobItem blobItem : blobContainerClient.listBlobs()) {
            System.out.println("This is the blob name: " + blobItem.getName());
        }
    }

    public void authWithIdentity() {
        BlobServiceClient blobStorageClient = new BlobServiceClientBuilder()
            .endpoint("<your-storage-account-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
    }

}

