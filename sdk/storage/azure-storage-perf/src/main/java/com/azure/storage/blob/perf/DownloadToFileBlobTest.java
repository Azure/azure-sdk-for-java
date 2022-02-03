// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.perf.core.ContainerTest;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.UUID;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

public class DownloadToFileBlobTest extends ContainerTest<PerfStressOptions> {
    private final BlobClient blobClient;
    private final BlobAsyncClient blobAsyncClient;

    public DownloadToFileBlobTest(PerfStressOptions options) {
        super(options);
        String blobName = "downloadToFileTest";
        blobClient = blobContainerClient.getBlobClient(blobName);
        blobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(blobName);
    }

    // Required resource setup goes here, upload the file to be downloaded during tests.
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(blobAsyncClient.upload(createRandomByteBufferFlux(options.getSize()), null))
            .then();
    }

    // Perform the API call to be tested here
    @Override
    public void run() {
        File file = new File(UUID.randomUUID().toString());
        file.deleteOnExit();
        blobClient.downloadToFile(file.getAbsolutePath());
    }

    @Override
    public Mono<Void> runAsync() {
        File file = new File(UUID.randomUUID().toString());
        file.deleteOnExit();
        return blobAsyncClient.downloadToFile(file.getAbsolutePath()).then();
    }
}
