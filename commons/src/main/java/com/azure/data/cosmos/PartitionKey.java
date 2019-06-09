/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.routing.PartitionKeyInternal;
import com.azure.data.cosmos.internal.Utils;

/**
 * Represents a partition key value in the Azure Cosmos DB database service. A
 * partition key identifies the partition where the document is stored in.
 */
public class PartitionKey {

    private PartitionKeyInternal internalPartitionKey;

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
        this.internalPartitionKey = PartitionKeyInternal.fromObjectArray(new Object[] { key }, true);
    }

    /**
     * CREATE a new instance of the PartitionKey object from a serialized JSON
     * partition key.
     *
     * @param jsonString the JSON string representation of this PartitionKey object.
     * @return the PartitionKey instance.
     */
    public static PartitionKey FromJsonString(String jsonString) {
        return new PartitionKey(PartitionKeyInternal.fromJsonString(jsonString));
    }

    public static PartitionKey None = new PartitionKey(PartitionKeyInternal.None);

    /**
     * Serialize the PartitionKey object to a JSON string.
     *
     * @return the string representation of this PartitionKey object.
     */
    public String toString() {
        return this.internalPartitionKey.toJson();
    }

    // TODO: make private
    public PartitionKeyInternal getInternalPartitionKey() {
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
}
