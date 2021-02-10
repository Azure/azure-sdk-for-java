// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl.keyextractor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.azure.cosmos.benchmark.linkedin.impl.Constants.ID;
import static com.azure.cosmos.benchmark.linkedin.impl.Constants.PARTITION_KEY;


/**
 * CosmosDB interactions require an id, and partitioningKey for locating an entity. This is the
 * interface for extracting the id and the partitioning key from the entity's key, thereby decoupling
 * the Entity's key from the primary key/partitioning key used in the data store.
 *
 * @param <K> the Entity's key type
 */
public interface KeyExtractor<K> {

    /**
     * Default method to check if the Key is valid.
     * For CosmosDB, the id and the partitioningKey must be defined
     *
     * @param key The Entity's key
     * @return true if the entity's key is valid
     */
    default boolean isKeyValid(final K key) {
        final String id = getId(key);
        final String partitioningKey = getPartitioningKey(key);
        return Objects.nonNull(id) && Objects.nonNull(partitioningKey);
    }

    /**
     * Default method to return the id associated with the Document stored in CosmosDB
     *
     * @param document ObjectNode modeling the document in CosmosDB
     * @return the document's id
     */
    default String getId(@Nonnull final ObjectNode document) {
        return getField(document, ID)
            .orElseThrow(() -> new IllegalArgumentException("The document must have the id defined"));
    }

    /**
     * Default method to extract the partitioningKey from the Document stored in CosmosDB.
     *
     * @param document ObjectNode modeling the document in CosmosDB
     * @return the document's partitioningKey
     */
    default String getPartitioningKey(@Nonnull final ObjectNode document) {
        return getField(document, PARTITION_KEY)
            .orElseThrow(() -> new IllegalArgumentException(String.format("partitioningKey not present in the document %s",
                getField(document, ID))));
    }


    /**
     * @param document ObjectNode modeling the document in CosmosDB
     * @param fieldName the fieldName to retrieve from the ObjectNode
     * @return String representation of the field value if found, else Null
     */
    default Optional<String> getField(@Nonnull final ObjectNode document, @Nonnull final String fieldName) {
        return Optional.ofNullable(document.get(fieldName))
            .map(JsonNode::asText);
    }

    /**
     * Interface to map the key to the id used in CosmosDB
     *
     * @param key The Entity's key
     * @return Id extracted from the Entity's key
     */
    String getId(final K key);

    /**
     * Interface to map the key to the partitioningKey used in CosmosDB
     *
     * @param key The Entity's key
     * @return Partitioning key extracted from the Entity's key
     */
    String getPartitioningKey(final K key);

    /**
     * Interface to map the Document/Entity stored in CosmosDB to the Key
     *
     * @param document ObjectNode modeling the document in CosmosDB
     * @return The Entity's key extracted from CosmosItemProperties data
     */
    K getKey(final ObjectNode document);

    /**
     * Maps the id and partitioningKey to the corresponding Key
     * @param id id of the document
     * @param partitioningKey partitioningKey value for the document
     * @return The Entity's key extracted from Document data
     */
    K getKey(final String id, final String partitioningKey);
}
