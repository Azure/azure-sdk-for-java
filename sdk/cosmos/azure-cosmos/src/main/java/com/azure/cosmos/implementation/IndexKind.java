// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

/**
 * These are the indexing types available for indexing a path in the Azure Cosmos DB database service.
 * For additional details, refer to
 * http://azure.microsoft.com/documentation/articles/documentdb-indexing-policies/#ConfigPolicy.
 */
public enum IndexKind {
    // The index entries are hashed to serve point look up queries.
    // Can be used to serve queries like: SELECT * FROM docs d WHERE d.prop = 5
    HASH("Hash"),

    // The index entries are ordered. RANGE indexes are optimized for inequality predicate queries with efficient range
    // scans.
    // Can be used to serve queries like: SELECT * FROM docs d WHERE d.prop > 5
    RANGE("Range"),

    // The index entries are indexed to serve spatial queries like below:
    // SELECT * FROM Root r WHERE ST_DISTANCE({"type":"POINT","coordinates":[71.0589,42.3601]}, r.location) $LE 10000
    SPATIAL("Spatial");

    IndexKind(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    private final String overWireValue;

    @Override
    public String toString() {
        return this.overWireValue;
    }
}
