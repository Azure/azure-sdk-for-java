// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.binarydata;

import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.serializer.ObjectSerializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * A {@link BinaryData} implementation backed by a serializable object.
 */
public final class SerializableBinaryData extends BinaryData {
    private static final ClientLogger LOGGER = new ClientLogger(SerializableBinaryData.class);

    private final Object content;
    private final ObjectSerializer serializer;

    private volatile byte[] bytes;
    private static final AtomicReferenceFieldUpdater<SerializableBinaryData, byte[]> BYTES_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(SerializableBinaryData.class, byte[].class, "bytes");

    /**
     * Creates a new instance of {@link SerializableBinaryData}.
     *
     * @param content The serializable object that forms the content of this instance.
     * @param serializer The serializer that serializes the {@code content}.
     *
     * @throws NullPointerException if {@code serializer} is null.
     */
    public SerializableBinaryData(Object content, ObjectSerializer serializer) {
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
    public <T> T toObject(Type type, ObjectSerializer serializer) throws IOException {
        return serializer.deserializeFromBytes(toBytes(), type);
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
    public boolean isReplayable() {
        return true;
    }

    @Override
    public BinaryData toReplayableBinaryData() {
        return this;
    }

    private byte[] getBytes() {
        try {
            return serializer.serializeToBytes(content);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException("Failed to serialize the content.", e));
        }
    }

    @Override
    public void close() throws IOException {
        // no-op
    }
}
