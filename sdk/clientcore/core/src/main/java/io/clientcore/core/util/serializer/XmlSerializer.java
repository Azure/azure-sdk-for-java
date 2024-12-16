// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * Generic interface covering basic XML serialization and deserialization methods.
 */
public abstract class XmlSerializer extends ObjectSerializer {
    /**
     * Creates an instance of the {@link XmlSerializer}.
     */
    public XmlSerializer() {
    }

    /**
     * Reads an XML byte array into its object representation.
     *
     * @param data The XML byte array.
     * @param type {@link Type} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized XML byte array.
     * @throws IOException If the deserialization fails.
     */
    @Override
    public abstract <T> T deserializeFromBytes(byte[] data, Type type) throws IOException;

    /**
     * Reads an XML stream into its object representation.
     *
     * @param stream XML stream.
     * @param type {@link Type} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized XML stream.
     * @throws IOException If the deserialization fails.
     */
    @Override
    public abstract <T> T deserializeFromStream(InputStream stream, Type type) throws IOException;

    /**
     * Converts the object into an XML byte array.
     *
     * @param value The object.
     * @return The XML binary representation of the serialized object.
     * @throws IOException If the serialization fails.
     */
    @Override
    public abstract byte[] serializeToBytes(Object value) throws IOException;

    /**
     * Writes an object's XML representation into a stream.
     *
     * @param stream {@link OutputStream} where the object's XML representation will be written.
     * @param value The object to serialize.
     * @throws IOException If the serialization fails.
     */
    @Override
    public abstract void serializeToStream(OutputStream stream, Object value) throws IOException;

    @Override
    public final boolean supportsFormat(SerializationFormat format) {
        return format == SerializationFormat.XML;
    }
}
