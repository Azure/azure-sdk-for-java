// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob.perf;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;

import com.azure.perf.test.core.NullOutputStream;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.TestDataCreationHelper;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import com.microsoft.azure.storage.blob.perf.core.ContainerTest;
import reactor.core.publisher.Mono;

public class DownloadBlobTest extends ContainerTest<PerfStressOptions> {
    private final CloudBlockBlob cloudBlockBlob;
    private static final OutputStream DEV_NULL = new NullOutputStream();

    public DownloadBlobTest(PerfStressOptions options) {
        super(options);

        try {
            cloudBlockBlob = cloudBlobContainer.getBlockBlobReference("downloadtest");
        } catch (URISyntaxException | StorageException e) {
            throw new RuntimeException(e);
        }
    }

    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync().then(Mono.fromCallable(() -> {
            try {
                cloudBlockBlob.upload(TestDataCreationHelper
                    .createRandomInputStream(options.getSize()), options.getSize());
            } catch (StorageException | IOException e) {
                throw new RuntimeException(e);
            }
            return 1;
        })).then();
    }

    @Override
    public void run() {
        try {
            cloudBlockBlob.download(DEV_NULL);
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> runAsync() {
        throw new UnsupportedOperationException();
    }
}
