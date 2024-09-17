// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.jsonschema;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClient;

import java.util.Objects;

/**
 * Class that creates {@link SchemaRegistryJsonSchemaSerializer} which interacts with Schema Registry.
 *
 * @see SchemaRegistryJsonSchemaSerializer
 */
public final class SchemaRegistryJsonSchemaSerializerBuilder {
    private static final ClientLogger LOGGER = new ClientLogger(SchemaRegistryJsonSchemaSerializerBuilder.class);

    private Boolean autoRegisterSchemas;
    private JsonSchemaGenerator jsonSchemaGenerator;
    private String schemaGroup;
    private SchemaRegistryAsyncClient schemaRegistryAsyncClient;
    private SchemaRegistryClient schemaRegistryClient;
    private JsonSerializer jsonSerializer;

    /**
     * <p>If true, the serializer will register schemas against Azure Schema Registry service under the specified
     * group if it fails to find an existing schema to serialize.  See
     * <a href="https://learn.microsoft.com/azure/event-hubs/schema-registry-overview">Azure Schema Registry
     * documentation</a> for a description of schema registration behavior.</p>
     *
     * <p>If specified false, serializer will query the service for an existing ID given schema content.
     * Serialization will fail if the schema has not been pre-created.</p>
     *
     * Auto-registration is <strong>NOT RECOMMENDED</strong> for production scenarios.
     *
     * @param autoRegisterSchemas flag for schema auto-registration
     *
     * @return The updated {@link SchemaRegistryJsonSchemaSerializerBuilder} instance.
     */
    public SchemaRegistryJsonSchemaSerializerBuilder autoRegisterSchemas(boolean autoRegisterSchemas) {
        this.autoRegisterSchemas = autoRegisterSchemas;
        return this;
    }

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
     * JSON serializer adapter to use. If none is supplied, the default is used.
     *
     * @param jsonSerializer Serializer to use.
     *
     * @return updated {@link SchemaRegistryJsonSchemaSerializerBuilder} instance.
     */
    public SchemaRegistryJsonSchemaSerializerBuilder serializer(JsonSerializer jsonSerializer) {
        this.jsonSerializer = jsonSerializer;
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
     * The {@link SchemaRegistryClient} to use to interact with the Schema Registry service.
     *
     * @param schemaRegistryClient The {@link SchemaRegistryClient}.
     *
     * @return updated {@link SchemaRegistryJsonSchemaSerializerBuilder} instance.
     */
    public SchemaRegistryJsonSchemaSerializerBuilder schemaRegistryClient(
        SchemaRegistryClient schemaRegistryClient) {
        this.schemaRegistryClient = schemaRegistryClient;
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
        this.jsonSchemaGenerator = jsonSchemaGenerator;
        return this;
    }

    /**
     * Creates a new instance of Schema Registry serializer.
     *
     * @return A new instance of {@link SchemaRegistryJsonSchemaSerializer}.
     *
     * @throws NullPointerException if {@link #schemaRegistryClient(SchemaRegistryAsyncClient)} or
     *     {@link #jsonSchemaGenerator(JsonSchemaGenerator)} is {@code null}.
     * @throws IllegalStateException if {@link #autoRegisterSchemas(boolean)} is true but
     *     {@link #schemaGroup(String)} is not set.
     */
    public SchemaRegistryJsonSchemaSerializer buildSerializer() {
        if (Objects.isNull(schemaRegistryAsyncClient)) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'schemaRegistryAsyncClient' cannot be null."));
        }

        if (Objects.isNull(jsonSchemaGenerator)) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'jsonSchemaGenerator' cannot be null."));
        }

        final boolean isAutoRegister = autoRegisterSchemas != null && autoRegisterSchemas;

        if (isAutoRegister && CoreUtils.isNullOrEmpty(schemaGroup)) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "'schemaGroup' cannot be null or empty when 'autoRegisterSchema' is true."));
        }

        final JsonSerializer serializerAdapterToUse = jsonSerializer != null
            ? jsonSerializer
            : JsonSerializerProviders.createInstance(true);

        if (Objects.isNull(serializerAdapterToUse)) {
            throw LOGGER.logExceptionAsError(new NullPointerException("Unable to find JsonSerializer to use. Pass one "
                + "into SchemaRegistryJsonSchemaSerializerBuilder.serializer() before "
                + "building the serializer."));
        }

        final SerializerOptions options = new SerializerOptions(schemaGroup, isAutoRegister, 100,
            serializerAdapterToUse);

        return new SchemaRegistryJsonSchemaSerializer(schemaRegistryAsyncClient, schemaRegistryClient,
            jsonSchemaGenerator, options);
    }
}

