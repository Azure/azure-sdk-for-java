package com.generic.core.models;

import com.generic.core.implementation.util.ImplUtils;
import com.generic.core.util.serializer.ObjectSerializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * A {@link BinaryData} implementation backed by a {@link ByteBuffer}.
 */
public final class ByteBufferBinaryData extends BinaryData {
    private final ByteBuffer content;

    private volatile byte[] bytes;
    private static final AtomicReferenceFieldUpdater<ByteBufferBinaryData, byte[]> BYTES_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(ByteBufferBinaryData.class, byte[].class, "bytes");

    /**
     * Creates a new instance of {@link ByteBufferBinaryData}.
     *
     * @param content The {@link ByteBuffer} content.
     * @throws NullPointerException If {@code content} is null.
     */
    public ByteBufferBinaryData(ByteBuffer content) {
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
    public void writeTo(OutputStream outputStream) throws IOException {
        ByteBuffer buffer = toByteBuffer();
        ImplUtils.writeByteBufferToStream(buffer, outputStream);
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
        byte[] bytes = new byte[content.remaining()];

        content.mark();
        content.get(bytes);
        content.flip();

        return bytes;
    }
}
