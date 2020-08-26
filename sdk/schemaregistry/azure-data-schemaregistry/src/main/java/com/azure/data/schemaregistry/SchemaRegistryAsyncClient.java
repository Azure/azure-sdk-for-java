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
import com.azure.data.schemaregistry.implementation.AzureSchemaRegistryRestService;
import com.azure.data.schemaregistry.implementation.models.SchemaId;
import com.azure.data.schemaregistry.models.SchemaRegistryObject;
import com.azure.data.schemaregistry.models.SerializationType;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;
import reactor.core.publisher.Mono;

/**
 * HTTP-based client that interacts with Azure Schema Registry service to store and retrieve schemas on demand.
 * <p>
 * Utilizes in-memory {@link Map} caching to minimize network I/O. Max size can be configured when instantiating by
 * using {@link SchemaRegistryClientBuilder#maxCacheSize(int)}, otherwise {@code 1000} will be used as the
 * default.
 * <p>
 * Two maps are maintained.
 * <ul>
 * <li>SchemaRegistryObject cache by GUID - stores GUIDs previously seen in payloads.</li>
 * <li>SchemaRegistryObject cache by schema string - minimizes HTTP calls when sending payloads of same schema.</li>
 * </ul>
 * <p>
 *
 * @see SchemaRegistryClientBuilder Follows builder pattern for object instantiation
 */
@ServiceClient(
    builder = SchemaRegistryClientBuilder.class,
    serviceInterfaces = AzureSchemaRegistryRestService.class)
public final class SchemaRegistryAsyncClient {

    private final ClientLogger logger = new ClientLogger(SchemaRegistryAsyncClient.class);

    static final Charset SCHEMA_REGISTRY_SERVICE_ENCODING = StandardCharsets.UTF_8;
    static final int MAX_SCHEMA_MAP_SIZE_DEFAULT = 1000;
    static final int MAX_SCHEMA_MAP_SIZE_MINIMUM = 10;

    private final AzureSchemaRegistryRestService restService;
    private final Integer maxSchemaMapSize;
    private final ConcurrentSkipListMap<String, Function<String, Object>> typeParserMap;
    private final Map<String, SchemaRegistryObject> idCache;
    private final Map<String, SchemaRegistryObject> schemaStringCache;

    SchemaRegistryAsyncClient(
        AzureSchemaRegistryRestService restService,
        int maxSchemaMapSize,
        ConcurrentSkipListMap<String, Function<String, Object>> typeParserMap) {
        this.restService = restService;
        this.maxSchemaMapSize = maxSchemaMapSize;
        this.typeParserMap = typeParserMap;
        this.idCache = new ConcurrentHashMap<>();
        this.schemaStringCache = new ConcurrentHashMap<>();
    }

    // testing - todo remove constructor and replace with mock
    SchemaRegistryAsyncClient(
        AzureSchemaRegistryRestService restService,
        Map<String, SchemaRegistryObject> idCache,
        Map<String, SchemaRegistryObject> schemaStringCache,
        ConcurrentSkipListMap<String, Function<String, Object>> typeParserMap) {
        this.restService = restService; // mockable
        this.idCache = idCache;
        this.schemaStringCache = schemaStringCache;
        this.typeParserMap = typeParserMap;
        this.maxSchemaMapSize = MAX_SCHEMA_MAP_SIZE_DEFAULT;
    }

    /**
     *
     * @param schemaGroup
     * @param schemaName
     * @param schemaString
     * @param serializationType
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SchemaRegistryObject> registerSchema(
        String schemaGroup, String schemaName, String schemaString, SerializationType serializationType) {

        if (schemaStringCache.containsKey(getSchemaStringCacheKey(schemaGroup, schemaName, schemaString))) {
            logger.verbose(
                "Cache hit schema string. Group: '{}', name: '{}', schema type: '{}', payload: '{}'",
                schemaGroup, schemaName, serializationType, schemaString);
            return Mono.fromCallable(
                () -> schemaStringCache.get(getSchemaStringCacheKey(schemaGroup, schemaName, schemaString)));
        }

        return registerSchemaWithResponse(schemaGroup, schemaName, schemaString, serializationType)
            .map(response -> response.getValue());
    }

    /**
     *
     * @param schemaGroup group under which schema is registered
     * @param schemaName schema name
     * @param schemaString string representation of schema
     * @param serializationType serialization format represented by schema
     * @return Response containing SchemaRegistryObject
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SchemaRegistryObject>> registerSchemaWithResponse(String schemaGroup, String schemaName,
        String schemaString, SerializationType serializationType) {
        return registerSchemaWithResponse(schemaGroup, schemaName, schemaString, serializationType, Context.NONE);
    }

    /**
     * Registers schema in registry service.  If the schema already exists, the existing schema ID is returned.
     * Otherwise, the schema is registered with a new schema ID.
     *
     * @param schemaGroup group under which schema is registered
     * @param schemaName schema name
     * @param schemaString string representation of schema
     * @param serializationType serialization format represented by schema
     * @param context HTTP client context
     * @return Response containing SchemaRegistryObject
     */
     Mono<Response<SchemaRegistryObject>> registerSchemaWithResponse(String schemaGroup, String schemaName,
        String schemaString, SerializationType serializationType, Context context) {
        logger.verbose(
            "Registering schema. Group: '{}', name: '{}', serialization type: '{}', payload: '{}'",
            schemaGroup, schemaName, serializationType, schemaString);

         return this.restService
             .createSchemaWithResponseAsync(schemaGroup, schemaName, serializationType.toString(), schemaString)
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

                 SchemaRegistryObject registered = new SchemaRegistryObject(schemaId.getId(),
                     serializationType,
                     schemaName,
                     schemaGroup,
                     schemaString.getBytes(SCHEMA_REGISTRY_SERVICE_ENCODING),
                     getParseFunc(serializationType));

                 resetIfNeeded();
                 schemaStringCache
                     .putIfAbsent(getSchemaStringCacheKey(schemaGroup, schemaName, schemaString), registered);
                 logger.verbose("Cached schema string. Group: '{}', name: '{}'", schemaGroup, schemaName);
                 SimpleResponse<SchemaRegistryObject> schemaRegistryObjectSimpleResponse = new SimpleResponse<>(
                     response.getRequest(), response.getStatusCode(),
                     response.getHeaders(), registered);
                 sink.next(schemaRegistryObjectSimpleResponse);
             });
    }

    /**
     * Retrieve schema from registry by ID.
     *
     * @param schemaId schema ID
     * @return SchemaRegistryObject containing schema and associated metadata
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SchemaRegistryObject> getSchema(String schemaId) {
        if (idCache.containsKey(schemaId)) {
            logger.verbose("Cache hit for schema id '{}'", schemaId);
            return Mono.fromCallable(() -> idCache.get(schemaId));
        }
        return getSchemaWithResponse(schemaId).map(Response::getValue);
    }

    /**
     * Get schema by schema ID
     * @param schemaId schema ID
     * @return Response with SchemaRegistryObject containing schema and associated metadata
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SchemaRegistryObject>> getSchemaWithResponse(String schemaId) {
        return getSchemaWithResponse(schemaId, Context.NONE);
    }

    /**
     *
     * @param schemaId
     * @param context HTTP client context
     * @return Response containing SchemaRegistryObject
     */
    Mono<Response<SchemaRegistryObject>> getSchemaWithResponse(String schemaId, Context context) {
        Objects.requireNonNull(schemaId, "'schemaId' should not be null");
        return this.restService.getSchemaByIdWithResponseAsync(schemaId)
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

                SchemaRegistryObject schemaObject = new SchemaRegistryObject(schemaId,
                    serializationType,
                    null,
                    null,
                    response.getValue().getBytes(SCHEMA_REGISTRY_SERVICE_ENCODING),
                    getParseFunc(serializationType));

                resetIfNeeded();
                idCache.putIfAbsent(schemaId, schemaObject);
                logger.verbose("Cached schema object. Path: '{}'", schemaId);
                SimpleResponse<SchemaRegistryObject> schemaRegistryObjectSimpleResponse = new SimpleResponse<>(
                    response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), schemaObject);
                sink.next(schemaRegistryObjectSimpleResponse);
            });
    }

    /**
     *
     * @param schemaGroup group under which schema is registered
     * @param schemaName schema name
     * @param schemaString string representation of schema
     * @param serializationType serialization format represented by schema
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> getSchemaId(String schemaGroup, String schemaName, String schemaString,
        SerializationType serializationType) {
        String cacheKey = getSchemaStringCacheKey(schemaGroup, schemaName, schemaString);
        if (schemaStringCache.containsKey(cacheKey)) {
            logger.verbose("Cache hit schema string. Group: '{}', name: '{}'", schemaGroup, schemaName);
            return Mono.fromCallable(() -> schemaStringCache.get(cacheKey).getSchemaId());
        }

        return getSchemaIdWithResponse(schemaGroup, schemaName, schemaString, serializationType)
            .map(response -> response.getValue());
    }

    /**
     *
     * @param schemaGroup group under which schema is registered
     * @param schemaName schema name
     * @param schemaString string representation of schema
     * @param serializationType serialization format represented by schema
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> getSchemaIdWithResponse(String schemaGroup, String schemaName, String schemaString,
        SerializationType serializationType) {
        return getSchemaIdWithResponse(schemaGroup, schemaName, schemaString, serializationType, Context.NONE);
    }

    /**
     *
     * @param schemaGroup group under which schema is registered
     * @param schemaName schema name
     * @param schemaString string representation of schema
     * @param serializationType serialization format represented by schema
     * @param context HTTP client context
     * @return Response containing schema ID as string
     */
    Mono<Response<String>> getSchemaIdWithResponse(String schemaGroup, String schemaName, String schemaString,
        SerializationType serializationType, Context context) {

        return this.restService
            .getIdBySchemaContentWithResponseAsync(schemaGroup, schemaName, serializationType.toString(),
                schemaString)
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
                    new SchemaRegistryObject(
                        schemaId.getId(),
                        serializationType,
                        schemaName,
                        schemaGroup,
                        schemaString.getBytes(SCHEMA_REGISTRY_SERVICE_ENCODING),
                        getParseFunc(serializationType)));
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

    /**
     * Return stored parse function for parsing schema payloads of specified schema type
     *
     * @param serializationType schema type of payload to be deserialized
     * @return parse method for deserializing schema string
     */
    private Function<String, Object> getParseFunc(SerializationType serializationType) {
        Function<String, Object> parseFunc = typeParserMap.get(serializationType.toString());

        if (parseFunc == null) {
            throw logger.logExceptionAsError(new NullPointerException(
                String.format("Unexpected serialization type '%s' received.  Currently loaded parsers: %s",
                    serializationType, typeParserMap.keySet().toString())));
        }
        return parseFunc;
    }

    /**
     * Returns unique cache key for schema by group, name, and schema string.
     *
     * Serialization type is not part of the key - SchemaRegistryObjects are only added to the cache when returned
     * from the service.  Service calls require serialization type to be passed for group settings validation.
     *
     * @param schemaGroup group under which schema is registered
     * @param schemaName schema registered name
     * @param schemaString string representation of schema
     * @return
     */
    private String getSchemaStringCacheKey(String schemaGroup, String schemaName, String schemaString) {
        return schemaGroup + schemaName + schemaString;
    }
}
