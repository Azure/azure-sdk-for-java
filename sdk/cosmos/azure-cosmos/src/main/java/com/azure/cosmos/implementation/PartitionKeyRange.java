// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.routing.Range;
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
     * Fields the Cosmos DB service returns on every partition key range that the SDK does not
     * need to retain in heap for the lifetime of the {@link com.azure.cosmos.implementation.routing.CollectionRoutingMap}.
     *
     * <p>The set is kept in lock-step with the equivalent Python optimization in
     * <a href="https://github.com/Azure/azure-sdk-for-python/pull/46297">azure-sdk-for-python#46297</a>
     * (item #2 — "Strip unused fields → compact PKRange"). Cross-SDK alignment is intentional:
     * a field that Java does not consume today may be wired up tomorrow, so the call about
     * what is safe to drop is made once across the SDKs rather than re-derived per-language.</p>
     *
     * <p>The list is a deny-list, not an allow-list, so any future field added by the service is
     * preserved automatically with no SDK change.</p>
     */
    private static final String[] DROPPED_FIELDS = new String[] {
        "_rid",
        "_etag",
        "ridPrefix",
        "_self",
        "ownedArchivalPKRangeIds",
        "_ts",
        "lsn"
    };

    /**
     * Constructor.
     *
     * <p>Fields listed in {@link #DROPPED_FIELDS} are removed from {@code objectNode} as part of
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
        super(stripUnusedFields(objectNode));
    }

    private static ObjectNode stripUnusedFields(ObjectNode objectNode) {
        if (objectNode != null) {
            for (String field : DROPPED_FIELDS) {
                objectNode.remove(field);
            }
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
