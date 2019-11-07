// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.routing;

import org.apache.commons.lang3.StringUtils;

/**
 * Used internally to represents the identity of a partition key range in the Azure Cosmos DB database service.
 */
public final class PartitionKeyRangeIdentity {
    private String collectionRid;
    private String partitionKeyRangeId;

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
            return String.format("%s,%s", this.collectionRid, this.partitionKeyRangeId);
        }

        return String.format("%s", this.partitionKeyRangeId);
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
        if (null == other) {
            return false;
        }
        if (this == other) {
            return true;
        }
        return other instanceof PartitionKeyRangeIdentity
                && ((PartitionKeyRangeIdentity) other).collectionRid.equals(this.collectionRid)
                && ((PartitionKeyRangeIdentity) other).partitionKeyRangeId.equals(this.partitionKeyRangeId);
    }

    @Override
    public int hashCode() {
        return ((this.collectionRid != null ? this.collectionRid.hashCode() : 0) * 397)
                ^ (this.partitionKeyRangeId != null ? this.partitionKeyRangeId.hashCode() : 0);
    }

    public String getCollectionRid() {
        return collectionRid;
    }

    public String getPartitionKeyRangeId() {
        return partitionKeyRangeId;
    }
}
