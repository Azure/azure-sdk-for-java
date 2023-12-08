package com.generic.core.models;

import com.generic.core.util.serializer.ObjectSerializer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A {@link BinaryData} implementation backed by a byte array.
 */
public final class ByteArrayBinaryData extends BinaryData {
    private final byte[] content;

    /**
     * Creates a new instance of {@link ByteArrayBinaryData}.
     *
     * @param content The byte array content.
     * @throws NullPointerException if {@code content} is null.
     */
    public ByteArrayBinaryData(byte[] content) {
        this.content = Objects.requireNonNull(content, "'content' cannot be null");
    }

    @Override
    public Long getLength() {
        return (long) this.content.length;
    }

    @Override
    public String toString() {
        return new String(content, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytes() {
        return content;
    }

    @Override
    public <T> T toObject(TypeReference<T> typeReference, ObjectSerializer serializer) {
        return serializer.deserializeFromBytes(this.content, typeReference);
    }

    @Override
    public InputStream toStream() {
        return new ByteArrayInputStream(this.content);
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(this.content).asReadOnlyBuffer();
    }

    @Override
    public boolean isReplayable() {
        return true;
    }

    @Override
    public BinaryData toReplayableBinaryData() {
        return this;
    }
}
