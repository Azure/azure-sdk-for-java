// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.file;

import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.storage.file.FileServiceAsyncClient;
import com.azure.storage.file.FileServiceClientBuilder;
import java.util.UUID;

/**
 * Sample demonstrates how to create, list and delete a share using the async file service client.
 */
public class AsyncSample {
    private static final String ENDPOINT = ConfigurationManager.getConfiguration().get("AZURE_STORAGE_FILE_ENDPOINT");

    // This is the helper method to generate random name.
    private static String generateRandomName() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * The main method show how to make async cal for creating and listing shares.
     * @param args No args needed for the main method
     */
    public static void main(String[] args) {
        // Create a file service client
        FileServiceAsyncClient fileServiceAsyncClient = new FileServiceClientBuilder().endpoint(ENDPOINT)
                                                            .buildAsyncClient();
        // Create a share
        String shareName = generateRandomName();
        fileServiceAsyncClient.createShare(shareName).subscribe(
            response -> {
                System.out.printf("Successfully created a share with status code: %d.", response.statusCode());
            },
            err -> {
                System.out.println("Failed to create a share. Reasons: " + err.getMessage());
            },
            () -> {
                System.out.println("Completed creating a share!");
            }
        );

        // List all shares and delete them.
        fileServiceAsyncClient.listShares().subscribe(
            shareItem -> {
                System.out.println("There is a share named: " + shareItem.name());
                fileServiceAsyncClient.deleteShare(shareItem.name()).subscribe(
                    response -> {
                        System.out.printf("Successfully delete the share: %s.", shareItem.name());
                    },
                    err -> {
                        System.out.printf("Failed to delete the share: %s. Reasons: %s.", shareItem.name(), err.getMessage());
                    },
                    () -> {
                        System.out.println("Completed deleting the share.");
                    }
                );
            },
            err -> {
                System.out.println("Failed to list shares. Reasons: " + err.getMessage());
            },
            () -> {
                System.out.println("Completed the listing and deleting the shares.");
            }
        );
    }
}
