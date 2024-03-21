// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.AppendBlobAsyncClient;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.stress.CrcOutputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.OffsetDateTime;

public class AppendBlockFromUrl extends BlobScenarioBase<StorageStressOptions> {
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobClient syncNoFaultClient;
    private final BlobAsyncClient asyncNoFaultClient;
    private final BlobAsyncClient sourceBlobClient;

    public AppendBlockFromUrl(StorageStressOptions options) {
        super(options);
        String sourceBlobName = generateBlobName();
        String destinationBlobName = generateBlobName();

        this.syncNoFaultClient = getSyncContainerClientNoFault().getBlobClient(destinationBlobName);
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(destinationBlobName);
        this.syncClient = getSyncContainerClient().getBlobClient(destinationBlobName);
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(destinationBlobName);
        this.sourceBlobClient = getAsyncContainerClientNoFault().getBlobAsyncClient(sourceBlobName);
    }

    @Override
    protected void runInternal(Context span) throws IOException {
        // Used for faulted upload
        AppendBlobClient destinationBlobClient = syncClient.getAppendBlobClient();
        // Used for non-faulted download for data comparison
        AppendBlobClient destinationBlobNoFaultClient = syncNoFaultClient.getAppendBlobClient();
        String sas = sourceBlobClient.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));

        destinationBlobClient.appendBlockFromUrlWithResponse(sourceBlobClient.getBlobUrl() + "?" + sas, null, null,
            null, null, null, span);
        // Since we cannot grab the data that was uploaded to the destination url, need to download the contents and
        // compare it with the original content
        long size = destinationBlobNoFaultClient.getProperties().getBlobSize();
        // Since appendBlob appends previous data, we need to compare it with the last blob size that was appended
        BlobRange range = size > 0 ? new BlobRange(size - options.getSize(), options.getSize()) : null;

        // Download the latest blob range and compare it with the original content
        try (CrcOutputStream outputStream = new CrcOutputStream()) {
            destinationBlobNoFaultClient.downloadStreamWithResponse(outputStream, range, null, null, false, null, span);
            outputStream.close();
            originalContent.checkMatch(outputStream.getContentInfo(), span).block();
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        // Used for faulted upload
        AppendBlobAsyncClient destinationBlobClient = asyncClient.getAppendBlobAsyncClient();
        // Used for non-faulted download for data comparison
        AppendBlobAsyncClient destinationBlobNoFaultClient = asyncNoFaultClient.getAppendBlobAsyncClient();
        String sas = sourceBlobClient.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));

        return destinationBlobClient.appendBlockFromUrlWithResponse(sourceBlobClient.getBlobUrl() + "?" + sas, null,
                null, null, null)
            .then(destinationBlobNoFaultClient.getProperties())
            .flatMap(properties -> {
                long size = properties.getBlobSize();
                BlobRange range = size > 0 ? new BlobRange(size - options.getSize(), options.getSize()) : null;
                return destinationBlobNoFaultClient.downloadStreamWithResponse(range, null, null, false)
                    .flatMap(response -> originalContent.checkMatch(response.getValue(), span));
                });
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(asyncNoFaultClient.getAppendBlobAsyncClient().create())
            .then(originalContent.setupBlob(sourceBlobClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.getAppendBlobAsyncClient().delete()
            .then(sourceBlobClient.delete())
            .then(super.cleanupAsync());
    }
}
