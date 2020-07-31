// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.credential.TokenCredential;
import com.azure.data.schemaregistry.CachedSchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.CachedSchemaRegistryClientBuilder;

import java.util.Objects;

/**
 * Builder implemenation for building {@link SchemaRegistryAvroSerializer} and {@link SchemaRegistryAvroAsyncSerializer}
 */
public final class SchemaRegistryAvroSerializerBuilder {
    private String registryUrl;
    private TokenCredential credential;
    private Boolean autoRegisterSchemas;
    private String schemaGroup;
    private Integer maxCacheSize;
    private Boolean avroSpecificReader;

    /**
     * Instantiates instance of Builder class.
     * Supplies client defaults.
     */
    public SchemaRegistryAvroSerializerBuilder() {
        this.registryUrl = null;
        this.credential = null;
        this.autoRegisterSchemas = null;
        this.schemaGroup = null;
        this.maxCacheSize = null;
        this.avroSpecificReader = false;
    }

    /**
     * Sets the service endpoint for the Azure Schema Registry instance.
     *
     * @return The updated {@link SchemaRegistryAvroSerializerBuilder} object.
     * @param schemaRegistryUrl The URL of the Azure Schema Registry instance
     * @throws NullPointerException if {@code schemaRegistryUrl} is null
     */
    public SchemaRegistryAvroSerializerBuilder schemaRegistryUrl(String schemaRegistryUrl) {
        Objects.requireNonNull(schemaRegistryUrl, "'schemaRegistryUrl' cannot be null.");
        this.registryUrl = schemaRegistryUrl;
        return this;
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
     * Specifies if objects should be deserialized into Avro SpecificRecords via Avro SpecificDatumReader
     * @param avroSpecificReader specific reader flag
     * @return updated {@link SchemaRegistryAvroSerializerBuilder} instance
     */
    public SchemaRegistryAvroSerializerBuilder avroSpecificReader(boolean avroSpecificReader) {
        this.avroSpecificReader = avroSpecificReader;
        return this;
    }

    /**
     * Specifies maximum schema object cache size for underlying CachedSchemaRegistryAsyncClient.  If specified cache
     * size is exceeded, all caches are recycled.
     *
     * @param maxCacheSize maximum number of schemas per cache
     * @return updated {@link SchemaRegistryAvroSerializerBuilder} instance
     */
    public SchemaRegistryAvroSerializerBuilder maxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
        return this;
    }

    /**
     * Instantiates {@link SchemaRegistryAvroSerializer}
     * @return {@link SchemaRegistryAvroSerializer} instance
     *
     * @throws NullPointerException if parameters are incorrectly set.
     * @throws IllegalArgumentException if credential is not set.
     */
    public SchemaRegistryAvroSerializer buildClient() {
        return new SchemaRegistryAvroSerializer(this.buildAsyncClient());
    }

    /**
     * Instantiates SchemaRegistry
     * @return {@link SchemaRegistryAvroAsyncSerializer} instance
     *
     * @throws NullPointerException if parameters are incorrectly set.
     * @throws IllegalArgumentException if credential is not set.
     */
    public SchemaRegistryAvroAsyncSerializer buildAsyncClient() {
        CachedSchemaRegistryClientBuilder builder = new CachedSchemaRegistryClientBuilder()
            .endpoint(registryUrl)
            .credential(credential);

        if (maxCacheSize != null) {
            builder.maxCacheSize(maxCacheSize);
        }

        AvroCodec codec = new AvroCodec(this.avroSpecificReader);

        CachedSchemaRegistryAsyncClient client = builder
            .addCodec(codec)
            .buildAsyncClient();

        return new SchemaRegistryAvroAsyncSerializer(client, codec, this.schemaGroup,
            this.autoRegisterSchemas);
    }
}
