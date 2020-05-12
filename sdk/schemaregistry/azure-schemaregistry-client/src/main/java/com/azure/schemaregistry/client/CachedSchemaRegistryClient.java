/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry.client;

import com.azure.schemaregistry.client.rest.RestService;
import com.azure.schemaregistry.client.rest.entities.responses.SchemaObjectResponse;
import com.azure.schemaregistry.client.rest.exceptions.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
 * @see CachedSchemaRegistryClient.Builder Follows static builder pattern for object instantiation
 */
public class CachedSchemaRegistryClient implements SchemaRegistryClient {
    private static final Logger log = LoggerFactory.getLogger(CachedSchemaRegistryClient.class);

    public static final int MAX_SCHEMA_MAP_SIZE_DEFAULT = 1000;
    public static final int MAX_SCHEMA_MAP_SIZE_MINIMUM = 10;

    private int maxSchemaMapSize;
    private final RestService restService;
    private final HashMap<String, Function<String, ?>> typeParserDictionary;

    private HashMap<String, SchemaRegistryObject<?>> guidCache;
    private HashMap<String, SchemaRegistryObject<?>> schemaStringCache;

    private CachedSchemaRegistryClient(
            String registryUrl,
            int maxSchemaMapSize,
            HashMap<String, Function<String, ?>> typeParserDictionary,
            String credentials)
    {
        if (registryUrl == null || registryUrl.isEmpty()) {
            throw new IllegalArgumentException("Schema Registry URL cannot be null or empty.");
        }

        this.restService = new RestService(registryUrl, credentials);
        this.maxSchemaMapSize = maxSchemaMapSize;
        this.typeParserDictionary = typeParserDictionary;
        this.guidCache = new HashMap<>();
        this.schemaStringCache = new HashMap<>();
    }

    // testing
    CachedSchemaRegistryClient(
        RestService restService,
        HashMap<String, SchemaRegistryObject<?>> guidCache,
        HashMap<String, SchemaRegistryObject<?>> schemaStringCache,
        HashMap<String, Function<String, ?>> typeParserDictionary) {
        this.restService = restService; // mockable
        this.guidCache = guidCache;
        this.schemaStringCache = schemaStringCache;
        this.typeParserDictionary = typeParserDictionary;
    }

    public synchronized void loadSchemaParser(String serializationType, Function<String, ?> parseMethod) {
        if (serializationType == null || serializationType.isEmpty()) {
            throw new IllegalArgumentException("Serialization type cannot be null or empty.");
        }
        if (this.typeParserDictionary.containsKey(serializationType.toLowerCase())) {
            throw new IllegalArgumentException("Multiple parse methods for single serialization type may not be added.");
        }
        this.typeParserDictionary.put(serializationType.toLowerCase(), parseMethod);
        log.debug(String.format("Loaded parser for '%s' serialization format.", serializationType.toLowerCase()));
    }

    @Override
    public synchronized SchemaRegistryObject<?> register(String schemaGroup, String schemaName, String schemaString, String serializationType)
            throws IOException, SchemaRegistryClientException {
        if (schemaStringCache.containsKey(schemaString)) {
            log.debug(String.format("Cache hit schema string. Group: '%s', name: '%s', serialization type: '%s', payload: '%s'",
                    schemaGroup, schemaName, serializationType, schemaString));
            return schemaStringCache.get(schemaString);
        }

        log.debug(String.format("Registering schema. Group: '%s', name: '%s', serialization type: '%s', payload: '%s'",
                schemaGroup, schemaName, serializationType, schemaString));

        String schemaGuid;
        try {
             schemaGuid = this.restService.registerSchema(schemaGroup, schemaName, schemaString, serializationType);
        } catch (RestClientException e) {
            throw new SchemaRegistryClientException("Schema registration failed.", e);
        }

        SchemaRegistryObject<?> registered = new SchemaRegistryObject<>(schemaGuid,
            serializationType,
            schemaString.getBytes(RestService.SERVICE_CHARSET),
            getParseFunc(serializationType));

        resetIfNeeded();
        schemaStringCache.put(schemaString, registered);
        log.debug(String.format("Cached schema string. Group: '%s', name: '%s'", schemaGroup, schemaName));
        return registered;
    }

    @Override
    public synchronized SchemaRegistryObject<?> getSchemaByGuid(String schemaGuid) throws IOException, SchemaRegistryClientException {
        if (guidCache.containsKey(schemaGuid)) {
            log.debug(String.format("Cache hit for schema guid '%s'", schemaGuid));
            return guidCache.get(schemaGuid);
        }

        SchemaObjectResponse response;
        try {
            response = this.restService.getSchemaByGuid(schemaGuid);
        } catch (RestClientException e) {
            throw new SchemaRegistryClientException(String.format("Failed to get schema for guid %s.", schemaGuid), e);
        }

        SchemaRegistryObject<?> schemaObject = new SchemaRegistryObject<>(response.schemaGuid,
            response.serializationType,
            response.schemaByteArray,
            getParseFunc(response.serializationType));

        resetIfNeeded();
        guidCache.put(schemaGuid, schemaObject);
        log.debug(String.format("Cached schema object. Path: '%s'", schemaGuid));
        return schemaObject;
    }

    @Override
    public synchronized String getGuid(String schemaGroup, String schemaName, String schemaString, String serializationType)
            throws IOException, SchemaRegistryClientException {
        if (schemaStringCache.containsKey(schemaString)) {
            log.debug(String.format("Cache hit schema string. Group: '%s', name: '%s'", schemaGroup, schemaName));
            return schemaStringCache.get(schemaString).schemaGuid;
        }

        String schemaGuid;
        try {
            schemaGuid = this.restService.getGuid(schemaGroup, schemaName, schemaString, serializationType);
        }
        catch(RestClientException e){
            throw new SchemaRegistryClientException(
                    String.format("Failed to fetch schema guid for schema. Group: '%s', name: '%s'", schemaGroup, schemaName),
                    e);
        }

        resetIfNeeded();
        schemaStringCache.put(
            schemaString,
            new SchemaRegistryObject<>(
                schemaGuid,
                serializationType,
                schemaString.getBytes(RestService.SERVICE_CHARSET),
                getParseFunc(serializationType)));
        log.debug(String.format("Cached schema string. Group: '%s', name: '%s'", schemaGroup, schemaName));
        return schemaGuid;
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

    private Function<String, ?> getParseFunc(String serializationType) throws IOException {
        Function<String, ?> parseFunc = typeParserDictionary.get(serializationType.toLowerCase());

        if (parseFunc == null) {
            log.error(String.format("No loaded schema parser for serialization type: '%s'", serializationType));
            throw new IOException(String.format("Unexpected serialization type '%s' received.  Currently loaded parsers: %s",
                serializationType,
                typeParserDictionary.keySet().toString()));
        }
        return parseFunc;
    }

    public static class Builder {
        private final String schemaRegistryUrl;
        private final HashMap<String, Function<String, ?>> typeParserDictionary;
        private int maxSchemaMapSize;

        public Builder(String schemaRegistryUrl) {
            if (schemaRegistryUrl == null || schemaRegistryUrl.isEmpty()) {
                throw new IllegalArgumentException("Schema Registry URL cannot be empty.");
            }
            this.schemaRegistryUrl = schemaRegistryUrl;
            this.maxSchemaMapSize = CachedSchemaRegistryClient.MAX_SCHEMA_MAP_SIZE_DEFAULT;
            this.typeParserDictionary = new HashMap<>();
        }

        public CachedSchemaRegistryClient build() {
            return new CachedSchemaRegistryClient(schemaRegistryUrl, maxSchemaMapSize, typeParserDictionary, null);
        }

        public Builder maxSchemaMapSize(int maxSchemaMapSize) throws IllegalArgumentException {
            if (maxSchemaMapSize < CachedSchemaRegistryClient.MAX_SCHEMA_MAP_SIZE_MINIMUM) {
                throw new IllegalArgumentException(
                        String.format("Schema map size must be greater than %s entries",
                                CachedSchemaRegistryClient.MAX_SCHEMA_MAP_SIZE_MINIMUM));
            }
            this.maxSchemaMapSize = maxSchemaMapSize;
            return this;
        }

        public Builder credential() {
            return this;
        }

        public <T> Builder loadSchemaParser(String serializationType, Function<String, T> parseMethod) {
            if (serializationType == null || serializationType.isEmpty()) {
                throw new IllegalArgumentException("Serialization type cannot be null or empty.");
            }
            if (this.typeParserDictionary.containsKey(serializationType.toLowerCase())) {
                throw new IllegalArgumentException("Multiple parse methods for single serialization type may not be added.");
            }
            this.typeParserDictionary.put(serializationType.toLowerCase(), parseMethod);
            return this;
        }
    }
}
