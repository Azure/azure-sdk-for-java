// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.options.BlockBlobCommitBlockListOptions;
import com.azure.storage.blob.options.BlockBlobStageBlockFromUrlOptions;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.stress.CrcOutputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Collections;

public class StageBlockFromUrl extends BlobScenarioBase<StorageStressOptions> {
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobClient syncNoFaultClient;
    private final BlobAsyncClient asyncNoFaultClient;
    private final BlobAsyncClient sourceClient;

    public StageBlockFromUrl(StorageStressOptions options) {
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
        // Used for non-faulted commitBlock and download for data comparison
        BlockBlobClient destinationNoFaultClient = syncNoFaultClient.getBlockBlobClient();
        String blockId = Base64.getEncoder().encodeToString(CoreUtils.randomUuid().toString()
            .getBytes(StandardCharsets.UTF_8));
        String sas = sourceClient.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        // First perform faulted stage block to send data to the service
        destinationClient.stageBlockFromUrlWithResponse(new BlockBlobStageBlockFromUrlOptions(blockId,
                sourceClient.getBlobUrl() + "?" + sas), null, span);
        // Then perform non-faulted commit block list to commit the block
        destinationNoFaultClient.commitBlockListWithResponse(
            new BlockBlobCommitBlockListOptions(Collections.singletonList(blockId)), null, span);
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
        BlockBlobAsyncClient destinationAsyncClient = asyncClient.getBlockBlobAsyncClient();
        // Used for non-faulted commitBlock and download for data comparison
        BlockBlobAsyncClient destinationAsyncNoFaultClient = asyncNoFaultClient.getBlockBlobAsyncClient();
        String blockId = Base64.getEncoder().encodeToString(CoreUtils.randomUuid().toString()
            .getBytes(StandardCharsets.UTF_8));
        String sas = sourceClient.generateSas(new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new BlobContainerSasPermission().setReadPermission(true)));
        // First perform faulted stage block to send data to the service
        return destinationAsyncClient.stageBlockFromUrlWithResponse(new BlockBlobStageBlockFromUrlOptions(blockId,
            sourceClient.getBlobUrl() + "?" + sas))
        // Then perform non-faulted commit block list to commit the block
            .then(destinationAsyncNoFaultClient.commitBlockListWithResponse(
            new BlockBlobCommitBlockListOptions(Collections.singletonList(blockId))))
        // Download the blob contents and compare it with the original content
            .then(destinationAsyncNoFaultClient.downloadStreamWithResponse(null, null, null, false))
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
