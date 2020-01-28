// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import java.io.IOException;
import java.io.OutputStream;

import com.azure.core.test.perf.RandomFlux;
import com.azure.core.test.perf.SizeOptions;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.perf.core.ContainerTest;

import reactor.core.publisher.Mono;

public class DownloadTest extends ContainerTest<SizeOptions> {
    private final BlobClient blobClient;
    private final BlobAsyncClient blobAsyncClient;

    public DownloadTest(SizeOptions options) {
        super(options);

        String blobName = "downloadtest";
        blobClient = BlobContainerClient.getBlobClient(blobName);
        blobAsyncClient = BlobContainerAsyncClient.getBlobAsyncClient(blobName);
    }

    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(blobAsyncClient.upload(RandomFlux.create(options.getSize()), null))
            .then();
    }

    @Override
    public void run() {
        blobClient.download(new NullOutputStream());
    }


    /**Writes to nowhere*/
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
            })
            .then();
            }
}
