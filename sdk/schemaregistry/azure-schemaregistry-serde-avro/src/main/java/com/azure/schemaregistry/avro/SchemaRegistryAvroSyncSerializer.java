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
 * @see SchemaRegistryAvroSyncSerializer.Builder See Builder for documentation on required parameters
 */
public class SchemaRegistryAvroSyncSerializer extends AbstractDataSerializer {
    private SchemaRegistryAvroSyncSerializer(Builder builder) {
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

    public static class Builder {
        private final String registryUrl;
        private boolean autoRegisterSchemas;
        private String credential;
        private int maxSchemaMapSize;
        private String schemaGroup;

        public Builder(String registryUrl) {
            this.registryUrl = registryUrl;
            this.credential = null;
            this.autoRegisterSchemas = AbstractDataSerializer.AUTO_REGISTER_SCHEMAS_DEFAULT;
            this.schemaGroup = AbstractDataSerializer.SCHEMA_GROUP_DEFAULT;
            this.maxSchemaMapSize = CachedSchemaRegistryClient.MAX_SCHEMA_MAP_SIZE_DEFAULT;
        }

        public SchemaRegistryAvroSyncSerializer build() {
            return new SchemaRegistryAvroSyncSerializer(this);
        }

        public Builder schemaGroup(String schemaGroup) {
            this.schemaGroup = schemaGroup;
            return this;
        }

        public Builder credential(String credential) {
            this.credential = credential;
            return this;
        }

        public Builder autoRegisterSchema(boolean autoRegisterSchemas) {
            this.autoRegisterSchemas = autoRegisterSchemas;
            return this;
        }

        public Builder maxSchemaMapSize(int maxSchemaMapSize) {
            this.maxSchemaMapSize = maxSchemaMapSize;
            return this;
        }
    }
}

