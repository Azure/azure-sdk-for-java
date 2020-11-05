// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.storage.service;

import com.azure.storage.blob.BlobContainerAsyncClient;

import static com.azure.spring.sample.storage.utils.LogUtils.logError;
import static com.azure.spring.sample.storage.utils.LogUtils.logInfo;

public class BlobContainerService {

    private final BlobContainerAsyncClient blobContainerAsyncClient;
    private final String containerName;

    public BlobContainerService(BlobContainerAsyncClient blobContainerAsyncClient) {
        this.blobContainerAsyncClient = blobContainerAsyncClient;
        this.containerName = blobContainerAsyncClient.getBlobContainerName();
    }

    public void createContainerIfNotExists() {
        final Boolean containerExists = blobContainerAsyncClient.exists().block();

        if (containerExists == null || !containerExists) {
            blobContainerAsyncClient.create().subscribe(
                v -> logInfo("Creating container %s.", containerName),
                e -> logError("Error occurred when creating container %s.", containerName),
                () -> logInfo("Completed creating container %s.", containerName)
            );
        }
    }

    public void deleteContainer() {
        blobContainerAsyncClient.delete().subscribe(
            v -> logInfo("Deleting container %s.", containerName),
            e -> logError("Error occurred when deleting container %s.", containerName),
            () -> logInfo("Completed deleting container %s.", containerName)
        );
    }

    public void listBlobsInContainer() {
        blobContainerAsyncClient.listBlobs().subscribe(
            blob -> logInfo("Name: %s, Directory? %b", blob.getName(), blob.isPrefix()));
    }
}
