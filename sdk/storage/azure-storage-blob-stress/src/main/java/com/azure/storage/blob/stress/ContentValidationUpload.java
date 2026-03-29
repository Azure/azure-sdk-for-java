// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.stress.CrcInputStream;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * Parallel blob upload with {@link com.azure.storage.blob.options.BlobParallelUploadOptions#setRequestChecksumAlgorithm}
 * enabled. Verifies stored data via CRC after upload (see {@code BlobContentValidationUploadTests}).
 */
public class ContentValidationUpload extends BlobScenarioBase<ContentValidationStressOptions> {
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobAsyncClient asyncNoFaultClient;
    private final ParallelTransferOptions parallelTransferOptions;

    public ContentValidationUpload(ContentValidationStressOptions options) {
        super(options);
        String blobName = generateBlobName();
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(blobName);
        this.syncClient = getSyncContainerClient().getBlobClient(blobName);
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(blobName);
        parallelTransferOptions = new ParallelTransferOptions()
            .setMaxConcurrency(options.getMaxConcurrency())
            .setMaxSingleUploadSizeLong(4 * 1024 * 1024L);
    }

    @Override
    protected void runInternal(Context span) {
        try (CrcInputStream inputStream = new CrcInputStream(originalContent.getBlobContentHead(), options.getSize())) {
            syncClient.uploadWithResponse(new BlobParallelUploadOptions(inputStream)
                .setParallelTransferOptions(parallelTransferOptions)
                .setRequestChecksumAlgorithm(options.getRequestChecksumAlgorithm()), null, span);
            originalContent.checkMatch(inputStream.getContentInfo(), span).block();
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        Flux<ByteBuffer> byteBufferFlux = new CrcInputStream(originalContent.getBlobContentHead(), options.getSize())
            .convertStreamToByteBuffer();
        return asyncClient.uploadWithResponse(new BlobParallelUploadOptions(byteBufferFlux)
                .setParallelTransferOptions(parallelTransferOptions)
                .setRequestChecksumAlgorithm(options.getRequestChecksumAlgorithm()))
            .then(originalContent.checkMatch(byteBufferFlux, span));
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(originalContent.setupBlob(asyncNoFaultClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.deleteIfExists()
            .then(super.cleanupAsync());
    }
}
