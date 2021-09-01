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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;
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
                final SerializationType serializationType =
                    SerializationType.fromString(response.getDeserializedHeaders().getSchemaType());
                final URI location = URI.create(response.getDeserializedHeaders().getLocation());
                final Matcher matcher = SCHEMA_PATTERN.matcher(location.getPath());

                if (!matcher.lookingAt()) {
                    sink.error(new IllegalArgumentException("Response location does not contain schema group or"
                        + " schema name. Location: " + location.getPath()));

                    return;
                }

                final String schemaGroup = matcher.group("schemaGroup");
                final String schemaName = matcher.group("schemaName");
                final SchemaProperties schemaObject = new SchemaProperties(schemaId,
                    serializationType,
                    schemaName,
                    response.getValue());
                final String schemaCacheKey = getSchemaStringCacheKey(schemaGroup, schemaName,
                     new String(response.getValue(), SCHEMA_REGISTRY_SERVICE_ENCODING));

                schemaStringCache.putIfAbsent(schemaCacheKey, schemaObject);
                idCache.putIfAbsent(schemaId, schemaObject);

                logger.verbose("Cached schema object. Path: '{}'", schemaId);

                SimpleResponse<SchemaProperties> schemaResponse = new SimpleResponse<>(
                    response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), schemaObject);

                sink.next(schemaResponse);
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
    Mono<Response<String>> getSchemaIdWithResponse(String schemaGroup, String schemaName, String schemaString,
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

    private static String getSchemaStringCacheKey(String schemaGroup, String schemaName, String schemaString) {
        return schemaGroup + schemaName + schemaString;
    }
}
