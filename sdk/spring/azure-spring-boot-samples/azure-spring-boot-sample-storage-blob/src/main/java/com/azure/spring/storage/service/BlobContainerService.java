// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.storage.service;

import com.azure.spring.storage.utils.LogUtils;
import com.azure.storage.blob.BlobContainerAsyncClient;

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
                v -> LogUtils.logInfo("Creating container %s.", containerName),
                e -> LogUtils.logError("Error occurred when creating container %s.", containerName),
                () -> LogUtils.logInfo("Completed creating container %s.", containerName)
            );
        }
    }

    public void deleteContainer() {
        blobContainerAsyncClient.delete().subscribe(
            v -> LogUtils.logInfo("Deleting container %s.", containerName),
            e -> LogUtils.logError("Error occurred when deleting container %s.", containerName),
            () -> LogUtils.logInfo("Completed deleting container %s.", containerName)
        );
    }

    public void listBlobsInContainer() {
        blobContainerAsyncClient.listBlobs().subscribe(
            blob -> LogUtils.logInfo("Name: %s, Directory? %b", blob.getName(), blob.isPrefix()));
    }
}
