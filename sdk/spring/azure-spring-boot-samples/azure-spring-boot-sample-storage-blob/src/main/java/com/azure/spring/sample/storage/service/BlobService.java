// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.storage.service;

import com.azure.storage.blob.BlobAsyncClient;

import java.io.File;

import static com.azure.spring.sample.storage.utils.LogUtils.logError;
import static com.azure.spring.sample.storage.utils.LogUtils.logInfo;

public class BlobService {

    private final BlobAsyncClient blobAsyncClient;
    private final String blobURL;

    public BlobService(BlobAsyncClient blobAsyncClient) {
        this.blobAsyncClient = blobAsyncClient;
        this.blobURL = blobAsyncClient.getBlobUrl();
    }

    public void uploadFile(File sourceFile) {
        blobAsyncClient.uploadFromFile(sourceFile.getPath()).subscribe(
            c -> logInfo("Start uploading file %s...", sourceFile),
            e -> logError("Failed to upload file %s with e %s.", sourceFile.toPath(), e.getMessage()),
            () -> logInfo("File %s is uploaded.", sourceFile.toPath())
        );
    }

    public void deleteBlob() {
        blobAsyncClient.delete().subscribe(
            c -> logInfo("Start deleting file %s...", blobURL),
            e -> logError("Failed to delete blob %s with e %s.", blobURL, e.getMessage()),
            () -> logInfo("Blob %s is deleted.", blobURL)
        );
    }

    public void downloadBlob(File downloadToFile) {
        blobAsyncClient.downloadToFile(downloadToFile.getPath(), true).subscribe(
            c -> logInfo("Start downloading file %s to %s...", blobURL, downloadToFile),
            e -> logError("Failed to download file from blob %s with error %s.", blobURL, e.getMessage()),
            () -> logInfo("File is downloaded to %s.", downloadToFile)
        );
    }
}
