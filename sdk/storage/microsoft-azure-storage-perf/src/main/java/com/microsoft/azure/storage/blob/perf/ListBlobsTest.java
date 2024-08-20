// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob.perf;

import com.azure.perf.test.core.NullInputStream;
import com.azure.perf.test.core.PerfStressOptions;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.perf.core.ContainerTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

public class ListBlobsTest extends ContainerTest<PerfStressOptions> {
    public ListBlobsTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync().then(
            Flux.range(0, options.getCount())
                .flatMap(ignored -> Mono.fromRunnable(() -> upload("getblobstest-" + UUID.randomUUID())))
                .then());
    }

    @Override
    public void globalSetup() {
        super.globalSetup();
        for (int i = 0; i < options.getCount(); i++) {
            upload("getblobstest-" + UUID.randomUUID());
        }
    }

    private void upload(String blobName) {
        try {
            cloudBlobContainer.getBlockBlobReference(blobName).upload(new NullInputStream(), 0);
        } catch (IOException | StorageException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        cloudBlobContainer.listBlobs().forEach(b -> { });
    }

    @Override
    public Mono<Void> runAsync() {
        throw new UnsupportedOperationException();
    }
}
