// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.jsonschema;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;

import java.util.Objects;

public final class SchemaRegistryJsonSchemaSerializerBuilder {
    private final ClientLogger logger = new ClientLogger(SchemaRegistryJsonSchemaSerializerBuilder.class);
    private SchemaRegistryAsyncClient schemaRegistryAsyncClient;
    private String schemaGroup;

    /**
     * Specifies schema group for interacting with Azure Schema Registry service.
     *
     * If auto-registering schemas, schema will be stored under this group. If not auto-registering, serializer will
     * request schema ID for matching data schema under specified group.
     *
     * @param schemaGroup Azure Schema Registry schema group
     *
     * @return updated {@link SchemaRegistryJsonSchemaSerializerBuilder} instance
     */
    public SchemaRegistryJsonSchemaSerializerBuilder schemaGroup(String schemaGroup) {
        this.schemaGroup = schemaGroup;
        return this;
    }

    /**
     * The {@link SchemaRegistryAsyncClient} to use to interact with the Schema Registry service.
     *
     * @param schemaRegistryAsyncClient The {@link SchemaRegistryAsyncClient}.
     *
     * @return updated {@link SchemaRegistryJsonSchemaSerializerBuilder} instance.
     */
    public SchemaRegistryJsonSchemaSerializerBuilder schemaRegistryClient(
        SchemaRegistryAsyncClient schemaRegistryAsyncClient) {
        this.schemaRegistryAsyncClient = schemaRegistryAsyncClient;
        return this;
    }

    /**
     * A JSON schema aware class that can generate and validate JSON schema for objects.
     *
     * @param jsonSchemaGenerator The JSON schema generator.
     *
     * @return updated {@link SchemaRegistryJsonSchemaSerializerBuilder} instance.
     */
    public SchemaRegistryJsonSchemaSerializerBuilder jsonSchemaGenerator(JsonSchemaGenerator jsonSchemaGenerator) {
        return this;
    }

    /**
     * Creates a new instance of Schema Registry serializer.
     *
     * @return A new instance of {@link SchemaRegistryJsonSchemaSerializer}.
     *
     * @throws NullPointerException if {@link #schemaRegistryClient(SchemaRegistryAsyncClient)} is {@code null}
     */
    public SchemaRegistryJsonSchemaSerializer buildSerializer() {
        if (Objects.isNull(schemaRegistryAsyncClient)) {
            throw logger.logExceptionAsError(new NullPointerException("'schemaRegistryAsyncClient' cannot be null."));
        }

        if (CoreUtils.isNullOrEmpty(schemaGroup)) {
            throw logger.logExceptionAsError(new IllegalStateException("'schemaGroup' cannot be null."));
        }

        final SerializerOptions options = new SerializerOptions(schemaGroup, 100);

        return new SchemaRegistryJsonSchemaSerializer(schemaRegistryAsyncClient, options);
    }
}
