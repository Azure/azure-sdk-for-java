/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry.client;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.schemaregistry.client.rest.AzureSchemaRegistryRestService;
import com.azure.schemaregistry.client.rest.AzureSchemaRegistryRestServiceBuilder;
import com.azure.schemaregistry.client.rest.models.GetSchemaByIdResponse;
import com.azure.schemaregistry.client.rest.models.SchemaId;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * HTTP-based client that interacts with Azure Schema Registry service to store and retrieve schemas on demand.
 *
 * Utilizes in-memory HashMap caching to minimize network I/O.
 *
 * Max HashMap size can be configured when instantiating.
 * Two maps are maintained -
 * - SchemaRegistryObject cache by GUID - accessed when consuming, store GUIDs previously seen in payloads
 * - SchemaRegistryObject cache by schema string - accessed when sending, minimizes HTTP calls when payloads of same schema
 *
 * TODO: implement max age for schema maps? or will schemas always be immutable?
 *
 * @see SchemaRegistryClient Implements SchemaRegistryClient interface to allow for testing with mock
 * @see CachedSchemaRegistryClientBuilder Follows builder pattern for object instantiation
 */
public class CachedSchemaRegistryClient implements SchemaRegistryClient {
    private final ClientLogger log = new ClientLogger(CachedSchemaRegistryClient.class);

    public static final int MAX_SCHEMA_MAP_SIZE_DEFAULT = 1000;
    public static final int MAX_SCHEMA_MAP_SIZE_MINIMUM = 10;

    private int maxSchemaMapSize;
    private final AzureSchemaRegistryRestService restService;
    private final HashMap<String, Function<String, Object>> typeParserDictionary;

    private HashMap<String, SchemaRegistryObject> guidCache;
    private HashMap<String, SchemaRegistryObject> schemaStringCache;

    CachedSchemaRegistryClient(
            String registryUrl,
            HttpPipeline pipeline,
            TokenCredential credential,
            int maxSchemaMapSize,
            HashMap<String, Function<String, Object>> typeParserDictionary)
    {
        if (registryUrl == null || registryUrl.isEmpty()) {
            throw new IllegalArgumentException("Schema Registry URL cannot be null or empty.");
        }

        this.restService = new AzureSchemaRegistryRestServiceBuilder()
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
    }

    public synchronized void loadSchemaParser(String serializationType, Function<String, Object> parseMethod) {
        if (serializationType == null || serializationType.isEmpty()) {
            throw new IllegalArgumentException("Serialization type cannot be null or empty.");
        }
        if (this.typeParserDictionary.containsKey(serializationType.toLowerCase())) {
            throw new IllegalArgumentException("Multiple parse methods for single serialization type may not be added.");
        }
        this.typeParserDictionary.put(serializationType.toLowerCase(), parseMethod);
        log.verbose(String.format("Loaded parser for '%s' serialization format.", serializationType.toLowerCase()));
    }

    @Override
    public synchronized SchemaRegistryObject register(String schemaGroup, String schemaName, String schemaString, String serializationType)
            throws IOException, SchemaRegistryClientException {
        if (schemaStringCache.containsKey(schemaString)) {
            log.verbose(String.format("Cache hit schema string. Group: '%s', name: '%s', serialization type: '%s', payload: '%s'",
                    schemaGroup, schemaName, serializationType, schemaString));
            return schemaStringCache.get(schemaString);
        }

        log.verbose(String.format("Registering schema. Group: '%s', name: '%s', serialization type: '%s', payload: '%s'",
                schemaGroup, schemaName, serializationType, schemaString));

        SchemaId schemaId;
        try {
            schemaId = this.restService.createSchema(schemaGroup, schemaName, schemaString, serializationType);
        } catch (HttpResponseException e) {
            throw new SchemaRegistryClientException("Register operation failed.", e);
        }

        SchemaRegistryObject registered = new SchemaRegistryObject(schemaId.getId(),
            serializationType,
            schemaString.getBytes(),
            getParseFunc(serializationType));

        resetIfNeeded();
        schemaStringCache.put(schemaString, registered);
        log.verbose(String.format("Cached schema string. Group: '%s', name: '%s'", schemaGroup, schemaName));
        return registered;
    }

    @Override
    public synchronized SchemaRegistryObject getSchemaByGuid(String schemaId) throws IOException, SchemaRegistryClientException {
        if (guidCache.containsKey(schemaId)) {
            log.verbose(String.format("Cache hit for schema id '%s'", schemaId));
            return guidCache.get(schemaId);
        }

        GetSchemaByIdResponse response;
        try {
            response = this.restService.getSchemaByIdWithResponseAsync(UUID.fromString(schemaId)).block();
        }
        catch (HttpResponseException e) {
            throw new SchemaRegistryClientException("Fetching schema failed.", e);
        }

        if (response == null) {
            throw new SchemaRegistryClientException("HTTP client returned null schema response");
        }

        String schemaType = response.getDeserializedHeaders().getXSchemaType();

        SchemaRegistryObject schemaObject = new SchemaRegistryObject(schemaId,
            schemaType,
            response.getValue().getBytes(),
            getParseFunc(schemaType));

        resetIfNeeded();
        guidCache.put(schemaId, schemaObject);
        log.verbose(String.format("Cached schema object. Path: '%s'", schemaId));
        return schemaObject;
    }

    @Override
    public synchronized String getSchemaId(String schemaGroup, String schemaName, String schemaString, String schemaType)
            throws IOException, SchemaRegistryClientException {
        if (schemaStringCache.containsKey(schemaString)) {
            log.verbose(String.format("Cache hit schema string. Group: '%s', name: '%s'", schemaGroup, schemaName));
            return schemaStringCache.get(schemaString).schemaId;
        }

        SchemaId schemaId;
        try {
            schemaId = this.restService.getIdBySchemaContent(schemaGroup, schemaName, schemaType, schemaString);
        }
        catch(HttpResponseException e){
            throw new SchemaRegistryClientException(
                    String.format("Failed to fetch schema guid for schema. Group: '%s', name: '%s'", schemaGroup, schemaName),
                    e);
        }

        resetIfNeeded();
        schemaStringCache.put(
            schemaString,
            new SchemaRegistryObject(
                schemaId.getId(),
                schemaType,
                schemaString.getBytes(),
                getParseFunc(schemaType)));
        log.verbose(String.format("Cached schema string. Group: '%s', name: '%s'", schemaGroup, schemaName));
        return schemaId.getId();
    }

    @Override
    public String deleteSchemaVersion(String schemaGroup, String schemaName, int version) throws IOException, SchemaRegistryClientException {
        // return this.restService.deleteSchemaVersion(schemaName, version);
        // remove from cache
        return null;
    }

    @Override
    public String deleteLatestSchemaVersion(String schemaGroup, String schemaName) throws IOException, SchemaRegistryClientException  {
        // return this.restService.deleteSchemaVersion(schemaName, null);
        // remove from cache
        return null;
    }

    @Override
    public List<String> deleteSchema(String schemaGroup, String schemaName) throws IOException, SchemaRegistryClientException  {
        // return this.restService.deleteSchema();
        // remove from cache
        return null;
    }

    public synchronized void reset() {
        guidCache.clear();
        schemaStringCache.clear();
        typeParserDictionary.clear();
    }

    /**
     * Checks if caches should be reinitialized to satisfy initial configuration
     */
    private synchronized void resetIfNeeded() {
        // don't call clear, just re-instantiate and let gc collect
        // don't clear parser dictionary
        if (guidCache.size() > this.maxSchemaMapSize) {
            guidCache = new HashMap<>();
        }
        if (schemaStringCache.size() > this.maxSchemaMapSize) {
            schemaStringCache = new HashMap<>();
        }
    }

    private Function<String, Object> getParseFunc(String serializationType) throws IOException {
        Function<String, Object> parseFunc = typeParserDictionary.get(serializationType.toLowerCase());

        if (parseFunc == null) {
            log.error(String.format("No loaded schema parser for serialization type: '%s'", serializationType));
            throw new IOException(String.format("Unexpected serialization type '%s' received.  Currently loaded parsers: %s",
                serializationType,
                typeParserDictionary.keySet().toString()));
        }
        return parseFunc;
    }
}
