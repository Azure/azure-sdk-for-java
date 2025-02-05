// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.http.serializer;

import io.clientcore.core.instrumentation.logging.ClientLogger;
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
 * {@link SerializationFormat} to use for each serialization and deserialize operation.
 */
public final class CompositeSerializer {
    private static final ClientLogger LOGGER = new ClientLogger(CompositeSerializer.class);

    private final List<ObjectSerializer> serializers;

    /**
     * Creates an instance of the {@link CompositeSerializer}.
     *
     * @param serializers The list of serializers to try in order.
     * @throws NullPointerException If the list of serializers is null.
     * @throws IllegalArgumentException If the list of serializers is empty.
     */
    public CompositeSerializer(List<ObjectSerializer> serializers) {
        Objects.requireNonNull(serializers, "The list of serializers cannot be null.");
        if (serializers.isEmpty()) {
            throw new IllegalArgumentException("The list of serializers cannot be empty.");
        }

        this.serializers = new ArrayList<>(serializers);
    }

    /**
     * Deserializes the provided data into an object of the provided type.
     *
     * @param data The data to deserialize.
     * @param type The type of the object to deserialize.
     * @param format The format of the data.
     * @return The deserialized object.
     * @param <T> The type of the object to deserialize.
     * @throws UnsupportedOperationException If none of the provided serializers support the provided format.
     * @throws IOException If an error occurs during deserialization.
     */
    public <T> T deserializeFromBytes(byte[] data, Type type, SerializationFormat format) throws IOException {
        for (ObjectSerializer serializer : serializers) {
            if (serializer.supportsFormat(format)) {
                return serializer.deserializeFromBytes(data, type);
            }
        }

        throw LOGGER.logThrowableAsError(
            new UnsupportedOperationException("None of the provided serializers support the format: " + format + "."));
    }

    /**
     * Deserializes the provided stream into an object of the provided type.
     *
     * @param stream The stream to deserialize.
     * @param type The type of the object to deserialize.
     * @param format The format of the data.
     * @return The deserialized object.
     * @param <T> The type of the object to deserialize.
     * @throws UnsupportedOperationException If none of the provided serializers support the provided format.
     * @throws IOException If an error occurs during deserialization.
     */
    public <T> T deserializeFromStream(InputStream stream, Type type, SerializationFormat format) throws IOException {
        for (ObjectSerializer serializer : serializers) {
            if (serializer.supportsFormat(format)) {
                return serializer.deserializeFromStream(stream, type);
            }
        }

        throw LOGGER.logThrowableAsError(
            new UnsupportedOperationException("None of the provided serializers support the format: " + format + "."));
    }

    /**
     * Serializes the provided value into a byte array.
     *
     * @param value The value to serialize.
     * @param format The format to serialize the value in.
     * @return The serialized byte array.
     * @throws UnsupportedOperationException If none of the provided serializers support the provided format.
     * @throws IOException If an error occurs during serialization.
     */
    public byte[] serializeToBytes(Object value, SerializationFormat format) throws IOException {
        for (ObjectSerializer serializer : serializers) {
            if (serializer.supportsFormat(format)) {
                return serializer.serializeToBytes(value);
            }
        }

        throw LOGGER.logThrowableAsError(
            new UnsupportedOperationException("None of the provided serializers support the format: " + format + "."));
    }

    /**
     * Serializes the provided value into the given stream.
     *
     * @param stream The stream to serialize the value to.
     * @param value The value to serialize.
     * @param format The format to serialize the value in.
     * @throws UnsupportedOperationException If none of the provided serializers support the provided format.
     * @throws IOException If an error occurs during serialization.
     */
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

    /**
     * Gets the first serializer that supports the provided format.
     *
     * @param format The format to get the serializer for.
     * @return The serializer that supports the provided format.
     * @throws UnsupportedOperationException If none of the provided serializers support the provided format.
     */
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
