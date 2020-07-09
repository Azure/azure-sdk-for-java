// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/**
 * Represents the sorting order for a path in a composite index, for a
 * container in the Azure Cosmos DB database service.
 */
public enum CompositePathSortOrder {
    /**
     * ASCENDING sort order for composite paths.
     */
    ASCENDING {
        public String toString() {
            return "ascending";
        }
    },

    /**
     * DESCENDING sort order for composite paths.
     */
    DESCENDING {
        public String toString() {
            return "descending";
        }
    },
}
