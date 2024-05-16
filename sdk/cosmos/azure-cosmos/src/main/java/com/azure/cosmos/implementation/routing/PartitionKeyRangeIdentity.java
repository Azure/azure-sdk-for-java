// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.util.Objects;

/**
 * Used internally to represents the identity of a partition key range in the Azure Cosmos DB database service.
 */
public final class PartitionKeyRangeIdentity {
    private final String collectionRid;
    private final String partitionKeyRangeId;

    public PartitionKeyRangeIdentity(String collectionRid, String partitionKeyRangeId) {
        if (collectionRid == null) {
            throw new IllegalArgumentException("collectionRid");
        }

        if (partitionKeyRangeId == null) {
            throw new IllegalArgumentException("partitionKeyRangeId");
        }

        this.collectionRid = collectionRid;
        this.partitionKeyRangeId = partitionKeyRangeId;
    }

    /**
     * This should only be used for user provided partitionKeyRangeId, because in this case
     * he knows what he is doing. If collection was deleted/created with same name - it is his responsibility.
     * <p>
     * If our code infers partitionKeyRangeId automatically and uses collection information from collection cache,
     * we need to ensure that request will reach correct collection. In this case constructor which takes collectionRid MUST
     * be used.
     *
     * @param partitionKeyRangeId a string represents the partition key range Id
     */
    public PartitionKeyRangeIdentity(String partitionKeyRangeId) {
        if (partitionKeyRangeId == null) {
            throw new IllegalArgumentException("partitionKeyRangeId");
        }

        this.collectionRid = null;
        this.partitionKeyRangeId = partitionKeyRangeId;
    }

    public static PartitionKeyRangeIdentity fromHeader(String header) {
        String[] parts = StringUtils.split(header,",");
        if (parts.length == 2) {
            return new PartitionKeyRangeIdentity(parts[0], parts[1]);
        } else if (parts.length == 1) {
            return new PartitionKeyRangeIdentity(parts[0]);
        } else {
            throw new IllegalStateException("x-ms-documentdb-partitionkeyrangeid header contains invalid value '" + header + "'");
        }
    }

    public String toHeader() {
        if (this.collectionRid != null) {
            return this.collectionRid + "," + this.partitionKeyRangeId;
        }

        return this.partitionKeyRangeId;
    }

    @Override
    public String toString() {
        return "PartitionKeyRangeIdentity{" +
                   "collectionRid='" + collectionRid + '\'' +
                   ", partitionKeyRangeId='" + partitionKeyRangeId + '\'' +
                   '}';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        PartitionKeyRangeIdentity that = (PartitionKeyRangeIdentity) other;

        if (!Objects.equals(this.collectionRid, that.collectionRid)) {
            return false;
        }

        return partitionKeyRangeId.equals(that.partitionKeyRangeId);
    }

    @Override
    public int hashCode() {
        int result = collectionRid != null ? collectionRid.hashCode() : 0;
        result = (397 * result) ^ partitionKeyRangeId.hashCode();
        return result;
    }

    public String getCollectionRid() {
        return collectionRid;
    }

    public String getPartitionKeyRangeId() {
        return partitionKeyRangeId;
    }
}
