// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.serializer;

import io.clientcore.core.implementation.http.serializer.CompositeSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Generic interface covering serializing and deserialization objects.
 */
public abstract class ObjectSerializer {
    /**
     * Creates an instance of {@link ObjectSerializer}.
     */
    public ObjectSerializer() {
    }

    /**
     * Creates an instance of {@link ObjectSerializer} which comprises multiple serializers.
     *
     * @param serializers The serializers.
     * @return An instance of {@link ObjectSerializer}.
     * @throws NullPointerException If {@code serializers} is null.
     * @throws IllegalArgumentException If {@code serializers} is empty.
     */
    public static ObjectSerializer compositeSerializer(List<ObjectSerializer> serializers) {
        return new CompositeSerializer(serializers);
    }

    /**
     * Reads a byte array into its object representation.
     * <p>
     * If the {@link ObjectSerializer#supportsFormat(Format)} doesn't support the provided {@link Format}, an
     * {@link UnsupportedOperationException} will be thrown.
     *
     * @param data The byte array.
     * @param type {@link Type} representing the object.
     * @param format The format to deserialize the object from.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized byte array.
     * @throws UnsupportedOperationException If the provided format is not supported.
     * @throws IOException If the deserialization fails.
     */
    public abstract <T> T deserializeFromBytes(byte[] data, Type type, Format format) throws IOException;

    /**
     * Reads a stream into its object representation.
     * <p>
     * If the {@link ObjectSerializer#supportsFormat(Format)} doesn't support the provided {@link Format}, an
     * {@link UnsupportedOperationException} will be thrown.
     *
     * @param stream {@link InputStream} of data.
     * @param type {@link Type} representing the object.
     * @param format The format to deserialize the object from.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized stream.
     * @throws UnsupportedOperationException If the provided format is not supported.
     * @throws IOException If the deserialization fails.
     */
    public abstract <T> T deserializeFromStream(InputStream stream, Type type, Format format) throws IOException;

    /**
     * Serializes an object into a byte array.
     * <p>
     * If the {@link ObjectSerializer#supportsFormat(Format)} doesn't support the provided {@link Format}, an
     * {@link UnsupportedOperationException} will be thrown.
     *
     * @param value The object to serialize.
     * @param format The format to serialize the object in.
     * @return The binary representation of the serialized object.
     * @throws UnsupportedOperationException If the provided format is not supported.
     * @throws IOException If the serialization fails.
     */
    public abstract byte[] serializeToBytes(Object value, Format format) throws IOException;

    /**
     * Serializes and writes an object into a provided stream.
     * <p>
     * If the {@link ObjectSerializer#supportsFormat(Format)} doesn't support the provided {@link Format}, an
     * {@link UnsupportedOperationException} will be thrown.
     *
     * @param stream {@link OutputStream} where the serialized object will be written.
     * @param value The object to serialize.
     * @param format The format to serialize the object in.
     * @throws UnsupportedOperationException If the provided format is not supported.
     * @throws IOException If the serialization fails.
     */
    public abstract void serializeToStream(OutputStream stream, Object value, Format format) throws IOException;

    /**
     * Indicates whether the given implementation of {@link ObjectSerializer} supports the provided format.
     * <p>
     * An implementation of {@link ObjectSerializer} may support multiple formats, such as JSON and XML.
     * <p>
     * A check for support should be made before attempting to serialize or deserialize an object.
     *
     * @param format The format to check support for.
     * @return Whether the format is supported.
     */
    public abstract boolean supportsFormat(Format format);

    /**
     * Formats supported by {@link ObjectSerializer}, and its subclasses.
     */
    public enum Format {
        /**
         * JSON format.
         */
        JSON,

        /**
         * Text format.
         */
        TEXT,

        /**
         * XML format.
         */
        XML
    }
}
