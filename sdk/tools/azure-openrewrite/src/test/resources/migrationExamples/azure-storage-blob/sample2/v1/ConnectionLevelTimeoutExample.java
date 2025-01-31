// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.HttpClientOptions;
import com.azure.storage.common.StorageSharedKeyCredential;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

/**
 * This example shows how to use connection level timeouts. These timeouts relate to discrete sections of the
 * connection/request/response process such as time to receive headers or time between reading bytes. These options
 * offer the most granular control. If one of these values times out, it will be automatically retried.
 * Please see {@link HttpClientOptions} for more detailed information on each option.
 */
public class ConnectionLevelTimeoutExample {

    /**
     * Entry point into the basic examples for Storage blobs.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException If an I/O error occurs
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
            .setResponseTimeout(Duration.ofNanos(1))
            .setReadTimeout(Duration.ofNanos(1))
            .setWriteTimeout(Duration.ofNanos(1))
            .setConnectTimeout(Duration.ofNanos(1));

        /*
         * Create a BlobServiceClient object that wraps the service endpoint, credential and a request pipeline.
         */
        BlobServiceClient storageClient = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .clientOptions(clientOptions)
            .buildClient();

        /*
         * Creating a blob container will cause a timeout exception since default duration is passed in when creating
         * blob container.
         */
        try {
            storageClient.createBlobContainer("myjavacontainerbasic" + System.currentTimeMillis());
        } catch (Exception ex) {
            if (ex.getCause() instanceof TimeoutException) {
                System.out.println("Operation failed due to timeout: " + ex.getMessage());
            }
        }
    }
}
