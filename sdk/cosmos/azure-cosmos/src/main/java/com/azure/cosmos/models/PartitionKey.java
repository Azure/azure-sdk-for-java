// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;

/**
 * Represents a partition key value in the Azure Cosmos DB database service. A
 * partition key identifies the partition where the item is stored in.
 */
public class PartitionKey {

    private final PartitionKeyInternal internalPartitionKey;
    private Object keyObject;

    PartitionKey(PartitionKeyInternal partitionKeyInternal) {
        this.internalPartitionKey = partitionKeyInternal;
    }

    /**
     * Constructor. CREATE a new instance of the PartitionKey object.
     *
     * @param key the value of the partition key.
     */
    @SuppressWarnings("serial")
    public PartitionKey(final Object key) {
        this.keyObject = key;
        this.internalPartitionKey = PartitionKeyInternal.fromObjectArray(new Object[] {key}, true);
    }

    /**
     * Gets the object used to create partition key
     * @return the partition key object
     */
    Object getKeyObject() {
        return keyObject;
    }

    /**
     * Create a new instance of the PartitionKey object from a serialized JSON
     * partition key.
     *
     * @param jsonString the JSON string representation of this PartitionKey object.
     * @return the PartitionKey instance.
     */
    static PartitionKey fromJsonString(String jsonString) {
        return new PartitionKey(PartitionKeyInternal.fromJsonString(jsonString));
    }

    public static final PartitionKey NONE = new PartitionKey(PartitionKeyInternal.None);

    /**
     * Serialize the PartitionKey object to a JSON string.
     *
     * @return the string representation of this PartitionKey object.
     */
    public String toString() {
        return this.internalPartitionKey.toJson();
    }

    // TODO: make private
    PartitionKeyInternal getInternalPartitionKey() {
        return internalPartitionKey;
    }

    /**
     * Overrides the Equal operator for object comparisons between two instances of
     * {@link PartitionKey}
     *
     * @param other The object to compare with.
     * @return True if two object instance are considered equal.
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        if (this == other) {
            return true;
        }

        PartitionKey otherKey = Utils.as(other, PartitionKey.class);
        return otherKey != null && this.internalPartitionKey.equals(otherKey.internalPartitionKey);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////

    static {
        ImplementationBridgeHelpers.PartitionKeyHelper.setPartitionKeyAccessor(
            new ImplementationBridgeHelpers.PartitionKeyHelper.PartitionKeyAccessor() {
                @Override
                public PartitionKey toPartitionKey(PartitionKeyInternal partitionKeyInternal) {
                    return new PartitionKey(partitionKeyInternal);
                }
            });
    }
}
