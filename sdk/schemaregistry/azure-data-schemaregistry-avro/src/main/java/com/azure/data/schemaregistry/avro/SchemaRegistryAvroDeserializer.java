// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.data.schemaregistry.AbstractDataDeserializer;
import com.azure.data.schemaregistry.SerializationException;
import com.azure.data.schemaregistry.client.CachedSchemaRegistryClient;

/**
 * A deserializer implementation capable of automatedly deserializing encoded byte array payloads into Java objects by
 * fetching payload-specified schemas from the Azure Schema Registry store.
 * <p>
 * SchemaRegistryAvroDeserializer instances should be built using the static Builder class.
 * <p>
 * Pluggable with the core Azure SDK Deserializer interface.
 *
 * @see AbstractDataDeserializer See AbstractDataDeserializer for internal deserialization implementation
 */
public class SchemaRegistryAvroDeserializer extends AbstractDataDeserializer {
    SchemaRegistryAvroDeserializer(CachedSchemaRegistryClient registryClient, boolean avroSpecificReader) {
        super(registryClient);

        loadByteDecoder(new AvroByteDecoder(avroSpecificReader));
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
        return super.deserialize(data);
    }
}
