// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.jsonschema;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClient;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SchemaRegistrySchema;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * An LRU cache of schemas.  If the schema is not yet fetched, performs the network operation and caches it.
 */
class SchemaRegistrySchemaCache {
    private static final String SIZE_KEY = "size";
    private static final String TOTAL_LENGTH_KEY = "totalLength";

    private static final ClientLogger LOGGER = new ClientLogger(SchemaRegistrySchemaCache.class);

    private final SchemaCache cache;
    private final SchemaRegistryAsyncClient schemaRegistryAsyncClient;
    private final SchemaRegistryClient schemaRegistryClient;
    private final String schemaGroup;
    private final boolean autoRegisterSchemas;
    private final Object lock = new Object();

    SchemaRegistrySchemaCache(SchemaRegistryAsyncClient registryAsyncClient, SchemaRegistryClient registryClient,
        String schemaGroup, boolean autoRegisterSchemas, int capacity) {

        this.schemaRegistryAsyncClient = registryAsyncClient;
        this.schemaRegistryClient = registryClient;
        this.schemaGroup = schemaGroup;
        this.autoRegisterSchemas = autoRegisterSchemas;
        this.cache = new SchemaCache(capacity);
    }

    /**
     * The schema id.  If {@link SchemaRegistryClient} is not set, the async client is used instead.
     *
     * @param schemaFullName Full name of schema in Schema Registry.
     * @param schemaDefinition Schema definition.
     * @return The ID of the schema.
     *
     * @throws IllegalStateException When SchemaRegistryJsonSchemaSerializer.schemaGroup is not set.
     */
    String getSchemaId(String schemaFullName, String schemaDefinition) {
        if (schemaRegistryClient == null) {
            return getSchemaIdAsync(schemaFullName, schemaDefinition).block();
        }

        final String existingSchemaId;
        synchronized (lock) {
            existingSchemaId = cache.getSchemaId(schemaDefinition);
        }

        if (existingSchemaId != null) {
            return existingSchemaId;
        }

        // It is possible to create the serializer without setting the schema group. This is the case when
        // autoRegisterSchemas is false. (ie. You are only using it to deserialize messages.)
        if (CoreUtils.isNullOrEmpty(schemaGroup)) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("Cannot serialize when 'schemaGroup' is not set."
                + " Please set in SchemaRegistryJsonSchemaSerializer.schemaGroup when creating serializer."));
        }

        final SchemaProperties serviceCall;
        if (autoRegisterSchemas) {
            serviceCall = this.schemaRegistryClient
                .registerSchema(schemaGroup, schemaFullName, schemaDefinition, SchemaFormat.JSON);
        } else {
            serviceCall = this.schemaRegistryClient.getSchemaProperties(
                schemaGroup, schemaFullName, schemaDefinition, SchemaFormat.JSON);
        }

        final String schemaId = serviceCall.getId();

        synchronized (lock) {
            cache.put(schemaId, schemaDefinition);
            logCacheStatus();
        }

        return schemaId;
    }

    /**
     * The schema id.
     *
     * @param schemaFullName Full name of schema in Schema Registry.
     * @param schemaDefinition Schema definition.
     * @return A Mono that completes with the ID of the schema.
     *
     * @throws IllegalStateException When SchemaRegistryJsonSchemaSerializer.schemaGroup is not set or
     * {@link SchemaRegistryAsyncClient} is not set.
     */
    Mono<String> getSchemaIdAsync(String schemaFullName, String schemaDefinition) {
        final String existingSchemaId;
        synchronized (lock) {
            existingSchemaId = cache.getSchemaId(schemaDefinition);
        }

        if (existingSchemaId != null) {
            return Mono.just(existingSchemaId);
        }

        // It is possible to create the serializer without setting the schema group. This is the case when
        // autoRegisterSchemas is false. (i.e. You are only using it to deserialize messages.)
        if (CoreUtils.isNullOrEmpty(schemaGroup)) {
            return monoError(LOGGER, new IllegalStateException("Cannot serialize when 'schemaGroup' is not set. Please"
                + " set in SchemaRegistryJsonSchemaSerializer.schemaGroup when creating serializer."));
        }

        if (Objects.isNull(schemaRegistryAsyncClient)) {
            return monoError(LOGGER, new IllegalStateException("Cannot use async methods if SchemaRegistryAsyncClient "
                + "Set client in SchemaRegistryJsonSchemaSerializer.schemaRegistryClient(SchemaRegistryAsyncClient)."
            ));
        }

        final Mono<SchemaProperties> serviceCall;
        if (autoRegisterSchemas) {
            serviceCall = this.schemaRegistryAsyncClient
                .registerSchema(schemaGroup, schemaFullName, schemaDefinition, SchemaFormat.JSON);
        } else {
            serviceCall = this.schemaRegistryAsyncClient.getSchemaProperties(
                schemaGroup, schemaFullName, schemaDefinition, SchemaFormat.JSON);
        }

        return serviceCall.map(properties -> {
            final String schemaId = properties.getId();

            synchronized (lock) {
                cache.put(schemaId, schemaDefinition);
                logCacheStatus();
            }

            return schemaId;
        });
    }

    /**
     * The schema id.  {@link SchemaRegistryAsyncClient} is used if {@link SchemaRegistryClient} is not set.
     *
     * @param schemaId Id to get the schema definition.
     * @return The schema definition associated with the id.
     */
    String getSchemaDefinition(String schemaId) {
        if (schemaRegistryClient == null) {
            return getSchemaDefinitionAsync(schemaId).block();
        }

        final String existing;
        synchronized (lock) {
            existing = cache.get(schemaId);
        }

        if (existing != null) {
            return existing;
        }

        final SchemaRegistrySchema registryObject = schemaRegistryClient.getSchema(schemaId);
        final String schemaString = registryObject.getDefinition();

        synchronized (lock) {
            cache.put(schemaId, schemaString);
            logCacheStatus();
        }

        return schemaString;
    }

    /**
     * The schema id.  {@link SchemaRegistryAsyncClient} is used if {@link SchemaRegistryClient} is not set.
     *
     * @param schemaId Id to get the schema definition.
     * @return The schema definition associated with the id.
     * @throws IllegalStateException When {@link SchemaRegistryAsyncClient} is not set.
     */
    Mono<String> getSchemaDefinitionAsync(String schemaId) {
        final String existing;
        synchronized (lock) {
            existing = cache.get(schemaId);
        }

        if (existing != null) {
            return Mono.just(existing);
        }

        if (Objects.isNull(schemaRegistryAsyncClient)) {
            return monoError(LOGGER, new IllegalStateException("Cannot use async methods if SchemaRegistryAsyncClient "
                + "Set client in SchemaRegistryJsonSchemaSerializer.schemaRegistryClient(SchemaRegistryAsyncClient)."
            ));
        }

        return schemaRegistryAsyncClient.getSchema(schemaId)
            .handle((registryObject, sink) -> {
                final String schemaString = registryObject.getDefinition();

                synchronized (lock) {
                    cache.put(schemaId, schemaString);
                    logCacheStatus();
                }

                sink.next(schemaString);
            });
    }

    /**
     * Gets number of cached schemas.
     *
     * @return Number of cached schemas.
     */
    int getSize() {
        synchronized (lock) {
            return cache.size();
        }
    }

    /**
     * Gets the length of schemas stored in cache.
     *
     * @return The length of schemas stored in cache.
     */
    int getTotalLength() {
        synchronized (lock) {
            return cache.getTotalLength();
        }
    }

    /**
     * Logs the cache status if log level verbose is enabled. Otherwise, no-op.
     */
    private void logCacheStatus() {
        if (!LOGGER.canLogAtLevel(LogLevel.VERBOSE)) {
            return;
        }

        final int size = cache.size();
        final int length = cache.getTotalLength();

        LOGGER.atVerbose()
            .addKeyValue(SIZE_KEY, size)
            .addKeyValue(TOTAL_LENGTH_KEY, length)
            .log("Cache entry added or updated. Total number of entries: {}; Total schema length: {}",
                size, length);
    }

    /**
     * Simple LRU cache. Accesses to cache are synchronized via the outer class lock.
     * TODO: When https://github.com/Azure/azure-sdk-for-java/pull/27408/ is merged, take a look at replacing.
     */
    private static final class SchemaCache extends LinkedHashMap<String, String> {
        private static final long serialVersionUID = -1L;

        private final int capacity;
        /**
         * Map of JSON schema definitions to schema ids.
         * Key: JSON schema definition
         * Value: Schema Id
         */
        private final HashMap<String, String> schemaToIdCache = new HashMap<>();

        private int totalLength;

        /**
         * Creates an LRU cache with maximum capacity.
         *
         * @param capacity Max size (number of entries) of the cache.
         */
        SchemaCache(int capacity) {
            super(64, 0.75f, true);
            this.capacity = capacity;
        }

        int getTotalLength() {
            return totalLength;
        }

        /**
         * Gets the schema id of a matching schema.
         *
         * @param schema Schema to get entry for.
         * @return The schema id or null if it does not exist in the cache.
         */
        String getSchemaId(String schema) {
            final String schemaId = schemaToIdCache.get(schema);

            if (schemaId != null) {
                // Simulate an access so that the entry does not expire.
                super.get(schemaId);
            }

            return schemaId;
        }

        /**
         * Adds a schema keyed by its schema id.
         */
        @Override
        public String put(String schemaId, String value) {
            final String existing = super.put(schemaId, value);
            final int currentLength = value.length();

            // The replaced node may be of a different size.
            if (existing == null) {
                totalLength = totalLength + currentLength;
            } else {
                final int difference = currentLength - existing.length();
                totalLength = totalLength - difference;
            }

            schemaToIdCache.put(value, schemaId);

            return existing;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            final boolean removingEntry = size() > capacity;

            if (removingEntry) {
                final String value = eldest.getValue();
                totalLength = totalLength - value.length();

                schemaToIdCache.remove(value);
            }

            return removingEntry;
        }
    }
}

