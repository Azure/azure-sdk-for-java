// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.perf;

import com.azure.perf.test.core.NullOutputStream;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.StoragePerfUtils;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.perf.core.DirectoryTest;
import reactor.core.publisher.Mono;

import java.io.OutputStream;
import java.util.UUID;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

public class DownloadFileShareTest extends DirectoryTest<PerfStressOptions> {

    protected final ShareFileClient shareFileClient;
    protected final ShareFileAsyncClient shareFileAsyncClient;

    private final OutputStream devNull = new NullOutputStream();

    private final int bufferSize;
    private final byte[] buffer;

    public DownloadFileShareTest(PerfStressOptions options) {
        super(options);
        String fileName = "perfstressdfilev11" + UUID.randomUUID();
        shareFileClient = shareDirectoryClient.getFileClient(fileName);
        shareFileAsyncClient = shareDirectoryAsyncClient.getFileClient(fileName);

        this.bufferSize = StoragePerfUtils.getDynamicDownloadBufferSize(options.getSize());
        this.buffer = new byte[bufferSize];
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
        shareFileClient.download(devNull);
    }

    @Override
    public Mono<Void> runAsync() {
        return shareFileAsyncClient.download()
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
