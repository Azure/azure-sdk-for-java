// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.file.share.perf.core.FileTestBase;
import reactor.core.publisher.Mono;

import java.io.OutputStream;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

public class DownloadFileShareTest extends FileTestBase<PerfStressOptions> {
    private static final int BUFFER_SIZE = 16 * 1024 * 1024;
    private static final OutputStream DEV_NULL = new NullOutputStream();

    private final byte[] buffer = new byte[BUFFER_SIZE];

    public DownloadFileShareTest(PerfStressOptions options) {
        super(options);
    }

    // Required resource setup goes here, upload the file to be downloaded during tests.
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(shareFileAsyncClient.upload(createRandomByteBufferFlux(options.getSize()), options.getSize()))
            .then();
    }

    // Perform the API call to be tested here
    @Override
    public void run() {
        shareFileClient.download(DEV_NULL);
    }

    static class NullOutputStream extends OutputStream {
        @Override
        public void write(int b) {

        }

        @Override
        public void write(byte[] b) {
        }

        @Override
        public void write(byte[] b, int off, int len) {
        }
    }

    @Override
    public Mono<Void> runAsync() {
        return shareFileAsyncClient.download()
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
