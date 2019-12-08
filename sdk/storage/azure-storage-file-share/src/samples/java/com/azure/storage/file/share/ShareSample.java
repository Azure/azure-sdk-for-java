// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.share;

import com.azure.core.util.Configuration;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.models.ShareProperties;

import java.util.UUID;

/**
 * Sample demonstrates how to create, list and delete shares, and get and set properties.
 */
public class ShareSample {
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_STORAGE_FILE_ENDPOINT");

    // This is the helper method to generate random name.
    private static String generateRandomName() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * The main method shows how to do the base operation using share sync client.
     * @param args No args needed for the main method
     */
    public static void main(String[] args) {
        // Build a share client
        String shareName = generateRandomName();
        ShareClient shareClient = new ShareClientBuilder().endpoint(ENDPOINT).
                                        shareName(shareName).buildClient();

        // Create first snapshot on share.
        String shareSnapshot1 = null;
        try {
            shareSnapshot1 = shareClient.createSnapshot().getSnapshot();
        } catch (ShareStorageException e) {
            System.out.println("Failed to create snapshot on share. Reasons: " + e.getMessage());
        }

        // Create a share using the share client
        try {
            shareClient.create();
        } catch (ShareStorageException e) {
            System.out.printf("Failed to create the share %s with share client. Reasons: %s%n", shareName, e.getMessage());
        }
        // Create 3 directories using share client
        for (int i = 0; i < 3; i++) {
            try {
                shareClient.createDirectory(generateRandomName());
            } catch (ShareStorageException e) {
                System.out.println("Failed to create directory using the share client. Reasons: " + e.getMessage());
            }
        }

        // Create another snapshot on share.
        String shareSnapshot2 = null;
        try {
            shareSnapshot2 = shareClient.createSnapshot().getSnapshot();
        } catch (ShareStorageException e) {
            System.out.println("Failed to create snapshot on share. Reasons: " + e.getMessage());
        }

        // Create another directory after having the snapshot
        try {
            shareClient.createDirectory(generateRandomName());
        } catch (ShareStorageException e) {
            System.out.println("Failed to create directory using the share client. Reasons: " + e.getMessage());
        }



        // Get the properties of the share with first snapshot.
        ShareClient shareClientWithSnapshot1 = new ShareClientBuilder()
            .endpoint(ENDPOINT)
            .shareName(shareName)
            .snapshot(shareSnapshot1)
            .buildClient();

        try {
            ShareProperties shareProperties1 = shareClientWithSnapshot1.getProperties();
            System.out.println("This is the first snapshot eTag: " + shareProperties1.getETag());
        } catch (ShareStorageException e) {
            System.out.println("Failed to get properties for the first share snapshot. Reasons: " + e.getMessage());
        }

        // Get the properties of the share with second snapshot.
        ShareClient shareClientWithSnapshot2 = new ShareClientBuilder()
            .endpoint(ENDPOINT)
            .shareName(shareName)
            .snapshot(shareSnapshot2)
            .buildClient();

        try {
            ShareProperties shareProperties2 = shareClientWithSnapshot2.getProperties();
            System.out.println("This is the second snapshot eTag: " + shareProperties2.getETag());
        } catch (ShareStorageException e) {
            System.out.println("Failed to get properties for the second share snapshot. Reasons: " + e.getMessage());
        }

        // Get the root directory and list all directories.
        try {
            shareClient.getRootDirectoryClient().listFilesAndDirectories().forEach(resource ->
                System.out.printf("Name: %s, Directory? %b%n", resource.getName(), resource.isDirectory()));
        } catch (ShareStorageException e) {
            System.out.println("Failed to delete the share. Reasons: " + e.getMessage());
        }

        // Create a file with size of 1024 bytes under the share.
        try {
            ShareFileClient fileClient = shareClient.getFileClient("myFile");
            fileClient.create(1024);
        } catch (ShareStorageException e) {
            System.out.println("Failed to create a file under the share. Reasons: " + e.getMessage());
        }

        // Create a file with size of 1024 bytes under the share using createFile API.
        try {
            shareClient.createFile(generateRandomName(), 1024);
        } catch (ShareStorageException e) {
            System.out.println("Failed to create file using the share client. Reasons: " + e.getMessage());
        }

        // Delete the file "myFile" using shareClient.
        try {
            shareClient.deleteFile("myFile");
        } catch (ShareStorageException e) {
            System.out.println("Failed to delete the file named myFile. Reasons: " + e.getMessage());
        }

        // Delete the share snapshot 1
        try {
            shareClientWithSnapshot1.delete();
        } catch (ShareStorageException e) {
            System.out.println("Failed to delete the share snapshot 1. Reasons: " + e.getMessage());
        }

        // Check the delete share snapshot properties.
        try {
            shareClientWithSnapshot1.getProperties();
        } catch (ShareStorageException e) {
            System.out.println("This is expected. The snapshot has been delete. Reasons: " + e.getMessage());
        }

        // Check the one not deleted.
        try {
            shareClientWithSnapshot2.getProperties();
        } catch (ShareStorageException e) {
            System.out.println("Failed to get the properties of share snapshot 2. Reasons: " + e.getMessage());
        }

        // Delete the share.
        try {
            shareClient.delete();
        } catch (ShareStorageException e) {
            System.out.println("Failed to delete the share. Reasons: " + e.getMessage());
        }

    }
}
