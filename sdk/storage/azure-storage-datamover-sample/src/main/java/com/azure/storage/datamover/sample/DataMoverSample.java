package com.azure.storage.datamover.sample;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.datamover.DataMover;
import com.azure.storage.datamover.DataMoverBuilder;
import com.azure.storage.datamover.DataTransfer;
import com.azure.storage.datamover.blob.BlobResources;
import com.azure.storage.datamover.filesystem.FileSystemResources;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DataMoverSample {

    private static final String STORAGE_CONNECTION_STRING = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
    private static final long timestamp = System.currentTimeMillis();

    public static void main(String[] args) {
        DataMover dataMover = new DataMoverBuilder().build();
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .connectionString(STORAGE_CONNECTION_STRING)
            .buildClient();


        //cleanup(blobServiceClient);
    }

    private static void transferFileToBlob(DataMover dataMover, BlobServiceClient blobServiceClient) throws Exception {
        BlobContainerClient containerClient = blobServiceClient.createBlobContainerIfNotExists(timestamp + "-01-transferFileToBlob");

        Path sampleFile = Paths.get(DataMoverSample.class.getResource("samplefile.txt").toURI());
        DataTransfer dataTransfer = dataMover.startTransfer(
            FileSystemResources.file(sampleFile),
            BlobResources.blob(containerClient.getBlobClient("samplefile.txt"))
        );

        dataTransfer.awaitCompletion();
    }

    private static void cleanup(BlobServiceClient blobServiceClient) {
        blobServiceClient.listBlobContainers()
            .forEach(item -> blobServiceClient.deleteBlobContainer(item.getName()));
    }
}
