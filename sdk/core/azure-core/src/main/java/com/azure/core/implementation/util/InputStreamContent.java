// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

/**
 * A {@link BinaryDataContent} implementation which is backed by an {@link InputStream}.
 */
public final class InputStreamContent extends BinaryDataContent {
    private static final ClientLogger LOGGER = new ClientLogger(InputStreamContent.class);
    private static final int INITIAL_BUFFER_CHUNK_SIZE = 8 * 1024;
    private static final int MAX_BUFFER_CHUNK_SIZE = 8 * 1024 * 1024;
    private static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;
    private final Supplier<InputStream> content;
    private final Long length;
    private final boolean isReplayable;

    private volatile byte[] bytes;
    private static final AtomicReferenceFieldUpdater<InputStreamContent, byte[]> BYTES_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(InputStreamContent.class, byte[].class, "bytes");


    /**
     * Creates an instance of {@link InputStreamContent}.
     *
     * @param inputStream The inputStream that is used as the content for this instance.
     * @throws NullPointerException if {@code content} is null.
     */
    public InputStreamContent(InputStream inputStream, Long length) {
        Objects.requireNonNull(inputStream, "'inputStream' cannot be null.");
        this.content = () -> inputStream;
        this.length = length;
        isReplayable = false;
    }

    private InputStreamContent(Supplier<InputStream> inputStreamSupplier, Long length, boolean isReplayable) {
        this.content = Objects.requireNonNull(inputStreamSupplier, "'inputStreamSupplier' cannot be null.");
        this.length = length;
        this.isReplayable = isReplayable;
    }

    @Override
    public Long getLength() {
        byte[] data = BYTES_UPDATER.get(this);
        if (data != null) {
            return (long) data.length;
        }
        return length;
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
        return this.content.get();
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toBytes()).asReadOnlyBuffer();
    }

    @Override
    public Flux<ByteBuffer> toFluxByteBuffer() {
        return FluxUtil.toFluxByteBuffer(this.content.get(), STREAM_READ_SIZE);
    }

    @Override
    public boolean isReplayable() {
        return isReplayable;
    }

    @Override
    public BinaryDataContent toReplayableContent() {
        if (isReplayable) {
            return this;
        }

        InputStream inputStream = this.content.get();
        if (canMarkReset(inputStream, length)) {
            return createMarkResetContent(inputStream, length);
        } else {
            return readAndBuffer(inputStream, length);
        }
    }

    @Override
    public Mono<BinaryDataContent> toReplayableContentAsync() {
        if (isReplayable) {
            return Mono.just(this);
        }

        InputStream inputStream = this.content.get();
        if (canMarkReset(inputStream, length)) {
            return Mono.fromCallable(() -> createMarkResetContent(inputStream, length));
        }

        return Mono.just(inputStream)
            .publishOn(Schedulers.boundedElastic()) // reading stream can be blocking.
            .map(is -> readAndBuffer(is, length));
    }

    private static boolean canMarkReset(InputStream inputStream, Long length) {
        return length != null && length < MAX_ARRAY_LENGTH && inputStream.markSupported();
    }

    private static InputStreamContent createMarkResetContent(InputStream inputStream, Long length) {
        inputStream.mark(length.intValue());
        return new InputStreamContent(
            () -> {
                try {
                    inputStream.reset();
                    return inputStream;
                } catch (IOException e) {
                    throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
                }
            }, length, true
        );
    }

    private static InputStreamContent readAndBuffer(InputStream inputStream, Long length) {
        try {
            List<ByteBuffer> byteBuffers = StreamUtil.readStreamToListOfByteBuffers(
                inputStream, length, INITIAL_BUFFER_CHUNK_SIZE, MAX_BUFFER_CHUNK_SIZE);

            return new InputStreamContent(
                () -> new IterableOfByteBuffersInputStream(byteBuffers),
                length, true);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    private byte[] getBytes() {
        try {
            ByteArrayOutputStream dataOutputBuffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[STREAM_READ_SIZE];
            InputStream inputStream = this.content.get();
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                dataOutputBuffer.write(data, 0, nRead);
            }
            return dataOutputBuffer.toByteArray();
        } catch (IOException ex) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
        }
    }
}
