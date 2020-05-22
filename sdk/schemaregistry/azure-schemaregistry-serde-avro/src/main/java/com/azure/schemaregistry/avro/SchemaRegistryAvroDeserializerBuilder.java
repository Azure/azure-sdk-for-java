// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.schemaregistry.avro;

import com.azure.core.credential.TokenCredential;
import com.azure.schemaregistry.client.CachedSchemaRegistryClient;

/**
 * Builder class for constructing {@link SchemaRegistryAvroDeserializer} and {@link SchemaRegistryAvroAsyncDeserializer}
 */
public class SchemaRegistryAvroDeserializerBuilder {

    private final String registryUrl;
    private TokenCredential credential;
    private boolean avroSpecificReader;
    private int maxSchemaMapSize;

    /**
     * Instantiates instance of Builder class.
     * Supplies client defaults.
     *
     * @param registryUrl base schema registry URL for storing and fetching schemas
     */
    public SchemaRegistryAvroDeserializerBuilder(String registryUrl) {
        this.registryUrl = registryUrl;
        this.credential = null;
        this.avroSpecificReader = false;
        this.maxSchemaMapSize = CachedSchemaRegistryClient.MAX_SCHEMA_MAP_SIZE_DEFAULT;
    }

    /**
     *
     * @param credential TokenCredential to be used for authenticating with Azure Schema Registry Service
     * @return updated {@link SchemaRegistryAvroDeserializerBuilder} instance
     */
    public SchemaRegistryAvroDeserializerBuilder credential(TokenCredential credential) {
        this.credential = credential;
        return this;
    }

    /**
     * Specifies if objects should be deserialized into Avro SpecificRecords via Avro SpecificDatumReader
     * @param avroSpecificReader specific reader flag
     * @return updated {@link SchemaRegistryAvroDeserializerBuilder} instance
     */
    public SchemaRegistryAvroDeserializerBuilder avroSpecificReader(boolean avroSpecificReader) {
        this.avroSpecificReader = avroSpecificReader;
        return this;
    }

    /**
     * Specifies maximum schema object cache size for underlying CachedSchemaRegistryClient.  If specified cache
     * size is exceeded, all caches are recycled.
     *
     * @param maxSchemaMapSize maximum number of schemas per cache
     * @return updated {@link SchemaRegistryAvroDeserializerBuilder} instance
     */
    public SchemaRegistryAvroDeserializerBuilder maxSchemaMapSize(int maxSchemaMapSize) {
        this.maxSchemaMapSize = maxSchemaMapSize;
        return this;
    }

    /**
     * Construct instance of {@link SchemaRegistryAvroAsyncDeserializer}
     *
     * @return {@link SchemaRegistryAvroAsyncDeserializer} instance
     *
     * @throws NullPointerException if parameters are incorrectly set.
     * @throws IllegalArgumentException if credential is not set.
     */
    public SchemaRegistryAvroAsyncDeserializer buildAsyncClient() {
        return new SchemaRegistryAvroAsyncDeserializer(this.buildSyncClient());
    }

    /**
     * Construct instance of {@link SchemaRegistryAvroDeserializer}
     *
     * @return {@link SchemaRegistryAvroDeserializer} instance
     *
     * @throws NullPointerException if parameters are incorrectly set.
     * @throws IllegalArgumentException if credential is not set.
     */
    public SchemaRegistryAvroDeserializer buildSyncClient() {
        return new SchemaRegistryAvroDeserializer(
            this.registryUrl,
            this.credential,
            this.avroSpecificReader,
            this.maxSchemaMapSize);
    }
}
