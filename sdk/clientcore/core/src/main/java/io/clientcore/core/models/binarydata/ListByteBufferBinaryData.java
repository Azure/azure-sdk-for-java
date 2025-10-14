// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models.binarydata;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.implementation.utils.ImplUtils;
import io.clientcore.core.implementation.utils.IterableOfByteBuffersInputStream;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.serialization.json.JsonWriter;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.ObjectSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * A {@link BinaryData} implementation backed by a {@link List} of {@link ByteBuffer}.
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
final class ListByteBufferBinaryData extends BinaryData {
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
    ListByteBufferBinaryData(List<ByteBuffer> content) {
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
            throw LOGGER.throwableAtError().log(TOO_LARGE_FOR_BYTE_ARRAY + getLength(), IllegalStateException::new);
        }

        return BYTES_UPDATER.updateAndGet(this, bytes -> bytes == null ? getBytes() : bytes);
    }

    @Override
    public <T> T toObject(Type type, ObjectSerializer serializer) {
        try {
            return serializer.deserializeFromBytes(toBytes(), type);
        } catch (IOException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }
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
    public void writeTo(OutputStream outputStream) {
        try {
            for (ByteBuffer bb : content) {
                ImplUtils.writeByteBufferToStream(bb, outputStream);
            }
        } catch (IOException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }
    }

    @Override
    public void writeTo(WritableByteChannel channel) {
        try {
            for (ByteBuffer bb : content) {
                bb = bb.duplicate();
                while (bb.hasRemaining()) {
                    channel.write(bb);
                }
            }
        } catch (IOException exception) {
            throw LOGGER.throwableAtError().log(exception, CoreException::from);
        }
    }

    @Override
    public void writeTo(JsonWriter jsonWriter) {
        Objects.requireNonNull(jsonWriter, "'jsonWriter' cannot be null");

        try {
            jsonWriter.writeBinary(toBytes());
        } catch (IOException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }
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
            throw LOGGER.throwableAtError().log(TOO_LARGE_FOR_BYTE_ARRAY + length, IllegalStateException::new);
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

    @Override
    public void close() {
        // no-op
    }
}
