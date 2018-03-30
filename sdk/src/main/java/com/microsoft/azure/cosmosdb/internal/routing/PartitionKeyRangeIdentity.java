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

package com.microsoft.azure.cosmosdb.internal.routing;

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
        String[] parts = header.split(",");
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
