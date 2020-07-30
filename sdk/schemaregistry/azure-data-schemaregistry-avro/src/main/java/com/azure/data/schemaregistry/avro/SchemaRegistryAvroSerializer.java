// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.data.schemaregistry.AbstractSchemaRegistrySerializer;
import com.azure.data.schemaregistry.SerializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * A serializer implementation capable of serializing objects and automatedly storing serialization schemas
 * in the Azure Schema Registry store.
 *
 * SchemaRegistryAvroSerializer instances should be built using the static Builder class.
 *
 * Pluggable with the core Azure SDK Serializer interface.
 *
 * @see AbstractSchemaRegistrySerializer See AbstractSchemaRegistrySerializer for internal serialization implementation
 */
public class SchemaRegistryAvroSerializer {
    private final SchemaRegistryAvroAsyncSerializer serializer;
    SchemaRegistryAvroSerializer(SchemaRegistryAvroAsyncSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * Serializes object into byte array payload using the configured byte encoder.
     * @param object target of serialization
     * @return byte array containing unique ID reference to schema, then the object serialized into bytes
     * @throws SerializationException Throws on serialization failure.
     */
    public byte[] serialize(Object object) throws SerializationException {
        if (object == null) {
            return null;
        }

        ByteArrayOutputStream s = serializer.serialize(new ByteArrayOutputStream(), object).block();
        if (s != null){
            s.toByteArray();
        }

        throw new SerializationException("Serialization failed, null output stream returned.");
    }

    /**
     * Deserializes byte array into Java object using payload-specified schema.
     *
     * @param data Byte array containing serialized bytes
     * @return decoded Java object
     *
     * @throws SerializationException Throws on deserialization failure.
     * Exception may contain inner exceptions detailing failure condition.
     */
    public Object deserialize(byte[] data) throws SerializationException {
        return serializer.deserialize(new ByteArrayInputStream(data), Object.class);
    }
}

