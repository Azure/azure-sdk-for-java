// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.stress.CrcOutputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class DownloadStream extends BlobScenarioBase<StorageStressOptions> {
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobAsyncClient asyncNoFaultClient;

    public DownloadStream(StorageStressOptions options) {
        super(options);
        String blobName = generateBlobName();
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(blobName);
        this.syncClient = getSyncContainerClient().getBlobClient(blobName);
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(blobName);
    }

    @Override
    protected void runInternal(Context span) throws IOException {
        try (CrcOutputStream outputStream = new CrcOutputStream()) {
            syncClient.downloadStreamWithResponse(outputStream, null, null, null, false, null, span);
            outputStream.close();
            originalContent.checkMatch(outputStream.getContentInfo(), span).block();
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        return asyncClient.downloadStreamWithResponse(null, null, null, false)
            .flatMap(response -> originalContent.checkMatch(response.getValue(), span));
    }

    @Override
    public Mono<Void> setupAsync() {
        // setup is called for each instance of scenario. Number of instances equals options.getParallel()
        // so we're setting up options.getParallel() blobs to scale beyond service limits for 1 blob.
        return super.setupAsync()
            .then(originalContent.setupBlob(asyncNoFaultClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.deleteIfExists()
            .then(super.cleanupAsync());
    }
}
