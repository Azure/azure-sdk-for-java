// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.internal.routing.Range;

import java.util.List;

/**
 * Represent a partition key range in the Azure Cosmos DB database service.
 */
public class PartitionKeyRange extends Resource {
    public static final String MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY = "";
    public static final String MAXIMUM_EXCLUSIVE_EFFECTIVE_PARTITION_KEY = "FF";
    public static final String MASTER_PARTITION_KEY_RANGE_ID = "M";

    /**
     * Initialize a partition key range object.
     */
    public PartitionKeyRange() {
        super();
    }

    /**
     * Initialize a partition key range object from json string.
     * 
     * @param jsonString
     *            the json string that represents the partition key range
     *            object.
     */
    public PartitionKeyRange(String jsonString) {
        super(jsonString);
    }

    /**
     * Set id of partition key range
     * @param id the name of the resource.
     * @return the partition key range
     */
    public PartitionKeyRange id(String id) {
        super.id(id);
        return this;
    }

    public PartitionKeyRange(String id, String minInclusive, String maxExclusive) {
        super();
        this.id(id);
        this.setMinInclusive(minInclusive);
        this.setMaxExclusive(maxExclusive);
    }

    public PartitionKeyRange(String id, String minInclusive, String maxExclusive, List<String> parents) {
        super();
        this.id(id);
        this.setMinInclusive(minInclusive);
        this.setMaxExclusive(maxExclusive);
        this.setParents(parents);
    }

    public String getMinInclusive() {
        return super.getString("minInclusive");
    }

    public void setMinInclusive(String minInclusive) {
        BridgeInternal.setProperty(this, "minInclusive", minInclusive);
    }

    public String getMaxExclusive() {
        return super.getString("maxExclusive");
    }

    public void setMaxExclusive(String maxExclusive) {
        BridgeInternal.setProperty(this, "maxExclusive", maxExclusive);
    }

    public Range<String> toRange() {
        return new Range<String>(this.getMinInclusive(), this.getMaxExclusive(), true, false);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PartitionKeyRange)) {
            return false;
        }

        PartitionKeyRange otherRange = (PartitionKeyRange) obj;

        return this.id().compareTo(otherRange.id()) == 0
                && this.getMinInclusive().compareTo(otherRange.getMinInclusive()) == 0
                && this.getMaxExclusive().compareTo(otherRange.getMaxExclusive()) == 0;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = (hash * 397) ^ this.id().hashCode();
        hash = (hash * 397) ^ this.getMinInclusive().hashCode();
        hash = (hash * 397) ^ this.getMaxExclusive().hashCode();
        return hash;
    }

    public void setParents(List<String> parents) {
        BridgeInternal.setProperty(this, Constants.Properties.PARENTS, parents);
    }

    /**
     * Used internally to indicate the ID of the parent range
     * @return a list partition key range ID
     */
    public List<String> getParents() { return this.getList(Constants.Properties.PARENTS, String.class); }
}
