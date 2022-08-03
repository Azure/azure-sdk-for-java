// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.NullOutputStream;
import com.azure.storage.blob.perf.core.AbstractDownloadTest;
import reactor.core.publisher.Mono;

import java.io.OutputStream;

public class DownloadBlobTest extends AbstractDownloadTest<BlobPerfStressOptions> {
    private static final int BUFFER_SIZE = 16 * 1024 * 1024;
    private static final OutputStream DEV_NULL = new NullOutputStream();

    private final byte[] buffer = new byte[BUFFER_SIZE];

    public DownloadBlobTest(BlobPerfStressOptions options) {
        super(options);
    }


    // Perform the API call to be tested here
    @Override
    public void run() {
        blobClient.download(DEV_NULL);
    }


    @Override
    public Mono<Void> runAsync() {
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
