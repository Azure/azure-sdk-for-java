// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob.perf;

import java.io.IOException;
import java.io.InputStream;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.TestDataCreationHelper;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobOutputStream;

import com.microsoft.azure.storage.blob.perf.core.BlobTestBase;
import reactor.core.publisher.Mono;

public class UploadOutputStreamTest extends BlobTestBase<PerfStressOptions> {

    public UploadOutputStreamTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = TestDataCreationHelper.createRandomInputStream(options.getSize());
            BlobOutputStream outputStream = cloudBlockBlob.openOutputStream();
            TestDataCreationHelper.copyStream(inputStream, outputStream, DEFAULT_BUFFER_SIZE);
            outputStream.close();
        } catch (StorageException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> runAsync() {
        throw new UnsupportedOperationException();
    }
}

