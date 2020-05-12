package com.azure.schemaregistry.avro;

import com.azure.schemaregistry.AbstractDataSerializer;
import com.azure.schemaregistry.ByteEncoder;
import com.azure.schemaregistry.client.CachedSchemaRegistryClient;

public final class SchemaRegistryAvroAsyncSerializerBuilder {
    private final String registryUrl;
    private String credential;
    private boolean autoRegisterSchemas;
    private String schemaGroup;
    private int maxSchemaMapSize;
    protected ByteEncoder byteEncoder = null;

    private SchemaRegistryAvroAsyncSerializerBuilder(String registryUrl) {
        this.registryUrl = registryUrl;
        this.credential = null;
        this.autoRegisterSchemas = AbstractDataSerializer.AUTO_REGISTER_SCHEMAS_DEFAULT;
        this.schemaGroup = AbstractDataSerializer.SCHEMA_GROUP_DEFAULT;
        this.maxSchemaMapSize = CachedSchemaRegistryClient.MAX_SCHEMA_MAP_SIZE_DEFAULT;
    }

    public static SchemaRegistryAvroAsyncSerializerBuilder aSchemaRegistryAvroAsyncSerializer() {
        return new SchemaRegistryAvroAsyncSerializerBuilder();
    }

    public SchemaRegistryAvroAsyncSerializerBuilder byteEncoder(ByteEncoder byteEncoder) {
        this.byteEncoder = byteEncoder;
        return this;
    }

    public SchemaRegistryAvroAsyncSerializer build() {
        SchemaRegistryAvroAsyncSerializer schemaRegistryAvroAsyncSerializer = new SchemaRegistryAvroAsyncSerializer(null);
        schemaRegistryAvroAsyncSerializer.setByteEncoder(byteEncoder);
        return schemaRegistryAvroAsyncSerializer;
    }

    public static class Builder {
        private final String registryUrl;
        private boolean autoRegisterSchemas;
        private String credential;
        private int maxSchemaMapSize;
        private String schemaGroup;

        public Builder(String registryUrl) {

        }

        public SchemaRegistryAvroAsyncSerializer build() {
            return new SchemaRegistryAvroAsyncSerializer(this);
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
