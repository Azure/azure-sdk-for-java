// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.options.BlockBlobCommitBlockListOptions;
import com.azure.storage.blob.options.BlockBlobStageBlockOptions;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.stress.CrcInputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

/**
 * Stage block with CRC64 enabled on the faulted client, then commit via the non-faulted client.
 */
public class StageBlockWithCRC64 extends BlobScenarioBase<StorageStressOptions> {
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobClient syncNoFaultClient;
    private final BlobAsyncClient asyncNoFaultClient;

    public StageBlockWithCRC64(StorageStressOptions options) {
        super(options);
        String blobName = generateBlobName();
        this.syncNoFaultClient = getSyncContainerClientNoFault().getBlobClient(blobName);
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(blobName);
        this.syncClient = getSyncContainerClient().getBlobClient(blobName);
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(blobName);
    }

    @Override
    protected void runInternal(Context span) {
        BlockBlobClient blockBlobClient = syncClient.getBlockBlobClient();
        BlockBlobClient blockBlobClientNoFault = syncNoFaultClient.getBlockBlobClient();
        String blockId = Base64.getEncoder().encodeToString(CoreUtils.randomUuid().toString()
            .getBytes(StandardCharsets.UTF_8));
        try (CrcInputStream inputStream = new CrcInputStream(originalContent.getBlobContentHead(), options.getSize())) {
            BinaryData data = BinaryData.fromStream(inputStream, options.getSize());
            blockBlobClient.stageBlockWithResponse(
                new BlockBlobStageBlockOptions(blockId, data)
                    .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64),
                null, span);
            blockBlobClientNoFault.commitBlockListWithResponse(
                new BlockBlobCommitBlockListOptions(Collections.singletonList(blockId)), null, span);
            originalContent.checkMatch(inputStream.getContentInfo(), span).block();
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        BlockBlobAsyncClient blockBlobAsyncClient = asyncClient.getBlockBlobAsyncClient();
        BlockBlobAsyncClient blockBlobAsyncClientNoFault = asyncNoFaultClient.getBlockBlobAsyncClient();
        String blockId = Base64.getEncoder().encodeToString(CoreUtils.randomUuid().toString()
            .getBytes(StandardCharsets.UTF_8));
        Flux<ByteBuffer> byteBufferFlux = new CrcInputStream(originalContent.getBlobContentHead(), options.getSize())
            .convertStreamToByteBuffer();
        return BinaryData.fromFlux(byteBufferFlux, options.getSize(), false)
            .flatMap(binaryData -> blockBlobAsyncClient.stageBlockWithResponse(
                new BlockBlobStageBlockOptions(blockId, binaryData)
                    .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64)))
            .then(blockBlobAsyncClientNoFault.commitBlockListWithResponse(
                new BlockBlobCommitBlockListOptions(Collections.singletonList(blockId))))
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
