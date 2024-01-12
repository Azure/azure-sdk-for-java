package com.generic.core.models;

import com.generic.core.util.serializer.ObjectSerializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * A {@link BinaryData} implementation backed by a serializable object.
 */
public final class SerializableBinaryData extends BinaryData {
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
    public boolean isReplayable() {
        return true;
    }

    @Override
    public BinaryData toReplayableBinaryData() {
        return this;
    }

    @Override
    public void close() throws IOException {
        if (content instanceof AutoCloseable) {
            try {
                ((AutoCloseable) content).close();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    private byte[] getBytes() {
        return serializer.serializeToBytes(content);
    }
}
