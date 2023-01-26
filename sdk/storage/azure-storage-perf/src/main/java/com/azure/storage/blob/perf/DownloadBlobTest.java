// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.NullOutputStream;
import com.azure.storage.StoragePerfUtils;
import com.azure.storage.blob.perf.core.AbstractDownloadTest;
import reactor.core.publisher.Mono;

import java.io.OutputStream;

public class DownloadBlobTest extends AbstractDownloadTest<BlobPerfStressOptions> {
    private final OutputStream devNull = new NullOutputStream();

    private final int bufferSize;
    private final byte[] buffer;

    public DownloadBlobTest(BlobPerfStressOptions options) {
        super(options);

        this.bufferSize = StoragePerfUtils.getDynamicDownloadBufferSize(options.getSize());
        this.buffer = new byte[bufferSize];
    }


    // Perform the API call to be tested here
    @Override
    public void run() {
        blobClient.download(devNull);
    }


    @Override
    public Mono<Void> runAsync() {
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
