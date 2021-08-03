// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.perf.core.DirectoryTest;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.UUID;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

public class DownloadToFileShareTest extends DirectoryTest<PerfStressOptions> {

    protected final ShareFileClient shareFileClient;
    protected final ShareFileAsyncClient shareFileAsyncClient;


    public DownloadToFileShareTest(PerfStressOptions options) {
        super(options);
        String fileName = "perfstressdfile" + UUID.randomUUID().toString();
        shareFileClient = shareDirectoryClient.getFileClient(fileName);
        shareFileAsyncClient = shareDirectoryAsyncClient.getFileClient(fileName);
    }

    // Required resource setup goes here, upload the file to be downloaded during tests.
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then(shareFileAsyncClient.create(options.getSize()))
            .then(shareFileAsyncClient.upload(createRandomByteBufferFlux(options.getSize()), options.getSize()))
            .then();
    }

    // Perform the API call to be tested here
    @Override
    public void run() {
        File file = new File(UUID.randomUUID().toString());
        file.deleteOnExit();
        shareFileClient.downloadToFile(file.getAbsolutePath());
    }

    @Override
    public Mono<Void> runAsync() {
        File file = new File(UUID.randomUUID().toString());
        file.deleteOnExit();
        return shareFileAsyncClient.downloadToFile(file.getAbsolutePath()).then();
    }
}
