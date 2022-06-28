// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.core.util.HttpClientOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import java.io.IOException;
import java.time.Duration;
import java.util.Locale;

/**
 * This example shows how to start using the Azure Storage Blob SDK for Java.
 */
public class HttpClientBuilderWithTimeoutExample {

    /**
     * Entry point into the basic examples for Storage blobs.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException      If an I/O error occurs
     * @throws RuntimeException If the downloaded data doesn't match the uploaded data
     */
    public static void main(String[] args) throws IOException {

        /*
         * From the Azure portal, get your Storage account's name and account key.
         */
        String accountName = SampleHelper.getAccountName();
        String accountKey = SampleHelper.getAccountKey();

        /*
         * Use your Storage account's name and key to create a credential object; this is used to access your account.
         */
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);

        /*
         * From the Azure portal, get your Storage account blob service URL endpoint.
         * The URL typically looks like this:
         */
        String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);

        /*
         * Create clientOptions with application ID and timeouts set
         */
        HttpClientOptions clientOptions = new HttpClientOptions()
            .setApplicationId("client-options-id")
            .setResponseTimeout(Duration.ofSeconds(30L))
            .setReadTimeout(Duration.ofSeconds(60L))
            .setWriteTimeout(Duration.ofSeconds(60L))
            .setConnectTimeout(Duration.ofSeconds(5L));

        /*
         * Create a BlobServiceClient object that wraps the service endpoint, credential and a request pipeline.
         */
        BlobServiceClient storageClient = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .clientOptions(clientOptions)
            .buildClient();

        /*
         * Create a client that references a to-be-created container in your Azure Storage account.
         */
        BlobContainerClient blobContainerClient = storageClient.createBlobContainer("myjavacontainerbasic" + System.currentTimeMillis());

        /*
         * If the create blob container doesn't complete within the configured timeout period a TimeoutException will occur.
         */
        try {
            blobContainerClient.createIfNotExistsWithResponse(null, Duration.ofNanos(10L), Context.NONE);
        } catch (Exception ex) {
            System.out.println("Operation failed due to timeout: " + ex.getMessage());
        }
    }
}
