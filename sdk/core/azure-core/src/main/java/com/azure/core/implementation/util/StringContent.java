// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.implementation.ImplUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
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

import static com.azure.core.util.FluxUtil.monoError;

/**
 * A {@link BinaryDataContent} implementation which is backed by a {@code String}.
 */
public final class StringContent extends BinaryDataContent {
    private static final ClientLogger LOGGER = new ClientLogger(StringContent.class);

    private final String content;

    private volatile byte[] bytes;
    private static final AtomicReferenceFieldUpdater<StringContent, byte[]> BYTES_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(StringContent.class, byte[].class, "bytes");

    /**
     * Creates a new instance of {@link StringContent}.
     * @param content The string content.
     * @throws NullPointerException if {@code content} is null.
     */
    public StringContent(String content) {
        this.content = Objects.requireNonNull(content, "'content' cannot be null.");
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
        return ByteBuffer.wrap(toBytes()).asReadOnlyBuffer();
    }

    @Override
    public Flux<ByteBuffer> toFluxByteBuffer() {
        return Mono.fromSupplier(() -> ByteBuffer.wrap(toBytes())).flux();
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        Objects.requireNonNull(outputStream, "'outputStream' cannot be null");

        outputStream.write(toBytes());
    }

    @Override
    public void writeTo(WritableByteChannel channel) throws IOException {
        Objects.requireNonNull(channel, "'channel' cannot be null");

        ImplUtils.fullyWriteBuffer(toByteBuffer(), channel);
    }

    @Override
    public Mono<Void> writeTo(AsynchronousByteChannel channel) {
        if (channel == null) {
            return monoError(LOGGER, new NullPointerException("'channel' cannot be null"));
        }

        return FluxUtil.writeToAsynchronousByteChannel(toFluxByteBuffer(), channel);
    }

    @Override
    public void writeTo(JsonWriter jsonWriter) throws IOException {
        Objects.requireNonNull(jsonWriter, "'jsonWriter' cannot be null");

        jsonWriter.writeString(toString());
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
        return BinaryDataContentType.TEXT;
    }

    private byte[] getBytes() {
        return this.content.getBytes(StandardCharsets.UTF_8);
    }
}
