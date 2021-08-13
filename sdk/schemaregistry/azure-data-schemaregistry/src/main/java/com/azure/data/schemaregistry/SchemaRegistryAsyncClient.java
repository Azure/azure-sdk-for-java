// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.implementation.AzureSchemaRegistry;
import com.azure.data.schemaregistry.implementation.models.SchemaId;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SerializationType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

/**
 * HTTP-based client that interacts with Azure Schema Registry service to store and retrieve schemas on demand.
 * @see SchemaRegistryClientBuilder Follows builder pattern for object instantiation
 */
@ServiceClient(builder = SchemaRegistryClientBuilder.class, isAsync = true)
public final class SchemaRegistryAsyncClient {

    static final Charset SCHEMA_REGISTRY_SERVICE_ENCODING = StandardCharsets.UTF_8;

    private final ClientLogger logger = new ClientLogger(SchemaRegistryAsyncClient.class);
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
        return FluxUtil.withContext(context -> registerSchemaWithResponse(schemaGroup, schemaName, schemaString,
            serializationType, context));
    }

    Mono<Response<SchemaProperties>> registerSchemaWithResponse(String schemaGroup, String schemaName,
                                    String schemaString, SerializationType serializationType, Context context) {
        logger.verbose("Registering schema. Group: '{}', name: '{}', serialization type: '{}', payload: '{}'",
            schemaGroup, schemaName, serializationType, schemaString);

        return this.restService.getSchemas().registerWithResponseAsync(schemaGroup, schemaName,
            com.azure.data.schemaregistry.implementation.models.SerializationType.AVRO, schemaString)
            .handle((response, sink) -> {
                if (response.getStatusCode() == 400) {
                    final SchemaRegistryErrorHttpResponse exception = new SchemaRegistryErrorHttpResponse(
                        response.getRequest(), response.getStatusCode(), response.getHeaders());

                    sink.error(logger.logExceptionAsError(new HttpResponseException(exception)));
                    return;
                }

                SchemaId schemaId = response.getValue();

                SchemaProperties registered = new SchemaProperties(schemaId.getId(),
                    serializationType,
                    schemaName,
                    schemaString.getBytes(SCHEMA_REGISTRY_SERVICE_ENCODING));

                schemaStringCache.putIfAbsent(getSchemaStringCacheKey(schemaGroup, schemaName, schemaString),
                    registered);
                idCache.putIfAbsent(schemaId.getId(), registered);

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
    Mono<Response<SchemaProperties>> getSchemaWithResponse(String schemaId) {
        return FluxUtil.withContext(context -> getSchemaWithResponse(schemaId, context));
    }

    Mono<Response<SchemaProperties>> getSchemaWithResponse(String schemaId, Context context) {
        Objects.requireNonNull(schemaId, "'schemaId' should not be null");
        return this.restService.getSchemas().getByIdWithResponseAsync(schemaId)
            .handle((response, sink) -> {
                if (response.getStatusCode() == 404) {
                    sink.error(logger.logExceptionAsError(new ResourceNotFoundException(
                        String.format("Schema does not exist, id: '%s'", schemaId),
                        new SchemaRegistryErrorHttpResponse(response.getRequest(), response.getStatusCode(),
                            response.getHeaders()))));

                    return;
                }

                SerializationType serializationType =
                    SerializationType.fromString(response.getDeserializedHeaders().getSchemaType());

                SchemaProperties schemaObject = new SchemaProperties(schemaId,
                    serializationType,
                    null,
                    response.getValue());

                idCache.putIfAbsent(schemaId, schemaObject);
                logger.verbose("Cached schema object. Path: '{}'", schemaId);
                SimpleResponse<SchemaProperties> schemaRegistryObjectSimpleResponse = new SimpleResponse<>(
                    response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), schemaObject);
                sink.next(schemaRegistryObjectSimpleResponse);
            });
    }

    /**
     * Gets the schema identifier associated with the given schema. Gets a cached value if it exists, otherwise makes a
     * call to the service.
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
            return Mono.fromCallable(() -> {
                logger.verbose("Cache hit schema string. Group: '{}', name: '{}'", schemaGroup, schemaName);
                return schemaStringCache.get(schemaStringCacheKey).getSchemaId();
            });
        }

        return getSchemaIdWithResponse(schemaGroup, schemaName, schemaString, serializationType)
            .map(response -> response.getValue());
    }

    /**
     * Gets the schema identifier associated with the given schema. Always makes a call to the service.
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

        return FluxUtil.withContext(context ->
            getSchemaIdWithResponse(schemaGroup, schemaName, schemaString, serializationType, context));
    }

    /**
     * Gets the schema id associated with the schema name a string representation of the schema.
     *
     * @param schemaGroup The schema group.
     * @param schemaName The schema name.
     * @param schemaString The string representation of the schema.
     * @param serializationType The serialization type of this schema.
     * @param context Context to pass along with this request.
     * @return A mono that completes with the schema id.
     */
    Mono<Response<String>> getSchemaIdWithResponse(String schemaGroup, String schemaName, String schemaString,
        SerializationType serializationType, Context context) {

        return this.restService.getSchemas()
            .queryIdByContentWithResponseAsync(schemaGroup, schemaName,
                com.azure.data.schemaregistry.implementation.models.SerializationType.AVRO, schemaString)
            .handle((response, sink) -> {
                if (response.getStatusCode() == 404) {
                    sink.error(logger.logThrowableAsError(new ResourceNotFoundException(
                        "Matching schema not found", new SchemaRegistryErrorHttpResponse(response.getRequest(),
                        response.getStatusCode(), response.getHeaders()))));

                    return;
                }

                SchemaId schemaId = response.getValue();
                SchemaProperties properties = new SchemaProperties(schemaId.getId(), serializationType, schemaName,
                    schemaString.getBytes(SCHEMA_REGISTRY_SERVICE_ENCODING));

                schemaStringCache.putIfAbsent(
                    getSchemaStringCacheKey(schemaGroup, schemaName, schemaString), properties);
                idCache.putIfAbsent(schemaId.getId(), properties);

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

    private static String getSchemaStringCacheKey(String schemaGroup, String schemaName, String schemaString) {
        return schemaGroup + schemaName + schemaString;
    }

    /**
     * Represents an erroneous response from Schema Registry.
     */
    private static final class SchemaRegistryErrorHttpResponse extends HttpResponse {
        private final int statusCode;
        private final HttpHeaders headers;

        private SchemaRegistryErrorHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers) {
            super(request);
            this.statusCode = statusCode;
            this.headers = headers;
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public String getHeaderValue(String name) {
            return headers.getValue(name);
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return Flux.empty();
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return Mono.empty();
        }

        @Override
        public Mono<String> getBodyAsString() {
            return Mono.empty();
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return Mono.empty();
        }
    }
}
