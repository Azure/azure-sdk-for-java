// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.schemaregistry.avro;

import com.azure.core.credential.TokenCredential;
import com.azure.schemaregistry.AbstractDataSerializer;
import com.azure.schemaregistry.client.CachedSchemaRegistryClient;

/**
 * Builder implemenation for building {@link SchemaRegistryAvroSerializer} and {@link SchemaRegistryAvroAsyncSerializer}
 */
public final class SchemaRegistryAvroSerializerBuilder {
    private final String registryUrl;
    private TokenCredential credential;
    private boolean autoRegisterSchemas;
    private String schemaGroup;
    private int maxSchemaMapSize;

    /**
     * Instantiates instance of Builder class.
     * Supplies client defaults.
     *
     * @param registryUrl base schema registry URL for storing and fetching schemas
     */
    private SchemaRegistryAvroSerializerBuilder(String registryUrl) {
        this.registryUrl = registryUrl;
        this.credential = null;
        this.autoRegisterSchemas = AbstractDataSerializer.AUTO_REGISTER_SCHEMAS_DEFAULT;
        this.schemaGroup = AbstractDataSerializer.SCHEMA_GROUP_DEFAULT;
        this.maxSchemaMapSize = CachedSchemaRegistryClient.MAX_SCHEMA_MAP_SIZE_DEFAULT;
    }

    /**
     * Specifies schema group for interacting with Azure Schema Registry service.
     *
     * If auto-registering schemas, schema will be stored under this group.
     * If not auto-registering, serializer will request schema ID for matching data schema under specified group.
     *
     * @param schemaGroup Azure Schema Registry schema group
     * @return updated {@link SchemaRegistryAvroSerializerBuilder} instance
     */
    public SchemaRegistryAvroSerializerBuilder schemaGroup(String schemaGroup) {
        this.schemaGroup = schemaGroup;
        return this;
    }

    /**
     * Specifies authentication behavior with Azure Schema Registry
     * @param credential TokenCredential to be used to authenticate with Azure Schema Registry service
     * @return updated {@link SchemaRegistryAvroSerializerBuilder} instance
     */
    public SchemaRegistryAvroSerializerBuilder credential(TokenCredential credential) {
        this.credential = credential;
        return this;
    }

    /**
     * If specified true, serializer will register schemas against Azure Schema Registry service under the specified
     * group.  See Azure Schema Registry documentation for a description of schema registration behavior.
     *
     * If specified false, serializer will simply query the service for an existing ID given schema content.
     * Serialization will fail if the schema has not been pre-created.
     *
     * Auto-registration is **NOT RECOMMENDED** for production scenarios.
     *
     * @param autoRegisterSchemas flag for schema auto-registration
     * @return updated {@link SchemaRegistryAvroSerializerBuilder} instance
     */
    public SchemaRegistryAvroSerializerBuilder autoRegisterSchema(boolean autoRegisterSchemas) {
        this.autoRegisterSchemas = autoRegisterSchemas;
        return this;
    }

    /**
     * Specifies maximum schema object cache size for underlying CachedSchemaRegistryClient.  If specified cache
     * size is exceeded, all caches are recycled.
     *
     * @param maxSchemaMapSize maximum number of schemas per cache
     * @return updated {@link SchemaRegistryAvroSerializerBuilder} instance
     */
    public SchemaRegistryAvroSerializerBuilder maxSchemaMapSize(int maxSchemaMapSize) {
        this.maxSchemaMapSize = maxSchemaMapSize;
        return this;
    }

    /**
     * Instantiates SchemaRegistry
     * @return {@link SchemaRegistryAvroAsyncSerializer} instance
     *
     * @throws NullPointerException if parameters are incorrectly set.
     * @throws IllegalArgumentException if credential is not set.
     */
    public SchemaRegistryAvroAsyncSerializer buildAsyncClient() {
        return new SchemaRegistryAvroAsyncSerializer(this.buildSyncClient());
    }

    /**
     * Instantiates {@link SchemaRegistryAvroSerializer}
     * @return {@link SchemaRegistryAvroSerializer} instance
     *
     * @throws NullPointerException if parameters are incorrectly set.
     * @throws IllegalArgumentException if credential is not set.
     */
    public SchemaRegistryAvroSerializer buildSyncClient() {
        return new SchemaRegistryAvroSerializer(
            this.registryUrl,
            this.credential,
            this.schemaGroup,
            this.autoRegisterSchemas,
            this.maxSchemaMapSize);
    }
}
