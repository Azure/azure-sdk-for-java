// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.options.BlockBlobSeekableByteChannelWriteOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.common.implementation.StorageSeekableByteChannel;
import com.azure.storage.stress.CrcInputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.storage.blob.options.BlockBlobSeekableByteChannelWriteOptions.WriteMode.OVERWRITE;

/**
 * Block-blob seekable byte channel write with CRC64 enabled (sync only).
 */
public class SeekableByteChannelWriteWithCRC64 extends BlobScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(SeekableByteChannelWriteWithCRC64.class);
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncNoFaultClient;

    public SeekableByteChannelWriteWithCRC64(StorageStressOptions options) {
        super(options);
        String blobName = generateBlobName();
        this.syncClient = getSyncContainerClient().getBlobClient(blobName);
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(blobName);
    }

    @Override
    protected void runInternal(Context span) throws IOException {
        BlockBlobClient blockBlobClient = syncClient.getBlockBlobClient();
        BlockBlobSeekableByteChannelWriteOptions writeOptions = new BlockBlobSeekableByteChannelWriteOptions(OVERWRITE)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        try (CrcInputStream crcInput = new CrcInputStream(originalContent.getBlobContentHead(), options.getSize())) {
            Flux<ByteBuffer> byteBufferFlux = crcInput.convertStreamToByteBuffer();
            try (StorageSeekableByteChannel channel = (StorageSeekableByteChannel) blockBlobClient.openSeekableByteChannelWrite(
                writeOptions)) {
                Mono<Void> writeOperation = byteBufferFlux
                    .doOnNext(buffer -> {
                        try {
                            while (buffer.hasRemaining()) {
                                channel.write(buffer);
                            }
                        } catch (IOException e) {
                            throw LOGGER.logExceptionAsError(new RuntimeException(e));
                        }
                    }).then();
                writeOperation.block();
                channel.getWriteBehavior().commit(options.getSize());
            }
            originalContent.checkMatch(byteBufferFlux, span).block();
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        return monoError(LOGGER, new RuntimeException(
            "openSeekableByteChannelWrite() does not exist on the async client"));
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(originalContent.setupBlob(asyncNoFaultClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.deleteIfExists().then(super.cleanupAsync());
    }
}
