// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * A {@link BinaryDataContent} implementation which is backed by a {@link ByteBuffer}.
 */
public final class ByteBufferContent extends BinaryDataContent {
    private final ByteBuffer content;

    /**
     * Creates a new instance of {@link ByteBufferContent}.
     *
     * @param content The {@link ByteBuffer} content.
     * @throws NullPointerException If {@code content} is null.
     */
    public ByteBufferContent(ByteBuffer content) {
        this.content = Objects.requireNonNull(content, "'content' cannot be null.");
    }

    @Override
    public Long getLength() {
        return (long) content.remaining();
    }

    @Override
    public String toString() {
        if (content.hasArray()) {
            return new String(content.array(), content.position(), content.remaining(), StandardCharsets.UTF_8);
        } else {
            return new String(toBytes(), StandardCharsets.UTF_8);
        }
    }

    @Override
    public byte[] toBytes() {
        if (content.hasArray()) {
            return Arrays.copyOfRange(content.array(), content.position(), content.remaining() + content.position());
        } else {
            byte[] remaining = new byte[content.remaining()];
            content.duplicate().get(remaining);
            return remaining;
        }
    }

    @Override
    public <T> T toObject(TypeReference<T> typeReference, ObjectSerializer serializer) {
        return serializer.deserializeFromBytes(toBytes(), typeReference);
    }

    @Override
    public InputStream toStream() {
        return new ByteArrayInputStream(toBytes());
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return content.asReadOnlyBuffer();
    }

    @Override
    public Flux<ByteBuffer> toFluxByteBuffer() {
        return Mono.fromSupplier(content::asReadOnlyBuffer).flux();
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
}
