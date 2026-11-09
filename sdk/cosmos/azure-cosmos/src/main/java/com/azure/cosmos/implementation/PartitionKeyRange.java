// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.routing.Range;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represent a partition key range in the Azure Cosmos DB database service.
 */
public class PartitionKeyRange extends Resource {
    public static final String MINIMUM_INCLUSIVE_EFFECTIVE_PARTITION_KEY = "";
    public static final String MAXIMUM_EXCLUSIVE_EFFECTIVE_PARTITION_KEY = "FF";
    public static final String MASTER_PARTITION_KEY_RANGE_ID = "M";

    /**
     * <p>This is an <b>allow-list</b>: any field the service returns that is not in this set
     * (including any field added by the service in the future) is dropped at construction.
     * That keeps per-instance heap bounded against server-side payload growth. Adding a new
     * field to the allow-list is a one-line change here when a consumer needs it.</p>
     */
    private static final Set<String> KEPT_FIELDS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            Constants.Properties.ID,
            "minInclusive",
            "maxExclusive",
            Constants.Properties.PARENTS,
            "status",
            "throughputFraction",
            Constants.Properties.R_ID
        )));

    /**
     * Constructor.
     *
     * <p>Fields not listed in {@link #KEPT_FIELDS} are removed from {@code objectNode} as part of
     * construction so the resulting instance retains only the fields the SDK actually needs.
     * This is the universal funnel for every {@code PartitionKeyRange} the SDK deserializes from
     * a service response (see {@link JsonSerializable#instantiateFromObjectNodeAndType}), so the
     * memory saving applies to all routing-map cache entries and any other code path that
     * consumes deserialized partition key ranges.</p>
     *
     * <p>The argument is mutated in place. This is safe because every production caller obtains
     * {@code objectNode} from Jackson deserialization and does not retain another reference to
     * it. Tests that need to preserve a fully-populated source object should use
     * {@code objectNode.deepCopy()} before handing it to this constructor.</p>
     *
     * @param objectNode the {@link ObjectNode} that represents the {@link JsonSerializable}
     */
    public PartitionKeyRange(ObjectNode objectNode) {
        super(stripToKeptFields(objectNode));
    }

    private static ObjectNode stripToKeptFields(ObjectNode objectNode) {
        if (objectNode != null) {
            objectNode.retain(KEPT_FIELDS);
        }
        return objectNode;
    }

    /**
     * Initialize a partition key range object.
     */
    public PartitionKeyRange() {
        super();
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
        this.set("minInclusive", minInclusive);
        return this;
    }

    public String getMaxExclusive() {
        return super.getString("maxExclusive");
    }

    public PartitionKeyRange setMaxExclusive(String maxExclusive) {
        this.set("maxExclusive", maxExclusive);
        return this;
    }

    public Range<String> toRange() {
        return new Range<>(this.getMinInclusive(), this.getMaxExclusive(), true, false);
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
        this.set(Constants.Properties.PARENTS, parents);
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
