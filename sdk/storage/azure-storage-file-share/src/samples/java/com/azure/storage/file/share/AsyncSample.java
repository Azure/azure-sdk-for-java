// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.share;

import com.azure.core.util.Configuration;
import com.azure.storage.file.share.options.ShareCreateOptions;

import java.util.UUID;

/**
 * Sample demonstrates how to create, list and delete a share using the async file service client.
 */
public class AsyncSample {
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("PRIMARY_STORAGE_FILE_ENDPOINT");
    private static final String SASTOKEN = Configuration.getGlobalConfiguration().get("SAS_TOKEN");

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
        ShareServiceAsyncClient fileServiceAsyncClient = new ShareServiceClientBuilder().endpoint(ENDPOINT)
                                                            .sasToken(SASTOKEN)
                                                            .buildAsyncClient();
        // Create a share
        String shareName = generateRandomName();
        fileServiceAsyncClient.createShareWithResponse(shareName, (ShareCreateOptions) null, null).subscribe(
            response -> System.out.printf("Successfully created a share with status code: %d.",
                response.getStatusCode()),
            err -> System.out.println("Failed to create a share. Reasons: " + err.getMessage()),
            () -> System.out.println("Completed creating a share!"));

        // List all shares and delete them.
        fileServiceAsyncClient.listShares().subscribe(
            shareItem -> {
                System.out.println("There is a share named: " + shareItem.getName());
                fileServiceAsyncClient.deleteShare(shareItem.getName()).subscribe(
                    response -> System.out.printf("Successfully delete the share: %s.", shareItem.getName()),
                    err -> System.out.printf("Failed to delete the share: %s. Reasons: %s.",
                        shareItem.getName(), err.getMessage()),
                    () -> System.out.println("Completed deleting the share."));
            },
            err -> System.out.println("Failed to list shares. Reasons: " + err.getMessage()),
            () -> System.out.println("Completed the listing and deleting the shares."));
    }
}
