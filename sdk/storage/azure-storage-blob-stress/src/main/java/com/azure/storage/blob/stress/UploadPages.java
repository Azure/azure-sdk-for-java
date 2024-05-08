// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.specialized.PageBlobAsyncClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.common.Utility;
import com.azure.storage.stress.CrcInputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

public class UploadPages extends PageBlobScenarioBase<StorageStressOptions> {
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobAsyncClient asyncNoFaultClient;
    private final PageBlobAsyncClient tempSetupPageBlobClient;

    public UploadPages(StorageStressOptions options) {
        super(options);
        String blobName = generateBlobName();
        String tempBlobName = generateBlobName();

        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(blobName);
        this.syncClient = getSyncContainerClient().getBlobClient(blobName);
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(blobName);
        // this blob is used to perform normal upload in the setup phase
        BlobAsyncClient tempSetupBlobClient = getAsyncContainerClientNoFault().getBlobAsyncClient(tempBlobName);
        this.tempSetupPageBlobClient = tempSetupBlobClient.getPageBlobAsyncClient();
    }

    @Override
    protected void runInternal(Context span) {
        try (CrcInputStream inputStream = new CrcInputStream(originalContent.getBlobContentHead(), options.getSize())) {
            PageBlobClient pageBlobClient = syncClient.getPageBlobClient();
            pageBlobClient.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(options.getSize() - 1),
                inputStream, null, null, null, span);
            originalContent.checkMatch(inputStream.getContentInfo(), span).block();
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        PageBlobAsyncClient pageBlobAsyncClient = asyncClient.getPageBlobAsyncClient();
        Flux<ByteBuffer> byteBufferFlux = Utility.convertStreamToByteBuffer(
            new CrcInputStream(originalContent.getBlobContentHead(), options.getSize()), options.getSize(),
            PageBlobClient.PAGE_BYTES);
        return pageBlobAsyncClient.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(options.getSize() - 1),
            byteBufferFlux, null, null)
            .then(originalContent.checkMatch(byteBufferFlux, span));
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then(asyncNoFaultClient.getPageBlobAsyncClient().create(options.getSize()))
            .then(tempSetupPageBlobClient.create(options.getSize()))
            .then(originalContent.setupPageBlob(tempSetupPageBlobClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.getPageBlobAsyncClient().delete()
            .then(tempSetupPageBlobClient.delete())
            .then(super.cleanupAsync());
    }
}
