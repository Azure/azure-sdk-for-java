// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage;

import android.util.Log;

import com.azure.identity.ClientSecretCredential;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * This example shows how to start using the Azure Storage Blob SDK for Java.
 */
public class BasicExample {

    /**
     * Entry point into the basic examples for Storage blobs.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException If an I/O error occurs
     * @throws RuntimeException If the downloaded data doesn't match the uploaded data
     */

    private static final String TAG = "STORAGE";
    public static void main(String accountName, ClientSecretCredential credential) throws IOException {


        /*
         * From the Azure portal, get your Storage account blob service URL endpoint.
         * The URL typically looks like this:
         */
        String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);

        /*
         * Create a BlobServiceClient object that wraps the service endpoint, credential and a request pipeline.
         */
        BlobServiceClient storageClient = new BlobServiceClientBuilder().endpoint(endpoint).credential(credential).buildClient();
        Log.i(TAG, "credentials created");
        /*
         * This example shows several common operations just to get you started.
         */

        /*
         * Create a client that references a to-be-created container in your Azure Storage account. This returns a
         * ContainerClient object that wraps the container's endpoint, credential and a request pipeline (inherited from storageClient).
         * Note that container names require lowercase.
         */
        BlobContainerClient blobContainerClient = storageClient.getBlobContainerClient("myjavacontainerbasic" + System.currentTimeMillis());

        /*
         * Create a container in Storage blob account.
         */
        blobContainerClient.create();
        Log.i(TAG, "blobContainerClient Created");
        /*
         * Create a client that references a to-be-created blob in your Azure Storage account's container.
         * This returns a BlockBlobClient object that wraps the blob's endpoint, credential and a request pipeline
         * (inherited from containerClient). Note that blob names can be mixed case.
         */
        BlockBlobClient blobClient = blobContainerClient.getBlobClient("HelloWorld.txt").getBlockBlobClient();

        String data = "Hello world!";
        InputStream dataStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));

        /*
         * Create the blob with string (plain text) content.
         */
        blobClient.upload(dataStream, data.length());

        dataStream.close();
        Log.i(TAG, "HelloWold.txt uploaded");
        /*
         * Download the blob's content to output stream.
         */
        int dataSize = (int) blobClient.getProperties().getBlobSize();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataSize);
        blobClient.downloadStream(outputStream);
        outputStream.close();

        /*
         * Verify that the blob data round-tripped correctly.
         */
        if (!data.equals(new String(outputStream.toByteArray(), StandardCharsets.UTF_8))) {
            throw new RuntimeException("The downloaded data does not match the uploaded data.");
        }

        /*
         * Create more blobs before listing.
         */
        for (int i = 0; i < 3; i++) {
            String sampleData = "Samples";
            InputStream dataInBlobs = new ByteArrayInputStream(sampleData.getBytes(Charset.defaultCharset()));
            blobContainerClient.getBlobClient("myblobsforlisting" + System.currentTimeMillis()).getBlockBlobClient()
                .upload(dataInBlobs, sampleData.length());
            dataInBlobs.close();
        }

        /*
         * List the blob(s) in our container.
         */

        /* This block is causing the app to hang. 
        blobContainerClient.listBlobs()
            .forEach(blobItem -> Log.i(TAG, "Blob name: " + blobItem.getName()));
        */

        /*
         * Delete the blob we created earlier.
         */
        blobClient.delete();
        Log.i(TAG, "blob deleted");

        /*
         * Delete the container we created earlier.
         */
        blobContainerClient.delete();
        Log.i(TAG, "blob container deleted");
    }
}
