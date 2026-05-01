// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.options.BlobDownloadContentOptions;
import com.azure.storage.blob.options.BlobDownloadStreamOptions;
import com.azure.storage.blob.stress.utils.OriginalContent;
import reactor.core.publisher.Mono;

/**
 * Download content with
 * {@link BlobDownloadContentOptions#setContentValidationAlgorithm} enabled.
 * Verifies the correctness of the download response content via CRC.
 */
public class ContentValidationDownloadContent extends BlobScenarioBase<ContentValidationDecoderStressOptions> {
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobAsyncClient asyncNoFaultClient;

    public ContentValidationDownloadContent(ContentValidationDecoderStressOptions options) {
        super(options);
        String blobName = generateBlobName();
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(blobName);
        this.syncClient = getSyncContainerClient().getBlobClient(blobName);
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(blobName);
    }

    @Override
    protected void runInternal(Context span) {
        originalContent.checkMatch(
            syncClient.downloadContentWithResponse(
                new BlobDownloadContentOptions()
                    .setContentValidationAlgorithm(options.getContentValidationAlgorithm()),
                null, span).getValue(),
            span).block();
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        // TODO return downloadContent once it stops buffering.
        return asyncClient.downloadStreamWithResponse(
            new BlobDownloadStreamOptions()
                .setContentValidationAlgorithm(options.getContentValidationAlgorithm()))
            .flatMap(response -> {
                long contentLength = Long.valueOf(response.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));
                return BinaryData.fromFlux(response.getValue(), contentLength, false);
            })
            .flatMap(bd -> originalContent.checkMatch(bd, span));
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then(originalContent.setupBlob(asyncNoFaultClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.deleteIfExists()
            .then(super.cleanupAsync());
    }
}
