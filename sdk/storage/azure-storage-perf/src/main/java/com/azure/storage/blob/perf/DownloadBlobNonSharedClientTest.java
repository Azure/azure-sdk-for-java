// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.NullOutputStream;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.perf.core.BlobTestBase;
import com.azure.storage.blob.perf.core.ContainerTest;
import reactor.core.publisher.Mono;

import java.io.OutputStream;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

public class DownloadBlobNonSharedClientTest extends BlobTestBase<BlobPerfStressOptions> {
    private static final int BUFFER_SIZE = 16 * 1024 * 1024;
    private static final OutputStream DEV_NULL = new NullOutputStream();
    String blobName = "downloadTest";

    private final byte[] buffer = new byte[BUFFER_SIZE];

    public DownloadBlobNonSharedClientTest(BlobPerfStressOptions options) {
        super(options);
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
        BlobClient blobClient = new BlobClientBuilder()
                                    .containerName(CONTAINER_NAME)
                                    .connectionString(connectionString)
                                    .blobName(blobName)
                                    .buildClient();
        blobClient.download(DEV_NULL);
    }

    @Override
    public Mono<Void> runAsync() {
        BlobAsyncClient blobAsyncClient = new BlobClientBuilder()
                                              .containerName(CONTAINER_NAME)
                                              .connectionString(connectionString)
                                              .blobName(blobName)
                                              .buildAsyncClient();
        return blobAsyncClient.download()
                   .map(b -> {
                       int readCount = 0;
                       int remaining = b.remaining();
                       while (readCount < remaining) {
                           int expectedReadCount = Math.min(remaining - readCount, BUFFER_SIZE);
                           b.get(buffer, 0, expectedReadCount);
                           readCount += expectedReadCount;
                       }
                       return 1;
                   }).then();
    }
}
