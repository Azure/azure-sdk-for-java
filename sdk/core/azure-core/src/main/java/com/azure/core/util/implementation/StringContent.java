// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.implementation;

import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link BinaryDataContent} implementation which is backed by a {@code String}.
 */
public class StringContent extends BinaryDataContent {
    private String content;
    private final AtomicReference<byte[]> bytes = new AtomicReference<>();

    /**
     * Creates a new instance of {@link StringContent}.
     * @param content The string content.
     */
    public StringContent(String content) {
        if (content == null) {
            // this is to maintain backward compatibility with BinaryData
            bytes.set(ZERO_BYTE_ARRAY);
            this.content = "";
            return;
        }
        this.content = content;
    }

    @Override
    public Long getLength() {
        return (long) toBytes().length;
    }

    @Override
    public String toString() {
        return this.content;
    }

    @Override
    public byte[] toBytes() {
        bytes.compareAndSet(null, getBytes());
        byte[] data = this.bytes.get();
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public <T> T toObject(TypeReference<T> typeReference, ObjectSerializer serializer) {
        Objects.requireNonNull(typeReference, "'typeReference' cannot be null.");
        Objects.requireNonNull(serializer, "'serializer' cannot be null.");
        return serializer.deserializeFromBytes(toBytes(), typeReference);
    }

    @Override
    public InputStream toStream() {
        return new ByteArrayInputStream(toBytes());
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toBytes()).asReadOnlyBuffer();
    }

    @Override
    public Flux<ByteBuffer> toFluxByteBuffer() {
        return Flux.defer(() -> Flux.just(ByteBuffer.wrap(toBytes())));
    }

    private byte[] getBytes() {
        return this.content.getBytes(StandardCharsets.UTF_8);
    }
}
