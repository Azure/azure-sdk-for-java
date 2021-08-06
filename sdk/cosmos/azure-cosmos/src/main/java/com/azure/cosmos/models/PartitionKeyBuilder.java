// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Undefined;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.util.Beta;
import com.azure.cosmos.util.Beta.SinceVersion;

import java.util.ArrayList;
import java.util.List;

@Beta(value = SinceVersion.V4_16_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class PartitionKeyBuilder {
    private final List<Object> partitionKeyValues;

    /**
     * Constructor. CREATE a new instance of the PartitionKeyBuilder object.
     */
    @Beta(value = SinceVersion.V4_16_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public PartitionKeyBuilder() {
        this.partitionKeyValues = new ArrayList<Object>();
    }

    /**
     * Adds partition value of type string
     * @param value The value of type string to be used as partition key
     * @return The current PartitionKeyBuilder object
     */
    @Beta(value = SinceVersion.V4_16_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public PartitionKeyBuilder add(String value) {
        this.partitionKeyValues.add(value);
        return this;
    }

    /**
     * Adds partition value of type double
     * @param value The value of type double to be used as partition key
     * @return The current PartitionKeyBuilder object
     */
    @Beta(value = SinceVersion.V4_16_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public PartitionKeyBuilder add(double value) {
        this.partitionKeyValues.add(value);
        return this;
    }

    /**
     * Adds partition value of type boolean
     * @param value The value of type boolean to be used as partition key
     * @return The current PartitionKeyBuilder object
     */
    @Beta(value = SinceVersion.V4_16_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public PartitionKeyBuilder add(boolean value) {
        this.partitionKeyValues.add(value);
        return this;
    }

    /**
     * Adds a null partition key value
     * @return The current PartitionKeyBuilder object
     */
    @Beta(value = SinceVersion.V4_16_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public PartitionKeyBuilder addNullValue() {
        this.partitionKeyValues.add(null);
        return this;
    }

    /**
     * Adds a None Partition Key
     * @return The current PartitionKeyBuilder object
     */
    @Beta(value = SinceVersion.V4_16_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public PartitionKeyBuilder addNoneValue() {
        this.partitionKeyValues.add(PartitionKey.NONE);
        return this;
    }

    /**
     * Builds a new instance of the type PartitionKey with the specified Partition Key values.
     * @return PartitionKey object
     */
    @Beta(value = SinceVersion.V4_16_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public PartitionKey build() {
        // Why these checks?
        // These changes are being added for SDK to support multiple paths in a partition key.
        //
        // Currently, when a resource does not specify a value for the PartitionKey,
        // we assign a temporary value `PartitionKey.None` and later discern whether
        // it is a PartitionKey.Undefined or PartitionKey.Empty based on the Collection Type.
        // We retain this behaviour for single path partition keys.
        //
        // For collections with multiple path keys, absence of a partition key values is
        // always treated as a PartitionKey.Undefined.
        if(this.partitionKeyValues.size() == 0) {
            throw new IllegalArgumentException("No partition key value has been specified");
        }

        if(this.partitionKeyValues.size() == 1 && PartitionKey.NONE.equals(this.partitionKeyValues.get(0))) {
            return PartitionKey.NONE;
        }

        PartitionKeyInternal partitionKeyInternal;
        Object[] valueArray = new Object[this.partitionKeyValues.size()];
        for(int i = 0; i < this.partitionKeyValues.size(); i++) {
            Object val = this.partitionKeyValues.get(i);
            if(PartitionKey.NONE.equals(val)) {
                valueArray[i] = Undefined.value();
            }
            else {
                valueArray[i] = val;
            }
        }

        partitionKeyInternal = PartitionKeyInternal.fromObjectArray(valueArray, true);
        return new PartitionKey(partitionKeyInternal);
    }
}
