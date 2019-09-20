// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.util.Configuration;
import com.azure.storage.file.models.FileServiceProperties;
import com.azure.storage.file.models.StorageException;

import java.util.UUID;

/**
 * Sample demonstrates how to create, list and delete shares, and get and set properties.
 */
public class FileServiceSample {
    private static final String CONNECTION_STRING = Configuration.getGlobalConfiguration().get("AZURE_STORAGE_CONNECTION_STRING");

    // This is the helper method to generate random name.
    private static String generateRandomName() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * The main method shows how to do the base operation using file service sync client.
     * @param args No args needed for the main method
     */
    public static void main(String[] args) {
        // Create a file service client.
        FileServiceClient fileServiceClient = new FileServiceClientBuilder()
                .connectionString(CONNECTION_STRING).buildClient();

        // Create 3 shares
        for (int i = 0; i < 3; i++) {
            try {
                fileServiceClient.createShare(generateRandomName());
            } catch (StorageException e) {
                System.out.printf("Failed to create share %d. Reasons: %s", i, e.getMessage());
            }
        }

        // Get properties from the file service
        try {
            FileServiceProperties properties = fileServiceClient.getProperties();

            System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b%n",
                properties.getHourMetrics(), properties.getMinuteMetrics());
        } catch (StorageException e) {
            System.out.println("Failed to get the account properties. Reasons: " + e.getMessage());
        }
        // List all shares under file service and delete them.
        fileServiceClient.listShares().forEach(
            shareItem -> {
                try {
                    System.out.printf("This is the share name: %s in the file account.%n", shareItem.getName());
                    fileServiceClient.deleteShare(shareItem.getName());
                    System.out.println("The share has been deleted from the storage file account!");
                } catch (StorageException e) {
                    System.out.println("Failed to delete the share. Reasons: " + e.getMessage());
                }
            }
        );

        // Delete all shares under file service

    }
}
