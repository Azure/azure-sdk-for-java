// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.options.PageBlobUploadPagesFromUrlOptions;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.PageBlobAsyncClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.stress.CrcOutputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.OffsetDateTime;

public class UploadPagesFromUrl extends PageBlobScenarioBase<StorageStressOptions> {
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobClient syncNoFaultClient;
    private final BlobAsyncClient asyncNoFaultClient;
    private final BlobAsyncClient sourceBlobClient;
    private final PageBlobAsyncClient sourcePageBlobClient;

    public UploadPagesFromUrl(StorageStressOptions options) {
        super(options);
        String sourceBlobName = generateBlobName();
        String destinationBlobName = generateBlobName();

        this.syncNoFaultClient = getSyncContainerClientNoFault().getBlobClient(destinationBlobName);
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(destinationBlobName);
        this.syncClient = getSyncContainerClient().getBlobClient(destinationBlobName);
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(destinationBlobName);
        this.sourceBlobClient = getAsyncContainerClientNoFault().getBlobAsyncClient(sourceBlobName);
        this.sourcePageBlobClient = sourceBlobClient.getPageBlobAsyncClient();
    }

    @Override
    protected void runInternal(Context span) throws IOException {
        // Used for faulted upload
        PageBlobClient destinationBlobClient = syncClient.getPageBlobClient();
        // Used for non-faulted download for data comparison
        PageBlobClient destinationBlobNoFaultClient = syncNoFaultClient.getPageBlobClient();
        String sas = sourcePageBlobClient.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));

        destinationBlobClient.uploadPagesFromUrlWithResponse(new PageBlobUploadPagesFromUrlOptions(
            new PageRange().setStart(0).setEnd(options.getSize() - 1), sourcePageBlobClient.getBlobUrl() + "?" + sas),
            null, span);

        // Download the latest blob range and compare it with the original content
        try (CrcOutputStream outputStream = new CrcOutputStream()) {
            destinationBlobNoFaultClient.downloadStreamWithResponse(outputStream, null, null, null, false, null, span);
            outputStream.close();
            originalContent.checkMatch(outputStream.getContentInfo(), span).block();
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        // Used for faulted upload
        PageBlobAsyncClient destinationBlobClient = asyncClient.getPageBlobAsyncClient();
        // Used for non-faulted download for data comparison
        PageBlobAsyncClient destinationBlobNoFaultClient = asyncNoFaultClient.getPageBlobAsyncClient();
        String sas = sourceBlobClient.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));

        return destinationBlobClient.uploadPagesFromUrlWithResponse(new PageBlobUploadPagesFromUrlOptions(
            new PageRange().setStart(0).setEnd(options.getSize() - 1), sourceBlobClient.getBlobUrl() + "?" + sas))
            .then(destinationBlobNoFaultClient.downloadStreamWithResponse(null, null, null, false)
                    .flatMap(response -> originalContent.checkMatch(response.getValue(), span)));
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(asyncNoFaultClient.getPageBlobAsyncClient().create(options.getSize()))
            .then(sourcePageBlobClient.create(options.getSize()))
            .then(originalContent.setupPageBlob(sourcePageBlobClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.getPageBlobAsyncClient().delete()
            .then(sourcePageBlobClient.delete())
            .then(super.cleanupAsync());
    }
}
