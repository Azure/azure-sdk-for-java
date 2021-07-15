// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob.perf;

import java.io.IOException;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.RepeatingInputStream;
import com.azure.perf.test.core.TestDataCreationHelper;
import com.microsoft.azure.storage.StorageException;

import com.microsoft.azure.storage.blob.perf.core.BlobTestBase;
import reactor.core.publisher.Mono;

public class UploadBlobTest extends BlobTestBase<PerfStressOptions> {
    protected final RepeatingInputStream inputStream;

    public UploadBlobTest(PerfStressOptions options) {
        super(options);
        inputStream = (RepeatingInputStream) TestDataCreationHelper.createRandomInputStream(options.getSize());
    }

    @Override
    public void run() {
        try {
            inputStream.reset();
            cloudBlockBlob.upload(TestDataCreationHelper.createRandomInputStream(options.getSize()), options.getSize());
        } catch (StorageException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> runAsync() {
        throw new UnsupportedOperationException();
    }
}
