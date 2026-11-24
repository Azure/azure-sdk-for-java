// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/**
 * Specifies the scope for computing BM25 statistics used by FullTextScore in hybrid search queries.
 */
public enum CosmosFullTextScoreScope {
    /**
     * Compute BM25 statistics (term frequency, inverse document frequency, and document length)
     * across all documents in the container, including all physical and logical partitions.
     * This is the default behavior.
     */
    GLOBAL,

    /**
     * Compute BM25 statistics only over the subset of documents within the partition key values
     * specified in the query. This is useful for multi-tenant scenarios where scoring should
     * reflect statistics that are accurate for a specific tenant's dataset.
     */
    LOCAL
}
