// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.implementation.AzureSchemaRegistry;
import com.azure.data.schemaregistry.implementation.models.SchemaId;
import com.azure.data.schemaregistry.implementation.models.SerializationType;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SchemaRegistrySchema;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * HTTP-based client that interacts with Azure Schema Registry service to store and retrieve schemas on demand.
 *
 * <p><strong>Register a schema</strong></p>
 * Registering a schema returns a unique schema id that can be used to quickly associate payloads with that schema.
 * Reactive operations must be subscribed to; this kicks off the operation.
 *
 * {@codesnippet com.azure.data.schemaregistry.schemaregistryasyncclient.registerschema}
 *
 * <p><strong>Get a schema</strong></p>
 * {@codesnippet com.azure.data.schemaregistry.schemaregistryasyncclient.getSchema}
 *
 * <p><strong>Get a schema id</strong></p>
 * {@codesnippet com.azure.data.schemaregistry.schemaregistryclient.getSchemaId}
 *
 * @see SchemaRegistryClientBuilder Builder object instantiation and additional samples.
 */
@ServiceClient(builder = SchemaRegistryClientBuilder.class, isAsync = true)
public final class SchemaRegistryAsyncClient {

    static final Charset SCHEMA_REGISTRY_SERVICE_ENCODING = StandardCharsets.UTF_8;

    private static final Pattern SCHEMA_PATTERN = Pattern.compile("/\\$schemagroups/(?<schemaGroup>.+)/schemas/(?<schemaName>.+?)/");
    private final ClientLogger logger = new ClientLogger(SchemaRegistryAsyncClient.class);
    private final AzureSchemaRegistry restService;

    SchemaRegistryAsyncClient(AzureSchemaRegistry restService) {
        this.restService = restService;
    }

    /**
     * Gets the fully qualified namespace of the Schema Registry instance.
     *
     * @return The fully qualified namespace of the Schema Registry instance.
     */
    public String getFullyQualifiedNamespace() {
        return this.restService.getEndpoint();
    }

    /**
     * Registers a new schema in the specified schema group with the given schema name. If the schema name already
     * exists in this schema group, a new version with the updated schema string will be registered.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param schemaDefinition The string representation of the schema.
     * @param schemaFormat The serialization type of this schema.
     *
     * @return The {@link SchemaProperties} of a successfully registered schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SchemaProperties> registerSchema(String groupName, String name, String schemaDefinition,
        SchemaFormat schemaFormat) {
        return registerSchemaWithResponse(groupName, name, schemaDefinition, schemaFormat)
            .map(Response::getValue);
    }

    /**
     * Registers a new schema in the specified schema group with the given schema name. If the schema name already
     * exists in this schema group, a new version with the updated schema string will be registered.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param schemaDefinition The string representation of the schema.
     * @param schemaFormat The serialization type of this schema.
     *
     * @return The schema properties on successful registration of the schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SchemaProperties>> registerSchemaWithResponse(String groupName, String name,
        String schemaDefinition, SchemaFormat schemaFormat) {
        return FluxUtil.withContext(context -> registerSchemaWithResponse(groupName, name, schemaDefinition,
                schemaFormat, context));
    }

    Mono<Response<SchemaProperties>> registerSchemaWithResponse(String groupName, String name, String schemaDefinition,
        SchemaFormat schemaFormat, Context context) {
        logger.verbose("Registering schema. Group: '{}', name: '{}', serialization type: '{}', payload: '{}'",
            groupName, name, schemaFormat, schemaDefinition);

        return restService.getSchemas().registerWithResponseAsync(groupName, name, getSerialization(schemaFormat),
            schemaDefinition)
            .handle((response, sink) -> {
                SchemaId schemaId = response.getValue();
                SchemaProperties registered = new SchemaProperties(schemaId.getId(), schemaFormat);

                SimpleResponse<SchemaProperties> schemaRegistryObjectSimpleResponse = new SimpleResponse<>(
                    response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), registered);
                sink.next(schemaRegistryObjectSimpleResponse);
            });
    }

    /**
     * Gets the schema properties of the schema associated with the unique schema id.
     *
     * @param id The unique identifier of the schema.
     *
     * @return The {@link SchemaProperties} associated with the given {@code id}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SchemaRegistrySchema> getSchema(String id) {
        return getSchemaWithResponse(id).map(Response::getValue);
    }

    /**
     * Gets the schema properties of the schema associated with the unique schema id.
     *
     * @param id The unique identifier of the schema.
     *
     * @return The {@link SchemaProperties} associated with the given {@code id} along with the HTTP response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SchemaRegistrySchema>> getSchemaWithResponse(String id) {
        return FluxUtil.withContext(context -> getSchemaWithResponse(id, context));
    }

    Mono<Response<SchemaRegistrySchema>> getSchemaWithResponse(String id, Context context) {
        Objects.requireNonNull(id, "'id' should not be null");
        return this.restService.getSchemas().getByIdWithResponseAsync(id)
            .handle((response, sink) -> {
                final SchemaFormat schemaFormat =
                    SchemaFormat.fromString(response.getDeserializedHeaders().getSchemaType());
                final SchemaProperties schemaObject = new SchemaProperties(id, schemaFormat);
                final String schemaDefinition = new String(response.getValue(), SCHEMA_REGISTRY_SERVICE_ENCODING);
                final SimpleResponse<SchemaRegistrySchema> schemaRegistryResponse = new SimpleResponse<>(
                    response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), new SchemaRegistrySchema(schemaObject, schemaDefinition));

                sink.next(schemaRegistryResponse);
            });
    }

    /**
     * Gets the schema identifier associated with the given schema. Gets a cached value if it exists, otherwise makes a
     * call to the service.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param schemaDefinition The string representation of the schema.
     * @param schemaFormat The serialization type of this schema.
     *
     * @return The unique identifier for this schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SchemaProperties> getSchemaProperties(String groupName, String name, String schemaDefinition,
        SchemaFormat schemaFormat) {

        return getSchemaPropertiesWithResponse(groupName, name, schemaDefinition, schemaFormat)
            .map(response -> response.getValue());
    }

    /**
     * Gets the schema identifier associated with the given schema. Always makes a call to the service.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param schemaDefinition The string representation of the schema.
     * @param schemaFormat The serialization type of this schema.
     *
     * @return The unique identifier for this schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SchemaProperties>> getSchemaPropertiesWithResponse(String groupName, String name,
        String schemaDefinition, SchemaFormat schemaFormat) {

        return FluxUtil.withContext(context ->
            getSchemaPropertiesWithResponse(groupName, name, schemaDefinition, schemaFormat, context));
    }

    /**
     * Gets the schema id associated with the schema name a string representation of the schema.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param schemaDefinition The string representation of the schema.
     * @param schemaFormat The serialization type of this schema.
     * @param context Context to pass along with this request.
     *
     * @return A mono that completes with the schema id.
     */
    Mono<Response<SchemaProperties>> getSchemaPropertiesWithResponse(String groupName, String name,
        String schemaDefinition, SchemaFormat schemaFormat, Context context) {

        return restService.getSchemas()
            .queryIdByContentWithResponseAsync(groupName, name, getSerialization(schemaFormat), schemaDefinition)
            .handle((response, sink) -> {
                SchemaId schemaId = response.getValue();
                SchemaProperties properties = new SchemaProperties(schemaId.getId(), schemaFormat);
                SimpleResponse<SchemaProperties> schemaIdResponse = new SimpleResponse<>(
                    response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), properties);

                sink.next(schemaIdResponse);
            });
    }

    /**
     * Gets the matching implementation class serialization type.
     *
     * @param schemaFormat Model serialization type.
     *
     * @return Implementation serialization type.
     *
     * @throws UnsupportedOperationException if the serialization type is not supported.
     */
    private static SerializationType getSerialization(SchemaFormat schemaFormat) {
        if (schemaFormat == SchemaFormat.AVRO) {
            return SerializationType.AVRO;
        } else {
            throw new UnsupportedOperationException("Serialization type is not supported: " + schemaFormat);
        }
    }
}
