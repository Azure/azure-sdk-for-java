// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.perf.core.ContainerTest;
import java.io.IOException;
import java.io.OutputStream;
import reactor.core.publisher.Mono;

public class DownloadBlobTest extends ContainerTest<PerfStressOptions> {
    private final BlobClient blobClient;
    private final BlobAsyncClient blobAsyncClient;

    public DownloadBlobTest(PerfStressOptions options) {
        super(options);
        String blobName = "downloadTest";
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
        blobClient.download(new NullOutputStream());
    }

    static class NullOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {

        }
    }

    @Override
    public Mono<Void> runAsync() {
        return blobAsyncClient.download()
            .map(b -> {
                for (int i = 0; i < b.remaining(); i++) {
                    b.get();
                }
                return 1;
            }).then();
    }
}
