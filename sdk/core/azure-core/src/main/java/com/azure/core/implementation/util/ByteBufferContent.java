// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.implementation.ImplUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.azure.json.JsonWriter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * A {@link BinaryDataContent} implementation which is backed by a {@link ByteBuffer}.
 */
public final class ByteBufferContent extends BinaryDataContent {
    private final ByteBuffer content;

    private volatile byte[] bytes;
    private static final AtomicReferenceFieldUpdater<ByteBufferContent, byte[]> BYTES_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(ByteBufferContent.class, byte[].class, "bytes");

    /**
     * Creates a new instance of {@link BinaryDataContent}.
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
        return new ByteArrayInputStream(toBytes());
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return content.asReadOnlyBuffer();
    }

    @Override
    public Flux<ByteBuffer> toFluxByteBuffer() {
        return Flux.just(content).map(ByteBuffer::asReadOnlyBuffer);
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        Objects.requireNonNull(outputStream, "'outputStream' cannot be null");

        ImplUtils.writeByteBufferToStream(toByteBuffer(), outputStream);
    }

    @Override
    public void writeTo(WritableByteChannel channel) throws IOException {
        Objects.requireNonNull(channel, "'channel' cannot be null");

        ImplUtils.fullyWriteBuffer(toByteBuffer(), channel);
    }

    @Override
    public Mono<Void> writeTo(AsynchronousByteChannel channel) {
        if (channel == null) {
            return Mono.error(new NullPointerException("'channel' cannot be null"));
        }

        return FluxUtil.writeToAsynchronousByteChannel(toFluxByteBuffer(), channel);
    }

    @Override
    public void writeTo(JsonWriter jsonWriter) throws IOException {
        Objects.requireNonNull(jsonWriter, "'jsonWriter' cannot be null");

        jsonWriter.writeBinary(toBytes());
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
        byte[] bytes = new byte[content.remaining()];

        content.mark();
        content.get(bytes);
        content.flip();

        return bytes;
    }
}
