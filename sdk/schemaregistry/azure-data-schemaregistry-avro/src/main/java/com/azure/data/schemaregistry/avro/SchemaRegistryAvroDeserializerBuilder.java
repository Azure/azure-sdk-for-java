// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.credential.TokenCredential;
import com.azure.data.schemaregistry.client.CachedSchemaRegistryClient;
import com.azure.data.schemaregistry.client.CachedSchemaRegistryClientBuilder;

import java.util.Objects;

/**
 * Builder class for constructing {@link SchemaRegistryAvroDeserializer} and {@link SchemaRegistryAvroAsyncDeserializer}
 */
public class SchemaRegistryAvroDeserializerBuilder {

    private String registryUrl;
    private TokenCredential credential;
    private boolean avroSpecificReader;
    private Integer maxSchemaMapSize;

    /**
     * Instantiates instance of Builder class.
     * Supplies default avro.specific.reader value.
     *
     */
    public SchemaRegistryAvroDeserializerBuilder() {
        this.registryUrl = null;
        this.credential = null;
        this.avroSpecificReader = false;
        this.maxSchemaMapSize = null;
    }

    /**
     * Sets the service endpoint for the Azure Schema Registry instance.
     *
     * @return The updated {@link SchemaRegistryAvroDeserializerBuilder} object.
     * @param schemaRegistryUrl The URL of the Azure Schema Registry instance
     * @throws NullPointerException if {@code schemaRegistryUrl} is null
     */
    public SchemaRegistryAvroDeserializerBuilder schemaRegistryUrl(String schemaRegistryUrl) {
        Objects.requireNonNull(schemaRegistryUrl, "'schemaRegistryUrl' cannot be null.");
        this.registryUrl = schemaRegistryUrl;
        return this;
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
        CachedSchemaRegistryClientBuilder builder = new CachedSchemaRegistryClientBuilder()
            .endpoint(registryUrl)
            .credential(credential);

        if (maxSchemaMapSize != null) {
            builder.maxSchemaMapSize(maxSchemaMapSize);
        }

        CachedSchemaRegistryClient client = builder.buildClient();

        return new SchemaRegistryAvroDeserializer(client, this.avroSpecificReader);
    }
}
