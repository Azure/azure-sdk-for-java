// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.models;

import com.generic.core.implementation.AccessibleByteArrayOutputStream;
import com.generic.core.implementation.util.IterableOfByteBuffersInputStream;
import com.generic.core.implementation.util.StreamUtil;
import com.generic.core.util.ClientLogger;
import com.generic.core.util.serializer.ObjectSerializer;

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
 * A {@link BinaryData} implementation backed by an {@link InputStream}.
 */
public final class InputStreamBinaryData extends BinaryData {
    private static final ClientLogger LOGGER = new ClientLogger(InputStreamBinaryData.class);
    private static final int INITIAL_BUFFER_CHUNK_SIZE = 8 * 1024;
    private static final int MAX_BUFFER_CHUNK_SIZE = 8 * 1024 * 1024;
    private static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;
    private final Supplier<InputStream> content;
    private final Long length;
    private final boolean isReplayable;

    private volatile byte[] bytes;
    private static final AtomicReferenceFieldUpdater<InputStreamBinaryData, byte[]> BYTES_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(InputStreamBinaryData.class, byte[].class, "bytes");

    /**
     * Creates an instance of {@link InputStreamBinaryData}.
     *
     * @param inputStream The inputStream that is used as the content for this instance.
     * @throws NullPointerException if {@code content} is null.
     */
    public InputStreamBinaryData(InputStream inputStream, Long length) {
        Objects.requireNonNull(inputStream, "'inputStream' cannot be null.");
        this.length = length;
        this.isReplayable = canMarkReset(inputStream, length);
        if (this.isReplayable) {
            inputStream.mark(length.intValue());
            this.content = () -> resettableContent(inputStream);
        } else {
            this.content = () -> inputStream;
        }
    }

    private InputStreamBinaryData(Supplier<InputStream> inputStreamSupplier, Long length, boolean isReplayable) {
        this.content = Objects.requireNonNull(inputStreamSupplier, "'inputStreamSupplier' cannot be null.");
        this.length = length;
        this.isReplayable = isReplayable;
    }

    @Override
    public byte[] toBytes() {
        if (length != null && length > MAX_ARRAY_SIZE) {
            throw LOGGER.logThrowableAsError(new IllegalStateException(TOO_LARGE_FOR_BYTE_ARRAY + length));
        }

        return BYTES_UPDATER.updateAndGet(this, bytes -> bytes == null ? getBytes() : bytes);
    }

    @Override
    public String toString() {
        return new String(toBytes(), StandardCharsets.UTF_8);
    }

    @Override
    public <T> T toObject(TypeReference<T> typeReference, ObjectSerializer serializer) {
        return serializer.deserializeFromBytes(toBytes(), typeReference);
    }

    @Override
    public InputStream toStream() {
        return content.get();
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toBytes()).asReadOnlyBuffer();
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
    public boolean isReplayable() {
        return isReplayable;
    }

    @Override
    public BinaryData toReplayableBinaryData() {
        if (isReplayable) {
            return this;
        }

        return readAndBuffer(this.content.get(), length);
    }

    private static boolean canMarkReset(InputStream inputStream, Long length) {
        return length != null && length < MAX_ARRAY_LENGTH && inputStream.markSupported();
    }

    private static InputStream resettableContent(InputStream stream) {
        try {
            stream.reset();
            return stream;
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    private static InputStreamBinaryData readAndBuffer(InputStream inputStream, Long length) {
        try {
            List<ByteBuffer> byteBuffers = StreamUtil.readStreamToListOfByteBuffers(
                inputStream, length, INITIAL_BUFFER_CHUNK_SIZE, MAX_BUFFER_CHUNK_SIZE);

            return new InputStreamBinaryData(() -> new IterableOfByteBuffersInputStream(byteBuffers),
                length, true);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    private byte[] getBytes() {
        try {
            AccessibleByteArrayOutputStream dataOutputBuffer = new AccessibleByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[STREAM_READ_SIZE];
            InputStream inputStream = this.content.get();
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                dataOutputBuffer.write(data, 0, nRead);
            }
            return dataOutputBuffer.toByteArrayUnsafe();
        } catch (IOException ex) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(ex));
        }
    }
}
