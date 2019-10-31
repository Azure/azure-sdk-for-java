// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.datalake.models.PathAccessControl;

import java.io.IOException;
import java.util.Locale;

/**
 * This example shows how to get and set access control of a file using the Azure Storage Data Lake SDK for Java.
 * Note: the same code can be used on a directory as well.
 */
public class GetSetAccessControlExample {

    /**
     * Entry point into the upload download examples for Storage datalake.
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
         * From the Azure portal, get your Storage account dfs service URL endpoint.
         * The URL typically looks like this:
         */
        String endpoint = String.format(Locale.ROOT, "http://%s.dfs.core.windows.net", accountName);

        /*
         * Create a DataLakeServiceClient object that wraps the service endpoint, credential and a request pipeline.
         */
        DataLakeServiceClient storageClient = new DataLakeServiceClientBuilder().endpoint(endpoint).credential(credential).buildClient();

        /*
         * This example shows several common operations just to get you started.
         */

        /*
         * Create a client that references a to-be-created file system in your Azure Storage account. This returns a
         * FileSystem object that wraps the file system's endpoint, credential and a request pipeline (inherited from storageClient).
         * Note that file system names require lowercase.
         */
        FileSystemClient fileSystemClient = storageClient.getFileSystemClient("myjavafilesystembasic" + System.currentTimeMillis());

        /*
         * Create a file system in Storage datalake account.
         */
        fileSystemClient.create();

        /*
         * Create a client that references a to-be-created file in your Azure Storage account's file system.
         * This returns a DataLakeFileClient object that wraps the file's endpoint, credential and a request pipeline
         * (inherited from fileSystemClient). Note that file names can be mixed case.
         */
        DataLakeFileClient fileClient = fileSystemClient.getFileClient("HelloWorld.txt");

        /*
         * Create the file.
         */
        fileClient.create();

        /*
         * Set access controls on the file
         */
        PathAccessControl pathAccessControl = new PathAccessControl()
            .setPermissions("0777");
        fileClient.setAccessControl(pathAccessControl);

        /*
         * Get access controls on the file
         */

        PathAccessControl returnedAccessControl = fileClient.getAccessControl();

        /*
         * Compare set and returned access control
         */
        if (pathAccessControl.getPermissions().equals(returnedAccessControl.getPermissions())) {
            throw new RuntimeException("The returned permissions do not match the uploaded permissions.");
        }

        /*
         * Delete the file we created earlier.
         */
        fileClient.delete();

        /*
         * Delete the file system we created earlier.
         */
        fileSystemClient.delete();
    }
}
