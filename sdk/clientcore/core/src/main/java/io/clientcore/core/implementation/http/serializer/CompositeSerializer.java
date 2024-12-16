// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.http.serializer;

import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.serializer.ObjectSerializer;
import io.clientcore.core.util.serializer.SerializationFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An internal type that comprises multiple {@link ObjectSerializer}s and adds functionality to determine which
 * {@link SerializationFormat} to use each serialization and deserialize operation.
 */
public final class CompositeSerializer {
    private static final ClientLogger LOGGER = new ClientLogger(CompositeSerializer.class);

    private final List<ObjectSerializer> serializers;

    /**
     * Creates an instance of the {@link CompositeSerializer}.
     *
     * @param serializers The list of serializers to try in order.
     */
    public CompositeSerializer(List<ObjectSerializer> serializers) {
        Objects.requireNonNull(serializers, "The list of serializers cannot be null.");
        if (serializers.isEmpty()) {
            throw new IllegalArgumentException("The list of serializers cannot be empty.");
        }

        this.serializers = new ArrayList<>(serializers);
    }

    public <T> T deserializeFromBytes(byte[] data, Type type, SerializationFormat format) throws IOException {
        for (ObjectSerializer serializer : serializers) {
            if (serializer.supportsFormat(format)) {
                return serializer.deserializeFromBytes(data, type);
            }
        }

        throw LOGGER.logThrowableAsError(
            new UnsupportedOperationException("None of the provided serializers support the format: " + format + "."));
    }

    public <T> T deserializeFromStream(InputStream stream, Type type, SerializationFormat format) throws IOException {
        for (ObjectSerializer serializer : serializers) {
            if (serializer.supportsFormat(format)) {
                return serializer.deserializeFromStream(stream, type);
            }
        }

        throw LOGGER.logThrowableAsError(
            new UnsupportedOperationException("None of the provided serializers support the format: " + format + "."));
    }

    public byte[] serializeToBytes(Object value, SerializationFormat format) throws IOException {
        for (ObjectSerializer serializer : serializers) {
            if (serializer.supportsFormat(format)) {
                return serializer.serializeToBytes(value);
            }
        }

        throw LOGGER.logThrowableAsError(
            new UnsupportedOperationException("None of the provided serializers support the format: " + format + "."));
    }

    public void serializeToStream(OutputStream stream, Object value, SerializationFormat format) throws IOException {
        for (ObjectSerializer serializer : serializers) {
            if (serializer.supportsFormat(format)) {
                serializer.serializeToStream(stream, value);
                return;
            }
        }

        throw LOGGER.logThrowableAsError(
            new UnsupportedOperationException("None of the provided serializers support the format: " + format + "."));
    }

    public ObjectSerializer getSerializerForFormat(SerializationFormat format) {
        for (ObjectSerializer serializer : serializers) {
            if (serializer.supportsFormat(format)) {
                return serializer;
            }
        }

        throw LOGGER.logThrowableAsError(
            new UnsupportedOperationException("None of the provided serializers support the format: " + format + "."));
    }
}
