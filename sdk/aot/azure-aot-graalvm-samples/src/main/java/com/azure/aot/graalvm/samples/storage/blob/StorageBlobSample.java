// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.aot.graalvm.samples.storage.blob;

import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import java.util.Random;

/**
 * A sample to demonstrate uploading a blob to Azure Blob Storage using GraalVM.
 */
public class StorageBlobSample {
    private static final String AZURE_STORAGE_CONNECTION_STRING = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
    private static final Random RANDOM = new Random();

    /**
     * The method to run the storage blob sample.
     */
    public static void runSample() {

        System.out.println("\n================================================================");
        System.out.println(" Starting Blob Storage Sample");
        System.out.println("================================================================");

        final BlobServiceClientBuilder blobServiceClientBuilder = new BlobServiceClientBuilder();
        if (AZURE_STORAGE_CONNECTION_STRING != null && !AZURE_STORAGE_CONNECTION_STRING.isEmpty()) {
            blobServiceClientBuilder.connectionString(AZURE_STORAGE_CONNECTION_STRING);
        } else {
            blobServiceClientBuilder.credential(new DefaultAzureCredentialBuilder().build());
        }

        final BlobServiceClient blobServiceClient = blobServiceClientBuilder.buildClient();

        final String containerName = "graal-uploads-" + RANDOM.nextInt(100);
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        if (!blobContainerClient.exists()) {
            blobContainerClient = blobServiceClient.createBlobContainer(containerName);
        }

        System.out.println("Beginning upload");
        final BlobClient blobClient = blobContainerClient.getBlobClient("graalvm-test.bin");

        byte[] bytes = new byte[1024 * 1024];
        RANDOM.nextBytes(bytes);
        blobClient.upload(BinaryData.fromBytes(bytes));

        System.out.println("Upload complete");

        blobContainerClient.delete();

        System.out.println("\n================================================================");
        System.out.println(" Blob Storage Sample Complete");
        System.out.println("================================================================");
    }
}
