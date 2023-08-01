// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob.perf.core;

import java.net.URISyntaxException;
import java.util.UUID;

import com.azure.perf.test.core.PerfStressOptions;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

public abstract class BlobTestBase<TOptions extends PerfStressOptions> extends ContainerTest<TOptions> {
    protected static final int DEFAULT_BUFFER_SIZE = 8192;
    protected final CloudBlockBlob cloudBlockBlob;

    public BlobTestBase(TOptions options) {
        super(options);
        String blobName = "randomblobtest-" + UUID.randomUUID().toString();
        try {
            cloudBlockBlob = cloudBlobContainer.getBlockBlobReference(blobName);
        } catch (URISyntaxException | StorageException e) {
            throw new RuntimeException(e);
        }
    }
}
