// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SchemaRegistrySchema;

/**
 * HTTP-based client that interacts with Azure Schema Registry service to store and retrieve schemas on demand.
 *
 * <p><strong>Register a schema</strong></p>
 * Registering a schema returns a unique schema id that can be used to quickly associate payloads with that schema.
 * {@codesnippet com.azure.data.schemaregistry.schemaregistryclient.registerschema}
 *
 * <p><strong>Get a schema</strong></p>
 * {@codesnippet com.azure.data.schemaregistry.schemaregistryclient.getSchema}
 *
 * <p><strong>Get a schema's properties</strong></p>
 * {@codesnippet com.azure.data.schemaregistry.schemaregistryclient.getSchemaProperties}
 *
 * @see SchemaRegistryClientBuilder Builder object instantiation and additional samples.
 */
@ServiceClient(builder = SchemaRegistryClientBuilder.class)
public final class SchemaRegistryClient {
    private final SchemaRegistryAsyncClient asyncClient;

    SchemaRegistryClient(SchemaRegistryAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Gets the fully qualified namespace of the Schema Registry instance.
     *
     * @return The fully qualified namespace of the Schema Registry instance.
     */
    public String getFullyQualifiedNamespace() {
        return asyncClient.getFullyQualifiedNamespace();
    }

    /**
     * Registers a new schema in the specified schema group with the given schema name. If the schema name already
     * exists in this schema group, a new version with the updated schema string will be registered.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param schemaDefinition The string representation of the schema.
     * @param format The serialization type of this schema.
     *
     * @return The schema properties on successful registration of the schema.
     *
     * @throws NullPointerException if {@code groupName}, {@code name}, {@code format}, or
     *     {@code schemaDefinition} are null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SchemaProperties registerSchema(String groupName, String name, String schemaDefinition,
        SchemaFormat format) {
        return this.asyncClient.registerSchema(groupName, name, schemaDefinition, format).block();
    }

    /**
     * Registers a new schema in the specified schema group with the given schema name. If the schema name already
     * exists in this schema group, a new version with the updated schema string will be registered.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param schemaDefinition The string representation of the schema.
     * @param format The serialization type of this schema.
     * @param context The context to pass to the Http pipeline.
     *
     * @return The schema properties on successful registration of the schema.
     *
     * @throws NullPointerException if {@code groupName}, {@code name}, {@code format}, or
     *     {@code schemaDefinition} are null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SchemaProperties> registerSchemaWithResponse(String groupName, String name, String schemaDefinition,
        SchemaFormat format, Context context) {
        return this.asyncClient.registerSchemaWithResponse(groupName, name, schemaDefinition, format,
            context).block();
    }

    /**
     * Gets the schema properties of the schema associated with the unique schema id.
     *
     * @param schemaId The unique identifier of the schema.
     *
     * @return The {@link SchemaProperties} associated with the given {@code schemaId}.
     *
     * @throws NullPointerException if {@code schemaId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SchemaRegistrySchema getSchema(String schemaId) {
        return this.asyncClient.getSchema(schemaId).block();
    }

    /**
     * Gets the schema properties of the schema associated with the unique schema schemaId.
     *
     * @param schemaId The unique identifier of the schema.
     * @param context The context to pass to the Http pipeline.
     *
     * @return The {@link SchemaProperties} associated with the given {@code id} and its HTTP response.
     *
     * @throws NullPointerException if {@code schemaId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SchemaRegistrySchema> getSchemaWithResponse(String schemaId, Context context) {
        return this.asyncClient.getSchemaWithResponse(schemaId, context).block();
    }

    /**
     * Gets the schema properties associated with the given schema id.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param schemaDefinition The string representation of the schema.
     * @param format The serialization type of this schema.
     *
     * @return The unique identifier for this schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SchemaProperties getSchemaProperties(String groupName, String name, String schemaDefinition,
        SchemaFormat format) {
        return this.asyncClient.getSchemaProperties(groupName, name, schemaDefinition, format).block();
    }

    /**
     * Gets the schema identifier associated with the given schema.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param schemaDefinition The string representation of the schema.
     * @param format The serialization type of this schema.
     * @param context The context to pass to the Http pipeline.
     *
     * @return The unique identifier for this schema.
     *
     * @throws NullPointerException if {@code groupName}, {@code name}, {@code format}, or
     *     {@code schemaDefinition} are null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SchemaProperties> getSchemaPropertiesWithResponse(String groupName, String name,
        String schemaDefinition, SchemaFormat format, Context context) {
        return this.asyncClient.getSchemaPropertiesWithResponse(groupName, name, schemaDefinition, format, context)
            .block();
    }
}
