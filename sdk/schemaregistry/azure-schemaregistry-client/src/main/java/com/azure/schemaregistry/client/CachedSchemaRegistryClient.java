// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.schemaregistry.client;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.schemaregistry.client.rest.AzureSchemaRegistryRestService;
import com.azure.schemaregistry.client.rest.AzureSchemaRegistryRestServiceClientBuilder;
import com.azure.schemaregistry.client.rest.models.GetSchemaByIdResponse;
import com.azure.schemaregistry.client.rest.models.SchemaId;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

/**
 * HTTP-based client that interacts with Azure Schema Registry service to store and retrieve schemas on demand.
 * <p>
 * Utilizes in-memory {@link Map} caching to minimize network I/O. Max size can be configured when instantiating by using {@link CachedSchemaRegistryClientBuilder#maxSchemaMapSize}, otherwise {@code 1000} will be used as the default.
 * <p>
 * Two maps are maintained.
 * <ul>
 * <li>SchemaRegistryObject cache by GUID - stores GUIDs previously seen in payloads.</li>
 * <li>SchemaRegistryObject cache by schema string - minimizes HTTP calls when sending payloads of same schema.</li>
 * </ul>
 * <p>
 * TODO: implement max age for schema maps? or will schemas always be immutable?
 *
 * @see SchemaRegistryClient Implements SchemaRegistryClient interface to allow for testing with mock
 * @see CachedSchemaRegistryClientBuilder Follows builder pattern for object instantiation
 */
@ServiceClient(
    builder = CachedSchemaRegistryClientBuilder.class,
    serviceInterfaces = AzureSchemaRegistryRestService.class)
public class CachedSchemaRegistryClient implements SchemaRegistryClient {
    private final ClientLogger logger = new ClientLogger(CachedSchemaRegistryClient.class);

    public static final int MAX_SCHEMA_MAP_SIZE_DEFAULT = 1000;
    public static final Charset SCHEMA_REGISTRY_SERVICE_ENCODING = StandardCharsets.UTF_8;
    static final int MAX_SCHEMA_MAP_SIZE_MINIMUM = 10;

    private final AzureSchemaRegistryRestService restService;
    private final int maxSchemaMapSize;
    private final HashMap<String, Function<String, Object>> typeParserDictionary;
    private final HashMap<String, SchemaRegistryObject> guidCache;
    private final HashMap<String, SchemaRegistryObject> schemaStringCache;

    CachedSchemaRegistryClient(
        String registryUrl,
        HttpPipeline pipeline,
        int maxSchemaMapSize,
        HashMap<String, Function<String, Object>> typeParserDictionary) {
        if (registryUrl == null || registryUrl.isEmpty()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Schema Registry URL cannot be null or empty."));
        }

        if (maxSchemaMapSize < MAX_SCHEMA_MAP_SIZE_MINIMUM) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException(
                    String.format("Max schema map size must be greater than %d schemas", MAX_SCHEMA_MAP_SIZE_MINIMUM)));
        }

        this.restService = new AzureSchemaRegistryRestServiceClientBuilder()
            .host(registryUrl)
            .pipeline(pipeline)
            .buildClient();

        this.maxSchemaMapSize = maxSchemaMapSize;
        this.typeParserDictionary = typeParserDictionary;
        this.guidCache = new HashMap<>();
        this.schemaStringCache = new HashMap<>();
    }

    // testing
    CachedSchemaRegistryClient(
        AzureSchemaRegistryRestService restService,
        HashMap<String, SchemaRegistryObject> guidCache,
        HashMap<String, SchemaRegistryObject> schemaStringCache,
        HashMap<String, Function<String, Object>> typeParserDictionary) {
        this.restService = restService; // mockable
        this.guidCache = guidCache;
        this.schemaStringCache = schemaStringCache;
        this.typeParserDictionary = typeParserDictionary;
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
     * @param schemaType tag used by schema registry store to identify schema serialization type, e.g. "avro"
     * @param parseMethod function to parse string into usable schema object
     * @throws IllegalArgumentException on bad schema type or if parser for schema type has already been registered
     */
    public synchronized void loadSchemaParser(String schemaType, Function<String, Object> parseMethod) {
        if (schemaType == null || schemaType.isEmpty()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Serialization type cannot be null or empty."));
        }
        if (this.typeParserDictionary.containsKey(schemaType.toLowerCase(Locale.ENGLISH))) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Multiple parse methods for single serialization type may not be added."));
        }
        this.typeParserDictionary.put(schemaType.toLowerCase(Locale.ENGLISH), parseMethod);
        logger.verbose(
            String.format("Loaded parser for '%s' serialization format.", schemaType.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public synchronized SchemaRegistryObject register(
        String schemaGroup, String schemaName, String schemaString, String serializationType)
        throws SchemaRegistryClientException {
        if (schemaStringCache.containsKey(schemaString)) {
            logger.verbose(
                String.format(
                    "Cache hit schema string. Group: '%s', name: '%s', serialization type: '%s', payload: '%s'",
                    schemaGroup, schemaName, serializationType, schemaString));
            return schemaStringCache.get(schemaString);
        }

        logger.verbose(
            String.format("Registering schema. Group: '%s', name: '%s', serialization type: '%s', payload: '%s'",
                schemaGroup, schemaName, serializationType, schemaString));

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
        schemaStringCache.put(schemaString, registered);
        logger.verbose(String.format("Cached schema string. Group: '%s', name: '%s'", schemaGroup, schemaName));
        return registered;
    }

    @Override
    public synchronized SchemaRegistryObject getSchemaByGuid(String schemaId)
        throws SchemaRegistryClientException {
        if (guidCache.containsKey(schemaId)) {
            logger.verbose(String.format("Cache hit for schema id '%s'", schemaId));
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
        guidCache.put(schemaId, schemaObject);
        logger.verbose(String.format("Cached schema object. Path: '%s'", schemaId));
        return schemaObject;
    }

    @Override
    public synchronized String getSchemaId(
        String schemaGroup, String schemaName, String schemaString, String schemaType)
        throws SchemaRegistryClientException {
        if (schemaStringCache.containsKey(schemaString)) {
            logger.verbose(String.format("Cache hit schema string. Group: '%s', name: '%s'", schemaGroup, schemaName));
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
        schemaStringCache.put(
            schemaString,
            new SchemaRegistryObject(
                schemaId.getId(),
                schemaType,
                schemaString.getBytes(SCHEMA_REGISTRY_SERVICE_ENCODING),
                getParseFunc(schemaType)));
        logger.verbose(String.format("Cached schema string. Group: '%s', name: '%s'", schemaGroup, schemaName));
        return schemaId.getId();
    }

    @Override
    public String deleteSchemaVersion(String schemaGroup, String schemaName, int version)
        throws SchemaRegistryClientException {
        // return this.restService.deleteSchemaVersion(schemaName, version);
        // remove from cache
        return null;
    }

    @Override
    public String deleteLatestSchemaVersion(String schemaGroup, String schemaName)
        throws SchemaRegistryClientException {
        // return this.restService.deleteSchemaVersion(schemaName, null);
        // remove from cache
        return null;
    }

    @Override
    public List<String> deleteSchema(String schemaGroup, String schemaName)
        throws SchemaRegistryClientException {
        // return this.restService.deleteSchema();
        // remove from cache
        return null;
    }

    /**
     * Explicit call to clear all caches.
     */
    public synchronized void reset() {
        guidCache.clear();
        schemaStringCache.clear();
        typeParserDictionary.clear();
    }

    /**
     * Checks if caches should be reinitialized to satisfy initial configuration
     */
    private synchronized void resetIfNeeded() {
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
        Function<String, Object> parseFunc = typeParserDictionary.get(schemaType.toLowerCase(Locale.ENGLISH));

        if (parseFunc == null) {
            throw logger.logExceptionAsError(new SchemaRegistryClientException(
                String.format("Unexpected serialization type '%s' received.  Currently loaded parsers: %s",
                    schemaType,
                    typeParserDictionary.keySet().toString())));
        }
        return parseFunc;
    }
}
