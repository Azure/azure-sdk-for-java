// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.client;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.client.implementation.AzureSchemaRegistryRestService;
import com.azure.data.schemaregistry.client.implementation.models.SchemaId;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

/**
 * HTTP-based client that interacts with Azure Schema Registry service to store and retrieve schemas on demand.
 * <p>
 * Utilizes in-memory {@link Map} caching to minimize network I/O. Max size can be configured when instantiating by
 * using {@link CachedSchemaRegistryClientBuilder#maxCacheSize(int)}, otherwise {@code 1000} will be used as the
 * default.
 * <p>
 * Two maps are maintained.
 * <ul>
 * <li>SchemaRegistryObject cache by GUID - stores GUIDs previously seen in payloads.</li>
 * <li>SchemaRegistryObject cache by schema string - minimizes HTTP calls when sending payloads of same schema.</li>
 * </ul>
 * <p>
 *
 * @see CachedSchemaRegistryClientBuilder Follows builder pattern for object instantiation
 */
@ServiceClient(
    builder = CachedSchemaRegistryClientBuilder.class,
    serviceInterfaces = AzureSchemaRegistryRestService.class)
public final class CachedSchemaRegistryAsyncClient {

    private final ClientLogger logger = new ClientLogger(CachedSchemaRegistryAsyncClient.class);

    static final Charset SCHEMA_REGISTRY_SERVICE_ENCODING = StandardCharsets.UTF_8;
    static final int MAX_SCHEMA_MAP_SIZE_DEFAULT = 1000;
    static final int MAX_SCHEMA_MAP_SIZE_MINIMUM = 10;

    private final AzureSchemaRegistryRestService restService;
    private final Integer maxSchemaMapSize;
    private final ConcurrentSkipListMap<String, Function<String, Object>> typeParserMap;
    private final Map<String, SchemaRegistryObject> idCache;
    private final Map<String, SchemaRegistryObject> schemaStringCache;

    CachedSchemaRegistryAsyncClient(
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
    CachedSchemaRegistryAsyncClient(
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

    public Mono<SchemaRegistryObject> registerSchema(
        String schemaGroup, String schemaName, String schemaString, String schemaType) {
        if (schemaStringCache.containsKey(getSchemaStringCacheKey(schemaGroup, schemaName, schemaString))) {
            logger.verbose(
                "Cache hit schema string. Group: '{}', name: '{}', schema type: '{}', payload: '{}'",
                schemaGroup, schemaName, schemaType, schemaString);
            return Mono.fromCallable(
                () -> schemaStringCache.get(getSchemaStringCacheKey(schemaGroup, schemaName, schemaString)));
        }

        return registerSchemaWithResponse(schemaGroup, schemaName, schemaString, schemaType)
            .map(response -> response.getValue());
    }

    public Mono<Response<SchemaRegistryObject>> registerSchemaWithResponse(String schemaGroup, String schemaName,
        String schemaString, String schemaType) {
        return registerSchemaWithResponse(schemaGroup, schemaName, schemaString, schemaType, Context.NONE);
    }

     Mono<Response<SchemaRegistryObject>> registerSchemaWithResponse(String schemaGroup, String schemaName,
        String schemaString, String schemaType, Context context) {
        logger.verbose(
            "Registering schema. Group: '{}', name: '{}', serialization type: '{}', payload: '{}'",
            schemaGroup, schemaName, schemaType, schemaString);

        return this.restService.createSchemaWithResponseAsync(schemaGroup, schemaName, schemaType, schemaString)
            .onErrorMap(HttpResponseException.class, e -> logger.logExceptionAsError(
                new SchemaRegistryClientException("Register operation failed, unexpected service response.", e)))
            .handle((response, sink) -> {
                if (response == null) {
                    sink.error(logger.logExceptionAsError(
                        new SchemaRegistryClientException("Client returned null response")));
                    return;
                }

                if (response.getStatusCode() == 400) {
                    sink.error(logger.logExceptionAsError(
                        new SchemaRegistryClientException("Invalid schema registration attempted")));
                    return;
                }

                SchemaId schemaId = response.getValue();

                SchemaRegistryObject registered = new SchemaRegistryObject(schemaId.getId(),
                    schemaType,
                    schemaString.getBytes(SCHEMA_REGISTRY_SERVICE_ENCODING),
                    getParseFunc(schemaType));

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

    public Mono<SchemaRegistryObject> getSchema(String schemaId) {
        if (idCache.containsKey(schemaId)) {
            logger.verbose("Cache hit for schema id '{}'", schemaId);
            return Mono.fromCallable(() -> idCache.get(schemaId));
        }
        return getSchemaWithResponse(schemaId).map(Response::getValue);
    }

    public Mono<Response<SchemaRegistryObject>> getSchemaWithResponse(String schemaId) {
        return getSchemaWithResponse(schemaId, Context.NONE);
    }

    Mono<Response<SchemaRegistryObject>> getSchemaWithResponse(String schemaId, Context context) {
        Objects.requireNonNull(schemaId, "'schemaId' should not be null");
        return this.restService.getSchemaByIdWithResponseAsync(schemaId)
            .onErrorMap(HttpResponseException.class,
                e -> logger.logExceptionAsError(
                    new SchemaRegistryClientException("Fetching schema failed, unexpected service response.", e)))
            .handle((response, sink) -> {
                if (response == null) {
                    sink.error(logger.logExceptionAsError(
                        new SchemaRegistryClientException("Client returned null response")));
                    return;
                }

                if (response.getStatusCode() == 404) {
                    sink.error(logger.logExceptionAsError(
                        new SchemaRegistryClientException(String.format("Schema does not exist, id %s", schemaId))));
                    return;
                }

                String schemaType = response.getDeserializedHeaders().getXSchemaType();

                SchemaRegistryObject schemaObject = new SchemaRegistryObject(schemaId,
                    schemaType,
                    response.getValue().getBytes(SCHEMA_REGISTRY_SERVICE_ENCODING),
                    getParseFunc(schemaType));

                resetIfNeeded();
                idCache.putIfAbsent(schemaId, schemaObject);
                logger.verbose("Cached schema object. Path: '{}'", schemaId);
                SimpleResponse<SchemaRegistryObject> schemaRegistryObjectSimpleResponse = new SimpleResponse<>(
                    response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), schemaObject);
                sink.next(schemaRegistryObjectSimpleResponse);
            });
    }


    public Mono<String> getSchemaId(String schemaGroup, String schemaName, String schemaString, String schemaType) {

        if (schemaStringCache.containsKey(getSchemaStringCacheKey(schemaGroup, schemaName, schemaString))) {
            logger.verbose("Cache hit schema string. Group: '{}', name: '{}'", schemaGroup, schemaName);
            return Mono.fromCallable(() -> schemaStringCache.get(schemaString).getSchemaId());
        }

        return getSchemaIdWithResponse(schemaGroup, schemaName, schemaString, schemaType)
            .map(response -> response.getValue());
    }

    public Mono<Response<String>> getSchemaIdWithResponse(String schemaGroup, String schemaName, String schemaString,
        String schemaType) {
        return getSchemaIdWithResponse(schemaGroup, schemaName, schemaString, schemaType, Context.NONE);
    }

    Mono<Response<String>> getSchemaIdWithResponse(String schemaGroup, String schemaName, String schemaString,
        String schemaType, Context context) {

        return this.restService.getIdBySchemaContentWithResponseAsync(schemaGroup, schemaName, schemaType, schemaString)
            .onErrorMap(HttpResponseException.class, e ->
                logger.logExceptionAsError(new SchemaRegistryClientException(String.format(
                    "Failed to fetch ID for schema, unexpected service response. Group: '%s', name: '%s'",
                    schemaGroup, schemaName), e)))
            .handle((response, sink) -> {
                if (response == null) {
                    sink.error(logger.logExceptionAsError(
                        new SchemaRegistryClientException("Client returned null response")));
                    return;
                }

                if (response.getStatusCode() == 404) {
                    sink.error(logger.logExceptionAsError(
                        new SchemaRegistryClientException("Existing matching schema not found.")));
                    return;
                }

                SchemaId schemaId = response.getValue();

                resetIfNeeded();
                schemaStringCache.putIfAbsent(
                    getSchemaStringCacheKey(schemaGroup, schemaName, schemaString),
                    new SchemaRegistryObject(
                        schemaId.getId(),
                        schemaType,
                        schemaString.getBytes(SCHEMA_REGISTRY_SERVICE_ENCODING),
                        getParseFunc(schemaType)));
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
    public void clearCache() {
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

    /**
     * Return stored parse function for parsing schema payloads of specified schema type
     *
     * @param schemaType schema type of payload to be deserialized
     * @return parse method for deserializing schema string
     */
    private Function<String, Object> getParseFunc(String schemaType) {
        Function<String, Object> parseFunc = typeParserMap.get(schemaType);

        if (parseFunc == null) {
            throw logger.logExceptionAsError(new SchemaRegistryClientException(
                String.format("Unexpected serialization type '%s' received.  Currently loaded parsers: %s",
                    schemaType,
                    typeParserMap.keySet().toString())));
        }
        return parseFunc;
    }

    private String getSchemaStringCacheKey(String schemaGroup, String schemaName, String schemaString) {
        return schemaGroup + schemaName + schemaString;
    }
}
