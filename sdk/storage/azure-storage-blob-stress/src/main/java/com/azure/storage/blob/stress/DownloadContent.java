// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

public class DownloadContent extends BlobScenarioBase<StorageStressOptions> {
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobAsyncClient asyncNoFaultClient;

    public DownloadContent(StorageStressOptions options) {
        super(options);
        String blobName = generateBlobName();
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(blobName);
        this.syncClient = getSyncContainerClient().getBlobClient(blobName);
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(blobName);
    }

    @Override
    protected void runInternal(Context span) {
        originalContent.checkMatch(syncClient.downloadContent(), span).block();
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        // TODO return downloadContent once it stops buffering

        return asyncClient.downloadStreamWithResponse(null, null, null, false)
            .flatMap(response ->  {
                long contentLength = Long.valueOf(response.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
                return BinaryData.fromFlux(response.getValue(), contentLength, false);
            })
            .flatMap(bd -> originalContent.checkMatch(bd, span));
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
