// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.stress;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.StorageSeekableByteChannel;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.options.ShareFileSeekableByteChannelWriteOptions;
import com.azure.storage.file.share.stress.utils.OriginalContent;
import com.azure.storage.stress.CrcInputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.azure.core.util.FluxUtil.monoError;

public class FileSeekableByteChannelWrite extends ShareScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(FileSeekableByteChannelWrite.class);
    private final OriginalContent originalContent = new OriginalContent();
    private final ShareFileClient syncClient;
    private final ShareFileAsyncClient asyncNoFaultClient;

    public FileSeekableByteChannelWrite(StorageStressOptions options) {
        super(options);
        String fileName = generateFileName();
        this.syncClient = getSyncShareClient().getFileClient(fileName);
        this.asyncNoFaultClient = getAsyncShareClientNoFault().getFileClient(fileName);
    }

    @Override
    protected void runInternal(Context span) throws IOException {
        Flux<ByteBuffer> byteBufferFlux = new CrcInputStream(originalContent.getContentHead(), options.getSize())
            .convertStreamToByteBuffer();

        try (StorageSeekableByteChannel channel = (StorageSeekableByteChannel) syncClient.getFileSeekableByteChannelWrite(
            new ShareFileSeekableByteChannelWriteOptions(true).setFileSize(options.getSize())
                .setChunkSizeInBytes(4 * 1024 * 1024L))) {
            // Perform buffered write upload to the blob
            Mono<Void> writeOperation = byteBufferFlux
                .doOnNext(buffer -> {
                    try {
                        // This will write each chunk as it comes
                        channel.write(buffer);
                    } catch (IOException e) {
                        throw LOGGER.logExceptionAsError(new RuntimeException(e));
                    }
                }).then();
            // Trigger the operation and wait for completion
            writeOperation.block();
            channel.getWriteBehavior().commit(options.getSize());
        }
        // Check if the blob content matches the original content
        originalContent.checkMatch(byteBufferFlux, span).block();
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        return monoError(LOGGER, new RuntimeException("getFileSeekableByteChannelWrite() does not exist on the async client"));
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(originalContent.setupFile(asyncNoFaultClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.deleteIfExists().then(super.cleanupAsync());
    }
}
