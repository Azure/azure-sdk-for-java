// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.share;

import com.azure.core.util.Configuration;
import com.azure.storage.file.share.models.ShareDirectoryProperties;
import com.azure.storage.file.share.models.ShareStorageException;

import java.util.UUID;

/**
 * Sample demonstrates how to create, list and delete directory and its subdirectories and files
 *  and how to get and set properties.
 */
public class DirectorySample {
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("PRIMARY_STORAGE_FILE_ENDPOINT");
    private static final String SASTOKEN = Configuration.getGlobalConfiguration().get("SAS_TOKEN");

    // This is the helper method to generate random name.
    private static String generateRandomName() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * The main method shows how to do the basic operation using directory sync client.
     * @param args No args needed for the main method.
     */
    public static void main(String[] args) {
        String shareName = generateRandomName();
        ShareClient shareClient = new ShareClientBuilder().endpoint(ENDPOINT).sasToken(SASTOKEN).shareName(shareName).buildClient();
        shareClient.create();
        // Build up a directory client
        ShareDirectoryClient directoryClient = new ShareFileClientBuilder().endpoint(ENDPOINT).shareName(generateRandomName())
                                            .sasToken(SASTOKEN)
                                            .shareName(shareName)
                                            .resourcePath(generateRandomName()).buildDirectoryClient();
        // Create a parent directory
        try {
            directoryClient.create();
        } catch (ShareStorageException e) {
            System.out.println("Failed to create a directory. Reasons: " + e.getMessage());
        }

        // Create a child directory.
        String childDirectoryName = generateRandomName();
        try {
            directoryClient.createSubdirectory(childDirectoryName);
        } catch (ShareStorageException e) {
            System.out.println("Failed to create sub directory. Reasons: " + e.getMessage());
        }

        // Create a 1KB file under the child directory.
        ShareDirectoryClient childDirClient = directoryClient.getSubdirectoryClient(childDirectoryName);
        String fileName = generateRandomName();
        try {
            childDirClient.createFile(fileName, 1024);
        } catch (ShareStorageException e) {
            System.out.println("Failed to create a file under the child directory. Reasons: " + e.getMessage());
        }

        // Delete the child directory. The operation will fail because storage service only allowed to delete the empty directory.
        try {
            childDirClient.delete();
        } catch (ShareStorageException e) {
            System.out.println("This is expected as the child directory is not empty.");
        }

        // List all the sub directories and files.
        try {
            directoryClient.listFilesAndDirectories().forEach(
                fileRef -> System.out.printf("Is the resource a directory? %b. The resource name is: %s%n",
                    fileRef.isDirectory(), fileRef.getName()));
        } catch (ShareStorageException e) {
            System.out.println("Failed to list all the subdirectories and files. Reasons: " + e.getMessage());
        }

        // Get the parent directory properties.
        try {
            ShareDirectoryProperties propertiesResponse = directoryClient.getProperties();
            System.out.printf("This is the eTag of the directory: %s%n", propertiesResponse.getETag());
        } catch (ShareStorageException e) {
            System.out.println("Failed to get the properties of the parent directory");
        }

        // Delete the file.
        try {
            childDirClient.deleteFile(fileName);
        } catch (ShareStorageException e) {
            System.out.println("Failed to delete the file. Reasons: " + e.getMessage());
        }

        // Delete the child folder
        try {
            directoryClient.deleteSubdirectory(childDirectoryName);
        } catch (ShareStorageException e) {
            System.out.println("Failed to delete the child directory. Reasons: " + e.getMessage());
        }

        // Delete the parent folder
        try {
            directoryClient.delete();
        } catch (ShareStorageException e) {
            System.out.println("Failed to delete the parent directory. Reasons: " + e.getMessage());
        }

        shareClient.delete();
    }

}
