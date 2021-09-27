// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link BinaryDataContent} implementation which is backed by a {@link Flux} of {@link ByteBuffer}.
 */
public final class FluxByteBufferContent extends BinaryDataContent {

    private final Flux<ByteBuffer> content;
    private final AtomicReference<byte[]> bytes = new AtomicReference<>();
    private final Long length;

    /**
     * Creates an instance of {@link FluxByteBufferContent}.
     * @param content The content for this instance.
     * @throws NullPointerException if {@code content} is null.
     */
    public FluxByteBufferContent(Flux<ByteBuffer> content) {
        this(content, null);
    }

    /**
     * Creates an instance of {@link FluxByteBufferContent}.
     * @param content The content for this instance.
     * @param length The length of the content in bytes.
     * @throws NullPointerException if {@code content} is null.
     */
    public FluxByteBufferContent(Flux<ByteBuffer> content, Long length) {
        this.content = Objects.requireNonNull(content, "'content' cannot be null.");
        this.length = length;
    }

    @Override
    public Long getLength() {
        return length;
    }

    @Override
    public String toString() {
        return new String(toBytes(), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytes() {
        byte[] data = this.bytes.get();
        if (data == null) {
            bytes.set(getBytes());
            data = this.bytes.get();
        }
        return data;
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
        return ByteBuffer.wrap(toBytes()).asReadOnlyBuffer();
    }

    @Override
    public Flux<ByteBuffer> toFluxByteBuffer() {
        return content;
    }

    private byte[] getBytes() {
        return FluxUtil.collectBytesInByteBufferStream(content)
                // this doesn't seem to be working (newBoundedElastic() didn't work either)
                // .publishOn(Schedulers.boundedElastic())
                .share()
                .block();
    }
}
