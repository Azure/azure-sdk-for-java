// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl.keyextractor;

import com.azure.cosmos.implementation.InternalObjectNode;
import com.google.common.base.Preconditions;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;


/**
 * CosmosDB interactions require an id, and partitioningKey for locating an entity. This is the
 * interface for extracting the id and the partitioning key from the entity's key, thereby decoupling
 * the Entity's key from the primary key/partitioning key used in the data store.
 *
 * @param <K> the Entity's key type
 */
public interface KeyExtractor<K> {

    String PARTITION_KEY_FIELD_NAME = "partitioningKey";

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
     * @param document CosmosItemProperties returned from CosmosDB
     * @return the CosmosItemProperties's id
     */
    default String getId(@Nonnull final InternalObjectNode document) {
        return Preconditions.checkNotNull(document.getId(), "A Document object must have the id defined");
    }

    /**
     * Default method to extract the partitioningKey from the Document stored in CosmosDB.
     *
     * @param document CosmosItemProperties returned from CosmosDB
     * @return the partitioningKey of the CosmosItemProperties
     */
    default String getPartitioningKey(@Nonnull final InternalObjectNode document) {
        return Preconditions.checkNotNull(
            Optional.ofNullable(document.get(PARTITION_KEY_FIELD_NAME)).map(Object::toString).orElse(null),
            "partitioningKey not present in the document %s", document.getId());
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
     * @param document CosmosItemProperties returned from CosmosDB
     * @return The Entity's key extracted from CosmosItemProperties data
     */
    K getKey(final InternalObjectNode document);

    /**
     * Maps the id and partitioningKey to the corresponding Key
     * @param id id of the document
     * @param partitioningKey partitioningKey value for the document
     * @return The Entity's key extracted from Document data
     */
    K getKey(final String id, final String partitioningKey);
}
