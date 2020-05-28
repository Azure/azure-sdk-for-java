// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.credential.TokenCredential;
import com.azure.data.schemaregistry.AbstractDataSerializer;
import com.azure.data.schemaregistry.SerializationException;
import com.azure.data.schemaregistry.client.CachedSchemaRegistryClientBuilder;

/**
 * A serializer implementation capable of serializing objects and automatedly storing serialization schemas
 * in the Azure Schema Registry store.
 *
 * SchemaRegistryAvroSerializer instances should be built using the static Builder class.
 *
 * Pluggable with the core Azure SDK Serializer interface.
 *
 * @see AbstractDataSerializer See AbstractDataSerializer for internal serialization implementation
 */
public class SchemaRegistryAvroSerializer extends AbstractDataSerializer {
    SchemaRegistryAvroSerializer(String registryUrl,
                                 TokenCredential credential,
                                 String schemaGroup,
                                 boolean autoRegisterSchemas,
                                 Integer maxSchemaMapSize) {
        super(new CachedSchemaRegistryClientBuilder()
                .endpoint(registryUrl)
                .credential(credential)
                .maxSchemaMapSize(maxSchemaMapSize)
                .buildClient());

        setByteEncoder(new AvroByteEncoder());

        this.autoRegisterSchemas = autoRegisterSchemas;
        this.schemaGroup = schemaGroup;
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

