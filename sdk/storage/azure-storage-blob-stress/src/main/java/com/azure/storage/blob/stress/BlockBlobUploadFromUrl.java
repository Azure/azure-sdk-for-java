// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.options.BlobUploadFromUrlOptions;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.stress.CrcOutputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.OffsetDateTime;

public class BlockBlobUploadFromUrl extends BlobScenarioBase<StorageStressOptions> {
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobClient syncNoFaultClient;
    private final BlobAsyncClient asyncNoFaultClient;
    private final BlobAsyncClient sourceClient;

    public BlockBlobUploadFromUrl(StorageStressOptions options) {
        super(options);
        String sourceBlobName = generateBlobName();
        String destinationBlobName = generateBlobName();

        this.syncNoFaultClient = getSyncContainerClientNoFault().getBlobClient(destinationBlobName);
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(destinationBlobName);
        this.syncClient = getSyncContainerClient().getBlobClient(destinationBlobName);
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(destinationBlobName);
        this.sourceClient = getAsyncContainerClientNoFault().getBlobAsyncClient(sourceBlobName);
    }

    @Override
    protected void runInternal(Context span) throws IOException {
        // Used for faulted upload
        BlockBlobClient destinationClient = syncClient.getBlockBlobClient();
        // Used for non-faulted download for data comparison
        BlockBlobClient destinationNoFaultClient = syncNoFaultClient.getBlockBlobClient();

        String sas = sourceClient.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        destinationClient.uploadFromUrlWithResponse(new BlobUploadFromUrlOptions(sourceClient.getBlobUrl() + "?" + sas),
            null, span);
        // Download the blob contents and compare it with the original content
        try (CrcOutputStream outputStream = new CrcOutputStream()) {
            destinationNoFaultClient.downloadStreamWithResponse(outputStream, null, null, null, false, null, span);
            outputStream.close();
            originalContent.checkMatch(outputStream.getContentInfo(), span).block();
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        // Used for faulted upload
        BlockBlobAsyncClient destinationClient = asyncClient.getBlockBlobAsyncClient();
        // Used for non-faulted download for data comparison
        BlockBlobAsyncClient destinationNoFaultClient = asyncNoFaultClient.getBlockBlobAsyncClient();

        String sas = sourceClient.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));

        return destinationClient.uploadFromUrlWithResponse(
            new BlobUploadFromUrlOptions(sourceClient.getBlobUrl() + "?" + sas))
            .then(destinationNoFaultClient.downloadStreamWithResponse(null, null, null, false))
            .flatMap(response -> originalContent.checkMatch(response.getValue(), span));
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then(originalContent.setupBlob(sourceClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.delete()
            .then(sourceClient.delete())
            .then(super.cleanupAsync());
    }

}
