// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import java.io.IOException;
import java.io.InputStream;

import com.azure.core.test.perf.RandomStream;
import com.azure.core.test.perf.SizeOptions;
import com.azure.storage.blob.perf.core.RandomBlobTest;
import com.azure.storage.blob.specialized.BlobOutputStream;

import reactor.core.publisher.Mono;

public class UploadOutputStreamTest extends RandomBlobTest<SizeOptions> {

    public UploadOutputStreamTest(SizeOptions options) {
        super(options);
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = RandomStream.create(options.getSize());
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
