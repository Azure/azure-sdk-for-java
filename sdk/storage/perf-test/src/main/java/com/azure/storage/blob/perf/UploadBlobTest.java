// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.RandomFlux;
import com.azure.perf.test.core.SizeOptions;
import com.azure.storage.blob.perf.core.BlobTestBase;
import reactor.core.publisher.Mono;

public class UploadBlobTest extends BlobTestBase<SizeOptions> {

    public UploadBlobTest(SizeOptions options) {
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
