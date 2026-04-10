// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.options.PageBlobUploadPagesOptions;
import com.azure.storage.blob.specialized.PageBlobAsyncClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.stress.CrcInputStream;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * Page blob upload pages with {@link PageBlobUploadPagesOptions#setContentValidationAlgorithm}.
 */
public class ContentValidationUploadPages extends PageBlobScenarioBase<ContentValidationStressOptions> {
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobAsyncClient asyncNoFaultClient;
    private final PageBlobAsyncClient tempSetupPageBlobClient;

    public ContentValidationUploadPages(ContentValidationStressOptions options) {
        super(options);
        String blobName = generateBlobName();
        String tempBlobName = generateBlobName();

        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(blobName);
        this.syncClient = getSyncContainerClient().getBlobClient(blobName);
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(blobName);
        BlobAsyncClient tempSetupBlobClient = getAsyncContainerClientNoFault().getBlobAsyncClient(tempBlobName);
        this.tempSetupPageBlobClient = tempSetupBlobClient.getPageBlobAsyncClient();
    }

    @Override
    protected void runInternal(Context span) {
        try (CrcInputStream inputStream = new CrcInputStream(originalContent.getBlobContentHead(), options.getSize())) {
            PageBlobClient pageBlobClient = syncClient.getPageBlobClient();
            PageRange range = new PageRange().setStart(0).setEnd(options.getSize() - 1);
            pageBlobClient.uploadPagesWithResponse(
                new PageBlobUploadPagesOptions(range, inputStream)
                    .setContentValidationAlgorithm(options.getContentValidationAlgorithm()),
                null, span);
            originalContent.checkMatch(inputStream.getContentInfo(), span).block();
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        PageBlobAsyncClient pageBlobAsyncClient = asyncClient.getPageBlobAsyncClient();
        Flux<ByteBuffer> byteBufferFlux = new CrcInputStream(originalContent.getBlobContentHead(), options.getSize())
            .convertStreamToByteBuffer();
        PageRange range = new PageRange().setStart(0).setEnd(options.getSize() - 1);
        return pageBlobAsyncClient.uploadPagesWithResponse(
                new PageBlobUploadPagesOptions(range, byteBufferFlux)
                    .setContentValidationAlgorithm(options.getContentValidationAlgorithm()))
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
        return asyncNoFaultClient.getPageBlobAsyncClient().deleteIfExists()
            .onErrorResume(e -> Mono.empty())
            .then(tempSetupPageBlobClient.deleteIfExists())
            .then(super.cleanupAsync());
    }
}
