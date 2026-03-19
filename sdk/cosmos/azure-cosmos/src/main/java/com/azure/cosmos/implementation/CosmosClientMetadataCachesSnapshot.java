// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

/**
 * Snapshot of Cosmos client metadata caches for warm-start optimization.
 * Uses JSON serialization to avoid Java deserialization security vulnerabilities (CWE-502).
 */
public class CosmosClientMetadataCachesSnapshot implements Serializable {
    private static final long serialVersionUID = 2L;
    private static final int ERROR_CODE = 0;
    private static final int FORMAT_VERSION = 1;
    private static final String VERSION_FIELD = "version";
    private static final String ENTRIES_FIELD = "entries";
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosClientMetadataCachesSnapshot.class);
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapperWithAllowDuplicates();

    public byte[] collectionInfoByNameCache;
    public byte[] collectionInfoByIdCache;

    public CosmosClientMetadataCachesSnapshot() {
    }

    public void serialize(CosmosAsyncClient client) {
        RxDocumentClientImpl documentClient =
            (RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(client);
        documentClient.serialize(this);
    }

    public void serializeCollectionInfoByNameCache(AsyncCache<String, DocumentCollection> cache) {
        this.collectionInfoByNameCache = serializeCollectionCacheToJson(cache);
    }

    public void serializeCollectionInfoByIdCache(AsyncCache<String, DocumentCollection> cache) {
        this.collectionInfoByIdCache = serializeCollectionCacheToJson(cache);
    }

    private byte[] serializeCollectionCacheToJson(AsyncCache<String, DocumentCollection> cache) {
        try {
            ObjectNode root = OBJECT_MAPPER.createObjectNode();
            root.put(VERSION_FIELD, FORMAT_VERSION);

            ObjectNode entries = OBJECT_MAPPER.createObjectNode();
            for (Map.Entry<String, DocumentCollection> entry : cache.getEntries().entrySet()) {
                entries.set(entry.getKey(), entry.getValue().toSerializableObjectNode());
            }
            root.set(ENTRIES_FIELD, entries);

            return OBJECT_MAPPER.writeValueAsBytes(root);
        } catch (Exception e) {
            throw CosmosBridgeInternal.cosmosException(ERROR_CODE, e);
        }
    }

    /**
     * Deserializes the collection-info-by-name cache from JSON.
     * Returns null if the data cannot be parsed (e.g., old Java serialization format),
     * allowing the SDK to fall back to fetching fresh metadata from the service.
     */
    public AsyncCache<String, DocumentCollection> getCollectionInfoByNameCache() {
        return deserializeCollectionCacheFromJson(collectionInfoByNameCache);
    }

    /**
     * Deserializes the collection-info-by-id cache from JSON.
     * Returns null if the data cannot be parsed (e.g., old Java serialization format),
     * allowing the SDK to fall back to fetching fresh metadata from the service.
     */
    public AsyncCache<String, DocumentCollection> getCollectionInfoByIdCache() {
        return deserializeCollectionCacheFromJson(collectionInfoByIdCache);
    }

    private AsyncCache<String, DocumentCollection> deserializeCollectionCacheFromJson(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(data);

            // Validate JSON structure
            if (!root.isObject()) {
                LOGGER.warn("Cache snapshot is not a JSON object, returning null for fresh fetch");
                return null;
            }

            JsonNode versionNode = root.get(VERSION_FIELD);
            if (versionNode == null || !versionNode.isInt() || versionNode.intValue() != FORMAT_VERSION) {
                LOGGER.warn("Cache snapshot has unsupported version (expected {}), returning null for fresh fetch",
                    FORMAT_VERSION);
                return null;
            }

            JsonNode entriesNode = root.get(ENTRIES_FIELD);
            if (entriesNode == null || !entriesNode.isObject()) {
                LOGGER.warn("Cache snapshot missing or invalid 'entries' field, returning null for fresh fetch");
                return null;
            }

            Map<String, DocumentCollection> entries = new java.util.HashMap<>();

            Iterator<Map.Entry<String, JsonNode>> fields = entriesNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();
                JsonNode valueNode = entry.getValue();

                if (!valueNode.isObject()) {
                    LOGGER.warn("Skipping cache entry '{}' with non-object value", key);
                    continue;
                }

                DocumentCollection collection = DocumentCollection.fromSerializableObjectNode((ObjectNode) valueNode);
                entries.put(key, collection);
            }

            return AsyncCache.fromMap(new RxCollectionCache.CollectionRidComparer(), entries);
        } catch (Exception e) {
            // Could not parse as JSON — likely old Java serialization format or corrupted data.
            // Return null so the SDK fetches fresh metadata from the service.
            LOGGER.warn("Failed to deserialize cache snapshot from JSON, returning null for fresh fetch: {}",
                e.getMessage());
            return null;
        }
    }
}
