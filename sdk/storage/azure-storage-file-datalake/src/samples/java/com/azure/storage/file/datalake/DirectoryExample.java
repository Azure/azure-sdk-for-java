// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.util.Configuration;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.PathProperties;

import java.util.UUID;

/**
 * Sample demonstrates how to create, list and delete directory and its subdirectories and files
 *  and how to get and set properties.
 */
public class DirectoryExample {
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_STORAGE_ENDPOINT");

    // This is the helper method to generate random name.
    private static String generateRandomName() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * The main method shows how to do the basic operation using directory sync client.
     * @param args No args needed for the main method.
     */
    public static void main(String[] args) {
        String fileSystemName = generateRandomName();
        DataLakeFileSystemClient dataLakeFileSystemClient = new DataLakeFileSystemClientBuilder().endpoint(ENDPOINT).fileSystemName(fileSystemName).sasToken(sasToken).buildClient();
        dataLakeFileSystemClient.create();
        // Build up a directory client
        DataLakeDirectoryClient directoryClient = new DataLakePathClientBuilder().endpoint(ENDPOINT)
            .pathName(generateRandomName())
            .fileSystemName(fileSystemName)
            .sasToken("<SAS_TOKEN>")
            .buildDirectoryClient();

        // Create a parent directory
        try {
            directoryClient.create();
        } catch (DataLakeStorageException e) {
            System.out.println("Failed to create a directory. Reasons: " + e.getMessage());
        }

        // Create a child directory.
        String childDirectoryName = generateRandomName();
        try {
            directoryClient.createSubdirectory(childDirectoryName);
        } catch (DataLakeStorageException e) {
            System.out.println("Failed to create sub directory. Reasons: " + e.getMessage());
        }

        // Create a 1KB file under the child directory.
        DataLakeDirectoryClient childDirClient = directoryClient.getSubdirectoryClient(childDirectoryName);
        String fileName = generateRandomName();
        try {
            childDirClient.createFile(fileName);
        } catch (DataLakeStorageException e) {
            System.out.println("Failed to create a file under the child directory. Reasons: " + e.getMessage());
        }

        // Delete the child directory. The operation will fail because storage service only allowed to delete the empty directory.
        try {
            childDirClient.delete();
        } catch (DataLakeStorageException e) {
            System.out.println("This is expected as the child directory is not empty.");
        }

        // Get the parent directory properties.
        try {
            PathProperties propertiesResponse = directoryClient.getProperties();
            System.out.printf("This is the eTag of the directory: %s%n", propertiesResponse.getETag());
        } catch (DataLakeStorageException e) {
            System.out.println("Failed to get the properties of the parent directory");
        }

        // Delete the file.
        try {
            childDirClient.deleteFile(fileName);
        } catch (DataLakeStorageException e) {
            System.out.println("Failed to delete the file. Reasons: " + e.getMessage());
        }

        // Delete the child folder
        try {
            directoryClient.deleteSubdirectory(childDirectoryName);
        } catch (DataLakeStorageException e) {
            System.out.println("Failed to delete the child directory. Reasons: " + e.getMessage());
        }

        // Delete the parent folder
        try {
            directoryClient.delete();
        } catch (DataLakeStorageException e) {
            System.out.println("Failed to delete the parent directory. Reasons: " + e.getMessage());
        }

        dataLakeFileSystemClient.delete();
    }

}
