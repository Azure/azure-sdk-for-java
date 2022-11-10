// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf.core;

import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.perf.BlobPerfStressOptions;
import reactor.core.publisher.Mono;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

public abstract class AbstractDownloadTest <TOptions extends BlobPerfStressOptions> extends BlobTestBase<TOptions> {
    private static final long GB = 1024 * 1024 * 1024;

    public AbstractDownloadTest(TOptions options) {
        super(options, BLOB_NAME_PREFIX);
    }

    // Upload one blob for the whole test run. All tests can download the same blob
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(blobAsyncClient.upload(createRandomByteBufferFlux(options.getSize()), new ParallelTransferOptions()
                .setMaxSingleUploadSizeLong(GB).setBlockSizeLong(GB)))
            .then();
    }
}
