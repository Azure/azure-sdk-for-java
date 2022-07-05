// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf.core;

import com.azure.storage.blob.perf.BlobPerfStressOptions;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

public abstract class AbstractUploadTest<TOptions extends BlobPerfStressOptions> extends BlobTestBase<TOptions> {

    public AbstractUploadTest(TOptions options) {
        super(options, BLOB_NAME_PREFIX + UUID.randomUUID());
    }

    @Override
    public Mono<Void> setupAsync() {
        // Upload one blob per test
        return super.setupAsync()
            .then(blobAsyncClient.upload(createRandomByteBufferFlux(options.getSize()), null))
            .then();
    }
}
