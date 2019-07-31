// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.storage.common.credentials.SharedKeyCredential;

import java.util.Locale;

/**
 * This example shows how to list all containers with storage client using the Azure Storage Blob SDK for Java.
 */
public class ListContainersExample {

    /**
     * Entry point into the list containers examples for Storage blobs.
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String accountName = SampleHelper.getAccountName();
        String accountKey = SampleHelper.getAccountKey();

        /*
         * Use your Storage account's name and key to create a credential object; this is used to access your account.
         */
        SharedKeyCredential credential = new SharedKeyCredential(accountName, accountKey);

        /*
         * From the Azure portal, get your Storage account blob service URL endpoint.
         * The URL typically looks like this:
         */
        String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);

        /*
         * Create a BlobServiceClient object that wraps the service endpoint, credential and a request pipeline.
         */
        BlobServiceClient storageClient = new BlobServiceClientBuilder().endpoint(endpoint).credential(credential).buildClient();

        /*
         * Create 3 different containers from the storageClient.
         */
        for (int i = 0; i < 3; i++) {
            storageClient.createContainer("mycontainersforlisting" + i + System.currentTimeMillis());
        }

        /*
         * List the containers' name under the Azure storage account.
         */
        storageClient.listContainers().forEach(containerItem -> {
            System.out.println("Container name: " + containerItem.name());

            /*
             * Clean up the containers at the same time.
             */
            storageClient.getContainerClient(containerItem.name()).delete();
        });
    }
}
