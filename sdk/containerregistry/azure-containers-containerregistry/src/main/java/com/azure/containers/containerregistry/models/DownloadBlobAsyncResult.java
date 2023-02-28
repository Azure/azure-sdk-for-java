// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.models;

import com.azure.containers.containerregistry.implementation.ConstructorAccessors;
import com.azure.containers.containerregistry.implementation.UtilsImpl;
import com.azure.core.annotation.Fluent;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.util.Objects;

import static com.azure.containers.containerregistry.implementation.UtilsImpl.validateDigest;

/**
 * The object returned by the downloadBlob operation
 * containing the blob contents and its digest.
 */
@Fluent
public final class DownloadBlobAsyncResult {
    static {
        ConstructorAccessors.setBlobDownloadResultAccessor(DownloadBlobAsyncResult::new);
    }

    private final Flux<ByteBuffer> content;
    private final MessageDigest sha256;

    /**
     * Initialize an instance of DownloadBlobResult.
     * @param digest The requested digest.
     * @param content The content of the blob.
     */
    private DownloadBlobAsyncResult(String digest, Flux<ByteBuffer> content) {
        this.sha256 = UtilsImpl.createSha256();
        this.content = content
            .doOnNext(buffer -> sha256.update(buffer.asReadOnlyBuffer()))
            .doOnComplete(() -> validateDigest(sha256, digest))
            .doOnError(UtilsImpl::mapException);
    }

    /**
     * Get the blob contents.
     * @return The contents of the blob.
     */
    public Flux<ByteBuffer> getValue() {
        return content;
    }

    /**
     * Transfers content bytes to the {@link AsynchronousByteChannel}.
     * @param channel The destination {@link AsynchronousByteChannel}.
     * @return A {@link Mono} that completes when transfer is completed.
     */
    public Mono<Void> writeValueToAsync(AsynchronousByteChannel channel) {
        Objects.requireNonNull(channel, "'channel' must not be null");
        return FluxUtil.writeToAsynchronousByteChannel(content, channel);
    }

    /**
     * Transfers content bytes to the {@link WritableByteChannel}.
     * @param channel The destination {@link WritableByteChannel}.
     * @return A {@link Mono} that completes when transfer is completed.
     * @throws UncheckedIOException When I/O operation fails.
     */
    public Mono<Void> writeValueTo(WritableByteChannel channel) {
        Objects.requireNonNull(channel, "'channel' must not be null");
        return FluxUtil.writeToWritableByteChannel(content, channel);
    }
}
