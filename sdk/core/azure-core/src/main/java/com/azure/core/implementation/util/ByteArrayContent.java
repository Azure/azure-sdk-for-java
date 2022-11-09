// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A {@link BinaryDataContent} implementation which is backed by a {@code byte[]}.
 */
public final class ByteArrayContent extends BinaryDataContent {
    private final byte[] content;

    /**
     * Creates a new instance of {@link ByteArrayContent}.
     *
     * @param content The byte array content.
     * @throws NullPointerException if {@code content} is null.
     */
    public ByteArrayContent(byte[] content) {
        this.content = Objects.requireNonNull(content, "'content' cannot be null");
    }

    @Override
    public Long getLength() {
        return (long) this.content.length;
    }

    @Override
    public String toString() {
        return new String(content, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytes() {
        return content;
    }

    @Override
    public <T> T toObject(TypeReference<T> typeReference, ObjectSerializer serializer) {
        return serializer.deserializeFromBytes(this.content, typeReference);
    }

    @Override
    public InputStream toStream() {
        return new ByteArrayInputStream(this.content);
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(this.content).asReadOnlyBuffer();
    }

    @Override
    public Flux<ByteBuffer> toFluxByteBuffer() {
        return Mono.fromSupplier(this::toByteBuffer).flux();
    }

    @Override
    public boolean isReplayable() {
        return true;
    }

    @Override
    public BinaryDataContent toReplayableContent() {
        return this;
    }

    @Override
    public Mono<BinaryDataContent> toReplayableContentAsync() {
        return Mono.just(this);
    }

    @Override
    public Mono<Void> writeToAsync(AsynchronousByteChannel channel) {
        return FluxUtil.writeToAsynchronousByteChannel(toFluxByteBuffer(), channel);
    }

    @Override
    public void writeTo(WritableByteChannel channel) throws IOException {
        ByteBuffer buffer = toByteBuffer();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }
}
