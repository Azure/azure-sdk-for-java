// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.schemaregistry.client;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.schemaregistry.client.implementation.AzureSchemaRegistryRestService;
import com.azure.schemaregistry.client.implementation.AzureSchemaRegistryRestServiceClientBuilder;
import com.azure.schemaregistry.client.implementation.models.GetSchemaByIdResponse;
import com.azure.schemaregistry.client.implementation.models.SchemaId;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * HTTP-based client that interacts with Azure Schema Registry service to store and retrieve schemas on demand.
 * <p>
 * Utilizes in-memory {@link Map} caching to minimize network I/O. Max size can be configured when instantiating by
 * using {@link CachedSchemaRegistryClientBuilder#maxSchemaMapSize}, otherwise {@code 1000} will be used as the default.
 * <p>
 * Two maps are maintained.
 * <ul>
 * <li>SchemaRegistryObject cache by GUID - stores GUIDs previously seen in payloads.</li>
 * <li>SchemaRegistryObject cache by schema string - minimizes HTTP calls when sending payloads of same schema.</li>
 * </ul>
 * <p>
 *
 * @see SchemaRegistryClient Implements SchemaRegistryClient interface to allow for testing with mock
 * @see CachedSchemaRegistryClientBuilder Follows builder pattern for object instantiation
 */
@ServiceClient(
    builder = CachedSchemaRegistryClientBuilder.class,
    serviceInterfaces = AzureSchemaRegistryRestService.class)
public final class CachedSchemaRegistryClient implements SchemaRegistryClient {
    private final ClientLogger logger = new ClientLogger(CachedSchemaRegistryClient.class);

    public static final Charset SCHEMA_REGISTRY_SERVICE_ENCODING = StandardCharsets.UTF_8;

    static final int MAX_SCHEMA_MAP_SIZE_DEFAULT = 1000;
    static final int MAX_SCHEMA_MAP_SIZE_MINIMUM = 10;

    private final AzureSchemaRegistryRestService restService;
    private final int maxSchemaMapSize;
    private final Map<String, Function<String, Object>> typeParserMap;
    private final Map<String, SchemaRegistryObject> guidCache;
    private final Map<String, SchemaRegistryObject> schemaStringCache;

    CachedSchemaRegistryClient(
        String registryUrl,
        HttpPipeline pipeline,
        int maxSchemaMapSize,
        Map<String, Function<String, Object>> typeParserMap) {
        if (CoreUtils.isNullOrEmpty(registryUrl)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Schema Registry URL cannot be null or empty."));
        }

        this.restService = new AzureSchemaRegistryRestServiceClientBuilder()
            .host(registryUrl)
            .pipeline(pipeline)
            .buildClient();

        this.maxSchemaMapSize = maxSchemaMapSize;
        this.typeParserMap = typeParserMap;
        this.guidCache = new ConcurrentHashMap<>();
        this.schemaStringCache = new ConcurrentHashMap<>();
    }

    // testing - todo remove constructor and replace with mock
    CachedSchemaRegistryClient(
        AzureSchemaRegistryRestService restService,
        Map<String, SchemaRegistryObject> guidCache,
        Map<String, SchemaRegistryObject> schemaStringCache,
        Map<String, Function<String, Object>> typeParserMap) {
        this.restService = restService; // mockable
        this.guidCache = guidCache;
        this.schemaStringCache = schemaStringCache;
        this.typeParserMap = typeParserMap;
        this.maxSchemaMapSize = MAX_SCHEMA_MAP_SIZE_DEFAULT;
    }

    /**
     * @return Azure Schema Registry service string encoding
     */
    @Override
    public Charset getEncoding() {
        return CachedSchemaRegistryClient.SCHEMA_REGISTRY_SERVICE_ENCODING;
    }

    /**
     * @param schemaType case-insensitive tag used by schema registry store to identify schema type, e.g. "avro"
     * @param parseMethod function to parse string into usable schema object
     * @throws IllegalArgumentException on bad schema type or if parser for schema type has already been registered
     */
    public void loadSchemaParser(String schemaType, Function<String, Object> parseMethod) {
        if (CoreUtils.isNullOrEmpty(schemaType)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Serialization type cannot be null or empty."));
        }
        if (this.typeParserMap.containsKey(schemaType.toLowerCase(Locale.ROOT))) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Multiple parse methods for single serialization type may not be added."));
        }
        this.typeParserMap.putIfAbsent(schemaType.toLowerCase(Locale.ROOT), parseMethod);
        logger.verbose(
            "Loaded parser for '{}' serialization format.", schemaType.toLowerCase(Locale.ROOT));
    }

    @Override
    public SchemaRegistryObject register(
        String schemaGroup, String schemaName, String schemaString, String serializationType) {
        if (schemaStringCache.containsKey(schemaString)) {
            logger.verbose(
                    "Cache hit schema string. Group: '{}', name: '{}', serialization type: '{}', payload: '{}'",
                    schemaGroup, schemaName, serializationType, schemaString);
            return schemaStringCache.get(schemaString);
        }

        logger.verbose(
            "Registering schema. Group: '{}', name: '{}', serialization type: '{}', payload: '{}'",
                schemaGroup, schemaName, serializationType, schemaString);

        SchemaId schemaId;
        try {
            schemaId = this.restService.createSchema(schemaGroup, schemaName, schemaString, serializationType);
        } catch (HttpResponseException e) {
            throw logger.logExceptionAsError(new SchemaRegistryClientException("Register operation failed.", e));
        }

        SchemaRegistryObject registered = new SchemaRegistryObject(schemaId.getId(),
            serializationType,
            schemaString.getBytes(SCHEMA_REGISTRY_SERVICE_ENCODING),
            getParseFunc(serializationType));

        resetIfNeeded();
        schemaStringCache.putIfAbsent(schemaString, registered);
        logger.verbose("Cached schema string. Group: '{}', name: '{}'", schemaGroup, schemaName);
        return registered;
    }

    @Override
    public SchemaRegistryObject getSchemaByGuid(String schemaId) {
        if (guidCache.containsKey(schemaId)) {
            logger.verbose("Cache hit for schema id '{}'", schemaId);
            return guidCache.get(schemaId);
        }

        GetSchemaByIdResponse response;
        try {
            response = this.restService.getSchemaByIdWithResponseAsync(UUID.fromString(schemaId)).block();
        } catch (HttpResponseException e) {
            throw logger.logExceptionAsError(new SchemaRegistryClientException("Fetching schema failed.", e));
        }

        if (response == null) {
            throw logger.logExceptionAsError(
                new SchemaRegistryClientException("HTTP client returned null schema response"));
        }

        String schemaType = response.getDeserializedHeaders().getXSchemaType();

        SchemaRegistryObject schemaObject = new SchemaRegistryObject(schemaId,
            schemaType,
            response.getValue().getBytes(SCHEMA_REGISTRY_SERVICE_ENCODING),
            getParseFunc(schemaType));

        resetIfNeeded();
        guidCache.putIfAbsent(schemaId, schemaObject);
        logger.verbose("Cached schema object. Path: '{}'", schemaId);
        return schemaObject;
    }

    @Override
    public String getSchemaId(
        String schemaGroup, String schemaName, String schemaString, String schemaType) {
        if (schemaStringCache.containsKey(schemaString)) {
            logger.verbose("Cache hit schema string. Group: '{}', name: '{}'", schemaGroup, schemaName);
            return schemaStringCache.get(schemaString).getSchemaId();
        }

        SchemaId schemaId;
        try {
            schemaId = this.restService.getIdBySchemaContent(schemaGroup, schemaName, schemaType, schemaString);
        } catch (HttpResponseException e) {
            throw logger.logExceptionAsError(new SchemaRegistryClientException(
                String.format("Failed to fetch schema guid for schema. Group: '%s', name: '%s'",
                    schemaGroup, schemaName),
                e));
        }

        resetIfNeeded();
        schemaStringCache.putIfAbsent(
            schemaString,
            new SchemaRegistryObject(
                schemaId.getId(),
                schemaType,
                schemaString.getBytes(SCHEMA_REGISTRY_SERVICE_ENCODING),
                getParseFunc(schemaType)));
        logger.verbose("Cached schema string. Group: '{}', name: '{}'", schemaGroup, schemaName);
        return schemaId.getId();
    }

    @Override
    public String deleteSchemaVersion(String schemaGroup, String schemaName, int version) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String deleteLatestSchemaVersion(String schemaGroup, String schemaName) {
        // return this.restService.deleteSchemaVersion(schemaName, null);
        // remove from cache
        return null;
    }

    @Override
    public List<String> deleteSchema(String schemaGroup, String schemaName) {
        // return this.restService.deleteSchema();
        // remove from cache
        return null;
    }

    /**
     * Explicit call to clear all caches.
     */
    public void reset() {
        guidCache.clear();
        schemaStringCache.clear();
        typeParserMap.clear();
    }

    // TODO: max age for schema maps? or will schemas always be immutable?
    /**
     * Checks if caches should be reinitialized to satisfy initial configuration
     */
    private void resetIfNeeded() {
        // todo add verbose log
        if (guidCache.size() > this.maxSchemaMapSize) {
            guidCache.clear();
        }
        if (schemaStringCache.size() > this.maxSchemaMapSize) {
            schemaStringCache.clear();
        }
    }

    /**
     * Return stored parse function for parsing schema payloads of specified schema type
     *
     * @param schemaType schema type of payload to be deserialized
     * @return parse method for deserializing schema string
     */
    private Function<String, Object> getParseFunc(String schemaType) {
        Function<String, Object> parseFunc = typeParserMap.get(schemaType.toLowerCase(Locale.ROOT));

        if (parseFunc == null) {
            throw logger.logExceptionAsError(new SchemaRegistryClientException(
                String.format("Unexpected serialization type '%s' received.  Currently loaded parsers: %s",
                    schemaType,
                    typeParserMap.keySet().toString())));
        }
        return parseFunc;
    }
}
