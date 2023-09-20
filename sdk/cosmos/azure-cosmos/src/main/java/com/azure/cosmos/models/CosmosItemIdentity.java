// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import java.util.Objects;

/**
 * Encapsulates the PartitionKey/id tuple that uniquely identifies a CosmosItem
 */
public final class CosmosItemIdentity {
    private final PartitionKey partitionKey;
    private final String id;

    /**
     * Instantiates an instance of the CosmosItemIdentity class
     * @param partitionKey the partition key - must not be null
     * @param id the item id value - must be unique within the scope of a logical partition, must not be null or empty
     */
    public CosmosItemIdentity(PartitionKey partitionKey, String id) {

        if (partitionKey == null) {
            throw new IllegalArgumentException("partitionKey is null.");
        }

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("id is null or empty");
        }

        this.partitionKey = partitionKey;
        this.id = id;
    }

    /**
     * <p>Gets the partition key</p>
     *
     * @return the partition key - must not be null
     */
    public PartitionKey getPartitionKey() {
        return this.partitionKey;
    }

    /**
     * <p>Gets the id value uniquely identifying this item within the scope of a logical partition</p>
     *
     * @return the id value uniquely identifying this item within the scope of a logical
     * partition - must not be null or empty
     */
    public String getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CosmosItemIdentity that = (CosmosItemIdentity) o;
        return Objects.equals(partitionKey, that.partitionKey) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partitionKey.toString(), id);
    }
}
