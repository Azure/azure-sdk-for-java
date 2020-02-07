// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import java.io.IOException;
import java.io.OutputStream;

import com.azure.perf.test.core.RandomFlux;
import com.azure.perf.test.core.SizeOptions;
import com.azure.storage.blob.*;
import com.azure.storage.blob.perf.core.ContainerTest;

import reactor.core.publisher.Mono;

public class DownloadBlobTest extends ContainerTest<SizeOptions> {
    private final BlobClient blobClient;
    private final BlobAsyncClient blobAsyncClient;

    public DownloadBlobTest(SizeOptions options) {
        super(options);
        String blobName = "downloadTest";
        blobClient = blobContainerClient.getBlobClient(blobName);
        blobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(blobName);
    }

    // Required resource setup goes here, upload the file to be downloaded during tests.
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(blobAsyncClient.upload(RandomFlux.create(options.getSize()), null))
            .then();
    }

    // Perform the API call to be tested here
    @Override
    public void run() {
        blobClient.download(new NullOutputStream());
    }

    class NullOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {

        }
    }

    @Override
    public Mono<Void> runAsync() {
        return blobAsyncClient.download()
            .map(b -> {
                b.get(new byte[b.remaining()]);
                return 1;
            }).then();
    }
}
