// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobClient;

public abstract class BlobTestBase<TOptions extends PerfStressOptions> extends ContainerTest<TOptions> {
    protected final BlobClient blobClient;
    protected final BlockBlobClient blockBlobClient;
    protected final BlobAsyncClient blobAsyncClient;
    protected final BlockBlobAsyncClient blockBlobAsyncClient;

    public BlobTestBase(TOptions options) {
        super(options);

        String blobName = "randomblobtest-" + UUID.randomUUID().toString();

        blobClient = blobContainerClient.getBlobClient(blobName);
        blobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(blobName);

        blockBlobClient = blobClient.getBlockBlobClient();
        blockBlobAsyncClient = blobAsyncClient.getBlockBlobAsyncClient();
    }

    public long copyStream(InputStream input, OutputStream out) throws IOException {
        long transferred = 0;
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer, 0, 8192)) >= 0) {
            out.write(buffer, 0, read);
            transferred += read;
        }
        return transferred;
    }
}
