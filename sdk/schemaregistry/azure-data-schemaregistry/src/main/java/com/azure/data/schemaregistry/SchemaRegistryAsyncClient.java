// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SerializationType;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

import com.azure.data.schemaregistry.implementation.AzureSchemaRegistry;
import com.azure.data.schemaregistry.implementation.models.SchemaId;
import reactor.core.publisher.Mono;

/**
 * HTTP-based client that interacts with Azure Schema Registry service to store and retrieve schemas on demand.
 * @see SchemaRegistryClientBuilder Follows builder pattern for object instantiation
 */
@ServiceClient(builder = SchemaRegistryClientBuilder.class, isAsync = true)
public final class SchemaRegistryAsyncClient {

    private final ClientLogger logger = new ClientLogger(SchemaRegistryAsyncClient.class);

    static final Charset SCHEMA_REGISTRY_SERVICE_ENCODING = StandardCharsets.UTF_8;
    static final int MAX_SCHEMA_MAP_SIZE_DEFAULT = 1000;
    static final int MAX_SCHEMA_MAP_SIZE_MINIMUM = 10;

    private final AzureSchemaRegistry restService;
    private final Integer maxSchemaMapSize;
    private final ConcurrentSkipListMap<String, Function<String, Object>> typeParserMap;
    private final Map<String, SchemaProperties> idCache;
    private final Map<String, SchemaProperties> schemaStringCache;

    SchemaRegistryAsyncClient(
        AzureSchemaRegistry restService,
        int maxSchemaMapSize,
        ConcurrentSkipListMap<String, Function<String, Object>> typeParserMap) {
        this.restService = restService;
        this.maxSchemaMapSize = maxSchemaMapSize;
        this.typeParserMap = typeParserMap;
        this.idCache = new ConcurrentHashMap<>();
        this.schemaStringCache = new ConcurrentHashMap<>();
    }

    /**
     * Registers a new schema in the specified schema group with the given schema name. If the schema name already
     * exists in this schema group, a new version with the updated schema string will be registered.
     *
     * @param schemaGroup The schema group.
     * @param schemaName The schema name.
     * @param schemaString The string representation of the schema.
     * @param serializationType The serialization type of this schema.
     *
     * @return The {@link SchemaProperties} of a successfully registered schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SchemaProperties> registerSchema(
        String schemaGroup, String schemaName, String schemaString, SerializationType serializationType) {

        if (schemaStringCache.containsKey(getSchemaStringCacheKey(schemaGroup, schemaName, schemaString))) {
            logger.verbose(
                "Cache hit schema string. Group: '{}', name: '{}', schema type: '{}', payload: '{}'",
                schemaGroup, schemaName, serializationType, schemaString);
            return Mono.fromCallable(
                () -> schemaStringCache.get(getSchemaStringCacheKey(schemaGroup, schemaName, schemaString)));
        }

        return registerSchemaWithResponse(schemaGroup, schemaName, schemaString, serializationType)
            .map(Response::getValue);
    }

    /**
     * Registers a new schema in the specified schema group with the given schema name. If the schema name already
     * exists in this schema group, a new version with the updated schema string will be registered.
     *
     * @param schemaGroup The schema group.
     * @param schemaName The schema name.
     * @param schemaString The string representation of the schema.
     * @param serializationType The serialization type of this schema.
     *
     * @return The schema properties on successful registration of the schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SchemaProperties>> registerSchemaWithResponse(String schemaGroup, String schemaName,
                                   String schemaString, SerializationType serializationType) {
        return registerSchemaWithResponse(schemaGroup, schemaName, schemaString, serializationType, Context.NONE);
    }

    Mono<Response<SchemaProperties>> registerSchemaWithResponse(String schemaGroup, String schemaName,
                                    String schemaString, SerializationType serializationType, Context context) {
        logger.verbose(
            "Registering schema. Group: '{}', name: '{}', serialization type: '{}', payload: '{}'",
            schemaGroup, schemaName, serializationType, schemaString);

        return this.restService
            .getSchemas().registerWithResponseAsync(schemaGroup, schemaName,
                com.azure.data.schemaregistry.implementation.models.SerializationType.AVRO, schemaString)
            .handle((response, sink) -> {
                if (response == null) {
                    sink.error(logger.logExceptionAsError(
                        new NullPointerException("Client returned null response")));
                    return;
                }

                if (response.getStatusCode() == 400) {
                    sink.error(logger.logExceptionAsError(
                        new IllegalStateException("Invalid schema registration attempted")));
                    return;
                }

                SchemaId schemaId = response.getValue();

                SchemaProperties registered = new SchemaProperties(schemaId.getId(),
                    serializationType,
                    schemaName,
                    schemaString.getBytes(SCHEMA_REGISTRY_SERVICE_ENCODING));

                resetIfNeeded();
                schemaStringCache
                    .putIfAbsent(getSchemaStringCacheKey(schemaGroup, schemaName, schemaString), registered);
                logger.verbose("Cached schema string. Group: '{}', name: '{}'", schemaGroup, schemaName);
                SimpleResponse<SchemaProperties> schemaRegistryObjectSimpleResponse = new SimpleResponse<>(
                    response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), registered);
                sink.next(schemaRegistryObjectSimpleResponse);
            });
    }

    /**
     * Gets the schema properties of the schema associated with the unique schemaId.
     * @param schemaId The unique identifier of the schema.
     *
     * @return The {@link SchemaProperties} associated with the given {@code schemaId}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SchemaProperties> getSchema(String schemaId) {
        if (idCache.containsKey(schemaId)) {
            logger.verbose("Cache hit for schema id '{}'", schemaId);
            return Mono.fromCallable(() -> idCache.get(schemaId));
        }
        return getSchemaWithResponse(schemaId).map(Response::getValue);
    }

    /**
     * Gets the schema properties of the schema associated with the unique schemaId.
     * @param schemaId The unique identifier of the schema.
     *
     * @return The {@link SchemaProperties} associated with the given {@code schemaId} along with the HTTP
     * response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SchemaProperties>> getSchemaWithResponse(String schemaId) {
        return getSchemaWithResponse(schemaId, Context.NONE);
    }

    Mono<Response<SchemaProperties>> getSchemaWithResponse(String schemaId, Context context) {
        Objects.requireNonNull(schemaId, "'schemaId' should not be null");
        return this.restService.getSchemas().getByIdWithResponseAsync(schemaId)
            .handle((response, sink) -> {
                if (response == null) {
                    sink.error(logger.logExceptionAsError(
                        new NullPointerException("Client returned null response")));
                    return;
                }

                if (response.getStatusCode() == 404) {
                    sink.error(logger.logExceptionAsError(
                        new IllegalStateException(String.format("Schema does not exist, id %s", schemaId))));
                    return;
                }

                SerializationType serializationType =
                    SerializationType.fromString(response.getDeserializedHeaders().getXSchemaType());

                SchemaProperties schemaObject = new SchemaProperties(schemaId,
                    serializationType,
                    null,
                    response.getValue().getBytes(SCHEMA_REGISTRY_SERVICE_ENCODING));

                resetIfNeeded();
                idCache.putIfAbsent(schemaId, schemaObject);
                logger.verbose("Cached schema object. Path: '{}'", schemaId);
                SimpleResponse<SchemaProperties> schemaRegistryObjectSimpleResponse = new SimpleResponse<>(
                    response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), schemaObject);
                sink.next(schemaRegistryObjectSimpleResponse);
            });
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
    public Mono<String> getSchemaId(String schemaGroup, String schemaName, String schemaString,
                                    SerializationType serializationType) {

        String schemaStringCacheKey = getSchemaStringCacheKey(schemaGroup, schemaName, schemaString);
        if (schemaStringCache.containsKey(schemaStringCacheKey)) {
            logger.verbose("Cache hit schema string. Group: '{}', name: '{}'", schemaGroup, schemaName);
            return Mono.fromCallable(() -> schemaStringCache.get(schemaStringCacheKey).getSchemaId());
        }

        return getSchemaIdWithResponse(schemaGroup, schemaName, schemaString, serializationType)
            .map(response -> response.getValue());
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
    public Mono<Response<String>> getSchemaIdWithResponse(String schemaGroup, String schemaName, String schemaString,
                                                          SerializationType serializationType) {
        return getSchemaIdWithResponse(schemaGroup, schemaName, schemaString, serializationType, Context.NONE);
    }

    Mono<Response<String>> getSchemaIdWithResponse(String schemaGroup, String schemaName, String schemaString,
                                                   SerializationType serializationType, Context context) {

        return this.restService.getSchemas()
            .queryIdByContentWithResponseAsync(schemaGroup, schemaName,
                com.azure.data.schemaregistry.implementation.models.SerializationType.AVRO, schemaString)
            .handle((response, sink) -> {
                if (response == null) {
                    sink.error(logger.logExceptionAsError(
                        new NullPointerException("Client returned null response")));
                    return;
                }

                if (response.getStatusCode() == 404) {
                    sink.error(
                        logger.logExceptionAsError(new IllegalStateException("Existing matching schema not found.")));
                    return;
                }

                SchemaId schemaId = response.getValue();

                resetIfNeeded();
                schemaStringCache.putIfAbsent(
                    getSchemaStringCacheKey(schemaGroup, schemaName, schemaString),
                    new SchemaProperties(
                        schemaId.getId(),
                        serializationType,
                        schemaName,
                        schemaString.getBytes(SCHEMA_REGISTRY_SERVICE_ENCODING)));
                logger.verbose("Cached schema string. Group: '{}', name: '{}'", schemaGroup, schemaName);

                SimpleResponse<String> schemaIdResponse = new SimpleResponse<>(
                    response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), schemaId.getId());
                sink.next(schemaIdResponse);
            });
    }

    /**
     * Explicit call to clear all caches.
     */
    void clearCache() {
        idCache.clear();
        schemaStringCache.clear();
        typeParserMap.clear();
    }

    // TODO: max age for schema maps? or will schemas always be immutable?

    /**
     * Checks if caches should be reinitialized to satisfy initial configuration
     */
    private void resetIfNeeded() {
        // todo add verbose log
        if (idCache.size() > this.maxSchemaMapSize) {
            idCache.clear();
            logger.verbose("Cleared schema ID cache.");
        }
        if (schemaStringCache.size() > this.maxSchemaMapSize) {
            schemaStringCache.clear();
            logger.verbose("Cleared schema string cache.");
        }
    }

    private String getSchemaStringCacheKey(String schemaGroup, String schemaName, String schemaString) {
        return schemaGroup + schemaName + schemaString;
    }
}
