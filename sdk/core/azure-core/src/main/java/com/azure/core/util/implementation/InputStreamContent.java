// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.implementation;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link BinaryDataContent} implementation which is backed by an {@link InputStream}.
 */
public final class InputStreamContent extends BinaryDataContent {
    private static final ClientLogger LOGGER = new ClientLogger(InputStreamContent.class);
    private final InputStream content;
    private final AtomicReference<byte[]> bytes = new AtomicReference<>();


    /**
     * Creates an instance of {@link InputStreamContent}.
     *
     * @param inputStream The inputStream that is used as the content for this instance.
     * @throws NullPointerException if {@code content} is null.
     */
    public InputStreamContent(InputStream inputStream) {
        this.content = Objects.requireNonNull(inputStream, "'inputStream' cannot be null.");
    }

    @Override
    public Long getLength() {
        if (bytes.get() != null) {
            return (long) bytes.get().length;
        }
        return null;
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
        return this.content;
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toBytes()).asReadOnlyBuffer();
    }

    @Override
    public Flux<ByteBuffer> toFluxByteBuffer() {
        return FluxUtil.toFluxByteBuffer(this.content, STREAM_READ_SIZE);
    }

    private byte[] getBytes() {
        try {
            ByteArrayOutputStream dataOutputBuffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[STREAM_READ_SIZE];
            while ((nRead = this.content.read(data, 0, data.length)) != -1) {
                dataOutputBuffer.write(data, 0, nRead);
            }
            return dataOutputBuffer.toByteArray();
        } catch (IOException ex) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
        }
    }
}
