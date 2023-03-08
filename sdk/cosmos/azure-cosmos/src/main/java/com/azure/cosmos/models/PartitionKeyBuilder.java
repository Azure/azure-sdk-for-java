// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Undefined;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for partition keys.
 */
public final class PartitionKeyBuilder {
    private final List<Object> partitionKeyValues;

    /**
     * Constructor. CREATE a new instance of the PartitionKeyBuilder object.
     */
    public PartitionKeyBuilder() {
        this.partitionKeyValues = new ArrayList<Object>();
    }

    /**
     * Adds partition value of type string
     * @param value The value of type string to be used as partition key
     * @return The current PartitionKeyBuilder object
     */
    public PartitionKeyBuilder add(String value) {
        this.partitionKeyValues.add(value);
        return this;
    }

    /**
     * Adds partition value of type double
     * @param value The value of type double to be used as partition key
     * @return The current PartitionKeyBuilder object
     */
    public PartitionKeyBuilder add(double value) {
        this.partitionKeyValues.add(value);
        return this;
    }

    /**
     * Adds partition value of type boolean
     * @param value The value of type boolean to be used as partition key
     * @return The current PartitionKeyBuilder object
     */
    public PartitionKeyBuilder add(boolean value) {
        this.partitionKeyValues.add(value);
        return this;
    }

    /**
     * Builds a new instance of the type PartitionKey with the specified Partition Key values.
     * @return PartitionKey object
     */
    public PartitionKey build() {
        if(this.partitionKeyValues.size() == 0) {
            throw new IllegalArgumentException("No partition key value has been specified");
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
        StringBuilder backendValues = new StringBuilder();
        if (valueArray.length == 1) {
            backendValues.append((String) valueArray[0]);
        } else {
            for (int i = 0; i < valueArray.length; i++) {
                backendValues.append((String) valueArray[i]);
                if (i < valueArray.length-1) {
                    backendValues.append("=");
                }
            }
        }
        return new PartitionKey(backendValues.toString(), partitionKeyInternal);
    }
}
