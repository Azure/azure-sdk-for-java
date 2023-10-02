// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.util;

import com.typespec.core.util.serializer.ObjectSerializer;
import com.typespec.core.util.serializer.TypeReference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * A {@link BinaryDataContent} implementation which is backed by a serializable object.
 */
public final class SerializableContent extends BinaryDataContent {

    private final Object content;
    private final ObjectSerializer serializer;

    private volatile byte[] bytes;
    private static final AtomicReferenceFieldUpdater<SerializableContent, byte[]> BYTES_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(SerializableContent.class, byte[].class, "bytes");

    /**
     * Creates a new instance of {@link SerializableContent}.
     * @param content The serializable object that forms the content of this instance.
     * @param serializer The serializer that serializes the {@code content}.
     * @throws NullPointerException if {@code serializer} is null.
     */
    public SerializableContent(Object content, ObjectSerializer serializer) {
        this.content = content;
        this.serializer = Objects.requireNonNull(serializer, "'serializer' cannot be null.");
    }

    @Override
    public Long getLength() {
        return this.content == null ? null : (long) toBytes().length;
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
        return new ByteArrayInputStream(getBytes());
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toBytes()).asReadOnlyBuffer();
    }

    @Override
    public Flux<ByteBuffer> toFluxByteBuffer() {
        return Mono.fromSupplier(() -> ByteBuffer.wrap(toBytes())).flux();
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
        return BinaryDataContentType.OBJECT;
    }

    private byte[] getBytes() {
        return serializer.serializeToBytes(content);
    }
}
