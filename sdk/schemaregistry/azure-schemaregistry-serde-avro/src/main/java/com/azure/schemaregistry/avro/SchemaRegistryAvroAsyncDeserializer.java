/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry.avro;

import com.azure.schemaregistry.AbstractDataDeserializer;
import com.azure.schemaregistry.SerializationException;
import com.azure.schemaregistry.client.CachedSchemaRegistryClient;

/**
 * A deserializer implementation capable of automatedly deserializing encoded byte array payloads into Java objects by
 * fetching payload-specified schemas from the Azure Schema Registry store.
 *
 * SchemaRegistryAvroSyncDeserializer instances should be built using the static Builder class.
 *
 * Pluggable with the core Azure SDK Deserializer interface.
 *
 * @see AbstractDataDeserializer See AbstractDataDeserializer for internal deserialization implementation
 * @see SchemaRegistryAvroAsyncDeserializer.Builder See Builder for documentation on builder parameters
 */
public class SchemaRegistryAvroAsyncDeserializer extends AbstractDataDeserializer {
    private SchemaRegistryAvroAsyncDeserializer(Builder builder) {
        super(new CachedSchemaRegistryClient.Builder(builder.registryUrl)
                .credential()
                .maxSchemaMapSize(builder.maxSchemaMapSize)
                .build());

        loadByteDecoder(new AvroByteDecoder.Builder()
            .avroSpecificReader(builder.avroSpecificReader)
            .build());
    }

    /**
     * Deserializes byte array into Java object using payload-specified schema.
     *
     * @param data Byte array containing serialized bytes
     * @return Java object.
     *
     * Object type is testable with instanceof operator.  Return value can be casted in the caller layer.
     *
     * @throws SerializationException Throws on deserialization failure.
     * Exception may contain inner exceptions detailing failure condition.
     */
    public Object deserializeSync(byte[] data) throws SerializationException {
        return super.deserialize(data);

    }

    public static class Builder {
        private final String registryUrl;
        private int maxSchemaMapSize;
        private String credential;
        private boolean avroSpecificReader;

        public Builder(String registryUrl) {
            this.registryUrl = registryUrl;
            this.credential = null;
            this.avroSpecificReader = false;
            this.maxSchemaMapSize = CachedSchemaRegistryClient.MAX_SCHEMA_MAP_SIZE_DEFAULT;
        }

        public SchemaRegistryAvroAsyncDeserializer build() {
            return new SchemaRegistryAvroAsyncDeserializer(this);
        }

        public Builder credential(String credential) {
            this.credential = credential;
            return this;
        }

        public Builder avroSpecificReader(boolean avroSpecificReader) {
            this.avroSpecificReader = avroSpecificReader;
            return this;
        }

        public Builder maxSchemaMapSize(int maxSchemaMapSize) {
            this.maxSchemaMapSize = maxSchemaMapSize;
            return this;
        }
    }
}
