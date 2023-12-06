package com.generic.core.models;

import com.generic.core.implementation.util.IterableOfByteBuffersInputStream;
import com.generic.core.util.logging.ClientLogger;
import com.generic.core.util.serializer.ObjectSerializer;

import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * A {@link BinaryData} implementation backed by a {@link List} of {@link ByteBuffer}.
 */
public final class ListByteBufferBinaryData extends BinaryData {
    private static final ClientLogger LOGGER = new ClientLogger(ListByteBufferBinaryData.class);

    private final List<ByteBuffer> content;

    private volatile byte[] bytes;
    private static final AtomicReferenceFieldUpdater<ListByteBufferBinaryData, byte[]> BYTES_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(ListByteBufferBinaryData.class, byte[].class, "bytes");

    private Long cachedLength;

    /**
     * Creates a new instance of {@link ListByteBufferBinaryData}.
     *
     * @param content The {@link List} of {@link ByteBuffer} content.
     * @throws NullPointerException If {@code content} is null.
     */
    public ListByteBufferBinaryData(List<ByteBuffer> content) {
        this.content = Objects.requireNonNull(content, "'content' cannot be null.");
    }
    @Override
    public Long getLength() {
        if (cachedLength == null) {
            cachedLength = content.stream().mapToLong(Buffer::remaining).sum();
        }
        return cachedLength;
    }

    @Override
    public String toString() {
        return new String(toBytes(), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytes() {
        if (getLength() > MAX_ARRAY_SIZE) {
            throw LOGGER.logThrowableAsError(new IllegalStateException(TOO_LARGE_FOR_BYTE_ARRAY + getLength()));
        }

        return BYTES_UPDATER.updateAndGet(this, bytes -> bytes == null ? getBytes() : bytes);
    }

    @Override
    public <T> T toObject(TypeReference<T> typeReference, ObjectSerializer serializer) {
        return serializer.deserializeFromBytes(toBytes(), typeReference);
    }

    @Override
    public InputStream toStream() {
        return new IterableOfByteBuffersInputStream(content);
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
        long length = getLength();
        if (length > MAX_ARRAY_SIZE) {
            throw LOGGER.logThrowableAsError(new IllegalStateException(TOO_LARGE_FOR_BYTE_ARRAY + length));
        }

        byte[] bytes = new byte[(int) length];
        int offset = 0;

        for (ByteBuffer bb : content) {
            bb = bb.duplicate();
            int count = bb.remaining();
            bb.get(bytes, offset, count);
            offset += count;
        }

        return bytes;
    }
}
