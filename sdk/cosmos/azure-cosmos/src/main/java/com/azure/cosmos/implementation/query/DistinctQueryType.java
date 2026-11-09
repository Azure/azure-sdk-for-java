// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import java.util.HashMap;
import java.util.Map;

public enum DistinctQueryType {
    // This means that the query does not have DISTINCT.
    NONE("None"),
    // This means that the query has DISTINCT, but it's not ordered perfectly.
    UNORDERED("Unordered"),
    // This means that the query has DISTINCT, and it is ordered perfectly.
    ORDERED("Ordered");

    private final String overWireValue;
    private static Map<String, DistinctQueryType> distinctQueryTypeHashMap = new HashMap<>();

    static {
        for (DistinctQueryType cl : DistinctQueryType.values()) {
            distinctQueryTypeHashMap.put(cl.toString(), cl);
        }
    }

    DistinctQueryType(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    /**
     * Given the over wire version of ConsistencyLevel gives the corresponding enum or return null
     *
     * @param consistencyLevel String value of consistency level
     * @return ConsistencyLevel Enum consistency level
     */
    public static DistinctQueryType fromServiceSerializedFormat(String consistencyLevel) {
        // this is 100x faster than org.apache.commons.lang3.EnumUtils.getEnum(String)
        // for more detail refer to https://github.com/moderakh/azure-cosmosdb-benchmark
        return distinctQueryTypeHashMap.get(consistencyLevel);
    }

    @Override
    public String toString() {
        return this.overWireValue;
    }
}
