// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.core.test.perf.RandomFlux;
import com.azure.core.test.perf.SizeOptions;
import com.azure.storage.blob.perf.core.RandomBlobTest;
import reactor.core.publisher.Mono;

public class UploadTest extends RandomBlobTest<SizeOptions> {

    public UploadTest(SizeOptions options) {
        super(options);
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Void> runAsync() {
        return blobAsyncClient.upload(RandomFlux.create(options.getSize()), null, true).then();
    }
}
