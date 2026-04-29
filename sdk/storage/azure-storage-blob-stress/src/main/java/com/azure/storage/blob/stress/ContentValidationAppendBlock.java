// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.options.AppendBlobAppendBlockOptions;
import com.azure.storage.blob.specialized.AppendBlobAsyncClient;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.stress.CrcInputStream;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * Append block with {@link AppendBlobAppendBlockOptions#setContentValidationAlgorithm}.
 */
public class ContentValidationAppendBlock extends BlobScenarioBase<ContentValidationStressOptions> {
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobAsyncClient asyncNoFaultClient;
    private final BlobAsyncClient tempSetupBlobClient;

    public ContentValidationAppendBlock(ContentValidationStressOptions options) {
        super(options);
        String blobName = generateBlobName();
        String tempBlobName = generateBlobName();

        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(blobName);
        this.syncClient = getSyncContainerClient().getBlobClient(blobName);
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(blobName);
        this.tempSetupBlobClient = getAsyncContainerClientNoFault().getBlobAsyncClient(tempBlobName);
    }

    @Override
    protected void runInternal(Context span) {
        try (CrcInputStream inputStream = new CrcInputStream(originalContent.getBlobContentHead(), options.getSize())) {
            AppendBlobClient appendBlobClient = syncClient.getAppendBlobClient();
            appendBlobClient.appendBlockWithResponse(
                new AppendBlobAppendBlockOptions(inputStream, options.getSize())
                    .setContentValidationAlgorithm(options.getContentValidationAlgorithm()),
                null, span);
            originalContent.checkMatch(inputStream.getContentInfo(), span).block();
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        AppendBlobAsyncClient appendBlobAsyncClient = asyncClient.getAppendBlobAsyncClient();
        Flux<ByteBuffer> byteBufferFlux = new CrcInputStream(originalContent.getBlobContentHead(), options.getSize())
            .convertStreamToByteBuffer();
        return appendBlobAsyncClient.appendBlockWithResponse(
                new AppendBlobAppendBlockOptions(byteBufferFlux, options.getSize())
                    .setContentValidationAlgorithm(options.getContentValidationAlgorithm()))
            .then(originalContent.checkMatch(byteBufferFlux, span));
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(asyncNoFaultClient.getAppendBlobAsyncClient().create())
            .then(originalContent.setupBlob(tempSetupBlobClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.getAppendBlobAsyncClient().deleteIfExists()
            .onErrorResume(e -> Mono.empty())
            .then(tempSetupBlobClient.deleteIfExists())
            .then(super.cleanupAsync());
    }
}
