// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.storage.common.StorageSharedKeyCredential;

import java.util.Locale;

/**
 * This example shows how to list all file systems with storage client using the Azure Storage Data Lake SDK for Java.
 */
public class ListFileSystemsExample {

    /**
     * Entry point into the list file system examples for Storage Data Lake.
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String accountName = SampleHelper.getAccountName();
        String accountKey = SampleHelper.getAccountKey();

        /*
         * Use your Storage account's name and key to create a credential object; this is used to access your account.
         */
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);

        /*
         * From the Azure portal, get your Storage account datalake service URL endpoint.
         * The URL typically looks like this:
         */
        String endpoint = String.format(Locale.ROOT, "https://%s.dfs.core.windows.net", accountName);

        /*
         * Create a DataLakeServiceClient object that wraps the service endpoint, credential and a request pipeline.
         */
        DataLakeServiceClient storageClient = new DataLakeServiceClientBuilder().endpoint(endpoint).credential(credential).buildClient();

        /*
         * Create 3 different file systems from the storageClient.
         */
        for (int i = 0; i < 3; i++) {
            storageClient.createFileSystem("myfilesystemsforlisting" + i + System.currentTimeMillis());
        }

        /*
         * List the file system's name under the Azure storage account.
         */
        storageClient.listFileSystems().forEach(fileSystemItem -> {
            System.out.println("File System name: " + fileSystemItem.getName());

            /*
             * Clean up the file systems at the same time.
             */
            storageClient.getFileSystemClient(fileSystemItem.getName()).delete();
        });
    }
}
