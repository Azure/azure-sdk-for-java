// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomInputStream;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.blob.perf.core.BlobTestBase;
import com.azure.storage.blob.specialized.BlobOutputStream;
import java.io.IOException;
import java.io.InputStream;
import reactor.core.publisher.Mono;

public class UploadOutputStreamTest extends BlobTestBase<PerfStressOptions> {

    private final InputStream inputStream;

    public UploadOutputStreamTest(PerfStressOptions options) {
        super(options);
        this.inputStream = createRandomInputStream(options.getSize());
    }

    @Override
    public void run() {
        try {
            BlobOutputStream blobOutputStream = blockBlobClient.getBlobOutputStream();
            copyStream(inputStream, blobOutputStream);
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
