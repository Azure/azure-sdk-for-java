// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.util;

import com.typespec.core.util.logging.ClientLogger;
import com.typespec.core.util.serializer.ObjectSerializer;
import com.typespec.core.util.serializer.TypeReference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * A {@link BinaryDataContent} implementation which is backed by a {@link List} of {@link ByteBuffer}.
 */
public class ListByteBufferContent extends BinaryDataContent {
    private static final ClientLogger LOGGER = new ClientLogger(ListByteBufferContent.class);

    private final List<ByteBuffer> content;

    private volatile byte[] bytes;
    private static final AtomicReferenceFieldUpdater<ListByteBufferContent, byte[]> BYTES_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(ListByteBufferContent.class, byte[].class, "bytes");

    private Long cachedLength;

    /**
     * Creates a new instance of {@link BinaryDataContent}.
     *
     * @param content The {@link ByteBuffer} content.
     * @throws NullPointerException If {@code content} is null.
     */
    public ListByteBufferContent(List<ByteBuffer> content) {
        this.content = Objects.requireNonNull(content, "'content' cannot be null.");
    }
    @Override
    public Long getLength() {
        if (cachedLength == null) {
            cachedLength = content.stream().mapToLong(Buffer::remaining).sum();
        }
        return cachedLength;
    }

    @Override
    public String toString() {
        return new String(toBytes(), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytes() {
        return BYTES_UPDATER.updateAndGet(this, bytes -> bytes == null ? getBytes() : bytes);
    }

    @Override
    public <T> T toObject(TypeReference<T> typeReference, ObjectSerializer serializer) {
        return serializer.deserializeFromBytes(toBytes(), typeReference);
    }

    @Override
    public InputStream toStream() {
        return new IterableOfByteBuffersInputStream(content);
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toBytes()).asReadOnlyBuffer();
    }

    @Override
    public Flux<ByteBuffer> toFluxByteBuffer() {
        return Flux.fromIterable(content).map(ByteBuffer::asReadOnlyBuffer);
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
    public BinaryDataContentType getContentType() {
        return BinaryDataContentType.BINARY;
    }

    private byte[] getBytes() {
        long length = getLength();
        if (length > MAX_ARRAY_SIZE) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(TOO_LARGE_FOR_BYTE_ARRAY + length));
        }

        byte[] bytes = new byte[(int) length];
        int offset = 0;

        for (ByteBuffer bb : content) {
            bb = bb.duplicate();
            int count = bb.remaining();
            bb.get(bytes, offset, count);
            offset += count;
        }

        return bytes;
    }
}
