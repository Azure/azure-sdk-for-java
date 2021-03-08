// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.BridgeInternal;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

/**
 * Represent a partition key range in the Azure Cosmos DB database service.
 */
public class PartitionKeyRange extends Resource {
    public static final String MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY = "";
    public static final String MAXIMUM_EXCLUSIVE_EFFECTIVE_PARTITION_KEY = "FF";
    public static final String MASTER_PARTITION_KEY_RANGE_ID = "M";

    /**
     * Constructor.
     *
     * @param objectNode the {@link ObjectNode} that represent the
     * {@link JsonSerializable}
     */
    public PartitionKeyRange(ObjectNode objectNode) {
        super(objectNode);
    }

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
    public PartitionKeyRange setId(String id) {
        super.setId(id);
        return this;
    }

    public PartitionKeyRange(String id, String minInclusive, String maxExclusive) {
        super();
        this.setId(id);
        this.setMinInclusive(minInclusive);
        this.setMaxExclusive(maxExclusive);
    }

    public PartitionKeyRange(String id, String minInclusive, String maxExclusive, List<String> parents) {
        super();
        this.setId(id);
        this.setMinInclusive(minInclusive);
        this.setMaxExclusive(maxExclusive);
        this.setParents(parents);
    }

    public String getMinInclusive() {
        return super.getString("minInclusive");
    }

    public PartitionKeyRange setMinInclusive(String minInclusive) {
        BridgeInternal.setProperty(this, "minInclusive", minInclusive);
        return this;
    }

    public String getMaxExclusive() {
        return super.getString("maxExclusive");
    }

    public PartitionKeyRange setMaxExclusive(String maxExclusive) {
        BridgeInternal.setProperty(this, "maxExclusive", maxExclusive);
        return this;
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

        return this.getId().compareTo(otherRange.getId()) == 0
                && this.getMinInclusive().compareTo(otherRange.getMinInclusive()) == 0
                && this.getMaxExclusive().compareTo(otherRange.getMaxExclusive()) == 0;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = (hash * 397) ^ this.getId().hashCode();
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

    @Override
    public String toJson() {
        return super.toJson();
    }
}
