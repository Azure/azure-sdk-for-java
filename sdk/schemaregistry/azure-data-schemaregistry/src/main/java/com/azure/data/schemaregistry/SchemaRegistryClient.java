// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SerializationType;

/**
 * HTTP-based client that interacts with Azure Schema Registry service to store and retrieve schemas on demand.
 *
 * @see SchemaRegistryClientBuilder Follows builder pattern for object instantiation
 */
@ServiceClient(builder = SchemaRegistryClientBuilder.class)
public final class SchemaRegistryClient {
    private final SchemaRegistryAsyncClient asyncClient;

    SchemaRegistryClient(SchemaRegistryAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Registers a new schema in the specified schema group with the given schema name. If the schema name already
     * exists in this schema group, a new version with the updated schema string will be registered.
     *
     * @param schemaGroup The schema group.
     * @param schemaName The schema name.
     * @param schemaString The string representation of the schema.
     * @param serializationType The serialization type of this schema.
     * @return The schema properties on successful registration of the schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SchemaProperties registerSchema(String schemaGroup, String schemaName, String schemaString,
                                           SerializationType serializationType) {
        return registerSchemaWithResponse(schemaGroup, schemaName, schemaString, serializationType, Context.NONE)
            .getValue();
    }

    /**
     * Registers a new schema in the specified schema group with the given schema name. If the schema name already
     * exists in this schema group, a new version with the updated schema string will be registered.
     *
     * @param schemaGroup The schema group.
     * @param schemaName The schema name.
     * @param schemaString The string representation of the schema.
     * @param serializationType The serialization type of this schema.
     * @param context The context to pass to the Http pipeline.
     * @return The schema properties on successful registration of the schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SchemaProperties> registerSchemaWithResponse(String schemaGroup, String schemaName,
                                 String schemaString, SerializationType serializationType, Context context) {
        return this.asyncClient.registerSchemaWithResponse(schemaGroup, schemaName, schemaString, serializationType,
            context).block();
    }

    /**
     * Gets the schema properties of the schema associated with the unique schemaId.
     * @param schemaId The unique identifier of the schema.
     *
     * @return The {@link SchemaProperties} associated with the given {@code schemaId}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SchemaProperties getSchema(String schemaId) {
        return getSchemaWithResponse(schemaId, Context.NONE).getValue();
    }

    /**
     * Gets the schema properties of the schema associated with the unique schemaId.
     * @param schemaId The unique identifier of the schema.
     * @param context The context to pass to the Http pipeline.
     * @return The {@link SchemaProperties} associated with the given {@code schemaId} along with the HTTP
     * response.
     */
    Response<SchemaProperties> getSchemaWithResponse(String schemaId, Context context) {
        return this.asyncClient.getSchemaWithResponse(schemaId, context).block();
    }

    /**
     * Gets the schema identifier associated with the given schema.
     *
     * @param schemaGroup The schema group.
     * @param schemaName The schema name.
     * @param schemaString The string representation of the schema.
     * @param serializationType The serialization type of this schema.
     *
     * @return The unique identifier for this schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String getSchemaId(String schemaGroup, String schemaName, String schemaString,
                              SerializationType serializationType) {
        return getSchemaIdWithResponse(schemaGroup, schemaName, schemaString, serializationType, Context.NONE)
            .getValue();
    }

    /**
     * Gets the schema identifier associated with the given schema.
     *
     * @param schemaGroup The schema group.
     * @param schemaName The schema name.
     * @param schemaString The string representation of the schema.
     * @param serializationType The serialization type of this schema.
     * @param context The context to pass to the Http pipeline.
     * @return The unique identifier for this schema.
     */
    Response<String> getSchemaIdWithResponse(String schemaGroup, String schemaName, String schemaString,
        SerializationType serializationType, Context context) {
        return this.asyncClient
            .getSchemaIdWithResponse(schemaGroup, schemaName, schemaString, serializationType, context).block();
    }

    void clearCache() {
        this.asyncClient.clearCache();
    }
}
