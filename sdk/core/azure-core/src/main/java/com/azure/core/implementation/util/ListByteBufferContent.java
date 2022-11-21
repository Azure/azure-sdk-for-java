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
import java.util.List;
import java.util.Objects;

/**
 * BinaryDataContent for a {@link List} of {@link ByteBuffer}.
 */
// TODO (jaschrep): do not merge to main, temporary implementation
public class ListByteBufferContent extends BinaryDataContent {
    private final List<ByteBuffer> content;

    private byte[] bytes;

    /**
     * Creates a new instance of {@link BinaryDataContent}.
     *
     * @param content The {{@link List} of {@link ByteBuffer} content.
     * @throws NullPointerException If {@code content} is null.
     */
    public ListByteBufferContent(List<ByteBuffer> content) {
        this.content = Objects.requireNonNull(content, "'content' cannot be null.");
    }

    @Override
    public Long getLength() {
        return content.stream().mapToLong(ByteBuffer::remaining).sum();
    }

    @Override
    public String toString() {
        return new String(toBytes(), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytes() {
        if (bytes != null) {
            return bytes;
        }
        if (getLength() > Integer.MAX_VALUE) {
            throw new UnsupportedOperationException("Content cannot fit in a single array.");
        }
        bytes = new byte[getLength().intValue()];
        int i = 0;
        for (ByteBuffer bb : content) {
            int remaining = bb.remaining();
            bb.mark();
            bb.get(bytes, i, remaining);
            i += remaining;
            bb.reset();
        }
        return bytes;
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
        ByteBuffer result = ByteBuffer.allocate(getLength().intValue());
        for (ByteBuffer bb : content) {
            bb.mark();
            result.put(bb);
            bb.reset();
        }
        return result;
    }

    @Override
    public Flux<ByteBuffer> toFluxByteBuffer() {
        return Flux.fromIterable(content);
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
