// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.perf;

import com.azure.perf.test.core.NullOutputStream;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.TestDataCreationHelper;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.perf.core.DirectoryTest;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

public class DownloadFileShareTest extends DirectoryTest<PerfStressOptions> {
    private static final int BUFFER_SIZE = 16 * 1024 * 1024;
    private static final OutputStream DEV_NULL = new NullOutputStream();

    protected final ShareFileClient shareFileClient;
    protected final ShareFileAsyncClient shareFileAsyncClient;

    private final byte[] buffer = new byte[BUFFER_SIZE];

    public DownloadFileShareTest(PerfStressOptions options) {
        super(options);
        String fileName = "perfstressdfilev11" + UUID.randomUUID().toString();
        shareFileClient = shareDirectoryClient.getFileClient(fileName);
        shareFileAsyncClient = shareDirectoryAsyncClient.getFileClient(fileName);
    }

    // Required resource setup goes here, upload the file to be downloaded during tests.
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then(shareFileAsyncClient.create(options.getSize()))
            .then(shareFileAsyncClient.upload(createRandomByteBufferFlux(options.getSize()), new ParallelTransferOptions()))
            .then();
    }

    // Perform the API call to be tested here
    @Override
    public void run() {
        shareFileClient.download(DEV_NULL);
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
