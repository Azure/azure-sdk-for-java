/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry.avro;

import com.azure.schemaregistry.AbstractDataSerializer;
import com.azure.schemaregistry.SerializationException;
import com.azure.schemaregistry.client.CachedSchemaRegistryClient;

/**
 * A serializer implementation capable of serializing objects and automatedly storing serialization schemas
 * in the Azure Schema Registry store.
 *
 * SchemaRegistryAvroSyncSerializer instances should be built using the static Builder class.
 *
 * Pluggable with the core Azure SDK Serializer interface.
 *
 * @see AbstractDataSerializer See AbstractDataSerializer for internal serialization implementation
 * @see SchemaRegistryAvroAsyncSerializer.Builder See Builder for documentation on required parameters
 */
public class SchemaRegistryAvroAsyncSerializer extends AbstractDataSerializer {
    private SchemaRegistryAvroAsyncSerializer(Builder builder) {
        super(new CachedSchemaRegistryClient.Builder(builder.registryUrl)
                .maxSchemaMapSize(builder.maxSchemaMapSize)
                .build());

        setByteEncoder(new AvroByteEncoder.Builder().build());
        this.serializationFormat = this.byteEncoder.serializationFormat();
        this.autoRegisterSchemas = builder.autoRegisterSchemas;
        this.schemaGroup = builder.schemaGroup;
    }

    /**
     * Serializes object into byte array payload using the configured byte encoder.
     * @param object target of serialization
     * @return byte array containing GUID reference to schema, then the object serialized into bytes
     * @throws SerializationException Throws on serialization failure.
     * Exception may contain inner exceptions detailing failure condition.
     */
    public byte[] serializeSync(Object object) throws SerializationException {
        if (object == null) {
            return null;
        }
        return serializeImpl(object);
    }


}

