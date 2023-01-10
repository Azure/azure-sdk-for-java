// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.NullOutputStream;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.perf.core.AbstractDownloadTest;
import reactor.core.publisher.Mono;

import java.io.OutputStream;

public class DownloadBlobNonSharedClientTest extends AbstractDownloadTest<BlobPerfStressOptions> {
    private static final int DEFAULT_BUFFER_SIZE = 16 * 1024 * 1024;
    String blobName = "downloadTest";

    private final OutputStream devNull = new NullOutputStream();

    private final int bufferSize;
    private final byte[] buffer;

    public DownloadBlobNonSharedClientTest(BlobPerfStressOptions options) {
        super(options);
        // Dynamically determine the buffer size to be the minimum of 2 * the download size or 16MB.
        // This reduces heap allocations when running smaller tests.
        this.bufferSize = (int) Math.min(2 * options.getSize(), DEFAULT_BUFFER_SIZE);
        this.buffer = new byte[bufferSize];
    }

    // Perform the API call to be tested here
    @Override
    public void run() {
        BlobClient blobClient = new BlobClientBuilder()
            .containerName(CONTAINER_NAME)
            .connectionString(connectionString)
            .blobName(blobName)
            .buildClient();

        blobClient.download(devNull);
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
                    int expectedReadCount = Math.min(remaining - readCount, bufferSize);
                    b.get(buffer, 0, expectedReadCount);
                    readCount += expectedReadCount;
                }
                return 1;
            }).then();
    }
}
