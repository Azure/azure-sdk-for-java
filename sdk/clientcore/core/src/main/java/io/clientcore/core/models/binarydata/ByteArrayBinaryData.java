// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models.binarydata;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.serialization.json.JsonWriter;
import io.clientcore.core.serialization.ObjectSerializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A {@link BinaryData} implementation backed by a byte array.
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class ByteArrayBinaryData extends BinaryData {
    private static final ClientLogger LOGGER = new ClientLogger(ByteArrayBinaryData.class);
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
    public <T> T toObject(Type type, ObjectSerializer serializer) {
        try {
            return serializer.deserializeFromBytes(this.content, type);
        } catch (IOException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }
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

    @Override
    public void close() {
        // no-op
    }
}
