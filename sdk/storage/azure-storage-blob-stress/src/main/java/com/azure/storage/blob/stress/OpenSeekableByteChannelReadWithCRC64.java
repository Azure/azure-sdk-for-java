// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobSeekableByteChannelReadResult;
import com.azure.storage.blob.options.BlobSeekableByteChannelReadOptions;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.stress.CrcInputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.channels.Channels;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * Seekable byte channel read with CRC64 Algorithm enabled (sync only).
 * Verifies the correctness of the download response content via CRC.
 */
public class OpenSeekableByteChannelReadWithCRC64
    extends BlobScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(OpenSeekableByteChannelReadWithCRC64.class);
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncNoFaultClient;

    public OpenSeekableByteChannelReadWithCRC64(StorageStressOptions options) {
        super(options);
        String blobName = generateBlobName();
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(blobName);
        this.syncClient = getSyncContainerClient().getBlobClient(blobName);
    }

    @Override
    protected void runInternal(Context span) throws IOException {
        BlobSeekableByteChannelReadResult result = syncClient.openSeekableByteChannelRead(
            new BlobSeekableByteChannelReadOptions()
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64),
            span);
        try (CrcInputStream crcStream = new CrcInputStream(Channels.newInputStream(result.getChannel()))) {
            byte[] buffer = new byte[8192];
            while (crcStream.read(buffer) != -1) {
                // do nothing
            }
            originalContent.checkMatch(crcStream.getContentInfo(), span).block();
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        return monoError(LOGGER,
            new RuntimeException("openSeekableByteChannelRead() does not exist on the async client"));
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
