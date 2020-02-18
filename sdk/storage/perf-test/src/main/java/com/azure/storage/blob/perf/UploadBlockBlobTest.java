// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.RandomFlux;
import com.azure.perf.test.core.RandomStream;
import com.azure.perf.test.core.SizeOptions;
import com.azure.storage.blob.perf.core.RandomBlobTest;
import reactor.core.publisher.Mono;

public class UploadBlockBlobTest extends RandomBlobTest<SizeOptions> {
    public UploadBlockBlobTest(SizeOptions options) {
        super(options);
    }

    @Override
    public void run() {
        blockBlobClient.upload(RandomStream.create(options.getSize()), options.getSize(), true);
    }

    @Override
    public Mono<Void> runAsync() {
        return blockBlobAsyncClient.upload(RandomFlux.create(options.getSize()), options.getSize(), true).then();
    }
}
