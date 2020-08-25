// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.blob.perf.core.BlobTestBase;
import com.azure.storage.blob.specialized.BlobOutputStream;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static com.azure.perf.test.core.TestDataCreationHelper.writeBytesToOutputStream;

public class UploadOutputStreamTest extends BlobTestBase<PerfStressOptions> {
    public UploadOutputStreamTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        try {
            BlobOutputStream blobOutputStream = blockBlobClient.getBlobOutputStream();
            writeBytesToOutputStream(blobOutputStream, options.getSize());
            blobOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> runAsync() {
        throw new UnsupportedOperationException();
    }
}
