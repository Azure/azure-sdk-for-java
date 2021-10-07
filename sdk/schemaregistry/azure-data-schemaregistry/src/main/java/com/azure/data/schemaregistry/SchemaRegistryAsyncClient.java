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
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SerializationType;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
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
     * Registers a new schema in the specified schema group with the given schema name. If the schema name already
     * exists in this schema group, a new version with the updated schema string will be registered.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param content The string representation of the schema.
     * @param serializationType The serialization type of this schema.
     *
     * @return The {@link SchemaProperties} of a successfully registered schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SchemaProperties> registerSchema(String groupName, String name, String content,
        SerializationType serializationType) {
        return registerSchemaWithResponse(groupName, name, content, serializationType)
            .map(Response::getValue);
    }

    /**
     * Registers a new schema in the specified schema group with the given schema name. If the schema name already
     * exists in this schema group, a new version with the updated schema string will be registered.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param content The string representation of the schema.
     * @param serializationType The serialization type of this schema.
     *
     * @return The schema properties on successful registration of the schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SchemaProperties>> registerSchemaWithResponse(String groupName, String name, String content,
        SerializationType serializationType) {
        return FluxUtil.withContext(context -> registerSchemaWithResponse(groupName, name, content,
            serializationType, context));
    }

    Mono<Response<SchemaProperties>> registerSchemaWithResponse(String groupName, String name, String content,
        SerializationType serializationType, Context context) {
        logger.verbose("Registering schema. Group: '{}', name: '{}', serialization type: '{}', payload: '{}'",
            groupName, name, serializationType, content);

        return this.restService.getSchemas().registerWithResponseAsync(groupName, name,
            com.azure.data.schemaregistry.implementation.models.SerializationType.AVRO, content)
            .handle((response, sink) -> {
                SchemaId schemaId = response.getValue();
                SchemaProperties registered = new SchemaProperties(schemaId.getId(),
                    serializationType,
                    name,
                    content.getBytes(SCHEMA_REGISTRY_SERVICE_ENCODING));

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
    public Mono<SchemaProperties> getSchema(String id) {
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
    public Mono<Response<SchemaProperties>> getSchemaWithResponse(String id) {
        return FluxUtil.withContext(context -> getSchemaWithResponse(id, context));
    }

    Mono<Response<SchemaProperties>> getSchemaWithResponse(String id, Context context) {
        Objects.requireNonNull(id, "'id' should not be null");
        return this.restService.getSchemas().getByIdWithResponseAsync(id)
            .handle((response, sink) -> {
                final SerializationType serializationType =
                    SerializationType.fromString(response.getDeserializedHeaders().getSchemaType());
                final URI location = URI.create(response.getDeserializedHeaders().getLocation());
                final Matcher matcher = SCHEMA_PATTERN.matcher(location.getPath());

                if (!matcher.lookingAt()) {
                    sink.error(new IllegalArgumentException("Response location does not contain schema group or"
                        + " schema name. Location: " + location.getPath()));

                    return;
                }

                final String schemaName = matcher.group("schemaName");
                final SchemaProperties schemaObject = new SchemaProperties(id,
                    serializationType,
                    schemaName,
                    response.getValue());
                final SimpleResponse<SchemaProperties> schemaResponse = new SimpleResponse<>(
                    response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), schemaObject);

                sink.next(schemaResponse);
            });
    }

    /**
     * Gets the schema identifier associated with the given schema. Gets a cached value if it exists, otherwise makes a
     * call to the service.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param content The string representation of the schema.
     * @param serializationType The serialization type of this schema.
     *
     * @return The unique identifier for this schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> getSchemaId(String groupName, String name, String content,
        SerializationType serializationType) {
        return getSchemaIdWithResponse(groupName, name, content, serializationType)
            .map(response -> response.getValue());
    }

    /**
     * Gets the schema identifier associated with the given schema. Always makes a call to the service.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param content The string representation of the schema.
     * @param serializationType The serialization type of this schema.
     *
     * @return The unique identifier for this schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> getSchemaIdWithResponse(String groupName, String name, String content,
        SerializationType serializationType) {

        return FluxUtil.withContext(context ->
            getSchemaIdWithResponse(groupName, name, content, serializationType, context));
    }

    /**
     * Gets the schema id associated with the schema name a string representation of the schema.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param content The string representation of the schema.
     * @param serializationType The serialization type of this schema.
     * @param context Context to pass along with this request.
     *
     * @return A mono that completes with the schema id.
     */
    Mono<Response<String>> getSchemaIdWithResponse(String groupName, String name, String content,
        SerializationType serializationType, Context context) {

        return this.restService.getSchemas()
            .queryIdByContentWithResponseAsync(groupName, name, getSerialization(serializationType), content)
            .handle((response, sink) -> {
                SchemaId schemaId = response.getValue();
                SimpleResponse<String> schemaIdResponse = new SimpleResponse<>(
                    response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), schemaId.getId());

                sink.next(schemaIdResponse);
            });
    }

    /**
     * Gets the matching implementation class serialization type.
     *
     * @param serializationType Model serialization type.
     *
     * @return Implementation serialization type.
     *
     * @throws UnsupportedOperationException if the serialization type is not supported.
     */
    private static com.azure.data.schemaregistry.implementation.models.SerializationType getSerialization(
        SerializationType serializationType) {
        if (serializationType == SerializationType.AVRO) {
            return com.azure.data.schemaregistry.implementation.models.SerializationType.AVRO;
        } else {
            throw new UnsupportedOperationException("Serialization type is not supported: " + serializationType);
        }
    }
}
